package org.ton

import org.ton.TvmDataCellStructure.*

sealed interface TvmDataCellLabel

sealed interface TvmBuiltinDataCellLabel

sealed interface TvmAtomicDataCellLabel : TvmDataCellLabel

open class TvmCompositeDataCellLabel(
    val name: String,  // TODO: proper id
    val internalStructure: TvmDataCellStructure,
) : TvmDataCellLabel

data class TvmIntegerLabel(
    val bitSize: Int,
    val isSigned: Boolean,
    val endian: Endian
) : TvmBuiltinDataCellLabel, TvmAtomicDataCellLabel

data object TvmMsgAddrLabel : TvmBuiltinDataCellLabel, TvmCompositeDataCellLabel(
    "MsgAddr",
    SwitchPrefix(
        switchSize = 3,
        mapOf(
            "100" to KnownTypePrefix(
                TvmInternalStdMsgAddrLabel,
                rest = Empty
            )
        )
    )
)

data class TvmMaybeRefLabel(
    val refInfo: TvmParameterInfo.CellInfo,
) : TvmBuiltinDataCellLabel, TvmCompositeDataCellLabel(
    "Maybe",
    SwitchPrefix(
        switchSize = 1,
        variants = mapOf(
            "0" to Empty,
            "1" to LoadRef(
                ref = refInfo,
                selfRest = Empty
            ),
        ),
    )
)

// artificial label
data object TvmInternalStdMsgAddrLabel : TvmAtomicDataCellLabel

data object TvmCoinsLabel : TvmBuiltinDataCellLabel, TvmAtomicDataCellLabel

enum class Endian {
    LittleEndian,
    BigEndian
}
