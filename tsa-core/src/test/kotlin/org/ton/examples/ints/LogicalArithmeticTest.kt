package org.ton.examples.ints

import org.ton.examples.compareMethodStateStack
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import kotlin.io.path.Path
import kotlin.test.Test

class LogicalArithmeticTest {
    private val logicalArithFiftPath: String = "/ints/logical_arith.fif"
    private val logicalArithFailureFiftPath: String = "/ints/logical_arith_failure.fif"

    @Test
    fun logicalArithResultTest() {
        val fiftResourcePath = this::class.java.getResource(logicalArithFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $logicalArithFiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..18).toSet()
        compareMethodStateStack(methodIds, methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }

    @Test
    fun logicalArithFailureTest() {
        val fiftResourcePath = this::class.java.getResource(logicalArithFailureFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $logicalArithFailureFiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..14).toSet()
        compareMethodStateStack(methodIds, methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }
}