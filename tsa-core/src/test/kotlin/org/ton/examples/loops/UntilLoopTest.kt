package org.ton.examples.loops

import org.ton.examples.compareSymbolicAndConcreteResults
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import kotlin.io.path.Path
import kotlin.test.Test

class UntilLoopTest {
    private val ctx = TvmContext(TvmComponents())

    private val untilLoopsFiftPath: String = "/loops/UntilLoops.fif"

    @Test
    fun testUntilLoops(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(untilLoopsFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $untilLoopsFiftPath")

        val symbolicResult = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..5).toSet()
        compareSymbolicAndConcreteResults(methodIds, symbolicResult) { methodId ->
            runFiftMethod(fiftResourcePath, methodId)
        }
    }
}
