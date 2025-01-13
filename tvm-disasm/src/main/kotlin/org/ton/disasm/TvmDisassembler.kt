package org.ton.disasm

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import org.ton.bitstring.BitString
import org.ton.bitstring.ByteBackedBitString
import org.ton.bitstring.ByteBackedMutableBitString
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.cell.CellSlice
import org.ton.disasm.bytecode.CellOperandType
import org.ton.disasm.bytecode.InstructionDescription
import org.ton.disasm.bytecode.dictPushConstMnemonic
import org.ton.disasm.bytecode.opcodeToRefOperandType
import org.ton.disasm.bytecode.opcodeToSubSliceOperandType
import org.ton.disasm.bytecode.pfxDictConstGetJmpMnemonic
import org.ton.disasm.trie.TrieMap
import org.ton.disasm.trie.TrieMapVertex
import org.ton.disasm.utils.HashMapESerializer
import org.ton.hashmap.HashMapE
import java.math.BigInteger

data object TvmDisassembler {
    private const val SPEC_PATH_STRING: String = "/cp0.json"
    private val trie: TrieMap by lazy {
        val specStream = this.javaClass.getResourceAsStream(SPEC_PATH_STRING)
            ?: error("Spec not found at path $SPEC_PATH_STRING")

        specStream.use {
            TrieMap.construct(it)
        }
    }

    // like in here: https://github.com/tact-lang/ton-opcode/blob/7f70823f67f3acf73556a187403b281f6e72d15d/src/decompiler/decompileAll.ts#L28
    private val defaultRoot = listOf(
        "SETCP",
        dictPushConstMnemonic,
        "DICTIGETJMPZ",
        "THROWARG",
    )

    fun disassemble(codeBoc: ByteArray): JsonObject {
        val codeAsCell = BagOfCells(codeBoc).roots.first()

        val (methods, mainMethod) = disassemble(codeAsCell)

        return JsonObject(
            mapOf(
                "mainMethod" to serializeInstList(mainMethod),
                "methods" to JsonObject(
                    methods.entries.associate { (methodId, inst) ->
                        methodId to JsonObject(
                            mapOf(
                                "id" to JsonPrimitive(methodId),
                                "instList" to serializeInstList(inst),
                            )
                        )
                    }
                )
            )
        )
    }

    private fun serializeInstList(instList: List<TvmInst>) =
        JsonArray(instList.map { it.toJson() })

    private fun disassemble(cell: Cell): Pair<Map<String, List<TvmInst>>, List<TvmInst>> {

        val slice = cell.beginParse()
        val initialLocation = TvmMainMethodLocation(index = 0)

        val insts = disassemble(slice, initialLocation)

        val result = hashMapOf<String, List<TvmInst>>()

        val defaultMain = insts.size == defaultRoot.size && (insts zip defaultRoot).all { it.first.type == it.second }

        if (defaultMain) {
            val dictInst = insts.firstNotNullOf { it as? TvmConstDictInst }
            dictInst.dict.forEach { (newMethodId, codeCell) ->
                val newInitialLocation = TvmInstMethodLocation(newMethodId, index = 0)
                result[newMethodId] =
                    disassemble(codeCell.beginParse(), newInitialLocation)
            }
        }

        return result to insts
    }

    private fun disassemble(
        slice: CellSlice,
        initialLocation: TvmInstLocation,
    ): List<TvmInst> {
        val resultInstructions = mutableListOf<TvmInst>()

        var location = initialLocation

        while (slice.remainingBits > 0) {
            val instDescriptor = getInstructionDescriptor(slice)

            val inst = parseInstruction(slice, instDescriptor, location)

            location = location.increment()
            resultInstructions.add(inst)
        }
        while (slice.refsPosition < slice.refs.size) {
            val nextSlice = slice.loadRef()

            val insts = disassemble(nextSlice.beginParse(), location)
            location = location.increment(insts.size)

            resultInstructions.addAll(insts)
        }

        return resultInstructions
    }

    private fun getInstructionDescriptor(slice: CellSlice): InstructionDescription {
        var position: TrieMapVertex? = trie.root
        while (position != null) {

            val curInst = position.inst
            if (curInst != null && operandRangeCheck(slice, curInst)) {
                return curInst
            }

            check(slice.remainingBits > 0) {
                "Slice must not be empty at this point, but it is"
            }
            val bit = slice.loadBit()
            position = position.step(bit)
        }

        error("Could not load next instruction for slice $slice")
    }

    private fun operandRangeCheck(slice: CellSlice, instDescriptor: InstructionDescription): Boolean {
        val rangeCheck = instDescriptor.bytecode.operandsRangeCheck
            ?: return true

        val value = slice.preloadUInt(rangeCheck.length)
        return value >= rangeCheck.from.toBigInteger() && value <= rangeCheck.to.toBigInteger()
    }

