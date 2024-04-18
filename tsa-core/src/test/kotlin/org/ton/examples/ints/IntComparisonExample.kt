package org.ton.examples.ints

import org.junit.jupiter.api.Test
import org.ton.examples.compareSymbolicAndConcreteResults
import org.ton.examples.compileAndAnalyzeAllMethods
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import org.usvm.test.TvmTestIntegerValue
import kotlin.io.path.Path
import kotlin.test.assertEquals

class IntComparisonExample {
    private val sourcesPath: String = "/ints/int_comparison_no_throw.fc"
    private val fiftPath: String = "/ints/Comparison.fif"

    @Test
    fun testIntComparisonExamples() {
        val sourceResourcePath = this::class.java.getResource(sourcesPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource source $sourcesPath")

        val symbolicResult = compileAndAnalyzeAllMethods(sourceResourcePath)
        symbolicResult.forEach { (methodId, tests) ->
            if (methodId == 0)
                return@forEach
            val results = tests.flatMap { test ->
                test.result.stack.map { (it as TvmTestIntegerValue).value.toInt() }
            }.sorted()
            assertEquals(listOf(1, 2), results)
        }
    }

    @Test
    fun testIntComparisonFift() {
        val fiftResourcePath = this::class.java.getResource(fiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $sourcesPath")

        val symbolicResult = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..13).toSet()
        compareSymbolicAndConcreteResults(methodIds, symbolicResult) { methodId ->
            runFiftMethod(fiftResourcePath, methodId)
        }
    }
}