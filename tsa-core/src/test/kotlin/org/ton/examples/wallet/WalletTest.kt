package org.ton.examples.wallet

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.ton.examples.checkAtLeastOneStateForAllMethods
import org.ton.examples.funcCompileAndAnalyzeAllMethods
import org.ton.examples.runHardTestsRegex
import org.ton.examples.runHardTestsVar
import kotlin.io.path.Path
import kotlin.test.Test

class WalletTest {
    private val sourcesPath: String = "/wallet/wallet.fc"

    @EnabledIfEnvironmentVariable(named = runHardTestsVar, matches = runHardTestsRegex)
    @Test
    fun testWallet() {
        val bytecodeResourcePath = this::class.java.getResource(sourcesPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource bytecode $sourcesPath")

        val methodStates = funcCompileAndAnalyzeAllMethods(bytecodeResourcePath)
        checkAtLeastOneStateForAllMethods(methodsNumber = 15, methodStates)
    }
}