package org.ton.examples.division

import org.usvm.machine.analyzeAllMethods
import org.usvm.machine.state.TvmIntegerOverflow
import kotlin.test.Test
import kotlin.test.assertTrue

class DivisionBySymbolicFromCellExample {
    private val bytecodePath: String = "/division/div-by-cell-number/boc.txt"

    @Test
    fun testDivisionByZero() {
        val bytecodeResourcePath = this::class.java.getResource(bytecodePath)?.path
            ?: error("Cannot find resource bytecode $bytecodePath")

        val methodStates = analyzeAllMethods(bytecodeResourcePath)
        val allStates = methodStates.values.flatten()
        val results = allStates.map { it.methodResult }
        val exceptions = results.filterIsInstance<TvmIntegerOverflow>()
        assertTrue(exceptions.isNotEmpty(), "Division by zero was not found!")
    }
}