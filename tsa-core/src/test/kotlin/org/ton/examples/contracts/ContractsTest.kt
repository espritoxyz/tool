package org.ton.examples.contracts

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.ton.examples.checkAtLeastOneStateForAllMethods
import org.ton.examples.funcCompileAndAnalyzeAllMethods
import org.ton.runHardTestsRegex
import org.ton.runHardTestsVar
import org.usvm.machine.MethodId
import org.usvm.machine.mainMethodId
import kotlin.io.path.Path
import kotlin.test.Ignore
import kotlin.test.Test

class ContractsTest {
    private val nftItemPath: String = "/contracts/nft-item/nft-item.fc"
    private val walletV4Path: String = "/contracts/wallet-v4/wallet-v4-code.fc"
    private val walletV5Path: String = "/contracts/wallet-v5/wallet_v5.fc"
    private val subscriptionPluginPath: String = "/contracts/wallet-v4/simple-subscription-plugin.fc"
    private val jettonMinterPath: String = "/contracts/modern-jetton/jetton-minter.func"
    private val jettonWalletPath: String = "/contracts/modern-jetton/jetton-wallet.func"
    private val universalLockupWalletPath: String = "/contracts/universal-lockup-wallet/uni-lockup-wallet.fc"
    private val vestingLockupWalletPath: String = "/contracts/vesting-lockup-wallet/vesting-lockup-wallet.fc"
    private val bridgePath: String = "/contracts/bridge/bridge_code.fc"
    private val bridgeMultisigPath: String = "/contracts/bridge/multisig-code.fc"
    private val bridgeVotesCollectorPath: String = "/contracts/bridge/votes-collector.fc"
    private val multisigPath: String = "/contracts/multisig/multisig-code.fc"
    private val storagePath: String = "/contracts/storage/storage-contract.fc"
    private val storageProviderPath: String = "/contracts/storage/storage-provider.fc"
    private val vestingPath: String = "/contracts/vesting/vesting_wallet.fc"
    private val singleNominatorPath: String = "/contracts/single-nominator/single-nominator.fc"
    private val nominatorPoolPath: String = "/contracts/nominator-pool/pool.fc"
    private val stocksPath: String = "/contracts/stocks/stock_options.fc"

    @Test
    fun testStocks() {
        analyzeContract(stocksPath, methodsNumber = 6)
    }

    @Test
    fun testWalletV4() {
        analyzeContract(walletV4Path, methodsNumber = 7)
    }

    @Ignore("slow hash validation https://github.com/explyt/tsa/issues/112")
    @Test
    fun testWalletV5() {
        analyzeContract(walletV5Path, methodsNumber = 7)
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

    @Test
    fun jettonWallet() {
        analyzeContract(jettonWalletPath, methodsNumber = 3)
    }

    @Test
    fun singleNominator() {
        analyzeContract(singleNominatorPath, methodsNumber = 3)
    }

    @Test
    fun storage() {
        analyzeContract(storagePath, methodsNumber = 7)
    }

    @Test
    fun vestingLockupWallet() {
        analyzeContract(vestingLockupWalletPath, methodsNumber = 6)
    }

    @Test
    fun testSubscriptionPlugin() {
        analyzeContract(subscriptionPluginPath, methodsNumber = 4)
    }

    @Test
    fun bridge() {
        analyzeContract(bridgePath, methodsNumber = 8)
    }

    @Test
    fun bridgeVotesCollector() {
        analyzeContract(bridgeVotesCollectorPath, methodsNumber = 5)
    }

    @EnabledIfEnvironmentVariable(named = runHardTestsVar, matches = runHardTestsRegex)
    @Test
    fun nominatorPool() {
        analyzeContract(nominatorPoolPath, methodsNumber = 10)
    }

    @Ignore("slow hash validation https://github.com/explyt/tsa/issues/112")
    @Test
    fun multisig() {
        analyzeContract(multisigPath, methodsNumber = 16)
    }

    @Ignore("ksmt bug https://github.com/UnitTestBot/ksmt/issues/160")
    @Test
    fun bridgeMultisig() {
        analyzeContract(bridgeMultisigPath, methodsNumber = 18)
    }

    @Test
    fun storageProvider() {
        analyzeContract(storageProviderPath, methodsNumber = 10)
    }

    @EnabledIfEnvironmentVariable(named = runHardTestsVar, matches = runHardTestsRegex)
    @Test
    fun vesting() {
        analyzeContract(vestingPath, methodsNumber = 9)
    }

    @Ignore("PFXDICTGETQ is not supported")
    @Test
    fun universalLockupWallet() {
        analyzeContract(universalLockupWalletPath, methodsNumber = 13)
    }

    private fun analyzeContract(
        contractPath: String,
        methodsNumber: Int,
        methodsBlackList: Set<MethodId> = hashSetOf(mainMethodId),
    ) {
        val bytecodeResourcePath = this::class.java.getResource(contractPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource bytecode $contractPath")

        val methodStates = funcCompileAndAnalyzeAllMethods(bytecodeResourcePath, methodsBlackList = methodsBlackList)
        checkAtLeastOneStateForAllMethods(methodsNumber = methodsNumber, methodStates)
    }
}
