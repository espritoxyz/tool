package org.ton.examples.loops

import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import org.usvm.machine.compileAndAnalyzeFift
import org.usvm.machine.state.TvmIntegerOverflow
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class AgainLoopTest {
    private val ctx = TvmContext(TvmComponents())

    private val againLoopsFiftPath: String = "/loops/AgainLoops.fif"

    @Test
    fun testAgainLoops(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(againLoopsFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $againLoopsFiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)
        assertEquals(3, methodStates.size)
        methodStates.all {
            it.value.size == 1 && it.value.all { state -> state.methodResult is TvmIntegerOverflow }
        }
    }
}
