package org.ton

sealed interface TvmDataCellLabel

sealed interface TvmPrependingSwitchDataCellLabel : TvmDataCellLabel
sealed interface TvmRealDataCellLabel : TvmDataCellLabel

data class TvmIntegerLabel(val bitSize: Int, val isSigned: Boolean, val endian: Endian) : TvmRealDataCellLabel

data object TvmMsgAddrLabel : TvmRealDataCellLabel, TvmPrependingSwitchDataCellLabel
data object TvmMaybeLabel : TvmRealDataCellLabel, TvmPrependingSwitchDataCellLabel

// artificial label
data object TvmInternalStdMsgAddrLabel : TvmDataCellLabel

data object TvmCoinsLabel : TvmRealDataCellLabel

enum class Endian {
    LittleEndian,
    BigEndian
}