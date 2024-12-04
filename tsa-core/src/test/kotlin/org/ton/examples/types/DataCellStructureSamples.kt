package org.ton.examples.types

import org.ton.Endian
import org.ton.TlbCoinsLabel
import org.ton.TlbCompositeLabel
import org.ton.TlbFullMsgAddrLabel
import org.ton.TlbIntegerLabelOfConcreteSize
import org.ton.TlbIntegerLabelOfSymbolicSize
import org.ton.TlbMaybeRefLabel
import org.ton.TlbStructure.Empty
import org.ton.TlbStructure.KnownTypePrefix
import org.ton.TlbStructure.LoadRef
import org.ton.TlbStructure.SwitchPrefix
import org.ton.TlbStructure.Unknown
import org.ton.TlbStructureIdProvider
import org.ton.TvmParameterInfo
import org.ton.TvmParameterInfo.DictCellInfo
import org.usvm.mkSizeExpr
import org.usvm.sizeSort

/**
 * empty$0 = LABEL;
 * full$1 x:^Any = LABEL;
 * */
val maybeStructure = TlbCompositeLabel(
    "LABEL",
    SwitchPrefix(
        id = TlbStructureIdProvider.provideId(),
        switchSize = 1,
        variants = mapOf(
            "0" to Empty,
            "1" to LoadRef(
                id = TlbStructureIdProvider.provideId(),
                ref = TvmParameterInfo.UnknownCellInfo,
                rest = Empty
            ),
        ),
    )
)

/**
 * _ x:int64 = StructInt64;
 * */
val int64Structure = TlbCompositeLabel(
    "StructInt64",
    KnownTypePrefix(
        id = TlbStructureIdProvider.provideId(),
        TlbIntegerLabelOfConcreteSize(64, isSigned = true, Endian.BigEndian),
        typeArgIds = emptyList(),
        rest = Empty
    )
)

/**
 * _ x:int64 rest:Any = LABEL;
 * */
val prefixInt64Structure = TlbCompositeLabel(
    "LABEL",
    KnownTypePrefix(
        id = TlbStructureIdProvider.provideId(),
        TlbIntegerLabelOfConcreteSize(64, isSigned = true, Endian.BigEndian),
        typeArgIds = emptyList(),
        rest = Unknown
    )
)

/**
 * _ x:^Any = LABEL;
 * */
val someRefStructure = TlbCompositeLabel(
    "LABEL",
    LoadRef(
        id = TlbStructureIdProvider.provideId(),
        rest = Empty,
        ref = TvmParameterInfo.UnknownCellInfo,
    )
)

/**
 * _ x:Coins = LABEL;
 * */
val coinsStructure = TlbCompositeLabel(
    "LABEL",
    KnownTypePrefix(
        id = TlbStructureIdProvider.provideId(),
        TlbCoinsLabel,
        typeArgIds = emptyList(),
        rest = Empty
    )
)

/**
 * _ x:MsgAddress = WrappedMsg;
 * */
val wrappedMsgStructure = TlbCompositeLabel(
    "WrappedMsg",
    KnownTypePrefix(
        id = TlbStructureIdProvider.provideId(),
        TlbFullMsgAddrLabel,
        typeArgIds = emptyList(),
        rest = Empty
    )
)

// Notice the structure!
val dict256Structure = TlbCompositeLabel(
    "LABEL",
    KnownTypePrefix(
        id = TlbStructureIdProvider.provideId(),
        typeLabel = TlbMaybeRefLabel(
            refInfo = DictCellInfo(256)
        ),
        typeArgIds = emptyList(),
        rest = Empty
    )
)

val intSwitchStructure = TlbCompositeLabel(
    "LABEL",
    SwitchPrefix(
        id = TlbStructureIdProvider.provideId(),
        switchSize = 2,
        mapOf(
            "00" to KnownTypePrefix(
                id = TlbStructureIdProvider.provideId(),
                typeLabel = TlbIntegerLabelOfConcreteSize(64, isSigned = true, Endian.BigEndian),
                typeArgIds = emptyList(),
                rest = Empty
            ),
            "01" to KnownTypePrefix(
                id = TlbStructureIdProvider.provideId(),
                typeLabel = TlbIntegerLabelOfConcreteSize(32, isSigned = true, Endian.BigEndian),
                typeArgIds = emptyList(),
                rest = Empty
            )
        )
    )
)

