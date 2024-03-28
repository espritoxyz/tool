package org.usvm.test

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.BvUtils.toBigIntegerSigned
import java.math.BigInteger
import org.ton.bytecode.TvmCodeBlock
import org.ton.bytecode.TvmType
import org.usvm.NULL_ADDRESS
import org.usvm.UConcreteHeapAddress
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.USort
import org.usvm.api.readField
import org.usvm.machine.TvmContext
import org.usvm.machine.state.TvmRefsMemoryRegion
import org.usvm.machine.state.TvmStack
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.tvmCellRefsRegion
import org.usvm.memory.UMemory
import org.usvm.model.UModelBase
import org.usvm.sizeSort

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

    fun resolveParameters(): List<TvmTestValue> =
        stack.inputElements.mapNotNull { entry ->
            val stackValue = entry.cell ?: return@mapNotNull null

            when (stackValue) {
                is TvmStack.TvmStackIntValue -> resolveInt257(stackValue.intValue)
                is TvmStack.TvmStackCellValue -> resolveCell(stackValue.cellValue)
                is TvmStack.TvmStackSliceValue -> resolveSlice(stackValue.sliceValue)
                is TvmStack.TvmStackBuilderValue -> resolveBuilder(stackValue.builderValue)
                is TvmStack.TvmStackNullValue -> TvmTestNullValue
                is TvmStack.TvmStackContinuationValue -> TODO()
                is TvmStack.TvmStackTupleValue -> TODO()
            }
        }.reversed()

    private fun <T : USort> evaluateInModel(expr: UExpr<T>): UExpr<T> = model.eval(expr)

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
        if (ref.address == NULL_ADDRESS) error("Unexpected null address")

        val cached = resolvedCache[ref.address]
        if (cached != null) return cached

        val data = resolveCellData(cell)

        val refsLength = resolveInt(memory.readField(cell, TvmContext.cellRefsLengthField, sizeSort))
        val refs = mutableListOf<TvmTestCellValue>()

        val storedRefs = mutableMapOf<Int, UHeapRef>()
        var updateNode = memory.tvmCellRefsRegion().getRefsUpdateNode(ref)

        while (updateNode != null) {
            when (updateNode) {
                is TvmRefsMemoryRegion.TvmRefsRegionInputNode -> {
                    val idx = resolveInt(updateNode.key)
                    storedRefs.putIfAbsent(idx, updateNode.value)
                }

                is TvmRefsMemoryRegion.TvmRefsRegionEmptyUpdateNode -> {}

                else -> error("Unexpected update node $updateNode")
            }

            updateNode = updateNode.prevUpdate
        }

        for (idx in 0 until refsLength) {
            val unresolvedRefCell = storedRefs[idx]

            val refCell = if (unresolvedRefCell != null) {
                resolveCell(unresolvedRefCell)
            } else {
                TvmTestCellValue()
            }

            refs.add(refCell)
        }

        val tvmCellValue = TvmTestCellValue(data, refs)

        tvmCellValue.also { resolvedCache[ref.address] = tvmCellValue }
    }

    private fun resolveInt257(expr: UExpr<out USort>): TvmTestIntegerValue {
        val value = extractInt257(evaluateInModel(expr))
        return TvmTestIntegerValue(value)
    }

    private fun resolveCellData(cell: UHeapRef): String = with(ctx) {
        val symbolicData = memory.readField(cell, TvmContext.cellDataField, cellDataSort)
        val data = extractCellData(evaluateInModel(symbolicData))
        val dataLength = resolveInt(memory.readField(cell, TvmContext.cellDataLengthField, sizeSort))

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