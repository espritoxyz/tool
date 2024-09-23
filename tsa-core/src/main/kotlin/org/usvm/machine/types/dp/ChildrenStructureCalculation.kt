package org.usvm.machine.types.dp

import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmDataCellStructure
import org.usvm.machine.TvmContext
import org.usvm.mkSizeExpr

fun calculateChildrenStructures(
    ctx: TvmContext,
    labelsWithoutUnknowns: Collection<TvmCompositeDataCellLabel>,
    dataLengths: List<Map<TvmCompositeDataCellLabel, AbstractSizeExpr>>,
    individualMaxCellTlbDepth: Map<TvmCompositeDataCellLabel, Int>,
): List<Map<TvmCompositeDataCellLabel, ChildrenStructure>> =
    calculateMapsByTlbDepth(labelsWithoutUnknowns) { label, curDepth, prevDepthValues ->
        val tlbDepthBound = individualMaxCellTlbDepth[label]
            ?: error("individualMaxCellTlbDepth must be calculated for all labels")

        if (tlbDepthBound >= curDepth) {
            val dataLengthsFromPreviousDepth = if (curDepth == 0) emptyMap() else dataLengths[curDepth - 1]
            getChildrenStructure(ctx, label.internalStructure, prevDepthValues, dataLengthsFromPreviousDepth)
        } else {
            prevDepthValues[label]
        }
    }

private fun getChildrenStructure(
    ctx: TvmContext,
    struct: TvmDataCellStructure,
    structuresFromPreviousDepth: Map<TvmCompositeDataCellLabel, ChildrenStructure>,
    dataLengthsFromPreviousDepth: Map<TvmCompositeDataCellLabel, AbstractSizeExpr>,
): ChildrenStructure? = with(ctx) {
    when (struct) {
        is TvmDataCellStructure.Unknown -> {
            error("Cannot calculate ChildrenStructure for Unknown leaf")
        }

        is TvmDataCellStructure.Empty -> {
            ChildrenStructure.empty(ctx)
        }

        is TvmDataCellStructure.LoadRef -> {
            // no need for data shift
            val furtherChildren = getChildrenStructure(ctx, struct.selfRest, structuresFromPreviousDepth, dataLengthsFromPreviousDepth)
                ?: return null  // cannot construct with given depth

            val exceededGuard = furtherChildren.children.last().exists()

            val newChildren = listOf(
                ChildStructure(mapOf(struct.ref to abstractTrue))
            ) + furtherChildren.children.subList(0, furtherChildren.children.size - 1)

            ChildrenStructure(newChildren, exceededGuard)
        }

        is TvmDataCellStructure.KnownTypePrefix -> {
            val offset = getKnownTypePrefixDataOffset(struct, dataLengthsFromPreviousDepth)
                ?: return null  // cannot construct with given depth

            val furtherChildren = getChildrenStructure(
                ctx,
                struct.rest,
                structuresFromPreviousDepth,
                dataLengthsFromPreviousDepth
            )?.shift(offset)
                ?: return null  // cannot construct with given depth

            if (struct.typeOfPrefix !is TvmCompositeDataCellLabel) {
                return furtherChildren
            }

            val innerChildren = structuresFromPreviousDepth[struct.typeOfPrefix]
                ?: return null  // cannot construct with given depth

            var newExceeded = innerChildren.numberOfChildrenExceeded or furtherChildren.numberOfChildrenExceeded

            for (i in 0 until TvmContext.MAX_REFS_NUMBER) {
                newExceeded = newExceeded or
                        (innerChildren.children[i].exists() and furtherChildren.children[3 - i].exists())
            }

            val newChildren = List(4) { childIdx ->
                var result = innerChildren.children[childIdx]
                for (childrenInInner in 0..childIdx) {
                    val guard = innerChildren.exactNumberOfChildren(ctx, childrenInInner)
                    result = result union (furtherChildren.children[childIdx - childrenInInner] and guard)
                }
                result
            }

            ChildrenStructure(newChildren, newExceeded)
        }

        is TvmDataCellStructure.SwitchPrefix -> {
            var atLeastOneBranch = false
            val switchSize = mkSizeExpr(struct.switchSize)
            val result = struct.variants.entries.fold(ChildrenStructure.empty(ctx)) { acc, (key, rest) ->
                val further = getChildrenStructure(
                    ctx,
                    rest,
                    structuresFromPreviousDepth,
                    dataLengthsFromPreviousDepth
                )?.shift(AbstractSizeExpr { switchSize })
                    ?: return@fold acc  // this branch is not reachable with given depth

                atLeastOneBranch = true
                val variantGuard = generateSwitchGuard(struct.switchSize, key)

                acc union (further and variantGuard)
            }

            if (!atLeastOneBranch) {
                null
            } else {
                result
            }
        }
    }
}
