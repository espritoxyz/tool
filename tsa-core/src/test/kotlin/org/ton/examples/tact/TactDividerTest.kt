package org.ton.examples.tact

import org.ton.examples.tactCompileAndAnalyzeAllMethods
import org.usvm.machine.state.TvmIntegerOverflowError
import org.usvm.test.resolver.TvmMethodFailure
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertTrue

class TactDividerTest {
    private val sourcesPath: String = "/tact/divider.tact"

    @Test
    fun testDivider() {
        val resourcePath = this::class.java.getResource(sourcesPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $sourcesPath")

        // TODO analyze only divide method 95202
        val symbolicResult = tactCompileAndAnalyzeAllMethods(
            resourcePath,
            methodsBlackList = hashSetOf(Int.MAX_VALUE, 0, 113617, 115390, 121275)
        )

        val allTests = symbolicResult.map { it.tests }.flatten()
        val results = allTests.map { it.result }
        val exceptions = results.mapNotNull { (it as? TvmMethodFailure)?.failure }.filterIsInstance<TvmIntegerOverflowError>()
        assertTrue(exceptions.isNotEmpty(), "Division by zero was not found!")
    }
}
