package org.usvm.test

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.BvUtils.toBigIntegerSigned
import org.ton.bytecode.TvmCodeBlock
import org.ton.bytecode.TvmType
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
import org.usvm.machine.state.TvmRefsMemoryRegion
import org.usvm.machine.state.TvmStack
import org.usvm.machine.state.TvmStack.TvmStackEntry
import org.usvm.machine.state.TvmStack.TvmStackTupleValue
import org.usvm.machine.state.TvmStack.TvmStackTupleValueConcreteNew
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.calcConsumedGas
import org.usvm.machine.state.lastStmt
import org.usvm.machine.state.tvmCellRefsRegion
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

    fun resolveParameters(): List<TvmTestValue> = stack.inputElements.mapNotNull { resolveEntry(it) }.reversed()

    fun resolveResult(): TvmMethodSymbolicResult {
        val results = state.stack.results

        // Do not include exit code for exceptional results to the result
        val resultsWithoutExitCode = if (state.methodResult is TvmMethodResult.TvmFailure) results.dropLast(1) else results
        val resolvedResults = resultsWithoutExitCode.mapNotNull { resolveEntry(it) }

        return when (val it = state.methodResult) {
            TvmMethodResult.NoCall -> error("Missed result for state $state")
            is TvmMethodResult.TvmFailure -> TvmMethodFailure(it, state.lastStmt, resolvedResults)
            is TvmMethodResult.TvmSuccess -> TvmSuccessfulExecution(resolvedResults)
        }
    }

    fun resolveGasUsage(): Int = model.eval(state.calcConsumedGas()).intValue()

    fun resolveEntry(entry: TvmStackEntry): TvmTestValue? {
        val stackValue = entry.cell ?: return null

        return when (stackValue) {
            is TvmStack.TvmStackIntValue -> resolveInt257(stackValue.intValue)
            is TvmStack.TvmStackCellValue -> resolveCell(stackValue.cellValue)
            is TvmStack.TvmStackSliceValue -> resolveSlice(stackValue.sliceValue)
            is TvmStack.TvmStackBuilderValue -> resolveBuilder(stackValue.builderValue)
            is TvmStack.TvmStackNullValue -> TvmTestNullValue
            is TvmStack.TvmStackContinuationValue -> TODO()
            is TvmStackTupleValue -> resolveTuple(stackValue)
        }
    }

    private fun <T : USort> evaluateInModel(expr: UExpr<T>): UExpr<T> = model.eval(expr)

    private fun resolveTuple(tuple: TvmStackTupleValue): TvmTestTupleValue = when (tuple) {
        is TvmStackTupleValueConcreteNew -> {
            val elements = tuple.entries.map {
                resolveEntry(it)
                    ?: TvmTestNullValue // We do not care what is its real value as it was never used
            }

            TvmTestTupleValue(elements)
        }
        is TvmStack.TvmStackTupleValueInputValue -> {
            val size = resolveInt(tuple.size)
            val elements = (0 ..< size).map {
                resolveEntry(tuple[it, stack])
                    ?: TvmTestNullValue // We do not care what is its real value as it was never used
            }

            TvmTestTupleValue(elements)
        }
    }

    private fun resolveBuilder(builder: UHeapRef): TvmTestBuilderValue {
        val cell = resolveCell(builder)
        return TvmTestBuilderValue(cell.data, cell.refs)
    }

    private fun resolveSlice(slice: UHeapRef): TvmTestSliceValue = with(ctx) {
        val cellValue = resolveCell(memory.readField(slice, TvmContext.sliceCellField, addressSort))
        val dataPosValue = resolveInt(memory.readField(slice, TvmContext.sliceDataPosField, sizeSort))
        val refPosValue = resolveInt(memory.readField(slice, TvmContext.sliceRefPosField, sizeSort))

        TvmTestSliceValue(cellValue, dataPosValue, refPosValue)
    }

    private fun resolveCell(cell: UHeapRef): TvmTestCellValue = with(ctx) {
        val ref = evaluateInModel(cell) as UConcreteHeapRef
        if (ref.address == NULL_ADDRESS) {
            return@with TvmTestCellValue()
        }

        val cached = resolvedCache[ref.address]
        if (cached != null) return cached

        val data = resolveCellData(cell)

        val refsLength = resolveInt(memory.readField(cell, TvmContext.cellRefsLengthField, sizeSort)).coerceAtMost(TvmContext.MAX_REFS_NUMBER)
        val refs = mutableListOf<TvmTestCellValue>()

        val storedRefs = mutableMapOf<Int, TvmTestCellValue>()
        val updateNode = memory.tvmCellRefsRegion().getRefsUpdateNode(ref)

        resolveRefUpdates(updateNode, storedRefs)

        for (idx in 0 until refsLength) {
            val refCell = storedRefs[idx]
                ?: TvmTestCellValue()

            refs.add(refCell)
        }

        val tvmCellValue = TvmTestCellValue(data, refs)

        tvmCellValue.also { resolvedCache[ref.address] = tvmCellValue }
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
                    val refCell = resolveCell(updateNode.value)
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

    private fun resolveInt257(expr: UExpr<out USort>): TvmTestIntegerValue {
        val value = extractInt257(evaluateInModel(expr))
        return TvmTestIntegerValue(value)
    }

    private fun resolveCellData(cell: UHeapRef): String = with(ctx) {
        val symbolicData = memory.readField(cell, TvmContext.cellDataField, cellDataSort)
        val data = extractCellData(evaluateInModel(symbolicData))
        val dataLength = resolveInt(memory.readField(cell, TvmContext.cellDataLengthField, sizeSort))
            .coerceAtMost(TvmContext.MAX_DATA_LENGTH).coerceAtLeast(0)

        return data.drop(TvmContext.MAX_DATA_LENGTH - dataLength)
    }

    private fun resolveInt(expr: UExpr<out USort>): Int = extractInt(evaluateInModel(expr))

    private fun extractInt(expr: UExpr<out USort>): Int =
        (expr as? KBitVecValue)?.toBigIntegerSigned()?.toInt() ?: error("Unexpected expr $expr")

    private fun extractCellData(expr: UExpr<out USort>): String =
        (expr as? KBitVecValue)?.stringValue ?: error("Unexpected expr $expr")

    private fun extractInt257(expr: UExpr<out USort>): BigInteger =
        (expr as? KBitVecValue)?.toBigIntegerSigned() ?: error("Unexpected expr $expr")
}