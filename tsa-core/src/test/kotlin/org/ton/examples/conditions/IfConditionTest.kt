package org.ton.examples.conditions

import org.ton.examples.compareMethodStateStack
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import kotlin.io.path.Path
import kotlin.test.Test

class IfConditionTest {
    private val ctx = TvmContext(TvmComponents())

    private val ifConditionsFiftPath: String = "/conditions/IfCondition.fif"

    @Test
    fun testIfConditions(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(ifConditionsFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $ifConditionsFiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..5).toSet()
        compareMethodStateStack(methodIds, methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }
}
