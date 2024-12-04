package org.ton

import org.ton.TlbStructure.Empty
import org.ton.TlbStructure.KnownTypePrefix
import org.ton.TlbStructure.LoadRef
import org.ton.TlbStructure.SwitchPrefix
import org.usvm.UExpr
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmSizeSort
import org.usvm.mkSizeExpr

/**
 * [TlbLabel] is a building block of TL-B schemes.
 * This is something that can be used as a prefix in [KnownTypePrefix] structure.
 * */
sealed interface TlbLabel {
    val arity: Int
}

/**
 * Some builtin [TlbLabel].
 * It can be both [TlbAtomicLabel] or [TlbCompositeLabel].
 * */
sealed interface TlbBuiltinLabel

/**
 * TL-B primitive.
 * */
sealed interface TlbAtomicLabel : TlbLabel

sealed interface TlbResolvedBuiltinLabel : TlbBuiltinLabel

/**
 * Named TL-B definition.
 * */
open class TlbCompositeLabel(
    val name: String,  // TODO: proper id
    val definitelyHasAny: Boolean = false,
) : TlbLabel {
    // this is lateinit for supporting recursive structure
    lateinit var internalStructure: TlbStructure

    override val arity: Int = 0

    constructor(name: String, internalStructure: TlbStructure, hasAny: Boolean = false) : this(name, hasAny) {
        this.internalStructure = internalStructure
    }
}

fun createWrapperStructure(label: TlbAtomicLabel): TlbStructure {
    require(label.arity == 0)
    return KnownTypePrefix(
        id = TlbStructureIdProvider.provideId(),
        typeLabel = label,
        typeArgIds = emptyList(),
        rest = Empty,
    )
}

sealed interface TlbIntegerLabel : TlbBuiltinLabel, TlbAtomicLabel {
    val bitSize: (TvmContext, List<UExpr<TvmSizeSort>>) -> UExpr<TvmSizeSort>
    val isSigned: Boolean
    val endian: Endian
    val lengthUpperBound: Int
}

data class TlbIntegerLabelOfConcreteSize(
    val concreteSize: Int,
    override val isSigned: Boolean,
    override val endian: Endian,
) : TlbIntegerLabel, TlbResolvedBuiltinLabel {
    override val arity: Int = 0
    override val bitSize: (TvmContext, List<UExpr<TvmSizeSort>>) -> UExpr<TvmSizeSort> = { ctx, _ ->
        ctx.mkSizeExpr(concreteSize)
    }
    override val lengthUpperBound: Int
        get() = concreteSize
    val canBeUsedAsSizeForTlbIntegers: Boolean
        get() = concreteSize <= 31
}

class TlbIntegerLabelOfSymbolicSize(
    override val isSigned: Boolean,
    override val endian: Endian,
    override val arity: Int,
    override val lengthUpperBound: Int = if (isSigned) 257 else 256,
    override val bitSize: (TvmContext, List<UExpr<TvmSizeSort>>) -> UExpr<TvmSizeSort>,
) : TlbIntegerLabel

data object TlbEmptyLabel : TlbCompositeLabel("", Empty)

sealed interface TlbMsgAddrLabel : TlbResolvedBuiltinLabel

// TODO: other types of addresses (not just std)
data object TlbFullMsgAddrLabel : TlbMsgAddrLabel, TlbCompositeLabel(
    "MsgAddr",
    SwitchPrefix(
        id = TlbStructureIdProvider.provideId(),
        switchSize = 3,
        mapOf(
            "100" to KnownTypePrefix(
                id = TlbStructureIdProvider.provideId(),
                TlbInternalStdMsgAddrLabel,
                typeArgIds = emptyList(),
                rest = Empty
            )
        )
    )
)

data object TlbBasicMsgAddrLabel : TlbMsgAddrLabel, TlbCompositeLabel(
    "MsgAddr",
    SwitchPrefix(
        id = TlbStructureIdProvider.provideId(),
        switchSize = 11,
        mapOf(
            "10000000000" to KnownTypePrefix(
                id = TlbStructureIdProvider.provideId(),
                TlbInternalShortStdMsgAddrLabel,
                typeArgIds = emptyList(),
                rest = Empty
            )
        )
    )
)

data class TlbMaybeRefLabel(
    val refInfo: TvmParameterInfo.CellInfo,
) : TlbResolvedBuiltinLabel, TlbCompositeLabel(
    "Maybe",
    SwitchPrefix(
        id = TlbStructureIdProvider.provideId(),
        switchSize = 1,
        variants = mapOf(
            "0" to Empty,
            "1" to LoadRef(
                id = TlbStructureIdProvider.provideId(),
                ref = refInfo,
                rest = Empty
            ),
        ),
    )
)

// artificial label
data object TlbInternalStdMsgAddrLabel : TlbAtomicLabel {
    override val arity = 0
}

// artificial label
data object TlbInternalShortStdMsgAddrLabel : TlbAtomicLabel {
    override val arity = 0
}

private val coinPrefixId = TlbStructureIdProvider.provideId()

data object TlbCoinsLabel : TlbResolvedBuiltinLabel, TlbCompositeLabel(
    "Coins",
    KnownTypePrefix(
        id = coinPrefixId,
        TlbIntegerLabelOfConcreteSize(
            concreteSize = 4,
            isSigned = false,
            endian = Endian.BigEndian,
        ),
        typeArgIds = emptyList(),
        rest = KnownTypePrefix(
            id = TlbStructureIdProvider.provideId(),
            TlbIntegerLabelOfSymbolicSize(
                isSigned = false,
                endian = Endian.BigEndian,
                lengthUpperBound = 120,
                arity = 1,
            ) { ctx, args ->
                ctx.mkBvMulExpr(args.single(), ctx.mkSizeExpr(8))
            },
            typeArgIds = listOf(coinPrefixId),
            rest = Empty
        )
    )
)

enum class Endian {
    LittleEndian,
    BigEndian
}
