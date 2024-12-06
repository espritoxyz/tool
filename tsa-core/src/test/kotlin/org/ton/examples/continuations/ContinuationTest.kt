package org.ton.examples.continuations

import org.ton.examples.compareSymbolicAndConcreteResults
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import org.ton.examples.testFiftOptions
import kotlin.io.path.Path
import kotlin.test.Test

class ContinuationTest {
    private val continuationsFiftPath: String = "/continuations/Continuations.fif"

    @Test
    fun testContinuations() {
        val fiftResourcePath = this::class.java.getResource(continuationsFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $continuationsFiftPath")

        val symbolicResult = compileAndAnalyzeFift(fiftResourcePath, tvmOptions = testFiftOptions)

        val methodIds = (0..3).toSet()
        compareSymbolicAndConcreteResults(methodIds, symbolicResult) { methodId ->
            runFiftMethod(fiftResourcePath, methodId)
        }
    }
}