// _ n:uint16 = X;
val structureX = TlbCompositeLabel(
    name = "X",
    internalStructure = KnownTypePrefix(
        id = TlbStructureIdProvider.provideId(),
        typeLabel = TlbIntegerLabelOfConcreteSize(16, isSigned = true, Endian.BigEndian),
        typeArgIds = emptyList(),
        rest = Empty
    )
)

// _ a:X b:X c:X = Y;
val structureY = TlbCompositeLabel(
    name = "Y",
    internalStructure = KnownTypePrefix(
        id = TlbStructureIdProvider.provideId(),
        structureX,
        typeArgIds = emptyList(),
        rest = KnownTypePrefix(
            id = TlbStructureIdProvider.provideId(),
            structureX,
            typeArgIds = emptyList(),
            rest = KnownTypePrefix(
                id = TlbStructureIdProvider.provideId(),
                structureX,
                typeArgIds = emptyList(),
                rest = Empty
            )
        )
    )
)

/**
 * a$1 = Recursive;
 * b$0 x:int8 rest:Recursive = Recursive;
 * */
val recursiveStructure = TlbCompositeLabel(
    name = "Recursive"
).also { label ->
    val structure = SwitchPrefix(
        id = TlbStructureIdProvider.provideId(),
        switchSize = 1,
        mapOf(
            "1" to Empty,
            "0" to KnownTypePrefix(
                id = TlbStructureIdProvider.provideId(),
                TlbIntegerLabelOfConcreteSize(concreteSize = 8, isSigned = true, endian = Endian.BigEndian),
                typeArgIds = emptyList(),
                rest = KnownTypePrefix(
                    id = TlbStructureIdProvider.provideId(),
                    label,
                    typeArgIds = emptyList(),
                    rest = Empty
                )
            )
        )
    )
    label.internalStructure = structure
}

/**
 * a$1 = RecursiveWithRef;
 * b$0 x:^Cell rest:RecursiveWithRef = RecursiveWithRef;
 * */
val recursiveWithRefStructure = TlbCompositeLabel(
    name = "RecursiveWithRef"
).also { label ->
    val structure = SwitchPrefix(
        id = TlbStructureIdProvider.provideId(),
        switchSize = 1,
        mapOf(
            "1" to Empty,
            "0" to LoadRef(
                id = TlbStructureIdProvider.provideId(),
                ref = TvmParameterInfo.UnknownCellInfo,
                rest = KnownTypePrefix(
                    id = TlbStructureIdProvider.provideId(),
                    label,
                    typeArgIds = emptyList(),
                    rest = Empty
                )
            )
        )
    )
    label.internalStructure = structure
}

/**
 * _ x:RecursiveWithRef ref:^Cell = RefAfterRecursive;
 * */
val refAfterRecursiveStructure = TlbCompositeLabel(
    name = "RefAfterRecursive",
    internalStructure = KnownTypePrefix(
        id = TlbStructureIdProvider.provideId(),
        recursiveWithRefStructure,
        typeArgIds = emptyList(),
        rest = LoadRef(
            id = TlbStructureIdProvider.provideId(),
            ref = TvmParameterInfo.UnknownCellInfo,
            rest = Empty,
        )
    )
)

/**
 * a$0 = LongData;
 * b$1 x:int256 y:int256 rest:LongData = LongData;
 * */
