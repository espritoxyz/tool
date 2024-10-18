package org.usvm.machine.types.dp

import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmDataCellStructure

fun calculateMinTlbDepth(
    compositeLabels: Collection<TvmCompositeDataCellLabel>
): Map<TvmCompositeDataCellLabel, Int> {
    val result = hashMapOf<TvmCompositeDataCellLabel, Int>()
    compositeLabels.forEach { label ->
        if (zeroDepthIsPossible(label)) {
            result[label] = 0
        }
    }
    for (curDepth in 1..MAX_TLB_DEPTH) {
        val newLabels = mutableListOf<TvmCompositeDataCellLabel>()
        compositeLabels.forEach { label ->
            if (result[label] != null)
                return@forEach
            val currentDepthIsPossible = constructionIsPossible(label.internalStructure) { result[it] != null }
            if (currentDepthIsPossible)
                newLabels.add(label)
        }
        newLabels.forEach { result[it] = curDepth }
    }
    return result
}

private fun zeroDepthIsPossible(label: TvmCompositeDataCellLabel) =
    zeroDepthIsPossible(label.internalStructure)

private fun zeroDepthIsPossible(struct: TvmDataCellStructure): Boolean =
    constructionIsPossible(struct) { false }

private fun constructionIsPossible(
    struct: TvmDataCellStructure,
    possibleCompositeLabel: (TvmCompositeDataCellLabel) -> Boolean
): Boolean =
    when (struct) {
        is TvmDataCellStructure.Unknown, is TvmDataCellStructure.Empty -> {
            true
        }
        is TvmDataCellStructure.LoadRef -> {
            constructionIsPossible(struct.selfRest, possibleCompositeLabel)
        }
        is TvmDataCellStructure.KnownTypePrefix -> {
            if (struct.typeOfPrefix is TvmCompositeDataCellLabel) {
                possibleCompositeLabel(struct.typeOfPrefix)
            } else {
                constructionIsPossible(struct.rest, possibleCompositeLabel)
            }
        }
        is TvmDataCellStructure.SwitchPrefix -> {
            struct.variants.any { constructionIsPossible(it.value, possibleCompositeLabel) }
        }
    }
