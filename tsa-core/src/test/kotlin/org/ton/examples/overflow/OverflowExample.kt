package org.ton.examples.overflow

import org.ton.examples.analyzeAllMethods
import org.usvm.machine.state.TvmIntegerOverflow
import kotlin.test.Test
import kotlin.test.assertTrue

class OverflowExample {
    private val bytecodePath: String = "/overflow/add-overflow/boc.txt"

    @Test
    fun testAddOverflowSymbolic() {
        val bytecodeResourcePath = this::class.java.getResource(bytecodePath)?.path
            ?: error("Cannot find resource bytecode $bytecodePath")

        val methodStates = analyzeAllMethods(bytecodeResourcePath)
        val allStates = methodStates.values.flatten()
        val results = allStates.map { it.methodResult }
        val exceptions = results.filterIsInstance<TvmIntegerOverflow>()
        assertTrue(exceptions.isNotEmpty(), "Integer overflow was not found!")
    }
}
