package org.ton.examples.loops

import org.ton.examples.compareActualAndExpectedStack
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import org.usvm.machine.compileAndAnalyzeFift
import kotlin.io.path.Path
import kotlin.test.Test

class WhileLoopTest {
    private val ctx = TvmContext(TvmComponents())

    private val whileLoopsFiftPath: String = "/loops/WhileLoops.fif"

    @Test
    fun testWhileLoops(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(whileLoopsFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $whileLoopsFiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)
        val expectedResult = listOf(5, 32)

        val expectedMethodStackValues = (0..2).associateWith { expectedResult }

        compareActualAndExpectedStack(expectedMethodStackValues, methodStates)
    }
}
