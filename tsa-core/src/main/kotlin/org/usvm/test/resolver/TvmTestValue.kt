package org.usvm.test.resolver

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigInteger

@Serializable
sealed interface TvmTestValue

@Serializable
data class TvmTestIntegerValue(
    val value: @Contextual BigInteger
): TvmTestValue

@Serializable
data class TvmTestCellValue(
    val data: String = "",
    val refs: List<TvmTestCellValue> = listOf(),
): TvmTestValue

@Serializable
data class TvmTestBuilderValue(
    val data: String,
    val refs: List<TvmTestCellValue>,
): TvmTestValue

@Serializable
data class TvmTestSliceValue(
    val cell: TvmTestCellValue,
    val dataPos: Int,
    val refPos: Int,
): TvmTestValue

@Serializable
data object TvmTestNullValue: TvmTestValue

@Serializable
data class TvmTestTupleValue(
    val elements: List<TvmTestValue>
) : TvmTestValue
