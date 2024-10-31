package org.ton.examples.conditions

import org.ton.examples.compareSymbolicAndConcreteResults
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmMachine
import org.usvm.machine.TvmOptions
import kotlin.io.path.Path
import kotlin.test.Test

class IfConditionTest {
    private val ctx = TvmContext(TvmOptions(), TvmComponents(TvmMachine.defaultOptions))

    private val ifConditionsFiftPath: String = "/conditions/IfCondition.fif"

    @Test
    fun testIfConditions(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(ifConditionsFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $ifConditionsFiftPath")

        val symbolicResult = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..5).toSet()
        compareSymbolicAndConcreteResults(methodIds, symbolicResult) { methodId ->
            runFiftMethod(fiftResourcePath, methodId)
        }
    }
}
