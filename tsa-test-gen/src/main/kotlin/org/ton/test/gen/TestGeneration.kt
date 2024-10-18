package org.ton.test.gen

import java.math.BigInteger
import java.nio.file.Files.createDirectories
import java.nio.file.Path
import java.util.Locale
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.Companion.ADDRESS_TAG_LENGTH
import org.usvm.machine.TvmContext.Companion.STD_ADDRESS_TAG
import org.usvm.test.resolver.TvmContractSymbolicTestResult
import org.usvm.test.resolver.TvmMethodFailure
import org.usvm.test.resolver.TvmSymbolicTest
import org.usvm.test.resolver.TvmTerminalMethodSymbolicResult
import org.usvm.test.resolver.TvmTestDataCellValue
import org.usvm.test.resolver.TvmTestDictCellValue
import org.usvm.test.resolver.TvmTestIntegerValue
import org.usvm.test.resolver.TvmTestNullValue
import org.usvm.test.resolver.TvmTestSliceValue
import org.usvm.test.resolver.TvmTestValue
import kotlin.io.path.nameWithoutExtension

fun generateTests(
    analysisResult: TvmContractSymbolicTestResult,
    projectPath: Path,
    sourceRelativePath: Path,
) {
    val entryTests = analysisResult.testSuites
        .single { it.methodId == TvmContext.RECEIVE_INTERNAL_ID }
        .filter { it.result is TvmMethodFailure }

    val name = extractContractName(sourceRelativePath)
    val wrapper = renderWrapperFile(name)
    val tests = renderTestFile(name, sourceRelativePath.toString(), entryTests)

    val wrapperFolder = projectPath.resolve(wrappersDirName)
    val testsFolder = projectPath.resolve(testsDirName)
    val wrapperFile = wrapperFolder.resolve("$name.ts").toFile()
    val testsFile = testsFolder.resolve("$name.spec.ts").toFile()

    createDirectories(wrapperFolder)
    createDirectories(testsFolder)

    wrapperFile.writeText(wrapper)
    testsFile.writeText(tests)
}

private fun renderArgValue(arg: TvmTestValue): String =
    when (arg) {
        is TvmTestNullValue -> "null()"
        is TvmTestIntegerValue -> arg.value.toString()
        is TvmTestSliceValue -> "${renderArgValue(truncateSliceCell(arg))}.beginParse()"
        is TvmTestDictCellValue -> {
            val dictInit = "Dictionary.empty(Dictionary.Keys.BigInt(${arg.keyLength}), sliceValue)"
            val dictStores = arg.entries.map { entry ->
                ".set(${renderArgValue(entry.key)}n, ${renderArgValue(entry.value)})"
            }

            "${dictInit}${dictStores.joinToString(separator = "")}"
        }
        is TvmTestDataCellValue -> {
            // TODO use loads

            val storeBits = { bitsToStore: String ->
                val bitsLength = bitsToStore.length
                val formattedBits = bitsToStore.takeIf { it.any { char -> char != '0' } } ?: "0"
                val bitsValue = "BigInt(\"0b$formattedBits\")"

                ".storeUint($bitsValue, $bitsLength)".takeIf { bitsLength > 0 } ?: ""
            }

            val storesBuilder = StringBuilder()
            var bitsLeft = arg.data

            arg.refs
                .map { it to renderArgValue(it) }
                .forEach { (ref, refValue) ->
                    when (ref) {
                        is TvmTestDataCellValue -> storesBuilder.append(".storeRef($refValue)")
                        is TvmTestDictCellValue -> {
                            val justConstructorIdx = bitsLeft.indexOfFirst { it == '1' }

                            check(justConstructorIdx != -1) {
                                "Cell contains more dict refs than non-zero bits"
                            }

                            val bitsToStore = bitsLeft.take(justConstructorIdx)
                            bitsLeft = bitsLeft.drop(justConstructorIdx + 1)

                            storesBuilder.append(storeBits(bitsToStore))
                            storesBuilder.append(".storeDict($refValue)")
                        }
                    }
                }

            storesBuilder.append(storeBits(bitsLeft))

            "beginCell()$storesBuilder.endCell()"
        }
        else -> TODO("Not yet implemented: $arg")
    }