val longDataStructure = TlbCompositeLabel(
    name = "LongData"
).also { label ->
    val structure = SwitchPrefix(
        id = TlbStructureIdProvider.provideId(),
        switchSize = 1,
        mapOf(
            "0" to Empty,
            "1" to KnownTypePrefix(
                id = TlbStructureIdProvider.provideId(),
                TlbIntegerLabelOfConcreteSize(256, isSigned = true, endian = Endian.BigEndian),
                typeArgIds = emptyList(),
                rest = KnownTypePrefix(
                    id = TlbStructureIdProvider.provideId(),
                    TlbIntegerLabelOfConcreteSize(256, isSigned = true, endian = Endian.BigEndian),
                    typeArgIds = emptyList(),
                    rest = KnownTypePrefix(
                        id = TlbStructureIdProvider.provideId(),
                        label,
                        typeArgIds = emptyList(),
                        rest = Empty,
                    )
                )
            )
        )
    )
    label.internalStructure = structure
}

/**
 * empty$010 = RefList;
 * some$101 next:^RefList = RefList;
 * */
val refListStructure = TlbCompositeLabel(
    name = "RefList"
).also { label ->
    val structure = SwitchPrefix(
        id = TlbStructureIdProvider.provideId(),
        switchSize = 3,
        mapOf(
            "010" to Empty,
            "101" to LoadRef(
                id = TlbStructureIdProvider.provideId(),
                rest = Empty,
                ref = TvmParameterInfo.DataCellInfo(label),
            )
        )
    )
    label.internalStructure = structure
}

/**
 * some$11011 = A;
 * _ rest:^A = B;
 * _ rest:^B = C;
 * _ rest:^C = NonRecursiveChain;
 * */
