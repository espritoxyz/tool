package org.ton.examples.loops

import org.ton.examples.compareSymbolicAndConcreteResults
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmMachine
import org.usvm.machine.TvmOptions
import kotlin.io.path.Path
import kotlin.test.Test

class RepeatLoopTest {
    private val ctx = TvmContext(TvmOptions(), TvmComponents(TvmMachine.defaultOptions))

    private val repeatLoopsFiftPath: String = "/loops/RepeatLoops.fif"

    @Test
    fun testRepeatLoops(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(repeatLoopsFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $repeatLoopsFiftPath")

        val symbolicResult = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..6).toSet()
        compareSymbolicAndConcreteResults(methodIds, symbolicResult) { methodId ->
            runFiftMethod(fiftResourcePath, methodId)
        }
    }
}