private fun extractContractName(sourceRelativePath: Path): String {
    val fileName = sourceRelativePath.fileName.nameWithoutExtension
    val words = fileName.split('_', '-')
    val capitalizedWords = words.map { word ->
        word.replaceFirstChar { firstChar ->
            if (firstChar.isLowerCase()) {
                firstChar.titlecase(Locale.getDefault())
            } else {
                firstChar.toString()
            }
        }
    }

    return capitalizedWords.joinToString(separator = "")
}

private fun extractAddress(bits: String): String? {
    // addr_std$10 anycast:(Maybe Anycast) workchain_id:int8 address:bits256
    val stdAddrLength = ADDRESS_TAG_LENGTH + 1 + TvmContext.STD_WORKCHAIN_BITS + TvmContext.ADDRESS_BITS

    // TODO for now assume that the address is addr_std$10
    if (bits.length < stdAddrLength || bits.take(2) != STD_ADDRESS_TAG) {
        return null
    }

    val workchainBin = bits.drop(3).take(TvmContext.STD_WORKCHAIN_BITS)
    val addrBin = bits.drop(11).take(TvmContext.ADDRESS_BITS)

    val workchain = workchainBin.binaryToSignedDecimal()
    val addr = addrBin.binaryToHex()
    val paddedAddr = addr.padStart(TvmContext.ADDRESS_BITS / 4, '0')

    return "$workchain:$paddedAddr"
}

private fun renderTestBodies(name: String, tests: List<TvmSymbolicTest>, ident: String): String {
    val testBodies = tests.map { test ->
        val result = test.result
        require(result is TvmTerminalMethodSymbolicResult) {
            "Unexpected test result: $result"
        }

        val sliceArgs = test.usedParameters.filterIsInstance<TvmTestSliceValue>()
        val cellArgs = test.usedParameters.filterIsInstance<TvmTestDataCellValue>()
        require(sliceArgs.size <= 1 && cellArgs.size <= 1) {
            "Unexpected number of slice & cell arguments: ${sliceArgs.size} ${cellArgs.size}"
        }

        val messageCell = cellArgs.singleOrNull() ?: TvmTestDataCellValue()
        val messageBody = sliceArgs.singleOrNull()?.let { truncateSliceCell(it) } ?: TvmTestDataCellValue()
        val defaultCurrency = TvmTestIntegerValue(BigInteger.valueOf(TvmContext.MIN_MESSAGE_CURRENCY))
        val contractCurrency = test.usedParameters.firstOrNull { it is TvmTestIntegerValue }
            ?: defaultCurrency
        val messageCurrency = test.usedParameters.lastOrNull { it is TvmTestIntegerValue }
            ?: defaultCurrency

        val msgBits = messageCell.data

        val contractAddress = extractAddress(test.contractAddress.data)
            ?: error("Unexpected incorrect contract address")
        val srcAddress = extractAddress(msgBits.drop(4))
            ?: ("0:" + "0".repeat(64))

        val bounceValue = if (msgBits.getOrNull(2) == '1') "true" else "false"
        val bouncedValue = if (msgBits.getOrNull(3) == '1') "true" else "false"

        val contractAddressValue = "Address.parseRaw(\"$contractAddress\")"
        val srcAddressValue = "Address.parseRaw(\"$srcAddress\")"
        val persistentDataValue = renderArgValue(test.initialData)
        val msgBodyValue = renderArgValue(messageBody)
        val contractCurrencyValue = renderArgValue(contractCurrency)
        val msgCurrencyValue = renderArgValue(messageCurrency)

        """
            $ident        const data = $persistentDataValue
            $ident        const contractAddress = $contractAddressValue
            $ident        const contractBalance = ${contractCurrencyValue}n
            $ident
            $ident        const contract = blockchain.openContract(new $name(contractAddress, { code, data }))
            $ident        await contract.initializeContract(blockchain, contractBalance)
            $ident
            $ident        const from = $srcAddressValue
            $ident        const msgBody = $msgBodyValue
            $ident        const msgCurrency = ${msgCurrencyValue}n
            $ident        const bounce = $bounceValue
            $ident        const bounced = $bouncedValue
            $ident  
            $ident        const sentMessageResult = await contract.internal(
            $ident            blockchain,
            $ident            from,
            $ident            msgBody,
            $ident            msgCurrency,
            $ident            bounce,
            $ident            bounced
            $ident        )
            $ident        expect(sentMessageResult.transactions).toHaveTransaction({
            $ident            from: from,
            $ident            to: contractAddress,
            $ident            exitCode: ${result.exitCode},
            $ident        })
        """.trimIndent()

    }

    val renderedTestBodies = testBodies.mapIndexed { idx, body ->
        """
        $ident    it('test-$idx', async () => {
                      $body
        $ident    })
        """.trimIndent()
    }.joinToString(separator = "\n$ident\n")

    return renderedTestBodies
}

