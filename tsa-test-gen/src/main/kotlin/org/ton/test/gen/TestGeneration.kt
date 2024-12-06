package org.ton.test.gen

import java.math.BigInteger
import java.nio.file.Path
import java.util.Locale
import org.ton.test.gen.dsl.TsContext
import org.ton.test.gen.dsl.models.TsBlockchain
import org.ton.test.gen.dsl.models.TsCell
import org.ton.test.gen.dsl.models.blockchainCreate
import org.ton.test.gen.dsl.models.compileContract
import org.ton.test.gen.dsl.models.openContract
import org.ton.test.gen.dsl.models.parseAddress
import org.ton.test.gen.dsl.models.toTsValue
import org.ton.test.gen.dsl.render.TsRenderer
import org.ton.test.gen.dsl.testFile
import org.ton.test.gen.dsl.wrapper.basic.TsBasicWrapperDescriptor
import org.ton.test.gen.dsl.wrapper.basic.constructor
import org.ton.test.gen.dsl.wrapper.basic.initializeContract
import org.ton.test.gen.dsl.wrapper.basic.internal
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.Companion.ADDRESS_TAG_LENGTH
import org.usvm.machine.TvmContext.Companion.STD_ADDRESS_TAG
import org.usvm.machine.truncateSliceCell
import org.usvm.test.resolver.TvmContractSymbolicTestResult
import org.usvm.test.resolver.TvmMethodFailure
import org.usvm.test.resolver.TvmSymbolicTest
import org.usvm.test.resolver.TvmTerminalMethodSymbolicResult
import org.usvm.test.resolver.TvmTestDataCellValue
import org.usvm.test.resolver.TvmTestIntegerValue
import org.usvm.test.resolver.TvmTestSliceValue
import kotlin.io.path.nameWithoutExtension

fun generateTests(
    analysisResult: TvmContractSymbolicTestResult,
    projectPath: Path,
    sourceRelativePath: Path,
): String {
    val entryTests = analysisResult.testSuites
        .single { it.methodId == TvmContext.RECEIVE_INTERNAL_ID }
        .filter { it.result is TvmMethodFailure }

    val name = extractContractName(sourceRelativePath)

    val ctx = TsContext()
    val test = ctx.recvInternalTests(name, entryTests, sourceRelativePath.toString())
    val renderedTests = TsRenderer(ctx).renderTests(test)

    writeRenderedTest(projectPath, renderedTests)
    return renderedTests.fileName
}

private fun TsContext.recvInternalTests(
    name: String,
    tests: List<TvmSymbolicTest>,
    sourcePath: String
) = testFile(name) {
    val wrapperDescriptor = TsBasicWrapperDescriptor(name)
    registerWrapper(wrapperDescriptor)

    describe("tsa-tests") {
        val code = newVar("code", TsCell)
        val blockchain = newVar("blockchain", TsBlockchain)

        emptyLine()

        beforeAll {
            code assign compileContract(sourcePath)
        }

        emptyLine()

        beforeEach {
            blockchain assign blockchainCreate()
        }

        emptyLine()

        tests.forEachIndexed { idx, test ->
            val input = resolveReceiveInternalInput(test)

            // TODO more specific names
            it("test-$idx") {
                val data = newVar("data", test.initialData.toTsValue())
                val contractAddr = newVar("contractAddr", parseAddress(input.address))
                val contractBalance = newVar("contractBalance", input.balance.toTsValue())

                emptyLine()

                val contract = newVar(
                    "contract",
                    blockchain.openContract(wrapperDescriptor.constructor(contractAddr, code, data))
                )
                +contract.initializeContract(blockchain, contractBalance)

                emptyLine()

                val srcAddr = newVar("srcAddr", parseAddress(input.srcAddress))
                val msgBody = newVar("msgBody", input.msgBody.toTsValue())
                val msgCurrency = newVar("msgCurrency", input.msgCurrency.toTsValue())
                val bounce = newVar("bounce", input.bounce.toTsValue())
                val bounced = newVar("bounced", input.bounced.toTsValue())

                emptyLine()

                val sendMessageResult = newVar(
                    "sendMessageResult",
                    contract.internal(blockchain, srcAddr, msgBody, msgCurrency, bounce, bounced)
                )
                sendMessageResult.expectToHaveTransaction {
                    from = srcAddr
                    to = contractAddr
                    exitCode = input.exitCode.toTsValue()
                }
            }
        }
    }
}

private fun resolveReceiveInternalInput(test: TvmSymbolicTest): TvmReceiveInternalInput {
    // assume that recv_internal args have specified order:
    // recv_internal(int balance, int msg_value, cell full_msg, slice msg_body)
    val args = test.usedParameters.reversed()
    val msgBody = args.getOrNull(0)
        ?: TvmTestSliceValue()
    val fullMsg = args.getOrNull(1)
        ?: TvmTestDataCellValue()
    val defaultCurrency = TvmTestIntegerValue(BigInteger.valueOf(TvmContext.MIN_MESSAGE_CURRENCY))
    val msgCurrency = args.getOrNull(2)
        ?: defaultCurrency
    val balance = args.getOrNull(3)
        ?: defaultCurrency

    require(msgBody is TvmTestSliceValue) {
        "Unexpected recv_internal arg at index 0: $msgBody"
    }
    require(fullMsg is TvmTestDataCellValue) {
        "Unexpected recv_internal arg at index 1: $fullMsg"
    }
    require(msgCurrency is TvmTestIntegerValue) {
        "Unexpected recv_internal arg at index 2: $msgCurrency"
    }
    require(balance is TvmTestIntegerValue) {
        "Unexpected recv_internal arg at index 3: $balance"
    }

    val msgBits = fullMsg.data
    val contractAddress = extractAddress(test.contractAddress.data)
        ?: error("Unexpected incorrect contract address")
    val srcAddress = extractAddress(msgBits.drop(4))
        ?: ("0:" + "0".repeat(64))

    val bounce = msgBits.getOrNull(2) == '1'
    val bounced = msgBits.getOrNull(3) == '1'

    val result = test.result
    require(result is TvmTerminalMethodSymbolicResult) {
        "Unexpected test result: $result"
    }

    return TvmReceiveInternalInput(
        truncateSliceCell(msgBody),
        fullMsg,
        msgCurrency,
        balance,
        contractAddress,
        srcAddress,
        bounce,
        bounced,
        result.exitCode.toInt()
    )
}

private data class TvmReceiveInternalInput(
    val msgBody: TvmTestDataCellValue,
    val fullMsg: TvmTestDataCellValue,
    val msgCurrency: TvmTestIntegerValue,
    val balance: TvmTestIntegerValue,
    val address: String,
    val srcAddress: String,
    val bounce: Boolean,
    val bounced: Boolean,
    val exitCode: Int,
)

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
