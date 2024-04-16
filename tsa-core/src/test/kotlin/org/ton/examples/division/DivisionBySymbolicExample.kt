package org.ton.examples.division

import org.ton.examples.analyzeAllMethods
import org.usvm.machine.state.TvmIntegerOverflowError
import kotlin.test.Test
import kotlin.test.assertTrue

class DivisionByZeroExample {
    private val bytecodePath: String = "/division/div-by-constant-zero/boc.txt"

    @Test
    fun testDivisionByZero() {
        val bytecodeResourcePath = this::class.java.getResource(bytecodePath)?.path
            ?: error("Cannot find resource bytecode $bytecodePath")

        val methodStates = analyzeAllMethods(bytecodeResourcePath)
        val allStates = methodStates.values.flatten()
        val results = allStates.map { it.methodResult }
        val exceptions = results.filterIsInstance<TvmIntegerOverflowError>()
        assertTrue(exceptions.isNotEmpty(), "Division by zero was not found!")
    }
}