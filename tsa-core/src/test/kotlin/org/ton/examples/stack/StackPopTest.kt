package org.ton.examples.stack

import org.junit.jupiter.api.Test
import org.ton.examples.compareSymbolicAndConcreteResults
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import org.ton.examples.testFiftOptions
import kotlin.io.path.Path

class StackPopTest {
    private val fiftPath: String = "/stack/StackPop.fif"

    @Test
    fun testPopCommand() {
        val fiftResourcePath = this::class.java.getResource(fiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $fiftPath")

        val symbolicResult = compileAndAnalyzeFift(fiftResourcePath, tvmOptions = testFiftOptions)

        compareSymbolicAndConcreteResults(methodIds = setOf(0, 1), symbolicResult) { methodId ->
            runFiftMethod(fiftResourcePath, methodId)
        }
    }
}