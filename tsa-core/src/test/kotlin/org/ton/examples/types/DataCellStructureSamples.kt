package org.ton.examples.types

import org.ton.Endian
import org.ton.TvmCoinsLabel
import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmDataCellStructure.*
import org.ton.TvmIntegerLabel
import org.ton.TvmMaybeRefLabel
import org.ton.TvmMsgAddrLabel
import org.ton.TvmParameterInfo.DataCellInfo
import org.ton.TvmParameterInfo.DictCellInfo

/**
 * empty$0 = LABEL;
 * full$1 x:^Any = LABEL;
 * */
val maybeStructure = SwitchPrefix(
    switchSize = 1,
    variants = mapOf(
        "0" to Empty,
        "1" to LoadRef(
            ref = DataCellInfo(Unknown),
            selfRest = Empty
        ),
    ),
)

/**
 * _ x:int64 = LABEL;
 * */
val int64Structure = KnownTypePrefix(
    TvmIntegerLabel(64, isSigned = true, Endian.BigEndian),
    rest = Empty
)

val prefixInt64Structure = KnownTypePrefix(
    TvmIntegerLabel(64, isSigned = true, Endian.BigEndian),
    rest = Unknown
)

/**
 * _ x:^Any = LABEL;
 * */
val someRefStructure = LoadRef(
    selfRest = Empty,
    ref = DataCellInfo(Unknown),
)

/**
 * _ x:Coins = LABEL;
 * */
val coinsStructure = KnownTypePrefix(
    TvmCoinsLabel,
    rest = Empty
)

/**
 * _ x:MsgAddress = LABEL;
 * */
val msgStructure = KnownTypePrefix(
    TvmMsgAddrLabel,
    rest = Empty
)

// Notice the structure!
val dict256Structure = KnownTypePrefix(
    typeOfPrefix = TvmMaybeRefLabel(
        refInfo = DictCellInfo(256)
    ),
    rest = Empty
)

val intSwitchStructure = SwitchPrefix(
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