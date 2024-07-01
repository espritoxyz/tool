package org.ton.examples.types

import org.ton.Endian
import org.ton.TvmCoinsLabel
import org.ton.TvmDataCellStructure
import org.ton.TvmIntegerLabel
import org.ton.TvmMsgAddrLabel

val maybeStructure = TvmDataCellStructure.SwitchPrefix(
    switchSize = 1,
    variants = mapOf(
        "0" to TvmDataCellStructure.Empty,
        "1" to TvmDataCellStructure.LoadRef(
            ref = TvmDataCellStructure.Unknown,
            selfRest = TvmDataCellStructure.Empty
        ),
    ),
)

val int64Structure = TvmDataCellStructure.KnownTypePrefix(
    TvmIntegerLabel(64, isSigned = true, Endian.BigEndian),
    rest = TvmDataCellStructure.Empty
)

val prefixInt64Structure = TvmDataCellStructure.KnownTypePrefix(
    TvmIntegerLabel(64, isSigned = true, Endian.BigEndian),
    rest = TvmDataCellStructure.Unknown
)

val someRefStructure = TvmDataCellStructure.LoadRef(
    selfRest = TvmDataCellStructure.Empty,
    ref = TvmDataCellStructure.Unknown,
)

val coinsStructure = TvmDataCellStructure.KnownTypePrefix(
    TvmCoinsLabel,
    rest = TvmDataCellStructure.Empty
)

val msgStructure = TvmDataCellStructure.KnownTypePrefix(
    TvmMsgAddrLabel,
    rest = TvmDataCellStructure.Empty
)
