package org.ton.examples

import kotlin.test.Test
import kotlin.test.assertTrue

class CounterExample {
    // TODO fix
    private val bytecodePath: String = "/counter.txt"

    @Test
    fun testCounter() {
        val bytecodeResourcePath = this::class.java.getResource(bytecodePath)?.path
            ?: error("Cannot find resource bytecode $bytecodePath")

        val methodStates = analyzeAllMethods(bytecodeResourcePath)
        val allStates = methodStates.values.flatten()
        val results = allStates.map { it.methodResult }
        assertTrue(results.isNotEmpty())
    }
}