    private fun parseInstruction(
        slice: CellSlice,
        instDescriptor: InstructionDescription,
        location: TvmInstLocation,
    ): TvmInst {
        val name = instDescriptor.mnemonic
        var operandsInfo = instDescriptor.bytecode.operands

        // to parse dict, we need parameter n, but in spec the first parameter is dict.
        // since one operand is ref, and another is a part of current slice, reversing is valid.
        if (name == dictPushConstMnemonic) {
            operandsInfo = operandsInfo.reversed()
        }

        val operandsValue = hashMapOf<String, JsonElement>()
        var parsedOperandMap: Map<String, Cell>? = null

        operandsInfo.forEach { operand ->
            val value = when (operand.type) {
                "int" -> {
                    val size = operand.size
                        ?: error("Size of operand $operand not found")
                    parseInt(slice, size)
                }

                "uint" -> {
                    val size = operand.size
                        ?: error("Size of operand $operand not found")
                    parseUInt(slice, size)
                }

                "ref" -> {
                    val (value, map) = parseRef(slice, name, operandsValue)
                    parsedOperandMap = map
                    value
                }

                "pushint_long" -> {
                    parsePushIntLong(slice)
                }

                "subslice" -> {
                    val bitLengthVarSize = operand.bits_length_var_size ?: 0
                    val refLengthVarSize = operand.refs_length_var_size ?: 0
                    val bitPadding = operand.bits_padding ?: 0
                    val refsAdd = operand.refs_add ?: 0
                    val completionTag = operand.completion_tag ?: false
                    parseSubSlice(slice, bitLengthVarSize, refLengthVarSize, bitPadding, refsAdd, completionTag, name)
                }

                else -> {
                    error("Unexpected operand type: ${operand.type}")
                }
            }

            value?.let { operandsValue[operand.name] = value }
        }

        return parsedOperandMap?.let {
            TvmConstDictInst(name, location, operandsValue, it)
        } ?: TvmInst(name, location, operandsValue)
    }

    private fun parseInt(slice: CellSlice, size: Int): JsonPrimitive {
        val result = slice.loadInt(size).toInt()
        return JsonPrimitive(result)
    }

    private fun parseUInt(slice: CellSlice, size: Int): JsonPrimitive {
        val result = slice.loadUInt(size).toInt()
        return JsonPrimitive(result)
    }

    private fun parseDictPushConst(
        ref: Cell,
        operands: Map<String, JsonElement>,
    ): Map<String, Cell> {
        val keySize = operands["n"]?.jsonPrimitive?.int
            ?: error("No 'n' parameter for DICTPUSHCONST")

        val wrappedRef = Cell(BitString(listOf(true)), ref)

        val map = HashMapE.tlbCodec(keySize, HashMapESerializer).loadTlb(wrappedRef)

        return map.toMap().map { (key, value) ->
            BigInteger("0" + key.toBinary(), 2).toString() to value
        }.toMap()
    }

    private fun parseRef(
        slice: CellSlice,
        opname: String,
        operands: Map<String, JsonElement>,
    ): Pair<JsonElement?, Map<String, Cell>> {
        val ref = slice.loadRef()

        val givenOperandType = opcodeToRefOperandType[opname]
            ?: error("Unexpected opcode with ref operand: $opname")

        if (opname == dictPushConstMnemonic) {
            val resultMap = parseDictPushConst(ref, operands)
            return null to resultMap
        }

        val operandType = if (opname == pfxDictConstGetJmpMnemonic) {
            // TODO: maybe process this case later
            CellOperandType.OrdinaryCell
        } else {
            givenOperandType
        }

        when (operandType) {
            CellOperandType.CodeCell -> {
                val newLocation = TvmInstLambdaLocation(0)
                val insts = disassemble(
                    ref.beginParse(),
                    newLocation,
                )
                return serializeInstList(insts) to emptyMap()
            }

            CellOperandType.OrdinaryCell -> {
                return serializeSlice(ref.beginParse()) to emptyMap()
            }

            CellOperandType.SpecialCell -> {
                error("Unexpected opname with special ref: $opname")
            }
        }
    }

    private fun parsePushIntLong(slice: CellSlice): JsonPrimitive {
        val prefix = slice.loadUInt(5).toInt()
        val length = 8 * prefix + 19
        val result = slice.loadInt(length)
        return JsonPrimitive(result.toString())
    }

    private fun parseSubSlice(
        slice: CellSlice,
        bitLengthVarSize: Int,
        refLengthVarSize: Int,
        bitPadding: Int,
        refsAdd: Int,
        completionTag: Boolean,
        opname: String,
    ): JsonElement {

        val operandType = opcodeToSubSliceOperandType[opname]
            ?: error("Unexpected opcode with subslice operand: $opname")

        val refsLength = slice.loadUInt(refLengthVarSize).toInt() + refsAdd
        val bitsLength = slice.loadUInt(bitLengthVarSize).toInt() * 8 + bitPadding

        check(bitsLength <= slice.remainingBits) {
            "Not enough bits (less than $bitsLength) in slice $slice"
        }

        val originalBits = slice.bits
        val originalBitsAsBytearray = if (originalBits is ByteBackedBitString) {
            originalBits.bytes
        } else {
            originalBits.toByteArray()
        }

        val bits = ByteBackedMutableBitString(originalBitsAsBytearray, slice.bitsPosition + bitsLength)
        while (completionTag && !bits.last()) {
            bits.size--
        }
        if (completionTag) {
            bits.size--
        }

        val refs = List(refsLength) { slice.loadRef() }
        val newSlice = CellSlice.of(bits, refs)
        newSlice.bitsPosition = slice.bitsPosition

        slice.skipBits(bitsLength)

        when (operandType) {
            CellOperandType.CodeCell -> {
                val newLocation = TvmInstLambdaLocation(0)
                val insts = disassemble(
                    newSlice,
                    newLocation,
                )
                return serializeInstList(insts)
            }

            CellOperandType.OrdinaryCell -> {
                return serializeSlice(newSlice)
            }

            CellOperandType.SpecialCell -> {
                error("Unexpected operation $opname with special subslice operand")
            }
        }
    }

    private fun serializeSlice(slice: CellSlice): JsonElement {
        return Json.encodeToJsonElement(
            mapOf(
                "_bits" to slice.bits.drop(slice.bitsPosition).map { JsonPrimitive(if (it) 1 else 0) },
                "_refs" to slice.refs.drop(slice.refsPosition).map { serializeSlice(it.beginParse()) },
            )
        )
    }
}
