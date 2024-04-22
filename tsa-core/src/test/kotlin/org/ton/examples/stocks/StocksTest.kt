package org.ton.examples.stocks

import org.ton.examples.checkAtLeastOneStateForAllMethods
import org.ton.examples.funcCompileAndAnalyzeAllMethods
import kotlin.io.path.Path
import kotlin.test.Test

class StocksTest {
    private val sourcesPath: String = "/stocks/stock_options.fc"

    @Test
    fun testStocks() {
        val bytecodeResourcePath = this::class.java.getResource(sourcesPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource bytecode $sourcesPath")

        val methodStates = funcCompileAndAnalyzeAllMethods(bytecodeResourcePath)
        checkAtLeastOneStateForAllMethods(methodsNumber = 6, methodStates)
    }
}
