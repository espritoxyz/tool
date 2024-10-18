package org.usvm.machine.types

import kotlinx.serialization.Serializable
import org.ton.TvmBuiltinDataCellLabel

@Serializable
sealed interface TvmStructuralExit<out DataCellType> {
    val ruleId: String
}

data class TvmUnexpectedDataReading<DataCellType>(
    val readingType: DataCellType,
) : TvmStructuralExit<DataCellType> {
    override val ruleId: String
        get() = "unexpected-data-reading"

    override fun toString() =
        "Unexpected reading of $readingType: slice should have no data left."
}

data class TvmReadingOutOfSwitchBounds<DataCellType>(
    val readingType: DataCellType,
) : TvmStructuralExit<DataCellType> {
    override val ruleId: String
        get() = "out-of-switch-bounds"

    override fun toString() =
        "Reading of $readingType is out of switch bounds"
}

object TvmUnexpectedRefReading : TvmStructuralExit<Nothing> {
    override val ruleId: String
        get() = "unexpected-ref-reading"

    override fun toString() =
        "Unexpected reading of a reference: slice should have no references left."
}

object TvmUnexpectedEndOfReading : TvmStructuralExit<Nothing> {
    override val ruleId: String
        get() = "unexpected-end-of-cell"

    override fun toString() =
        "Unexpected end of reading: slice is not supposed to be empty"
}

data class TvmReadingOfUnexpectedType<DataCellType>(
    val labelType: TvmBuiltinDataCellLabel,
    val actualType: DataCellType,
) : TvmStructuralExit<DataCellType> {
    override val ruleId: String
        get() = "unexpected-cell-type"

    override fun toString() =
        "Reading of unexpected type: expected reading of $labelType, but read $actualType"
}