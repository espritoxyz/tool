package org.ton.examples.loops

import org.ton.examples.compareSymbolicAndConcreteResults
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import kotlin.io.path.Path
import kotlin.test.Test

class AgainLoopTest {
    private val ctx = TvmContext(TvmComponents())

    private val againLoopsFiftPath: String = "/loops/AgainLoops.fif"

    @Test
    fun testAgainLoops(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(againLoopsFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $againLoopsFiftPath")

        val symbolicResult = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..2).toSet()
        compareSymbolicAndConcreteResults(methodIds, symbolicResult) { methodId ->
            runFiftMethod(fiftResourcePath, methodId)
        }
    }
}