val nonRecursiveChainStructure = TlbCompositeLabel(
    name = "NonRecursiveChain",
    internalStructure = LoadRef(
        id = TlbStructureIdProvider.provideId(),
        rest = Empty,
        ref = TvmParameterInfo.DataCellInfo(
            TlbCompositeLabel(
                name = "C",
                internalStructure = LoadRef(
                    id = TlbStructureIdProvider.provideId(),
                    rest = Empty,
                    ref = TvmParameterInfo.DataCellInfo(
                        TlbCompositeLabel(
                            name = "B",
                            internalStructure = LoadRef(
                                id = TlbStructureIdProvider.provideId(),
                                rest = Empty,
                                ref = TvmParameterInfo.DataCellInfo(
                                    TlbCompositeLabel(
                                        name = "A",
                                        internalStructure = SwitchPrefix(
                                            id = TlbStructureIdProvider.provideId(),
                                            switchSize = 5,
                                            mapOf("11011" to Empty)
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    )
)

/**
 * _ x:^StructInt64 = StructInRef;
 * */
val structIntRef = TlbCompositeLabel(
    name = "StructInRef",
    internalStructure = LoadRef(
        id = TlbStructureIdProvider.provideId(),
        ref = TvmParameterInfo.DataCellInfo(int64Structure),
        rest = Empty,
    )
)

/**
 * _ x:^StructInt64 y:Any = StructInRefAndUnknownSuffix;
 * */
val structInRefAndUnknownSuffix = TlbCompositeLabel(
    name = "StructInRefAndUnknownSuffix",
    internalStructure = LoadRef(
        id = TlbStructureIdProvider.provideId(),
        ref = TvmParameterInfo.DataCellInfo(int64Structure),
        rest = Unknown,
    )
)

/**
 * _ x:(int (n*10)) = X n;
 * */
val symbolicIntLabel = TlbIntegerLabelOfSymbolicSize(
    isSigned = true,
    endian = Endian.BigEndian,
    arity = 1,
) { ctx, args ->
    val n = args.single()
    check(n.sort.sizeBits == ctx.sizeSort.sizeBits)
    ctx.mkBvMulExpr(n, ctx.mkSizeExpr(10))
}

private val rootIdForCustomVarUInteger = TlbStructureIdProvider.provideId()

/**
 * _ len:uint16 x:(int (len * 10)) = CustomVarInteger;
 * */
val customVarInteger = TlbCompositeLabel(
    name = "CustomVarInteger",
    internalStructure = KnownTypePrefix(
        id = rootIdForCustomVarUInteger,
        TlbIntegerLabelOfConcreteSize(concreteSize = 16, isSigned = false, endian = Endian.BigEndian),
        typeArgIds = emptyList(),
        rest = KnownTypePrefix(
            id = TlbStructureIdProvider.provideId(),
            symbolicIntLabel,
            typeArgIds = listOf(rootIdForCustomVarUInteger),
            rest = Empty,
        )
    )
)

/**
 * _ len:uint16 x:(int (len * 10)) y:int4 = CustomVarIntegerWithSuffix;
 * */
val customVarIntegerWithSuffix = TlbCompositeLabel(
    name = "CustomVarIntegerWithSuffix",
    internalStructure = KnownTypePrefix(
        id = rootIdForCustomVarUInteger,
        TlbIntegerLabelOfConcreteSize(concreteSize = 16, isSigned = false, endian = Endian.BigEndian),
        typeArgIds = emptyList(),
        rest = KnownTypePrefix(
            id = TlbStructureIdProvider.provideId(),
            symbolicIntLabel,
            typeArgIds = listOf(rootIdForCustomVarUInteger),
            rest = KnownTypePrefix(
                id = TlbStructureIdProvider.provideId(),
                TlbIntegerLabelOfConcreteSize(concreteSize = 4, isSigned = true, endian = Endian.BigEndian),
                typeArgIds = emptyList(),
                rest = Empty,
            ),
        )
    )
)

/**
 * _ x:CustomVarInteger y:CustomVarInteger = DoubleCustomVarInteger;
 * */
val doubleCustomVarInteger = TlbCompositeLabel(
    name = "DoubleCustomVarInteger",
    internalStructure = KnownTypePrefix(
        id = TlbStructureIdProvider.provideId(),
        customVarInteger,
        typeArgIds = emptyList(),
        rest = KnownTypePrefix(
            id = TlbStructureIdProvider.provideId(),
            customVarInteger,
            typeArgIds = emptyList(),
            rest = Empty,
        )
    )
)

/**
 * _ x:int10 y:Coins = IntAndCoins;
 * */
val intAndCoins = TlbCompositeLabel(
    name = "IntAndCoins",
    internalStructure = KnownTypePrefix(
        id = TlbStructureIdProvider.provideId(),
        TlbIntegerLabelOfConcreteSize(10, isSigned = true, endian = Endian.BigEndian),
        typeArgIds = emptyList(),
        rest = KnownTypePrefix(
            id = TlbStructureIdProvider.provideId(),
            TlbCoinsLabel,
            typeArgIds = emptyList(),
            rest = Empty,
        )
    )
)

/**
 * _ x1:int5 x2:int5 y:Coins = DoubleIntAndCoins;
 * */
val doubleIntAndCoins = TlbCompositeLabel(
    name = "DoubleIntAndCoins",
    internalStructure = KnownTypePrefix(
        id = TlbStructureIdProvider.provideId(),
        TlbIntegerLabelOfConcreteSize(5, isSigned = true, endian = Endian.BigEndian),
        typeArgIds = emptyList(),
        rest = KnownTypePrefix(
            id = TlbStructureIdProvider.provideId(),
            TlbIntegerLabelOfConcreteSize(5, isSigned = true, endian = Endian.BigEndian),
            typeArgIds = emptyList(),
            rest = KnownTypePrefix(
                id = TlbStructureIdProvider.provideId(),
                TlbCoinsLabel,
                typeArgIds = emptyList(),
                rest = Empty,
            )
        )
    )
)

/**
 * _ x:int10 y:int4 = IntAndInt;
 * */
val intAndInt = TlbCompositeLabel(
    name = "IntAndInt",
    internalStructure = KnownTypePrefix(
        id = TlbStructureIdProvider.provideId(),
        TlbIntegerLabelOfConcreteSize(10, isSigned = true, endian = Endian.BigEndian),
        typeArgIds = emptyList(),
        rest = KnownTypePrefix(
            id = TlbStructureIdProvider.provideId(),
            TlbIntegerLabelOfConcreteSize(4, isSigned = true, endian = Endian.BigEndian),
            typeArgIds = emptyList(),
            rest = Empty,
        )
    )
)
