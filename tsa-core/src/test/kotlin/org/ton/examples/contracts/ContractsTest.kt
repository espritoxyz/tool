package org.ton.examples.contracts

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.ton.examples.checkAtLeastOneStateForAllMethods
import org.ton.examples.funcCompileAndAnalyzeAllMethods
import org.ton.examples.runHardTestsRegex
import org.ton.examples.runHardTestsVar
import kotlin.io.path.Path
import kotlin.test.Test

class ContractsTest {
    private val nftItemPath: String = "/contracts/nft-item/nft-item.fc"
    private val walletPath: String = "/contracts/wallet-v4/wallet-v4-code.fc"
    private val jettonMinterPath: String = "/contracts/modern-jetton/jetton-minter.func"

    @Test
    fun testWallet() {
        val methodsBlackList = hashSetOf(-1, Int.MAX_VALUE) // TODO exclude recv_external because of dict_add_builder (DICTADDB) instruction
        analyzeContract(walletPath, methodsNumber = 6, methodsBlackList) // TODO methodsNumber = 7
    }

    @EnabledIfEnvironmentVariable(named = runHardTestsVar, matches = runHardTestsRegex)
    @Test
    fun nftItem() {
        analyzeContract(nftItemPath, methodsNumber = 15)
    }

    @Test
    fun jettonMinter() {
        analyzeContract(jettonMinterPath, methodsNumber = 4)
    }

    private fun analyzeContract(
        contractPath: String,
        methodsNumber: Int,
        methodsBlackList: Set<Int> = setOf(Int.MAX_VALUE)
    ) {
        val bytecodeResourcePath = this::class.java.getResource(contractPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource bytecode $contractPath")

        val methodStates = funcCompileAndAnalyzeAllMethods(bytecodeResourcePath, methodsBlackList = methodsBlackList)
        checkAtLeastOneStateForAllMethods(methodsNumber = methodsNumber, methodStates)
    }
}