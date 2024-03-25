package org.ton.examples.ints

import org.usvm.machine.compileAndAnalyzeAllMethods
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertTrue

class IntConstExample {
    private val sourcesPath: String = "/ints/int_const_example.fc"

    @Test
    fun testIntConstExamples() {
        val bytecodeResourcePath = this::class.java.getResource(sourcesPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource bytecode $sourcesPath")

        val methodStates = compileAndAnalyzeAllMethods(bytecodeResourcePath)
        assertTrue(methodStates.isNotEmpty())
    }
}