package org.ton.examples.stack

import org.junit.jupiter.api.Test
import org.ton.examples.compareMethodStateStack
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import kotlin.io.path.Path

class StackPopTest {
    private val fiftPath: String = "/stack/StackPop.fif"

    @Test
    fun testPopCommand() {
        val fiftResourcePath = this::class.java.getResource(fiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $fiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        compareMethodStateStack(methodIds = setOf(0, 1), methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }
}