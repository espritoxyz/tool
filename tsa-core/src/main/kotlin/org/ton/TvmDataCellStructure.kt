package org.ton

import org.usvm.test.resolver.TvmCellDataType

sealed interface TvmDataCellStructure {
    data object Unknown: TvmDataCellStructure
    data object Empty: TvmDataCellStructure

    class KnownTypePrefix(
        val typeOfPrefix: TvmCellDataType,
        val rest: TvmDataCellStructure,
    ): TvmDataCellStructure

    class SwitchPrefix(
        val switchSize: Int,
        val variants: Map<String, SwitchVariant>
    ): TvmDataCellStructure

    class SwitchVariant(
        val selfRest: TvmDataCellStructure,
        val refs: List<TvmDataCellStructure> = emptyList()
    )
}