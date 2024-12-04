package org.ton.test.gen.dsl.models

import java.math.BigInteger
import org.usvm.test.resolver.TvmTestBuilderValue
import org.usvm.test.resolver.TvmTestCellValue
import org.usvm.test.resolver.TvmTestDataCellValue
import org.usvm.test.resolver.TvmTestDictCellValue
import org.usvm.test.resolver.TvmTestIntegerValue
import org.usvm.test.resolver.TvmTestSliceValue

fun parseAddress(address: String): TsExpression<TsAddress> =
    TsMethodCall(
        caller = null,
        executableName = "Address.parse",
        arguments = listOf(TsStringValue(address)),
        async = false,
        type = TsAddress
    )

fun randomAddress(workchain: TsExpression<TsInt>) =
    TsMethodCall(
        caller = null,
        executableName = "randomAddress",
        arguments = listOf(workchain),
        async = false,
        type = TsAddress
    )

fun toNano(value: String): TsExpression<TsBigint> =
    TsMethodCall(
        caller = null,
        executableName = "toNano",
        arguments = listOf(TsStringValue(value)),
        async = false,
        type = TsBigint
    )

fun blockchainCreate(): TsExpression<TsBlockchain> =
    TsMethodCall(
        caller = null,
        executableName = "Blockchain.create",
        arguments = emptyList(),
        async = true,
        TsBlockchain
    )

fun compileContract(target: String): TsExpression<TsCell> =
    TsMethodCall(
        caller = null,
        executableName = "compileContract",
        arguments = listOf(TsStringValue(target)),
        async = true,
        type = TsCell
    )

fun cellFromHex(hex: String): TsExpression<TsCell> =
    TsMethodCall(
        caller = null,
        executableName = "cellFromHex",
        arguments = listOf(hex.toTsValue()),
        async = true,
        type = TsCell
    )

operator fun <T : TsNum> TsExpression<T>.plus(value: TsExpression<T>): TsExpression<T> =
    TsNumAdd(this, value)

operator fun <T : TsNum> TsExpression<T>.minus(value: TsExpression<T>): TsExpression<T> =
    TsNumSub(this, value)

fun <T : TsWrapper> TsExpression<TsBlockchain>.openContract(wrapper: TsExpression<T>) =
    TsMethodCall(
        caller = this,
        executableName = "openContract",
        arguments = listOf(wrapper),
        async = false,
        type = TsSandboxContract(wrapper.type)
    )

fun Boolean.toTsValue(): TsBooleanValue = TsBooleanValue(this)
fun Int.toTsValue(): TsIntValue = TsIntValue(this)
fun BigInteger.toTsValue(): TsBigintValue = TsBigintValue(TvmTestIntegerValue(this))
fun TvmTestIntegerValue.toTsValue(): TsBigintValue = TsBigintValue(this)
fun String.toTsValue(): TsStringValue = TsStringValue(this)
fun TvmTestDataCellValue.toTsValue(): TsDataCellValue = TsDataCellValue(this)
fun TvmTestDictCellValue.toTsValue(): TsDictValue = TsDictValue(this)
fun TvmTestSliceValue.toTsValue(): TsSliceValue = TsSliceValue(this)
fun TvmTestBuilderValue.toTsValue(): TsBuilderValue = TsBuilderValue(this)

fun TvmTestCellValue.toTsValue(): TsExpression<TsCell> = when (this) {
    is TvmTestDataCellValue -> toTsValue()
    is TvmTestDictCellValue -> toTsValue()
}
