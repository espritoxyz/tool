package org.usvm.test.resolver

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.BvUtils.toBigIntegerSigned
import kotlinx.collections.immutable.persistentListOf
import org.ton.bytecode.TvmCodeBlock
import org.usvm.machine.types.TvmType
import org.usvm.NULL_ADDRESS
import org.usvm.UAddressSort
import org.usvm.UConcreteHeapAddress
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.USort
import org.usvm.api.readField
import org.usvm.isTrue
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.intValue
import org.usvm.machine.state.TvmMethodResult
import org.usvm.machine.types.TvmSymbolicCellMaybeDictConstructorBit
import org.usvm.machine.types.TvmSymbolicCellDataInteger
import org.usvm.machine.types.TvmSymbolicCellDataType
import org.usvm.machine.types.TvmDataCellLoadedTypeInfo
import org.usvm.machine.state.TvmCellRefsRegionValueInfo
import org.usvm.machine.state.TvmDataCellTypesError
import org.usvm.machine.state.TvmRefsMemoryRegion
import org.usvm.machine.state.TvmStack
import org.usvm.machine.state.TvmStack.TvmStackTupleValue
import org.usvm.machine.state.TvmStack.TvmStackTupleValueConcreteNew
import org.usvm.machine.state.TvmState
import org.usvm.machine.types.TvmSymbolicCellDataBitArray
import org.usvm.machine.state.calcConsumedGas
import org.usvm.machine.state.ensureSymbolicBuilderInitialized
import org.usvm.machine.state.ensureSymbolicCellInitialized
import org.usvm.machine.state.ensureSymbolicSliceInitialized
import org.usvm.machine.state.lastStmt
import org.usvm.machine.state.tvmCellRefsRegion
import org.usvm.machine.types.TvmDataCellType
import org.usvm.machine.types.TvmDictCellType
import org.usvm.machine.types.TvmSymbolicCellDataCoins
import org.usvm.machine.types.TvmSymbolicCellDataMsgAddr
import org.usvm.machine.types.getPossibleTypes
import org.usvm.memory.UMemory
import org.usvm.model.UModelBase
import org.usvm.sizeSort
import java.math.BigInteger

