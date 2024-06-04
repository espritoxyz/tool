package org.ton

sealed interface TvmParameterInfo {

    data object NoInfo: TvmParameterInfo

    data class DataCellInfo(val dataCellStructure: TvmDataCellStructure): TvmParameterInfo
}