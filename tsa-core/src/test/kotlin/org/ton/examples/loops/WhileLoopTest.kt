package org.ton.examples.loops

import org.ton.examples.compareSymbolicAndConcreteResults
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmOptions
import kotlin.io.path.Path
import kotlin.test.Test

class WhileLoopTest {
    private val ctx = TvmContext(TvmOptions(), TvmComponents())

    private val whileLoopsFiftPath: String = "/loops/WhileLoops.fif"

    @Test
    fun testWhileLoops(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(whileLoopsFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $whileLoopsFiftPath")

        val symbolicResult = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..5).toSet()
        compareSymbolicAndConcreteResults(methodIds, symbolicResult) { methodId ->
            runFiftMethod(fiftResourcePath, methodId)
        }
    }
}