class TvmTestStateResolver(
    private val ctx: TvmContext,
    private val model: UModelBase<TvmType>,
    private val state: TvmState,
) {
    private val stack: TvmStack
        get() = state.stack

    private val memory: UMemory<TvmType, TvmCodeBlock>
        get() = state.memory

    private val resolvedCache = mutableMapOf<UConcreteHeapAddress, TvmTestCellValue>()

    fun resolveParameters(): List<TvmTestValue> = stack.inputValues.filterNotNull().map { resolveStackValue(it) }.reversed()

    fun resolveResultStack(): TvmMethodSymbolicResult {
        val results = state.stack.results

        // Do not include exit code for exceptional results to the result
        val resultsWithoutExitCode = if (state.methodResult is TvmMethodResult.TvmFailure) results.dropLast(1) else results
        val resolvedResults = resultsWithoutExitCode.filterNotNull().map { resolveStackValue(it) }

        return when (val it = state.methodResult) {
            TvmMethodResult.NoCall -> error("Missed result for state $state")
            is TvmMethodResult.TvmFailure -> TvmMethodFailure(it, state.lastStmt, it.exit.exitCode, resolvedResults)
            is TvmMethodResult.TvmSuccess -> TvmSuccessfulExecution(it.exit.exitCode, resolvedResults)
            is TvmMethodResult.TvmStructuralError -> resolveTvmStructuralError(resolvedResults, it)
        }
    }

    private fun resolveTvmStructuralError(
        stack: List<TvmTestValue>,
        exit: TvmMethodResult.TvmStructuralError,
    ): TvmExecutionWithStructuralError =
        when (exit) {
            is TvmDataCellTypesError -> TvmExecutionWithDataCellTypesError(
                expected = resolveCellDataType(exit.expectedType),
                actual = resolveCellDataType(exit.actualType),
                stack = stack
            )
        }

    fun resolveGasUsage(): Int = model.eval(state.calcConsumedGas()).intValue()

    fun resolveStackValue(stackValue: TvmStack.TvmStackValue): TvmTestValue {
        return when (stackValue) {
            is TvmStack.TvmStackIntValue -> resolveInt257(stackValue.intValue)
            is TvmStack.TvmStackCellValue -> resolveCell(stackValue.cellValue.also { state.ensureSymbolicCellInitialized(it) })
            is TvmStack.TvmStackSliceValue -> resolveSlice(stackValue.sliceValue.also { state.ensureSymbolicSliceInitialized(it) })
            is TvmStack.TvmStackBuilderValue -> resolveBuilder(stackValue.builderValue.also { state.ensureSymbolicBuilderInitialized(it) })
            is TvmStack.TvmStackNullValue -> TvmTestNullValue
            is TvmStack.TvmStackContinuationValue -> TODO()
            is TvmStackTupleValue -> resolveTuple(stackValue)
        }
    }

    private fun <T : USort> evaluateInModel(expr: UExpr<T>): UExpr<T> = model.eval(expr)

    private fun resolveTuple(tuple: TvmStackTupleValue): TvmTestTupleValue = when (tuple) {
        is TvmStackTupleValueConcreteNew -> {
            val elements = tuple.entries.map {
                it.cell(stack)?.let { value -> resolveStackValue(value) }
                    ?: TvmTestNullValue // We do not care what is its real value as it was never used
            }

            TvmTestTupleValue(elements)
        }
        is TvmStack.TvmStackTupleValueInputValue -> {
            val size = resolveInt(tuple.size)
            val elements = (0 ..< size).map {
                tuple[it, stack].cell(stack)?.let { value -> resolveStackValue(value) }
                    ?: TvmTestNullValue // We do not care what is its real value as it was never used
            }

            TvmTestTupleValue(elements)
        }
    }

    private fun resolveBuilder(builder: UHeapRef): TvmTestBuilderValue {
        val ref = evaluateInModel(builder) as UConcreteHeapRef

        val cached = resolvedCache[ref.address]
        check(cached is TvmTestDataCellValue?)
        if (cached != null) {
            return TvmTestBuilderValue(cached.data, cached.refs)
        }

        val cell = resolveCellLike(ref, builder)
        return TvmTestBuilderValue(cell.data, cell.refs)
    }

    private fun resolveSlice(slice: UHeapRef): TvmTestSliceValue = with(ctx) {
        val cellValue = resolveCell(memory.readField(slice, TvmContext.sliceCellField, addressSort))
        require(cellValue is TvmTestDataCellValue)
        val dataPosValue = resolveInt(memory.readField(slice, TvmContext.sliceDataPosField, sizeSort))
        val refPosValue = resolveInt(memory.readField(slice, TvmContext.sliceRefPosField, sizeSort))

        TvmTestSliceValue(cellValue, dataPosValue, refPosValue)
    }

    private fun resolveCellLike(ref: UConcreteHeapRef, cell: UHeapRef): TvmTestDataCellValue = with(ctx) {
        if (ref.address == NULL_ADDRESS) {
            return@with TvmTestDataCellValue()
        }

        val data = resolveCellData(cell)

        val refsLength = resolveInt(memory.readField(cell, TvmContext.cellRefsLengthField, sizeSort)).coerceAtMost(TvmContext.MAX_REFS_NUMBER)
        val refs = mutableListOf<TvmTestCellValue>()

        val storedRefs = mutableMapOf<Int, TvmTestCellValue>()
        val updateNode = memory.tvmCellRefsRegion().getRefsUpdateNode(ref)

        resolveRefUpdates(updateNode, storedRefs)

        for (idx in 0 until refsLength) {
            val refCell = storedRefs[idx]
                ?: TvmTestDataCellValue()

            refs.add(refCell)
        }

        val knownLoads = state.tvmDataCellLoadedTypeInfo.addressToActions[ref] ?: persistentListOf()
        val tvmCellValue = TvmTestDataCellValue(data, refs, resolveTypeLoad(knownLoads))

        tvmCellValue.also { resolvedCache[ref.address] = tvmCellValue }
    }

    private fun resolveCell(cell: UHeapRef): TvmTestCellValue = with(ctx) {
        val ref = evaluateInModel(cell) as UConcreteHeapRef
        if (ref.address == NULL_ADDRESS) {
            return@with TvmTestDataCellValue()
        }

        val cached = resolvedCache[ref.address]
        if (cached != null) return cached

        val typeVariants = state.getPossibleTypes(ref)

        // If typeVariants has more than one type, we can choose any of them.
        val type = typeVariants.first()

        require(type is TvmDictCellType || type is TvmDataCellType)

        if (type is TvmDictCellType) {
            return TvmTestDictCellValue.also { resolvedCache[ref.address] = it }
        }

        resolveCellLike(ref, cell)
    }

    private fun resolveRefUpdates(
        updateNode: TvmRefsMemoryRegion.TvmRefsRegionUpdateNode<TvmSizeSort, UAddressSort>?,
        storedRefs: MutableMap<Int, TvmTestCellValue>
    ) {
        @Suppress("NAME_SHADOWING")
        var updateNode = updateNode

        while (updateNode != null) {
            when (updateNode) {
                is TvmRefsMemoryRegion.TvmRefsRegionInputNode -> {
                    val idx = resolveInt(updateNode.key)
                    val value = TvmCellRefsRegionValueInfo(state).actualizeSymbolicValue(updateNode.value)
                    val refCell = resolveCell(value)
                    storedRefs.putIfAbsent(idx, refCell)
                }

                is TvmRefsMemoryRegion.TvmRefsRegionEmptyUpdateNode -> {}
                is TvmRefsMemoryRegion.TvmRefsRegionCopyUpdateNode -> {
                    val guardValue = evaluateInModel(updateNode.guard)
                    if (guardValue.isTrue) {
                        resolveRefUpdates(updateNode.updates, storedRefs)
                    }
                }
                is TvmRefsMemoryRegion.TvmRefsRegionPinpointUpdateNode -> {
                    val guardValue = evaluateInModel(updateNode.guard)
                    if (guardValue.isTrue) {
                        val idx = resolveInt(updateNode.key)
                        val refCell = resolveCell(updateNode.value)
                        storedRefs.putIfAbsent(idx, refCell)
                    }
                }
            }

            updateNode = updateNode.prevUpdate
        }
    }

    private fun resolveTypeLoad(loads: List<TvmDataCellLoadedTypeInfo.Load>): List<TvmCellDataTypeLoad> =
        loads.mapNotNull {
            if (model.eval(it.guard).isTrue) {
                TvmCellDataTypeLoad(resolveCellDataType(it.type), resolveInt(it.offset))
            } else {
                null
            }
        }

    private fun resolveCellDataType(type: TvmSymbolicCellDataType): TvmCellDataType =
        when (type) {
            is TvmSymbolicCellDataInteger -> TvmCellDataInteger(resolveInt(type.sizeBits), type.isSigned, type.endian)
            is TvmSymbolicCellMaybeDictConstructorBit -> TvmCellDataMaybeConstructorBit
            is TvmSymbolicCellDataBitArray -> TvmCellDataBitArray(resolveInt(type.sizeBits))
            is TvmSymbolicCellDataMsgAddr -> TvmCellDataMsgAddr
            is TvmSymbolicCellDataCoins -> TvmCellDataCoins(resolveInt(type.coinsPrefix))
        }

    private fun resolveInt257(expr: UExpr<out USort>): TvmTestIntegerValue {
        val value = extractInt257(evaluateInModel(expr))
        return TvmTestIntegerValue(value)
    }

    private fun resolveCellData(cell: UHeapRef): String = with(ctx) {
        val symbolicData = memory.readField(cell, TvmContext.cellDataField, cellDataSort)
        val data = extractCellData(evaluateInModel(symbolicData))
        val dataLength = resolveInt(memory.readField(cell, TvmContext.cellDataLengthField, sizeSort))
            .coerceAtMost(TvmContext.MAX_DATA_LENGTH).coerceAtLeast(0)

        return data.take(dataLength)
    }

    private fun resolveInt(expr: UExpr<out USort>): Int = extractInt(evaluateInModel(expr))

    private fun extractInt(expr: UExpr<out USort>): Int =
        (expr as? KBitVecValue)?.toBigIntegerSigned()?.toInt() ?: error("Unexpected expr $expr")

    private fun extractCellData(expr: UExpr<out USort>): String =
        (expr as? KBitVecValue)?.stringValue ?: error("Unexpected expr $expr")

    private fun extractInt257(expr: UExpr<out USort>): BigInteger =
        (expr as? KBitVecValue)?.toBigIntegerSigned() ?: error("Unexpected expr $expr")
}