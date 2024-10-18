package org.ton

import org.ton.TvmDataCellStructure.Empty
import org.ton.TvmDataCellStructure.SwitchPrefix
import org.ton.TvmDataCellStructure.KnownTypePrefix
import org.ton.TvmDataCellStructure.LoadRef

/**
 * [TvmDataCellLabel] is a building block of TL-B schemes.
 * This is something that can be used as a prefix in [KnownTypePrefix] structure.
 * */
sealed interface TvmDataCellLabel

/**
 * Some builtin [TvmDataCellLabel].
 * It can be both [TvmAtomicDataCellLabel] or [TvmCompositeDataCellLabel].
 * */
sealed interface TvmBuiltinDataCellLabel

/**
 * TL-B primitive.
 * */
sealed interface TvmAtomicDataCellLabel : TvmDataCellLabel

/**
 * Named TL-B definition.
 * */
open class TvmCompositeDataCellLabel(
    val name: String,  // TODO: proper id
) : TvmDataCellLabel {
    // this is lateinit for supporting recursive structure
    lateinit var internalStructure: TvmDataCellStructure

    constructor(name: String, internalStructure: TvmDataCellStructure) : this(name) {
        this.internalStructure = internalStructure
    }
}

data class TvmIntegerLabel(
    val bitSize: Int,
    val isSigned: Boolean,
    val endian: Endian
) : TvmBuiltinDataCellLabel, TvmAtomicDataCellLabel

data object TvmEmptyLabel : TvmCompositeDataCellLabel("", Empty)

// TODO: other types of addresses (not just std)
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
