package org.ton

sealed interface TvmParameterInfo {

    data object NoInfo: TvmParameterInfo

    data class DataCellInfo(val dataCellStructure: TvmDataCellStructure): TvmParameterInfo

    data class SliceInfo(val cellInfo: DataCellInfo): TvmParameterInfo
}