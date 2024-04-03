package org.ton.examples.loops

import org.ton.examples.compareMethodStateStackNumbers
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import org.usvm.machine.compileAndAnalyzeFift
import org.usvm.machine.runFiftMethod
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
        compareMethodStateStackNumbers(methodIds, methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }
}
