package org.usvm.test.resolver

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.ton.Endian
import org.usvm.machine.types.stdMsgAddrSize
import java.math.BigInteger

@Serializable
sealed interface TvmTestValue

@Serializable
data class TvmTestIntegerValue(
    val value: @Contextual BigInteger
): TvmTestValue

@Serializable
sealed interface TvmTestCellValue: TvmTestValue

@Serializable
data object TvmTestDictCellValue: TvmTestCellValue  // TODO: contents

@Serializable
data class TvmTestDataCellValue(
    val data: String = "",
    val refs: List<TvmTestCellValue> = listOf(),
    val knownTypes: List<TvmCellDataTypeLoad> = listOf()
): TvmTestCellValue

@Serializable
data class TvmTestBuilderValue(
    val data: String,
    val refs: List<TvmTestCellValue>,
): TvmTestValue

@Serializable
data class TvmTestSliceValue(
    val cell: TvmTestDataCellValue,
    val dataPos: Int,
    val refPos: Int,
): TvmTestValue

@Serializable
data object TvmTestNullValue: TvmTestValue

@Serializable
data class TvmTestTupleValue(
    val elements: List<TvmTestValue>
) : TvmTestValue

@Serializable
sealed interface TvmCellDataType {
    val bitSize: Int
}

@Serializable
data class TvmCellDataInteger(override val bitSize: Int, val isSigned: Boolean, val endian: Endian): TvmCellDataType

@Serializable
data object TvmCellDataMaybeConstructorBit: TvmCellDataType {
    override val bitSize: Int = 1
}

// TODO: only stdAddr is supported now
@Serializable
data object TvmCellDataMsgAddr: TvmCellDataType {
    override val bitSize: Int = stdMsgAddrSize
}

@Serializable
data class TvmCellDataBitArray(override val bitSize: Int): TvmCellDataType

@Serializable
data class TvmCellDataCoins(val coinPrefix: Int): TvmCellDataType {
    override val bitSize: Int = 4 + coinPrefix * 8

    init {
        require(coinPrefix in 0..15)
    }
}

@Serializable
data class TvmCellDataTypeLoad(
    val type: TvmCellDataType,
    val offset: Int
)