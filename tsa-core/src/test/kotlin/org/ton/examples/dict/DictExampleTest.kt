package org.ton.examples.dict

import org.ton.examples.compileAndAnalyzeAllMethods
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertTrue

class DictExample {
    private val sourcesPath: String = "/dict/dict_examples.fc"

    @Test
    fun testDictExamples() {
        val bytecodeResourcePath = this::class.java.getResource(sourcesPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource bytecode $sourcesPath")

        val methodStates = compileAndAnalyzeAllMethods(bytecodeResourcePath)
        assertTrue(methodStates.isNotEmpty())
    }
}