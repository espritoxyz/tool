package org.ton

import org.usvm.test.resolver.TvmCellDataTypeLoad

sealed interface TvmParameterInfo {

    data object NoInfo: TvmParameterInfo

    data class DataCellInfo(
        val knownTypes: TvmCellDataTypeLoad
    ): TvmParameterInfo
}