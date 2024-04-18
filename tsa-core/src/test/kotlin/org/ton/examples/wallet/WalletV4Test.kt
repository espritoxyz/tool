package org.ton.examples.wallet

import org.ton.examples.checkAtLeastOneStateForAllMethods
import org.ton.examples.compileAndAnalyzeAllMethods
import kotlin.io.path.Path
import kotlin.test.Test

class WalletV4Test {
    private val sourcesPath: String = "/wallet-v4/wallet-v4-code.fc"

    @Test
    fun testWallet() {
        val bytecodeResourcePath = this::class.java.getResource(sourcesPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource bytecode $sourcesPath")

        val methodsBlackList = hashSetOf(-1, Int.MAX_VALUE) // TODO exclude recv_external because of dict_add_builder (DICTADDB) instruction
        val symbolicResult = compileAndAnalyzeAllMethods(bytecodeResourcePath, methodsBlackList = methodsBlackList)
        checkAtLeastOneStateForAllMethods(methodsNumber = 6, symbolicResult) // TODO methodsNumber = 7
    }
}
