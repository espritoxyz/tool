package org.ton.examples.stack

import org.junit.jupiter.api.Test
import org.ton.examples.compareActualAndExpectedStack
import org.usvm.machine.compileAndAnalyzeFift
import kotlin.io.path.Path

class StackPopTest {
    private val fiftPath: String = "/stack/StackPop.fif"

    @Test
    fun testPopCommand() {
        val fiftResourcePath = this::class.java.getResource(fiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $fiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val expectedMethodStackValues = mapOf(
            0 to listOf(42, 13, 52),
            1 to listOf(42, 17, 52),
        )

        compareActualAndExpectedStack(expectedMethodStackValues, methodStates)
    }
}