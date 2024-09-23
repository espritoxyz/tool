package org.usvm.machine.types.dp

import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmDataCellStructure
import org.usvm.machine.TvmContext
import org.usvm.mkSizeAddExpr
import org.usvm.mkSizeExpr

fun calculateDataLengths(
    ctx: TvmContext,
    labelsWithoutUnknowns: Collection<TvmCompositeDataCellLabel>,
    individualMaxCellTlbDepth: Map<TvmCompositeDataCellLabel, Int>,
): List<Map<TvmCompositeDataCellLabel, AbstractSizeExpr>> =
    calculateMapsByTlbDepth(labelsWithoutUnknowns) { label, curDepth, prevDepthValues ->
        val tlbDepthBound = individualMaxCellTlbDepth[label]
            ?: error("individualMaxCellTlbDepth must be calculated for all labels")

        if (tlbDepthBound >= curDepth) {
            getDataLength(ctx, label.internalStructure, prevDepthValues)
        } else {
            prevDepthValues[label]
        }
    }

/**
 * If returns null, construction of the given depth is impossible.
 * */
private fun getDataLength(
    ctx: TvmContext,
    struct: TvmDataCellStructure,
    lengthsFromPreviousDepth: Map<TvmCompositeDataCellLabel, AbstractSizeExpr>,
): AbstractSizeExpr? = with(ctx) {
    when (struct) {
        is TvmDataCellStructure.Unknown -> {
            error("Cannot calculate length for Unknown leaf")
        }

        is TvmDataCellStructure.Empty -> {
            AbstractSizeExpr { zeroSizeExpr }
        }

        is TvmDataCellStructure.LoadRef -> {
            // no need for shift
            getDataLength(ctx, struct.selfRest, lengthsFromPreviousDepth)
        }

        is TvmDataCellStructure.KnownTypePrefix -> {
            val furtherWithoutShift = getDataLength(ctx, struct.rest, lengthsFromPreviousDepth)
                ?: return null  // cannot construct with given depth

            val offset = getKnownTypePrefixDataOffset(struct, lengthsFromPreviousDepth)
                ?: return null  // cannot construct with given depth

            furtherWithoutShift.shiftAndAdd(offset)
        }

        is TvmDataCellStructure.SwitchPrefix -> {
            val switchSize = struct.switchSize
            val childLengths = struct.variants.mapNotNull { (key, variant) ->

                val furtherWithoutShift = getDataLength(ctx, variant, lengthsFromPreviousDepth)
                    ?: return@mapNotNull null  // cannot construct this variant with given depth

                val condition = generateSwitchGuard(switchSize, key)

                condition to furtherWithoutShift.shift(switchSize)
            }

            if (childLengths.isEmpty()) {
                return null  // cannot construct with given depth
            }

            var childIte = childLengths.first().second  // arbitrary value
            childLengths.subList(1, childLengths.size).forEach { (condition, value) ->
                val prev = childIte
                childIte = AbstractSizeExpr { param ->
                    mkIte(condition.apply(param), value.apply(param), prev.apply(param))
                }
            }

            AbstractSizeExpr {
                mkSizeAddExpr(childIte.apply(it), mkSizeExpr(switchSize))
            }
        }
    }
}
