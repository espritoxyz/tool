package org.ton.examples.ints

import org.junit.jupiter.api.Test
import org.ton.examples.compareActualAndExpectedStack
import org.ton.examples.intValue
import org.usvm.machine.compileAndAnalyzeAllMethods
import org.usvm.machine.compileAndAnalyzeFift
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

        val expectedMethodStackValues = mapOf(
            0 to listOf(-1),
            1 to listOf(0),
            2 to listOf(1),
            3 to listOf(1),
            4 to listOf(0),
            5 to listOf(-1),
            6 to listOf(0),
            7 to listOf(0),
            8 to listOf(-1),
            9 to listOf(-1),
            10 to listOf(0),
            11 to listOf(-1),
            12 to listOf(0),
            13 to listOf(-1),
        )

        compareActualAndExpectedStack(expectedMethodStackValues, methodStates)
    }
}