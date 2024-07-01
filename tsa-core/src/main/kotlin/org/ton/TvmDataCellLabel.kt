package org.ton

sealed interface TvmDataCellLabel

data class TvmIntegerLabel(val bitSize: Int, val isSigned: Boolean, val endian: Endian): TvmDataCellLabel

data object TvmMsgAddrLabel: TvmDataCellLabel

data object TvmCoinsLabel: TvmDataCellLabel

enum class Endian {
    LittleEndian,
    BigEndian
}