package org.usvm.machine.types.dp

import org.ton.TvmAtomicDataCellLabel
import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmDataCellStructure
import org.usvm.machine.TvmContext
import org.usvm.machine.types.maximumLength
import kotlin.math.min

/**
 * Maximum TL-B depth is calculated based on data length.
 * */
fun calculateMaxCellTlbDepths(
    labels: Collection<TvmCompositeDataCellLabel>
): Map<TvmCompositeDataCellLabel, Int> {
    val result = hashMapOf<TvmCompositeDataCellLabel, Int>()

    // Calculate trimmed maximum possible data length
    // Then, based on that, calculate maximum TL-B depth
    calculateMapsByTlbDepth(labels) { label, curDepth, prevDepthValues ->
        val curResult = result[label]
        if (curResult != null)
            return@calculateMapsByTlbDepth TvmContext.MAX_DATA_LENGTH + 1

        val value = getMaxCellLength(label.internalStructure, prevDepthValues)
        if (value != null && value > TvmContext.MAX_DATA_LENGTH) {
            result[label] = curDepth - 1
        }

        value?.let { min(it, TvmContext.MAX_DATA_LENGTH + 1) }
    }

    labels.forEach { label ->
        if (label !in result) {
            result[label] = MAX_TLB_DEPTH
        }
    }

    return result
}

private fun getMaxCellLength(
    struct: TvmDataCellStructure,
    maxLengthFromPreviousDepth: Map<TvmCompositeDataCellLabel, Int>
): Int? {
    return when (struct) {
        is TvmDataCellStructure.Unknown, is TvmDataCellStructure.Empty -> {
            0
        }

        is TvmDataCellStructure.LoadRef -> {
            getMaxCellLength(struct.selfRest, maxLengthFromPreviousDepth)
        }

        is TvmDataCellStructure.KnownTypePrefix -> {
            val further = getMaxCellLength(struct.rest, maxLengthFromPreviousDepth)
                ?: return null

            val offset = when (struct.typeOfPrefix) {
                is TvmAtomicDataCellLabel -> struct.typeOfPrefix.maximumLength()
                is TvmCompositeDataCellLabel -> maxLengthFromPreviousDepth[struct.typeOfPrefix]
            } ?: return null

            offset + further
        }

        is TvmDataCellStructure.SwitchPrefix -> {
            var longestVariant: Int? = null
            for (variant in struct.variants.values) {
                val further = getMaxCellLength(variant, maxLengthFromPreviousDepth)
                if (further != null && (longestVariant == null || longestVariant < further)) {
                    longestVariant = further
                }
            }

            longestVariant?.let { it + struct.switchSize }
        }
    }
}
