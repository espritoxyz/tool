package org.ton.examples.loops

import org.ton.examples.compareMethodStateStack
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import org.usvm.machine.compileAndAnalyzeFift
import org.usvm.machine.runFiftMethod
import kotlin.io.path.Path
import kotlin.test.Test

class AgainLoopTest {
    private val ctx = TvmContext(TvmComponents())

    private val againLoopsFiftPath: String = "/loops/AgainLoops.fif"

    @Test
    fun testAgainLoops(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(againLoopsFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $againLoopsFiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..2).toSet()
        compareMethodStateStack(methodIds, methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }
}
