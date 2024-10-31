package org.ton.examples.types

import org.ton.Endian
import org.ton.TvmCoinsLabel
import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmDataCellStructure.Empty
import org.ton.TvmDataCellStructure.KnownTypePrefix
import org.ton.TvmDataCellStructure.LoadRef
import org.ton.TvmDataCellStructure.SwitchPrefix
import org.ton.TvmDataCellStructure.Unknown
import org.ton.TvmFullMsgAddrLabel
import org.ton.TvmIntegerLabel
import org.ton.TvmMaybeRefLabel
import org.ton.TvmParameterInfo
import org.ton.TvmParameterInfo.DictCellInfo

/**
 * empty$0 = LABEL;
 * full$1 x:^Any = LABEL;
 * */
val maybeStructure = TvmCompositeDataCellLabel(
    "LABEL",
    SwitchPrefix(
        switchSize = 1,
        variants = mapOf(
            "0" to Empty,
            "1" to LoadRef(
                ref = TvmParameterInfo.UnknownCellInfo,
                selfRest = Empty
            ),
        ),
    )
)

/**
 * _ x:int64 = StructInt64;
 * */
val int64Structure = TvmCompositeDataCellLabel(
    "StructInt64",
    KnownTypePrefix(
        TvmIntegerLabel(64, isSigned = true, Endian.BigEndian),
        rest = Empty
    )
)

/**
 * _ x:int64 rest:Any = LABEL;
 * */
val prefixInt64Structure = TvmCompositeDataCellLabel(
    "LABEL",
    KnownTypePrefix(
        TvmIntegerLabel(64, isSigned = true, Endian.BigEndian),
        rest = Unknown
    )
)

/**
 * _ x:^Any = LABEL;
 * */
val someRefStructure = TvmCompositeDataCellLabel(
    "LABEL",
    LoadRef(
        selfRest = Empty,
        ref = TvmParameterInfo.UnknownCellInfo,
    )
)

/**
 * _ x:Coins = LABEL;
 * */
val coinsStructure = TvmCompositeDataCellLabel(
    "LABEL",
    KnownTypePrefix(
        TvmCoinsLabel,
        rest = Empty
    )
)

/**
 * _ x:MsgAddress = WrappedMsg;
 * */
val wrappedMsgStructure = TvmCompositeDataCellLabel(
    "WrappedMsg",
    KnownTypePrefix(
        TvmFullMsgAddrLabel,
        rest = Empty
    )
)

// Notice the structure!
val dict256Structure = TvmCompositeDataCellLabel(
    "LABEL",
    KnownTypePrefix(
        typeOfPrefix = TvmMaybeRefLabel(
            refInfo = DictCellInfo(256)
        ),
        rest = Empty
    )
)

val intSwitchStructure = TvmCompositeDataCellLabel(
    "LABEL",
    SwitchPrefix(
        switchSize = 2,
        mapOf(
            "00" to KnownTypePrefix(
                typeOfPrefix = TvmIntegerLabel(64, isSigned = true, Endian.BigEndian),
                rest = Empty
            ),
            "01" to KnownTypePrefix(
                typeOfPrefix = TvmIntegerLabel(32, isSigned = true, Endian.BigEndian),
                rest = Empty
            )
        )
    )
)

// _ n:uint16 = X;
val structureX = TvmCompositeDataCellLabel(
    name = "X",
    internalStructure = KnownTypePrefix(
        typeOfPrefix = TvmIntegerLabel(16, isSigned = true, Endian.BigEndian),
        rest = Empty
    )
)

// _ a:X b:X c:X = Y;
val structureY = TvmCompositeDataCellLabel(
    name = "Y",
    internalStructure = KnownTypePrefix(
        structureX,
        rest = KnownTypePrefix(
            structureX,
            rest = KnownTypePrefix(
                structureX,
                rest = Empty
            )
        )
    )
)

/**
 * a$1 = Recursive;
 * b$0 x:int8 rest:Recursive = Recursive;
 * */
val recursiveStructure = TvmCompositeDataCellLabel(
    name = "Recursive"
).also { label ->
    val structure = SwitchPrefix(
        switchSize = 1,
        mapOf(
            "1" to Empty,
            "0" to KnownTypePrefix(
                TvmIntegerLabel(bitSize = 8, isSigned = true, endian = Endian.BigEndian),
                rest = KnownTypePrefix(
                    label,
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
val recursiveWithRefStructure = TvmCompositeDataCellLabel(
    name = "RecursiveWithRef"
).also { label ->
    val structure = SwitchPrefix(
        switchSize = 1,
        mapOf(
            "1" to Empty,
            "0" to LoadRef(
                ref = TvmParameterInfo.UnknownCellInfo,
                selfRest = KnownTypePrefix(
                    label,
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
val refAfterRecursiveStructure = TvmCompositeDataCellLabel(
    name = "RefAfterRecursive",
    internalStructure = KnownTypePrefix(
        recursiveWithRefStructure,
        rest = LoadRef(
            ref = TvmParameterInfo.UnknownCellInfo,
            selfRest = Empty,
        )
    )
)

/**
 * a$0 = LongData;
 * b$1 x:int256 y:int256 rest:LongData = LongData;
 * */
val longDataStructure = TvmCompositeDataCellLabel(
    name = "LongData"
).also { label ->
    val structure = SwitchPrefix(
        switchSize = 1,
        mapOf(
            "0" to Empty,
            "1" to KnownTypePrefix(
                TvmIntegerLabel(bitSize = 256, isSigned = true, endian = Endian.BigEndian),
                rest = KnownTypePrefix(
                    TvmIntegerLabel(bitSize = 256, isSigned = true, endian = Endian.BigEndian),
                    rest = KnownTypePrefix(
                        label,
                        rest = Empty
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
val refListStructure = TvmCompositeDataCellLabel(
    name = "RefList"
).also { label ->
    val structure = SwitchPrefix(
        switchSize = 3,
        mapOf(
            "010" to Empty,
            "101" to LoadRef(
                selfRest = Empty,
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
val nonRecursiveChainStructure = TvmCompositeDataCellLabel(
    name = "NonRecursiveChain",
    internalStructure = LoadRef(
        selfRest = Empty,
        ref = TvmParameterInfo.DataCellInfo(
            TvmCompositeDataCellLabel(
                name = "C",
                internalStructure = LoadRef(
                    selfRest = Empty,
                    ref = TvmParameterInfo.DataCellInfo(
                        TvmCompositeDataCellLabel(
                            name = "B",
                            internalStructure = LoadRef(
                                selfRest = Empty,
                                ref = TvmParameterInfo.DataCellInfo(
                                    TvmCompositeDataCellLabel(
                                        name = "A",
                                        internalStructure = SwitchPrefix(
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
val structIntRef = TvmCompositeDataCellLabel(
    name = "StructInRef",
    internalStructure = LoadRef(
        ref = TvmParameterInfo.DataCellInfo(int64Structure),
        selfRest = Empty,
    )
)

/**
 * _ x:^StructInt64 y:Any = StructInRefAndUnknownSuffix;
 * */
val structInRefAndUnknownSuffix = TvmCompositeDataCellLabel(
    name = "StructInRefAndUnknownSuffix",
    internalStructure = LoadRef(
        ref = TvmParameterInfo.DataCellInfo(int64Structure),
        selfRest = Unknown,
    )
)
