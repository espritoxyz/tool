package org.usvm.test

import java.math.BigInteger

sealed interface TvmTestValue

data class TvmTestIntegerValue(val value: BigInteger): TvmTestValue

data class TvmTestCellValue(
    val data: String = "",
    val refs: List<TvmTestCellValue> = listOf(),
): TvmTestValue

data class TvmTestBuilderValue(
    val data: String,
    val refs: List<TvmTestCellValue>,
): TvmTestValue

data class TvmTestSliceValue(
    val cell: TvmTestCellValue,
    val dataPos: Int,
    val refPos: Int,
): TvmTestValue

data object TvmTestNullValue: TvmTestValue

data class TvmTestTupleValue(
    val elements: List<TvmTestValue>
) : TvmTestValue
