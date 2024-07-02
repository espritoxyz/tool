package org.ton.examples.types

import org.ton.Endian
import org.ton.TvmCoinsLabel
import org.ton.TvmDataCellStructure
import org.ton.TvmIntegerLabel
import org.ton.TvmInternalStdMsgAddrLabel
import org.ton.TvmMsgAddrLabel
import org.ton.TvmParameterInfo

val maybeStructure = TvmDataCellStructure.SwitchPrefix(
    switchSize = 1,
    variants = mapOf(
        "0" to TvmDataCellStructure.Empty,
        "1" to TvmDataCellStructure.LoadRef(
            ref = TvmParameterInfo.DataCellInfo(TvmDataCellStructure.Unknown),
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
    ref = TvmParameterInfo.DataCellInfo(TvmDataCellStructure.Unknown),
)

val coinsStructure = TvmDataCellStructure.KnownTypePrefix(
    TvmCoinsLabel,
    rest = TvmDataCellStructure.Empty
)

// Notice the structure!
val msgStructure = TvmDataCellStructure.KnownTypePrefix(
    TvmMsgAddrLabel,
    rest = TvmDataCellStructure.SwitchPrefix(
        switchSize = 3,
        mapOf(
            "100" to TvmDataCellStructure.KnownTypePrefix(
                TvmInternalStdMsgAddrLabel,
                rest = TvmDataCellStructure.Empty
            )
        )
    )
)