private fun renderWrapperFile(name: String): String = """
    import {Address, Cell, Contract, ContractProvider, TupleItem} from '@ton/core'
    import {Blockchain, createShardAccount, internal} from "@ton/sandbox"

    export class $name implements Contract {
        constructor(readonly address: Address, readonly init: { code: Cell; data: Cell }) {}

        async internal(
            blockchain: Blockchain,
            sender: Address,
            body: Cell,
            value: bigint,
            bounce: boolean,
            bounced: boolean
        ) {
            return await blockchain.sendMessage(internal({
                from: sender,
                to: this.address,
                body: body,
                value: value ,
                bounce: bounce,
                bounced: bounced,
            }))
        }

        async initializeContract(blockchain: Blockchain, balance: bigint) {
            const contr = await blockchain.getContract(this.address);
            contr.account = createShardAccount({
                address: this.address,
                code: this.init.code,
                data: this.init.data,
                balance: balance,
                workchain: 0
            })
        }

        async get(provider: ContractProvider, name: string, args: TupleItem[]) {
            return await provider.get(name, args)
        }
    }
""".trimIndent()


private fun renderTestFile(
    name: String,
    sourcePath: String,
    tests: List<TvmSymbolicTest>,
): String {
    val imports = """
        import {Blockchain} from '@ton/sandbox'
        import {Address, beginCell, Builder, Cell, Dictionary, DictionaryValue, Slice} from '@ton/core'
        import '@ton/test-utils'
        import {compileFunc} from "@ton-community/func-js"
        import * as fs from "node:fs"
        import {$name} from "../$wrappersDirName/$name"
        
    """.trimIndent()

    val compileFunction = """
        
        async function compileContract(): Promise<Cell> {
            let compileResult = await compileFunc({
                targets: ['$sourcePath'],
                sources: (x) => fs.readFileSync(x).toString("utf8"),
            })

            if (compileResult.status === "error") {
                console.error("Compilation Error!")
                console.error(`\n${'$'}{compileResult.message}`)
                process.exit(1)
            }

            return Cell.fromBoc(Buffer.from(compileResult.codeBoc, "base64"))[0]
        }
        
    """.trimIndent()

    val testCode = """
        |
        |const sliceValue: DictionaryValue<Slice> = {
        |    serialize: (src: Slice, builder: Builder) => {
        |        builder.storeSlice(src)
        |    },
        |    parse: (src: Slice) => {
        |        return src.clone();
        |    }
        |}
        |
        |describe('TvmTest', () => {
        |    let code: Cell
        |    let blockchain: Blockchain
        |
        |    beforeAll(async () => {
        |        code = await compileContract()
        |    })
        |
        |    beforeEach(async () => {
        |        blockchain = await Blockchain.create()
        |    })
        |
            ${renderTestBodies(name, tests, "|")}
        |})
    """.trimMargin()

    return imports + compileFunction + testCode
}
