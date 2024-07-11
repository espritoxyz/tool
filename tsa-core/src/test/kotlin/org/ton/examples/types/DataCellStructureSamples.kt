package org.ton.examples.types

import org.ton.Endian
import org.ton.TvmCoinsLabel
import org.ton.TvmDataCellStructure.*
import org.ton.TvmIntegerLabel
import org.ton.TvmInternalStdMsgAddrLabel
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

// Notice the structure!
val msgStructure = KnownTypePrefix(
    TvmMsgAddrLabel,
    rest = SwitchPrefix(
        switchSize = 3,
        mapOf(
            "100" to KnownTypePrefix(
                TvmInternalStdMsgAddrLabel,
                rest = Empty
            )
        )
    )
)

// Notice the structure!
val dict256Structure =
    KnownTypePrefix(
        typeOfPrefix = TvmMaybeRefLabel,
        rest = SwitchPrefix(
            switchSize = 1,
            mapOf(
                "0" to Empty,
                "1" to LoadRef(
                    ref = DictCellInfo(256),
                    selfRest = Empty
                )
            )
        )
    )
