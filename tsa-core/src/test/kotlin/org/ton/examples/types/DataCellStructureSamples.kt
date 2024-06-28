package org.ton.examples.types

import org.ton.TvmDataCellStructure
import org.usvm.machine.types.Endian
import org.usvm.test.resolver.TvmCellDataInteger

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
    TvmCellDataInteger(64, isSigned = true, Endian.BigEndian),
    rest = TvmDataCellStructure.Empty
)

val prefixInt64Structure = TvmDataCellStructure.KnownTypePrefix(
    TvmCellDataInteger(64, isSigned = true, Endian.BigEndian),
    rest = TvmDataCellStructure.Unknown
)

val someRefStructure = TvmDataCellStructure.LoadRef(
    selfRest = TvmDataCellStructure.Empty,
    ref = TvmDataCellStructure.Unknown,
)