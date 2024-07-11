package org.ton

sealed interface TvmDataCellLabel

sealed interface TvmBuiltinDataCellLabel : TvmDataCellLabel

sealed interface TvmAtomicDataCellLabel : TvmDataCellLabel

interface TvmCompositeDataCellLabel : TvmDataCellLabel {
    val name: String  // TODO: proper id
    val internalStructure: TvmDataCellStructure
}

data class TvmIntegerLabel(
    val bitSize: Int,
    val isSigned: Boolean,
    val endian: Endian
) : TvmBuiltinDataCellLabel, TvmAtomicDataCellLabel

data object TvmMsgAddrLabel : TvmBuiltinDataCellLabel, TvmCompositeDataCellLabel {
    override val name: String = "MsgAddr"

    override val internalStructure: TvmDataCellStructure
        get() = TODO("Not yet implemented")
}

data object TvmMaybeRefLabel : TvmBuiltinDataCellLabel, TvmCompositeDataCellLabel {
    override val name: String = "Maybe"
    override val internalStructure: TvmDataCellStructure
        get() = TODO("Not yet implemented")
}

// artificial label
data object TvmInternalStdMsgAddrLabel : TvmDataCellLabel, TvmAtomicDataCellLabel

data object TvmCoinsLabel : TvmBuiltinDataCellLabel, TvmAtomicDataCellLabel

enum class Endian {
    LittleEndian,
    BigEndian
}
