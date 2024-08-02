package org.ton.examples.continuations

import org.ton.examples.compareSymbolicAndConcreteResults
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmOptions
import kotlin.io.path.Path
import kotlin.test.Test

class ContinuationTest {
    private val ctx = TvmContext(TvmOptions(), TvmComponents())

    private val continuationsFiftPath: String = "/continuations/Continuations.fif"

    @Test
    fun testContinuations(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(continuationsFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $continuationsFiftPath")

        val symbolicResult = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..3).toSet()
        compareSymbolicAndConcreteResults(methodIds, symbolicResult) { methodId ->
            runFiftMethod(fiftResourcePath, methodId)
        }
    }
}
