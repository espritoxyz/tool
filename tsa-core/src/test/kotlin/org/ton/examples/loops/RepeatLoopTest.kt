package org.ton.examples.loops

import org.ton.examples.compareMethodStateStack
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
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

        val methodIds = (0..3).toSet()
        compareMethodStateStack(methodIds, methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }
}
