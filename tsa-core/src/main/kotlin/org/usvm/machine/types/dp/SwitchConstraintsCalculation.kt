package org.usvm.machine.types.dp

import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmDataCellStructure
import org.usvm.machine.TvmContext
import org.usvm.mkSizeExpr

fun calculateSwitchConstraints(
    ctx: TvmContext,
    labels: Collection<TvmCompositeDataCellLabel>,
    dataLengths: List<Map<TvmCompositeDataCellLabel, AbstractSizeExpr>>,
    individualMaxCellTlbDepth: Map<TvmCompositeDataCellLabel, Int>,
): List<Map<TvmCompositeDataCellLabel, AbstractGuard>> =
    calculateMapsByTlbDepth(labels) { label, curDepth, prevDepthValues ->
        val tlbDepthBound = individualMaxCellTlbDepth[label]
            ?: error("individualMaxCellTlbDepth must be calculated for all labels")

        if (tlbDepthBound >= curDepth) {
            val dataLengthsFromPreviousDepth = if (curDepth == 0) emptyMap() else dataLengths[curDepth - 1]
            getSwitchConstraints(ctx, label.internalStructure, prevDepthValues, dataLengthsFromPreviousDepth)
        } else {
            prevDepthValues[label] ?: error("The value should be counted by now")
        }
    }

private fun getSwitchConstraints(
    ctx: TvmContext,
    struct: TvmDataCellStructure,
    constraintsFromPreviousDepth: Map<TvmCompositeDataCellLabel, AbstractGuard>,
    dataLengthsFromPreviousDepth: Map<TvmCompositeDataCellLabel, AbstractSizeExpr>,
): AbstractGuard = with(ctx) {
    when (struct) {
        is TvmDataCellStructure.Unknown, is TvmDataCellStructure.Empty -> {
            // no constraints here
            abstractTrue
        }

        is TvmDataCellStructure.LoadRef -> {
            // no need for data shift
            getSwitchConstraints(ctx, struct.selfRest, constraintsFromPreviousDepth, dataLengthsFromPreviousDepth)
        }

        is TvmDataCellStructure.KnownTypePrefix -> {
            val offset = getKnownTypePrefixDataOffset(struct, dataLengthsFromPreviousDepth)
                ?: return abstractFalse  // cannot construct with given depth

            val innerGuard = if (struct.typeOfPrefix is TvmCompositeDataCellLabel) {
                constraintsFromPreviousDepth[struct.typeOfPrefix]
                    ?: return abstractFalse  // cannot construct with given depth
            } else {
                abstractTrue
            }

            val further = getSwitchConstraints(ctx, struct.rest, constraintsFromPreviousDepth, dataLengthsFromPreviousDepth)

            innerGuard and further.shift(offset)
        }

        is TvmDataCellStructure.SwitchPrefix -> {
            val switchSize = mkSizeExpr(struct.switchSize)
            struct.variants.entries.fold(abstractFalse) { acc, (key, variant) ->
                val further = getSwitchConstraints(
                    ctx,
                    variant,
                    constraintsFromPreviousDepth,
                    dataLengthsFromPreviousDepth
                ).shift(AbstractSizeExpr { switchSize })

                val switchGuard = generateSwitchGuard(struct.switchSize, key)

                acc or (switchGuard and further)
            }
        }
    }
}
