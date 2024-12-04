package org.usvm.test.resolver

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.ton.Endian
import org.usvm.machine.TvmContext.Companion.stdMsgAddrSize
import java.math.BigInteger

@Serializable
sealed interface TvmTestValue

@JvmInline
@Serializable
value class TvmTestIntegerValue(
    val value: @Contextual BigInteger
): TvmTestValue

sealed interface TvmTestReferenceValue

@Serializable
sealed interface TvmTestCellValue: TvmTestValue, TvmTestReferenceValue

@Serializable
data class TvmTestDictCellValue(
    val keyLength: Int,
    val entries: Map<TvmTestIntegerValue, TvmTestSliceValue>,
): TvmTestCellValue

@Serializable
data class TvmTestDataCellValue(
    val data: String = "",
    val refs: List<TvmTestCellValue> = listOf(),
    val knownTypes: List<TvmCellDataTypeLoad> = listOf()
): TvmTestCellValue {
    fun dataCellDepth(): Int =
        if (refs.isEmpty()) {
            0
        } else {
            val childrenDepths = refs.mapNotNull {
                // null for dict cells
                (it as? TvmTestDataCellValue)?.dataCellDepth()
            }
            1 + (childrenDepths.maxOrNull() ?: 0)
        }
}

@Serializable
data class TvmTestBuilderValue(
    val data: String,
    val refs: List<TvmTestCellValue>,
): TvmTestValue, TvmTestReferenceValue

@Serializable
data class TvmTestSliceValue(
    val cell: TvmTestDataCellValue = TvmTestDataCellValue(),
    val dataPos: Int = 0,
    val refPos: Int = 0,
): TvmTestValue, TvmTestReferenceValue

@Serializable
data object TvmTestNullValue: TvmTestValue

@Serializable
data class TvmTestTupleValue(
    val elements: List<TvmTestValue>
) : TvmTestValue

@Serializable
sealed interface TvmTestCellDataTypeRead {
    val bitSize: Int
}

@Serializable
data class TvmTestCellDataIntegerRead(override val bitSize: Int, val isSigned: Boolean, val endian: Endian): TvmTestCellDataTypeRead

@Serializable
data object TvmTestCellDataMaybeConstructorBitRead: TvmTestCellDataTypeRead {
    override val bitSize: Int = 1
}

// TODO: only stdAddr is supported now
@Serializable
data object TvmTestCellDataMsgAddrRead: TvmTestCellDataTypeRead {
    override val bitSize: Int = stdMsgAddrSize
}

@Serializable
data class TvmTestCellDataBitArrayRead(override val bitSize: Int): TvmTestCellDataTypeRead

@Serializable
data class TvmTestCellDataCoinsRead(val coinPrefix: Int): TvmTestCellDataTypeRead {
    override val bitSize: Int = 4 + coinPrefix * 8

    init {
        require(coinPrefix in 0..15)
    }
}

@Serializable
data class TvmCellDataTypeLoad(
    val type: TvmTestCellDataTypeRead,
    val offset: Int
)