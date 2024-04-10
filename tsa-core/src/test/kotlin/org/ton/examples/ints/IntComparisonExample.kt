package org.ton.examples.ints

import org.junit.jupiter.api.Test
import org.ton.examples.compareMethodStateStack
import org.ton.examples.compileAndAnalyzeAllMethods
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.intValue
import org.ton.examples.runFiftMethod
import org.usvm.machine.state.takeLastInt
import kotlin.io.path.Path
import kotlin.test.assertEquals

class IntComparisonExample {
    private val sourcesPath: String = "/ints/int_comparison_no_throw.fc"
    private val fiftPath: String = "/ints/Comparison.fif"

    @Test
    fun testIntComparisonExamples() {
        val sourceResourcePath = this::class.java.getResource(sourcesPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource source $sourcesPath")

        val methodStates = compileAndAnalyzeAllMethods(sourceResourcePath)
        methodStates.entries.forEach { (method, states) ->
            if (method.id == 0)
                return@forEach
            val results = states.map { it.stack.takeLastInt().intValue() }.sorted()
            assertEquals(listOf(1, 2), results)
        }
    }

    @Test
    fun testIntComparisonFift() {
        val fiftResourcePath = this::class.java.getResource(fiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $sourcesPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..13).toSet()
        compareMethodStateStack(methodIds, methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }
}