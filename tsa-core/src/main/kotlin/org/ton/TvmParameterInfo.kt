package org.ton

sealed interface TvmParameterInfo {

    data object NoInfo : TvmParameterInfo

    data class SliceInfo(val cellInfo: DataCellInfo) : TvmParameterInfo

    sealed interface CellInfo : TvmParameterInfo
    data object UnknownCellInfo : CellInfo
    data class DataCellInfo(val dataCellStructure: TvmDataCellLabel) : CellInfo
    data class DictCellInfo(val keySize: Int) : CellInfo
}
