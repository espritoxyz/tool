package org.ton.examples.loops

import org.ton.examples.compareActualAndExpectedStack
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import org.usvm.machine.compileAndAnalyzeFift
import kotlin.io.path.Path
import kotlin.test.Test

class RepeatLoopTest {
    private val ctx = TvmContext(TvmComponents())

    private val repeatLoopsFiftPath: String = "/loops/RepeatLoops.fif"

    @Test
    fun testRepeatLoops(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(repeatLoopsFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $repeatLoopsFiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)
        val expectedResult = listOf(32)

        val expectedMethodStackValues = (0..2).associateWith { expectedResult }

        compareActualAndExpectedStack(expectedMethodStackValues, methodStates)
    }
}
