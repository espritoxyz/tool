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

val int64Structure = KnownTypePrefix(
    TvmIntegerLabel(64, isSigned = true, Endian.BigEndian),
    rest = Empty
)

val prefixInt64Structure = KnownTypePrefix(
    TvmIntegerLabel(64, isSigned = true, Endian.BigEndian),
    rest = Unknown
)

val someRefStructure = LoadRef(
    selfRest = Empty,
    ref = DataCellInfo(Unknown),
)

val coinsStructure = KnownTypePrefix(
    TvmCoinsLabel,
    rest = Empty
)

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

// n:uint16 = X;
val structureX = TvmCompositeDataCellLabel(
    name = "X",
    internalStructure = KnownTypePrefix(
        typeOfPrefix = TvmIntegerLabel(16, isSigned = true, Endian.BigEndian),
        rest = Empty
    )
)

// a:X b:X c:X = Y;
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