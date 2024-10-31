package org.usvm.machine.interpreter

import org.ton.bitstring.BitString
import org.ton.bytecode.TvmAppCryptoChksignuInst
import org.ton.bytecode.TvmAppCryptoHashcuInst
import org.ton.bytecode.TvmAppCryptoHashsuInst
import org.ton.bytecode.TvmAppCryptoInst
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.hashmap.HashMapE
import org.ton.tlb.TlbCodec
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.api.makeSymbolicPrimitive
import org.usvm.api.readField
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.Companion.dictKeyLengthField
import org.usvm.machine.TvmContext.Companion.sliceDataPosField
import org.usvm.machine.TvmContext.Companion.sliceRefPosField
import org.usvm.machine.TvmContext.TvmInt257Sort
import org.usvm.machine.TvmStepScopeManager
import org.usvm.machine.bigIntValue
import org.usvm.machine.state.DictId
import org.usvm.machine.state.TvmStack
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.addInt
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.dictContainsKey
import org.usvm.machine.state.dictGetValue
import org.usvm.machine.state.dictKeyEntries
import org.usvm.machine.state.doWithStateCtx
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.preloadDataBitsFromCellWithoutChecks
import org.usvm.machine.state.readCellRef
import org.usvm.machine.state.slicePreloadDataBits
import org.usvm.machine.state.takeLastBuilder
import org.usvm.machine.state.takeLastCell
import org.usvm.machine.state.takeLastIntOrThrowTypeError
import org.usvm.machine.state.takeLastSlice
import org.usvm.machine.state.unsignedIntegerFitsBits
import org.usvm.machine.truncateSliceCell
import org.usvm.machine.types.TvmBuilderType
import org.usvm.machine.types.TvmCellType
import org.usvm.machine.types.TvmRealReferenceType
import org.usvm.machine.types.TvmSliceType
import org.usvm.mkSizeExpr
import org.usvm.sizeSort
import org.usvm.test.resolver.TvmTestBuilderValue
import org.usvm.test.resolver.TvmTestDataCellValue
import org.usvm.test.resolver.TvmTestDictCellValue
import org.usvm.test.resolver.TvmTestIntegerValue
import org.usvm.test.resolver.TvmTestReferenceValue
import org.usvm.test.resolver.TvmTestSliceValue
import org.usvm.test.resolver.TvmTestStateResolver
import java.math.BigInteger

class TvmCryptoInterpreter(private val ctx: TvmContext) {
    fun visitCryptoStmt(scope: TvmStepScopeManager, stmt: TvmAppCryptoInst) {
        when (stmt) {
            is TvmAppCryptoHashsuInst -> visitSingleHashInst(scope, stmt, operandType = TvmSliceType)
            is TvmAppCryptoHashcuInst -> visitSingleHashInst(scope, stmt, operandType = TvmCellType)
            is TvmAppCryptoChksignuInst -> visitCheckSignatureInst(scope, stmt)
            else -> TODO("$stmt")
        }
    }

