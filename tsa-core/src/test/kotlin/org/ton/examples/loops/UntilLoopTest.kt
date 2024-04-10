package org.ton.examples.loops

import org.ton.examples.compareMethodStateStack
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

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..2).toSet()
        compareMethodStateStack(methodIds, methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }
}
