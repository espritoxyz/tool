package org.ton.examples.dict

import org.ton.examples.funcCompileAndAnalyzeAllMethods
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertTrue

class DictExample {
    private val sourcesPath: String = "/dict/dict_examples.fc"

    @Test
    fun testDictExamples() {
        val resourcePath = this::class.java.getResource(sourcesPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $sourcesPath")

        val symbolicResult = funcCompileAndAnalyzeAllMethods(resourcePath)
        assertTrue(symbolicResult.isNotEmpty())
    }
}
