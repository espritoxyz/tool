package org.usvm.machine.types.dp

import org.ton.TvmAtomicDataCellLabel
import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmDataCellStructure
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.api.readField
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.state.TvmState
import org.usvm.machine.types.offset
import org.usvm.mkSizeAddExpr
import org.usvm.mkSizeExpr
import org.usvm.mkSizeGeExpr
import org.usvm.sizeSort

/**
 * Returns size information for lists (Empty or Unknown).
 * */
fun calculateSizeInfoForLeaves(
    address: UConcreteHeapRef,
    state: TvmState,
    structure: TvmDataCellStructure,
    dataLengthsFromPreviousDepth: Map<TvmCompositeDataCellLabel, AbstractSizeExpr>,
    childrenStructureFromPreviousDepth: Map<TvmCompositeDataCellLabel, ChildrenStructure>,
): List<Pair<TvmDataCellStructure.Leaf, VertexCalculatedSize>> = with(state.ctx) {
    val result = mutableListOf<Pair<TvmDataCellStructure.Leaf, VertexCalculatedSize>>()

    val bfsQueue = ArrayDeque(listOf(structure to VertexCalculatedSize(trueExpr, zeroSizeExpr, zeroSizeExpr)))
    while (bfsQueue.isNotEmpty()) {
        val (curStruct, curSize) = bfsQueue.removeFirst()

        when (curStruct) {
            is TvmDataCellStructure.Leaf -> {
                result.add(curStruct to curSize)
            }

            is TvmDataCellStructure.LoadRef -> {
                val nextStruct = curStruct.selfRest
                val newSize = VertexCalculatedSize(
                    curSize.guard,
                    dataLength = curSize.dataLength,
                    refsLength = mkSizeAddExpr(curSize.refsLength, oneSizeExpr),
                )
                bfsQueue.add(nextStruct to newSize)
            }

            is TvmDataCellStructure.KnownTypePrefix -> {
                val nextStruct = curStruct.rest
                val (curDataOffset, curRefsOffset) = when (val label = curStruct.typeOfPrefix) {
                    is TvmAtomicDataCellLabel -> {
                        label.offset(state, address, curSize.dataLength) to zeroSizeExpr
                    }
                    is TvmCompositeDataCellLabel -> {
                        val dataOffset = dataLengthsFromPreviousDepth[label]
                            ?: error("dataLengthsFromPreviousDepth for $label must be calculated")
                        val childrenStructure = childrenStructureFromPreviousDepth[label]
                            ?: error("childrenStructureFromPreviousDepth for $label must be calculated")
                        val param = AbstractionForUExpr(address, curSize.dataLength, state)
                        val refsOffset = childrenStructure.numberOfChildren(this).apply(param)
                        dataOffset.apply(param) to refsOffset
                    }
                }
                val newSize = VertexCalculatedSize(
                    curSize.guard,
                    dataLength = mkSizeAddExpr(curSize.dataLength, curDataOffset),
                    refsLength = mkSizeAddExpr(curSize.refsLength, curRefsOffset),
                )
                bfsQueue.add(nextStruct to newSize)
            }

            is TvmDataCellStructure.SwitchPrefix -> {
                val newDataLength = mkSizeAddExpr(curSize.dataLength, mkSizeExpr(curStruct.switchSize))
                curStruct.variants.forEach { (key, nextStruct) ->
                    val switchGuard = generateSwitchGuard(curStruct.switchSize, key)
                    val param = AbstractionForUExpr(address, curSize.dataLength, state)
                    val newSize = VertexCalculatedSize(
                        curSize.guard and switchGuard.apply(param),
                        dataLength = newDataLength,
                        refsLength = curSize.refsLength,
                    )
                    bfsQueue.add(nextStruct to newSize)
                }
            }
        }
    }

    return result
}

data class VertexCalculatedSize(
    val guard: UBoolExpr,
    val dataLength: UExpr<TvmSizeSort>,
    val refsLength: UExpr<TvmSizeSort>,
)

fun calculateGeneralSizeConstraints(
    address: UConcreteHeapRef,
    state: TvmState,
    structure: TvmDataCellStructure,
    dataLengthsFromPreviousDepth: Map<TvmCompositeDataCellLabel, AbstractSizeExpr>,
    childrenStructureFromPreviousDepth: Map<TvmCompositeDataCellLabel, ChildrenStructure>,
): UBoolExpr = with(state.ctx) {
    val dataLengthField = state.memory.readField(address, TvmContext.cellDataLengthField, sizeSort)
    val refsLengthField = state.memory.readField(address, TvmContext.cellRefsLengthField, sizeSort)

    val info = calculateSizeInfoForLeaves(address, state, structure, dataLengthsFromPreviousDepth, childrenStructureFromPreviousDepth)
    return info.fold(trueExpr as UBoolExpr) { acc, (leaf, sizeInfo) ->
        when (leaf) {
            is TvmDataCellStructure.Unknown -> {
                val newGuard = mkSizeGeExpr(dataLengthField, sizeInfo.dataLength) and mkSizeGeExpr(refsLengthField, sizeInfo.refsLength)
                acc and (sizeInfo.guard implies newGuard)
            }
            is TvmDataCellStructure.Empty -> {
                val newGuard = (dataLengthField eq sizeInfo.dataLength) and (refsLengthField eq sizeInfo.refsLength)
                acc and (sizeInfo.guard implies newGuard)
            }
        }
    }
}
