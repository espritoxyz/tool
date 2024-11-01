package org.ton.tlb

import org.ton.Endian.BigEndian
import org.ton.TvmBasicMsgAddrLabel
import org.ton.TvmCoinsLabel
import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmDataCellLabel
import org.ton.TvmDataCellStructure
import org.ton.TvmDataCellStructure.Empty
import org.ton.TvmDataCellStructure.KnownTypePrefix
import org.ton.TvmDataCellStructure.LoadRef
import org.ton.TvmDataCellStructure.SwitchPrefix
import org.ton.TvmDataCellStructure.Unknown
import org.ton.TvmFullMsgAddrLabel
import org.ton.TvmIntegerLabel
import org.ton.TvmMaybeRefLabel
import org.ton.TvmParameterInfo
import org.ton.TvmParameterInfo.DataCellInfo

class TvmTlbTransformer(
    definitions: List<TvmTlbTypeDefinition>,
    private val onlyBasicAddresses: Boolean = false,
) {
    private val typeDefinitions = definitions.associateBy { it.id }
    private val transformed = hashMapOf<Pair<TvmTlbTypeDefinition, List<TvmTlbTypeExpr>>, TvmDataCellLabel>()
    private val cellTypeId = definitions.first { it.name == "Cell" }.id
    private val anyTypeId = definitions.first { it.name == "Any" }.id

    private fun getTypeDef(id: Int): TvmTlbTypeDefinition = typeDefinitions[id]
        ?: error("Unknown type id: $id")

    fun transformTypeDefinition(
        def: TvmTlbTypeDefinition,
        args: List<TvmTlbTypeExpr> = emptyList(),
    ): TvmDataCellLabel? {
        return transformed.getOrPut(def to args) {
            if (def.isBuiltin) {
                transformBuiltins(def, args) ?: return null
            } else {
                transformComplexType(def, args)
            }
        }
    }

    private fun sequenceOfExprsHasAny(
        sequence: Iterable<TvmTlbTypeExpr>
    ): Boolean = sequence.any {
        it is TvmTlbType && it.id in listOf(cellTypeId, anyTypeId)
    }

    private fun transformBuiltins(
        def: TvmTlbTypeDefinition,
        args: List<TvmTlbTypeExpr>,
    ): TvmDataCellLabel? {
        val name = def.name

        // TODO check `isSigned` and `endian` fields
        return when {
            name == "#" -> TvmIntegerLabel(bitSize = 32, isSigned = false, endian = BigEndian)
            name == "##" -> TvmIntegerLabel(bitSize = args.toIntConst(), isSigned = true, endian = BigEndian)
            name == "bits" -> TODO()
            name == "uint" -> TvmIntegerLabel(bitSize = args.toIntConst(), isSigned = false, endian = BigEndian)
            name == "int" -> TvmIntegerLabel(bitSize = args.toIntConst(), isSigned = true, endian = BigEndian)
            name.startsWith("bits") -> TODO()
            name.startsWith("int") -> {
                val bits = name.removePrefix("int").toInt()
                TvmIntegerLabel(bitSize = bits, isSigned = true, endian = BigEndian)
            }
            name.startsWith("uint") -> {
                val bits = name.removePrefix("uint").toInt()
                TvmIntegerLabel(bitSize = bits, isSigned = false, endian = BigEndian)
            }
            name == "Cell" || name == "Any" -> null
            else -> TODO()
        }
    }

    private fun transformComplexType(
        def: TvmTlbTypeDefinition,
        args: List<TvmTlbTypeExpr>,
    ): TvmDataCellLabel {
        // special cases
        when (def.name) {
            "Maybe" -> {
                require(args.size == 1)
                when (val arg = args.single()) {
                    is TvmTlbReference -> {
                        val (internal, hasAny) = transformSequenceOfExprs(listOf(arg.ref))
                        val label = TvmCompositeDataCellLabel(
                            "<anonymous-label>",
                            internal,
                            hasAny,
                        )
                        return TvmMaybeRefLabel(DataCellInfo(label))
                    }
                    else -> {
                        val (internal, hasAny) = transformSequenceOfExprs(listOf(arg))
                        val structure = SwitchPrefix(
                            switchSize = 1,
                            mapOf(
                                "0" to Empty,
                                "1" to internal
                            )
                        )
                        return TvmCompositeDataCellLabel("Maybe", structure, hasAny)
                    }
                }

            }
            "Either" -> {
                require(args.size == 2)
                val (left, leftHasAny) = transformSequenceOfExprs(listOf(args[0]))
                val (right, rightHasAny) = transformSequenceOfExprs(listOf(args[1]))
                val structure = SwitchPrefix(
                    switchSize = 1,
                    mapOf(
                        "0" to left,
                        "1" to right
                    )
                )
                return TvmCompositeDataCellLabel("Either", structure, hasAny = leftHasAny || rightHasAny)
            }
            "MsgAddress" -> {
                return if (onlyBasicAddresses) TvmBasicMsgAddrLabel else TvmFullMsgAddrLabel
            }
            "Grams", "Coins" -> {  // TODO: add variant for `VarUInteger`
                return TvmCoinsLabel
            }
        }

        if (args.isNotEmpty()) {
            TODO()
        }

        val someConstructorHasAny = def.constructors.any { c ->
            val typeExprs = c.fields.map { it.typeExpr }
            sequenceOfExprsHasAny(typeExprs)
        }
        if (someConstructorHasAny) {
            // this one must not be recursive (if it is, it will lead to infinite recursion)
            val structure = transformConstructors(
                def.constructors.map { ConstructorTagSuffix(it, it.tag) }
            )
            return TvmCompositeDataCellLabel(def.name, structure, hasAny = true)
        }

        return TvmCompositeDataCellLabel(
            name = def.name
        ).also { label ->
            transformed[def to args] = label
            val structure = transformConstructors(
                def.constructors.map { ConstructorTagSuffix(it, it.tag) }
            )
            label.internalStructure = structure
        }
    }

    private fun transformConstructors(
        constructors: List<ConstructorTagSuffix>,
    ): TvmDataCellStructure {
        if (constructors.size == 1 && constructors.single().tagSuffix.isEmpty()) {
            return transformConstructor(constructors.single().constructor)
        }

        val minLen = constructors.minOf { it.tagSuffix.length }
        val groupedConstructors = hashMapOf<String, MutableList<ConstructorTagSuffix>>()

        constructors.forEach { constructor ->
            val prefix = constructor.tagSuffix.substring(0 until minLen)

            constructor.tagSuffix = constructor.tagSuffix.substring(minLen)
            groupedConstructors.getOrPut(prefix) { mutableListOf() }.add(constructor)
        }

        val variants = groupedConstructors.mapValues { transformConstructors(it.value) }

        return SwitchPrefix(
            minLen,
            variants,
        )
    }

    private fun transformSequenceOfExprs(
        sequence: List<TvmTlbTypeExpr>,
    ): Pair<TvmDataCellStructure, Boolean> {
        var last: TvmDataCellStructure = Empty
        var foundAny = false
        sequence.asReversed().forEach { expr ->
            last = when (expr) {
                is TvmTlbReference -> {
                    val ref = expr.ref
                    require(ref is TvmTlbType) {
                        "Unexpected reference: $ref"
                    }

                    val dataInfo = transformTypeDefinition(getTypeDef(ref.id), ref.args)?.let {
                        DataCellInfo(it)
                    } ?: TvmParameterInfo.UnknownCellInfo

                    LoadRef(dataInfo, last)
                }

                is TvmTlbType -> {
                    val typeDef = getTypeDef(expr.id)

                    transformTypeDefinition(typeDef, expr.args)?.let { label ->
                        // unfold last label, if it has `Any`
                        if (last is Empty && label is TvmCompositeDataCellLabel && label.definitelyHasAny) {
                            foundAny = true
                            label.internalStructure
                        } else {
                            KnownTypePrefix(label, last)
                        }
                    } ?: let {
                        foundAny = true
                        Unknown
                    }
                }

                else -> TODO()
            }
        }

        return last to foundAny
    }

    private fun transformConstructor(
        constructor: TvmTlbTypeConstructor
    ): TvmDataCellStructure {
        val exprs = constructor.fields.map { it.typeExpr }
        return transformSequenceOfExprs(exprs).first
    }

    private data class ConstructorTagSuffix(
        val constructor: TvmTlbTypeConstructor,
        var tagSuffix: String,
    )

    private fun List<TvmTlbTypeExpr>.toIntConst(): Int =
        (singleOrNull() as? TvmTlbIntConst)?.value ?: error("Unexpected args: $this")
}
