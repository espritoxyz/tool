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

fun initializeContract(
    blockchain: TsExpression<TsBlockchain>,
    address: TsExpression<TsAddress>,
    code: TsExpression<TsCell>,
    data: TsExpression<TsCell>,
    balance: TsExpression<TsBigint> = toNano("100")
) = TsMethodCall(
    caller = null,
    executableName = "initializeContract",
    arguments = listOf(blockchain, address, code, data, balance),
    async = true,
    type = TsVoid
)

fun cellFromHex(hex: String): TsExpression<TsCell> =
    TsMethodCall(
        caller = null,
        executableName = "cellFromHex",
        arguments = listOf(hex.toTsValue()),
        async = true,
        type = TsCell
    )

fun beginCell(): TsExpression<TsBuilder> =
    TsMethodCall(
        caller = null,
        executableName = "beginCell",
        arguments = emptyList(),
        async = false,
        type = TsBuilder
    )

fun TsExpression<TsBuilder>.storeUint(
    value: TsExpression<TsInt>,
    bits: TsExpression<TsInt>
): TsExpression<TsBuilder> = TsMethodCall(
    caller = this,
    executableName = "storeUint",
    arguments = listOf(value, bits),
    async = false,
    type = TsBuilder
)

fun TsExpression<TsBuilder>.storeInt(
    value: TsExpression<TsInt>,
    bits: TsExpression<TsInt>
): TsExpression<TsBuilder> = TsMethodCall(
    caller = this,
    executableName = "storeInt",
    arguments = listOf(value, bits),
    async = false,
    type = TsBuilder
)

fun TsExpression<TsBuilder>.storeAddress(
    address: TsExpression<TsAddress>
): TsExpression<TsBuilder> = TsMethodCall(
    caller = this,
    executableName = "storeAddress",
    arguments = listOf(address),
    async = false,
    type = TsBuilder
)

fun TsExpression<TsBuilder>.storeCoins(
    coins: TsExpression<TsInt>
): TsExpression<TsBuilder> = TsMethodCall(
    caller = this,
    executableName = "storeCoins",
    arguments = listOf(coins),
    async = false,
    type = TsBuilder
)

fun TsExpression<TsBuilder>.storeRef(
    cell: TsExpression<TsCell>
): TsExpression<TsBuilder> = TsMethodCall(
    caller = this,
    executableName = "storeRef",
    arguments = listOf(cell),
    async = false,
    type = TsBuilder
)

fun TsExpression<TsBuilder>.endCell(): TsExpression<TsCell> = TsMethodCall(
    caller = this,
    executableName = "endCell",
    arguments = listOf(),
    async = false,
    type = TsCell
)

operator fun <T : TsNum> TsExpression<T>.plus(value: TsExpression<T>): TsExpression<T> =
    TsNumAdd(this, value)

operator fun <T : TsNum> TsExpression<T>.minus(value: TsExpression<T>): TsExpression<T> =
    TsNumSub(this, value)

operator fun <T : TsNum> TsExpression<T>.div(value: TsExpression<T>): TsExpression<T> =
    TsNumDiv(this, value)

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
