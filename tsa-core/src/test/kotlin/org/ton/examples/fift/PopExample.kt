package org.ton.examples.fift

import io.ksmt.expr.KBitVecCustomValue
import org.junit.jupiter.api.Test
import org.usvm.machine.compileAndAnalyzeFift
import org.usvm.machine.state.TvmMethodResult
import org.usvm.machine.state.TvmStack
import kotlin.io.path.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PopExample {
    private val fiftPath: String = "/fift-examples/PopExample.fif"

    private fun extractIntegers(result: TvmMethodResult.TvmSuccess): List<Int?> =
        result.stack.stackContents.map {
            ((it as? TvmStack.TvmConcreteStackEntry)?.cell?.intValue as? KBitVecCustomValue)?.value?.intValueExact()
        }

    @Test
    fun testPopCommand() {
        val fiftResourcePath = this::class.java.getResource(fiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $fiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)
        val allStates = methodStates.values.flatten()
        val results = allStates.map { it.methodResult }
        assertEquals(2, results.size)

        val result1 = results[0]
        assertTrue(result1 is TvmMethodResult.TvmSuccess)
        val values1 = extractIntegers(result1)
        assertEquals(listOf(42, 13, 52), values1)

        val result2 = results[1]
        assertTrue(result2 is TvmMethodResult.TvmSuccess)
        val values2 = extractIntegers(result2)
        assertEquals(listOf(42, 17, 52), values2)
    }
}