package org.ton

import org.usvm.test.resolver.TvmCellDataType

sealed interface TvmDataCellStructure {
    data object Empty: TvmDataCellStructure

    class KnownTypePrefix(
        val typeOfPrefix: TvmCellDataType,
        val rest: TvmDataCellStructure,
    ): TvmDataCellStructure

    class SwitchPrefix(
        val variants: Map<String, TvmDataCellStructure>
    ): TvmDataCellStructure
}