    private fun visitSingleHashInst(scope: TvmStepScopeManager, stmt: TvmAppCryptoInst, operandType: TvmRealReferenceType) {
        require(operandType != TvmBuilderType) {
            "A single hash function for builders does not exist"
        }

        scope.consumeDefaultGas(stmt)

        scope.calcOnState {
            val value = stack.popHashableStackValue(operandType)
                ?: return@calcOnState

            val hash = addressToHash[value] ?: run {
                val res = makeSymbolicPrimitive(ctx.int257sort)
                addressToHash = addressToHash.put(value, res)
                res
            }

            // Hash is a 256-bit unsigned integer
            scope.assert(
                ctx.unsignedIntegerFitsBits(hash, 256u),
                unsatBlock = { error("Cannot make hash fit in 256 bits") }
            ) ?: return@calcOnState

            stack.addInt(hash)
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitCheckSignatureInst(scope: TvmStepScopeManager, stmt: TvmAppCryptoChksignuInst) {
        scope.consumeDefaultGas(stmt)

        val key = scope.takeLastIntOrThrowTypeError()
        val signature = scope.calcOnState { stack.takeLastSlice() }
        if (signature == null) {
            scope.doWithState(ctx.throwTypeCheckError)
            return
        }

        val hash = scope.takeLastIntOrThrowTypeError()

        // Check that signature is correct - it contains at least 512 bits
        val bits = scope.slicePreloadDataBits(signature, bits = 512)
        if (bits == null) {
            scope.doWithStateCtx {
                throwUnknownCellUnderflowError(this)
            }

            return
        }

        // TODO do real check?
        val condition = scope.calcOnState { makeSymbolicPrimitive(ctx.boolSort) }
        with(ctx) {
            scope.fork(
                condition,
                falseStateIsExceptional = false,
                blockOnTrueState = {
                    stack.addInt(zeroValue)
                    newStmt(stmt.nextStmt())
                },
                blockOnFalseState =  {
                    stack.addInt(minusOneValue)
                    newStmt(stmt.nextStmt())
                }
            )
        }
    }

    context(TvmState)
    private fun TvmStack.popHashableStackValue(referenceType: TvmRealReferenceType): UHeapRef? =
        when (referenceType) {
            TvmBuilderType -> takeLastBuilder()
            TvmCellType -> takeLastCell()
            TvmSliceType -> takeLastSlice()
        }

    /**
     * Generate expression that fixates ref's value given by model, and its hash (which is originally a mock).
     * */
    fun fixateValueAndHash(state: TvmState, ref: UHeapRef, hash: UExpr<TvmInt257Sort>): UBoolExpr = with(ctx) {
        val resolver = TvmTestStateResolver(ctx, state.models.first(), state)
        val value = resolver.resolveRef(ref)
        val fixateValueCond = fixateConcreteValue(state, ref, value, resolver)
        val concreteHash = calculateConcreteHash(value)
        val hashCond = hash eq concreteHash
        return fixateValueCond and hashCond
    }

    private fun calculateConcreteHash(value: TvmTestReferenceValue): UExpr<TvmInt257Sort> {
        return when (value) {
            is TvmTestDataCellValue -> {
                val cell = transformTvmTestDataCellValueIntoCell(value)
                calculateHashOfCell(cell)
            }
            is TvmTestDictCellValue -> {
                val cell = transformTvmTestDictCellValueInfoCell(value)
                calculateHashOfCell(cell)
            }
            is TvmTestBuilderValue -> {
                TODO()
            }
            is TvmTestSliceValue -> {
                val restCell = truncateSliceCell(value)
                calculateConcreteHash(restCell)
            }
        }
    }

    private fun calculateHashOfCell(cell: Cell): UExpr<TvmInt257Sort> {
        val hash = BigInteger(ByteArray(1) { 0 } + cell.hash().toByteArray())
        return ctx.mkBv(hash, ctx.int257sort)
    }

    private fun transformTvmTestDataCellValueIntoCell(value: TvmTestDataCellValue): Cell {
        val refs = value.refs.map {
            when (it) {
                is TvmTestDataCellValue -> transformTvmTestDataCellValueIntoCell(it)
                is TvmTestDictCellValue -> transformTvmTestDictCellValueInfoCell(it)
            }
        }
        val binaryData = BitString(value.data.map { it == '1' })
        return Cell(binaryData, *refs.toTypedArray())
    }

    private fun transformTvmTestDictCellValueInfoCell(value: TvmTestDictCellValue): Cell {
        val patchedContent = value.entries.map { (key, entryValue) ->
            val keyPadded = key.value.toString(2).padStart(length = value.keyLength)
            val bitArray = keyPadded.map { it == '1' }
            BitString(bitArray) to entryValue
        }.toMap()
        val hashMap = HashMapE.fromMap(patchedContent)
        val valueCodec = object : TlbCodec<TvmTestSliceValue> {
            override fun loadTlb(cellSlice: CellSlice): TvmTestSliceValue {
                error("Should not be called")
            }

            override fun storeTlb(cellBuilder: CellBuilder, value: TvmTestSliceValue) {
                val valueCell = transformTvmTestDataCellValueIntoCell(value.cell)
                cellBuilder.storeBits(valueCell.bits.drop(value.dataPos))
                cellBuilder.storeRefs(valueCell.refs.drop(value.refPos))
            }

        }
        val codec = HashMapE.tlbCodec(value.keyLength, valueCodec)
        val cellForHashMapE = codec.createCell(hashMap)
        check(cellForHashMapE.bits.single() && cellForHashMapE.refs.size == 1)
        return cellForHashMapE.refs.single()
    }

    private fun fixateConcreteValue(
        state: TvmState,
        ref: UHeapRef,
        value: TvmTestReferenceValue,
        resolver: TvmTestStateResolver,
    ): UBoolExpr =
        when (value) {
            is TvmTestDataCellValue -> {
                fixateConcreteValueForDataCell(state, ref, value, resolver)
            }
            is TvmTestSliceValue -> {
                fixateConcreteValueForSlice(state, ref, value, resolver)
            }
            is TvmTestDictCellValue -> {
                fixateConcreteValueForDictCell(state, ref, value, resolver)
            }
            is TvmTestBuilderValue -> {
                TODO()
            }
        }

    private fun fixateConcreteValueForSlice(
        state: TvmState,
        ref: UHeapRef,
        value: TvmTestSliceValue,
        resolver: TvmTestStateResolver,
    ): UBoolExpr = with(ctx) {
        val dataPosSymbolic = state.memory.readField(ref, sliceDataPosField, sizeSort)
        val refPosSymbolic = state.memory.readField(ref, sliceRefPosField, sizeSort)
        val posGuard = (dataPosSymbolic eq mkSizeExpr(value.dataPos)) and (refPosSymbolic eq mkSizeExpr(value.refPos))
        val cellRef = state.memory.readField(ref, TvmContext.sliceCellField, addressSort)
        return posGuard and fixateConcreteValueForDataCell(state, cellRef, value.cell, resolver)
    }

    private fun fixateConcreteValueForDataCell(
        state: TvmState,
        ref: UHeapRef,
        value: TvmTestDataCellValue,
        resolver: TvmTestStateResolver,
    ): UBoolExpr = with(ctx) {
        val childrenCond = value.refs.foldIndexed(trueExpr as UBoolExpr) { index, acc, child ->
            val childRef = state.readCellRef(ref, mkSizeExpr(index))
            acc and fixateConcreteValue(state, childRef, child, resolver)
        }

        val symbolicData = state.preloadDataBitsFromCellWithoutChecks(ref, zeroSizeExpr, value.data.length)
        val symbolicDataLength = state.memory.readField(ref, TvmContext.cellDataLengthField, sizeSort)
        val symbolicRefNumber = state.memory.readField(ref, TvmContext.cellRefsLengthField, sizeSort)

        val dataCond = if (value.data.isEmpty()) {
            trueExpr
        } else {
            val concreteData = mkBv(BigInteger(value.data, 2), value.data.length.toUInt())
            (symbolicData eq concreteData)
        }

        val curCond = dataCond and (symbolicDataLength eq mkSizeExpr(value.data.length)) and (symbolicRefNumber eq mkSizeExpr(value.refs.size))

        childrenCond and curCond
    }

    private fun fixateConcreteValueForDictCell(
        state: TvmState,
        ref: UHeapRef,
        value: TvmTestDictCellValue,
        resolver: TvmTestStateResolver,
    ): UBoolExpr = with(ctx) {
        val keyLength = state.memory.readField(ref, dictKeyLengthField, int257sort)
        var result = keyLength eq value.keyLength.toBv257()

        val model = resolver.model
        val modelRef = model.eval(ref) as UConcreteHeapRef

        val dictId = DictId(value.keyLength)
        val keySort = mkBvSort(value.keyLength.toUInt())
        val entries = dictKeyEntries(model, state.memory, modelRef, dictId, keySort)

        entries.forEach { entry ->
            val key = entry.setElement
            val keyContains = state.dictContainsKey(ref, dictId, key)
            val entryValue = state.dictGetValue(ref, dictId, key)

            val concreteKey = TvmTestIntegerValue(model.eval(key).bigIntValue())
            val concreteValue = value.entries[concreteKey]
            if (concreteValue == null) {
                result = result and keyContains.not()
            } else {
                val valueConstraint = fixateConcreteValue(state, entryValue, concreteValue, resolver)
                result = result and keyContains and valueConstraint
            }
        }

        return result
    }
}
