package org.ton

sealed interface TvmParameterInfo {

    data object NoInfo : TvmParameterInfo

    data class SliceInfo(val cellInfo: DataCellInfo) : TvmParameterInfo

    sealed interface CellInfo : TvmParameterInfo

    data class DataCellInfo(val dataCellStructure: TvmDataCellStructure) : CellInfo

    data class DictCellInfo(val keySize: Int) : CellInfo
}