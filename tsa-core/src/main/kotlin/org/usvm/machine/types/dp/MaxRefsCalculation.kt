package org.usvm.machine.types.dp

import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmDataCellStructure
import org.usvm.machine.TvmContext
import kotlin.math.min

fun calculateMaximumRefs(
    compositeLabels: Collection<TvmCompositeDataCellLabel>,
    individualMaxCellTlbDepth: Map<TvmCompositeDataCellLabel, Int>,
): List<Map<TvmCompositeDataCellLabel, Int>> =
    calculateMapsByTlbDepth(compositeLabels) { label, curDepth, prevDepthValues ->
        val tlbDepthBound = individualMaxCellTlbDepth[label]
            ?: error("individualMaxCellTlbDepth must be calculated for all labels")

        if (tlbDepthBound >= curDepth) {
            getMaximumRefs(label.internalStructure, prevDepthValues)
        } else {
            prevDepthValues[label]
        }
    }

/**
 * If returns null, construction of the given depth is impossible.
 * */
private fun getMaximumRefs(
    struct: TvmDataCellStructure,
    maxRefsFromPreviousDepth: Map<TvmCompositeDataCellLabel, Int>,
): Int? {
    val cur: Int = when (struct) {
        is TvmDataCellStructure.Unknown -> {
            TvmContext.MAX_REFS_NUMBER
        }
        is TvmDataCellStructure.Empty -> {
            0
        }
        is TvmDataCellStructure.SwitchPrefix -> {
            struct.variants.values.maxOf { getMaximumRefs(it, maxRefsFromPreviousDepth) ?: -1 }
        }
        is TvmDataCellStructure.LoadRef -> {
            1 + (getMaximumRefs(struct.selfRest, maxRefsFromPreviousDepth) ?: return null)
        }
        is TvmDataCellStructure.KnownTypePrefix -> {
            val add = if (struct.typeOfPrefix is TvmCompositeDataCellLabel) {
                maxRefsFromPreviousDepth[struct.typeOfPrefix]
                    ?: return null
            } else {
                0
            }
            val further = getMaximumRefs(struct.rest, maxRefsFromPreviousDepth)
                ?: return null
            add + further
        }
    }
    if (cur == -1)
        return null
    return min(cur, TvmContext.MAX_REFS_NUMBER)
}
