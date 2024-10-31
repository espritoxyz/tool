package org.usvm.machine.types.dp

import org.ton.TvmAtomicDataCellLabel
import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmDataCellStructure
import org.ton.TvmParameterInfo
import org.usvm.machine.TvmContext
import org.usvm.machine.types.defaultCellValueOfMinimalLength
import org.usvm.test.resolver.TvmTestDataCellValue
import org.usvm.test.resolver.TvmTestDictCellValue
import org.usvm.test.resolver.TvmTestIntegerValue
import org.usvm.test.resolver.TvmTestSliceValue
import java.math.BigInteger

private data class DPParamsForDefaultCellCalculation(
    val maxRefs: Int,
    val maxTlbDepth: Int,
    val maxCellDepth: Int,
    val label: TvmCompositeDataCellLabel,
)

fun getDefaultDict(keyLength: Int): TvmTestDictCellValue {  // dict mustn't be empty
    return TvmTestDictCellValue(
        keyLength,
        mapOf(
            TvmTestIntegerValue(BigInteger.ZERO) to TvmTestSliceValue(TvmTestDataCellValue(), dataPos = 0, refPos = 0),
        )
    )
}

fun calculateDefaultCells(
    labels: Collection<TvmCompositeDataCellLabel>,
    individualMaxCellTlbDepth: Map<TvmCompositeDataCellLabel, Int>,
): Map<TvmCompositeDataCellLabel, TvmTestDataCellValue> {
    val calculatedValues = hashMapOf<DPParamsForDefaultCellCalculation, TvmTestDataCellValue?>()
    for (maxCellDepth in 0..MAX_CELL_DEPTH_FOR_DEFAULT) {
        for (maxTlbDepth in 0..MAX_TLB_DEPTH) {
            for (maxRefs in 0..TvmContext.MAX_REFS_NUMBER) {
                for (label in labels) {
                    val tlbDepthBound = individualMaxCellTlbDepth[label]
                        ?: error("individualMaxCellTlbDepth must be calculated for all labels")

                    val result =
                        if (maxTlbDepth <= tlbDepthBound) {
                            getDefaultCell(
                                label.internalStructure,
                                maxRefs,
                                maxTlbDepth,
                                maxCellDepth,
                                calculatedValues
                            )
                        } else {
                            val params = DPParamsForDefaultCellCalculation(maxRefs, tlbDepthBound, maxCellDepth, label)
                            calculatedValues[params]
                        }

                    val params = DPParamsForDefaultCellCalculation(maxRefs, maxTlbDepth, maxCellDepth, label)
                    calculatedValues[params] = result

                }
            }
        }
    }

    val result = hashMapOf<TvmCompositeDataCellLabel, TvmTestDataCellValue>()
    labels.forEach { label ->
        val params = DPParamsForDefaultCellCalculation(
            maxRefs = TvmContext.MAX_REFS_NUMBER,
            maxTlbDepth = MAX_TLB_DEPTH,
            maxCellDepth = MAX_CELL_DEPTH_FOR_DEFAULT,
            label = label
        )
        calculatedValues[params]?.let { result[label] = it }
    }

    return result
}

private fun getDefaultCell(
    struct: TvmDataCellStructure,
    maxRefs: Int,
    maxTlbDepth: Int,
    maxCellDepth: Int,
    calculatedValues: Map<DPParamsForDefaultCellCalculation, TvmTestDataCellValue?>,
): TvmTestDataCellValue? {
    return when (struct) {
        is TvmDataCellStructure.Unknown, is TvmDataCellStructure.Empty -> {
            TvmTestDataCellValue()
        }

        is TvmDataCellStructure.LoadRef -> {
            if (maxRefs == 0 || maxCellDepth == 0) {
                return null
            }

            val nextCell = when (struct.ref) {
                is TvmParameterInfo.UnknownCellInfo -> {
                    TvmTestDataCellValue()
                }
                is TvmParameterInfo.DictCellInfo -> {
                    getDefaultDict(struct.ref.keySize)
                }
                is TvmParameterInfo.DataCellInfo -> {
                    when (struct.ref.dataCellStructure) {
                        is TvmAtomicDataCellLabel -> {
                            val content = struct.ref.dataCellStructure.defaultCellValueOfMinimalLength()
                            TvmTestDataCellValue(data = content)
                        }
                        is TvmCompositeDataCellLabel -> {
                            val params = DPParamsForDefaultCellCalculation(
                                maxRefs = TvmContext.MAX_REFS_NUMBER,
                                maxTlbDepth = MAX_TLB_DEPTH,
                                maxCellDepth = maxCellDepth - 1,
                                label = struct.ref.dataCellStructure
                            )
                            if (params !in calculatedValues.keys) {
                                error("Needed value was not calculated when it was needed during DP process")
                            }

                            calculatedValues[params]
                                ?: return null
                        }
                    }
                }
            }

            val furtherStruct = getDefaultCell(struct.selfRest, maxRefs - 1, maxTlbDepth, maxCellDepth, calculatedValues)
                ?: return null

            TvmTestDataCellValue(
                data = furtherStruct.data,
                refs = listOf(nextCell) + furtherStruct.refs
            )
        }

        is TvmDataCellStructure.KnownTypePrefix -> {
            when (struct.typeOfPrefix) {
                is TvmAtomicDataCellLabel -> {
                    val content = struct.typeOfPrefix.defaultCellValueOfMinimalLength()
                    val further = getDefaultCell(struct.rest, maxRefs, maxTlbDepth, maxCellDepth, calculatedValues)
                        ?: return null
                    if (content.length + further.data.length > TvmContext.MAX_DATA_LENGTH) {
                        return null
                    }
                    TvmTestDataCellValue(
                        data = content + further.data,
                        refs = further.refs
                    )
                }

                is TvmCompositeDataCellLabel -> {

                    if (maxTlbDepth == 0) {
                        return null
                    }

                    var result: TvmTestDataCellValue? = null

                    for (innerRefs in 0..maxRefs) {
                        val params = DPParamsForDefaultCellCalculation(
                            maxRefs = innerRefs,
                            maxTlbDepth = maxTlbDepth - 1,
                            maxCellDepth = maxCellDepth,
                            label = struct.typeOfPrefix,
                        )

                        if (params !in calculatedValues.keys) {
                            error("Needed value was not calculated when it was needed during DP process")
                        }

                        val variant = calculatedValues[params]
                            ?: continue

                        val further = getDefaultCell(
                            struct.rest,
                            maxRefs - innerRefs,
                            maxTlbDepth,
                            maxCellDepth,
                            calculatedValues,
                        )
                        if (further != null && (result == null || result.data.length > variant.data.length + further.data.length)) {
                            result = TvmTestDataCellValue(
                                data = variant.data + further.data,
                                refs = variant.refs + further.refs,
                            )
                        }
                    }
                    if (result == null || result.data.length > TvmContext.MAX_DATA_LENGTH) {
                        return null
                    }

                    result
                }
            }
        }

        is TvmDataCellStructure.SwitchPrefix -> {
            var result: TvmTestDataCellValue? = null

            for ((key, variant) in struct.variants) {
                val further = getDefaultCell(variant, maxRefs, maxTlbDepth, maxCellDepth, calculatedValues)

                if (further != null && (result == null || result.data.length > further.data.length + key.length)) {
                    result = TvmTestDataCellValue(
                        data = key + further.data,
                        further.refs
                    )
                }
            }

            result
        }
    }
}
