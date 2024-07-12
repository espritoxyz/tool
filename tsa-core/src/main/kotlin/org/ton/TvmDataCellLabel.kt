package org.ton

import org.ton.TvmDataCellStructure.*

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

    override val internalStructure: TvmDataCellStructure = SwitchPrefix(
        switchSize = 3,
        mapOf(
            "100" to KnownTypePrefix(
                TvmInternalStdMsgAddrLabel,
                rest = Empty
            )
        )
    )
}

data class TvmMaybeRefLabel(
    val refInfo: TvmParameterInfo.CellInfo,
) : TvmBuiltinDataCellLabel, TvmCompositeDataCellLabel {
    override val name: String = "Maybe"
    override val internalStructure: TvmDataCellStructure = SwitchPrefix(
        switchSize = 1,
        variants = mapOf(
            "0" to Empty,
            "1" to LoadRef(
                ref = refInfo,
                selfRest = Empty
            ),
        ),
    )
}

// artificial label
data object TvmInternalStdMsgAddrLabel : TvmAtomicDataCellLabel

data object TvmCoinsLabel : TvmBuiltinDataCellLabel, TvmAtomicDataCellLabel

enum class Endian {
    LittleEndian,
    BigEndian
}
