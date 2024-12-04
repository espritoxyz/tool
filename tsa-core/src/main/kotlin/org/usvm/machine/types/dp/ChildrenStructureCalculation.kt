package org.usvm.machine.types.dp

import org.ton.TlbCompositeLabel
import org.ton.TlbStructure
import org.usvm.machine.TvmContext
import org.usvm.mkSizeExpr

fun calculateChildrenStructures(
    ctx: TvmContext,
    labelsWithoutUnknowns: Collection<TlbCompositeLabel>,
    dataLengths: List<Map<TlbCompositeLabel, AbstractSizeExpr>>,
    individualMaxCellTlbDepth: Map<TlbCompositeLabel, Int>,
): List<Map<TlbCompositeLabel, ChildrenStructure>> =
    calculateMapsByTlbDepth(ctx.tvmOptions.tlbOptions.maxTlbDepth, labelsWithoutUnknowns) { label, curDepth, prevDepthValues ->
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
    struct: TlbStructure,
    structuresFromPreviousDepth: Map<TlbCompositeLabel, ChildrenStructure>,
    dataLengthsFromPreviousDepth: Map<TlbCompositeLabel, AbstractSizeExpr>,
): ChildrenStructure? = with(ctx) {
    when (struct) {
        is TlbStructure.Unknown -> {
            error("Cannot calculate ChildrenStructure for Unknown leaf")
        }

        is TlbStructure.Empty -> {
            ChildrenStructure.empty(ctx)
        }

        is TlbStructure.LoadRef -> {
            // no need for data shift
            val furtherChildren = getChildrenStructure(ctx, struct.rest, structuresFromPreviousDepth, dataLengthsFromPreviousDepth)
                ?: return null  // cannot construct with given depth

            val exceededGuard = furtherChildren.children.last().exists()

            val newChildren = listOf(
                ChildStructure(mapOf(struct.ref to abstractTrue))
            ) + furtherChildren.children.subList(0, furtherChildren.children.size - 1)

            ChildrenStructure(newChildren, exceededGuard)
        }

        is TlbStructure.KnownTypePrefix -> {
            val offset = getKnownTypePrefixDataLength(struct, dataLengthsFromPreviousDepth)
                ?: return null  // cannot construct with given depth

            val furtherChildren = getChildrenStructure(
                ctx,
                struct.rest,
                structuresFromPreviousDepth,
                dataLengthsFromPreviousDepth
            )?.shift(offset)
                ?: return null  // cannot construct with given depth

            if (struct.typeLabel !is TlbCompositeLabel) {
                return furtherChildren
            }

            val innerChildren = structuresFromPreviousDepth[struct.typeLabel]?.addTlbLevel(struct)
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

        is TlbStructure.SwitchPrefix -> {
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
