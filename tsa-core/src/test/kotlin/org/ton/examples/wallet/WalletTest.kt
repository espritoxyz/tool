package org.ton.examples.wallet

import org.ton.examples.checkAtLeastOneStateForAllMethods
import org.ton.examples.funcCompileAndAnalyzeAllMethods
import kotlin.io.path.Path
import kotlin.test.Test

class WalletTest {
    private val sourcesPath: String = "/wallet/wallet.fc"

    @Test
    fun testWallet() {
        val bytecodeResourcePath = this::class.java.getResource(sourcesPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource bytecode $sourcesPath")

        val methodStates = funcCompileAndAnalyzeAllMethods(bytecodeResourcePath)
        checkAtLeastOneStateForAllMethods(methodsNumber = 15, methodStates)
    }
}