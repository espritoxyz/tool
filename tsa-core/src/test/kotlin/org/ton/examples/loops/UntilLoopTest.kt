package org.ton.examples.loops

import org.ton.examples.compareActualAndExpectedStack
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import org.usvm.machine.compileAndAnalyzeFift
import kotlin.io.path.Path
import kotlin.test.Test

class UntilLoopTest {
    private val ctx = TvmContext(TvmComponents())

    private val untilLoopsFiftPath: String = "/loops/UntilLoops.fif"

    @Test
    fun testUntilLoops(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(untilLoopsFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $untilLoopsFiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)
        val expectedResult = listOf(5, 32)

        val expectedMethodStackValues = (0..2).associateWith { expectedResult }

        compareActualAndExpectedStack(expectedMethodStackValues, methodStates)
    }
}
