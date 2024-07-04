package org.ton.examples.continuations

import org.ton.examples.compareSymbolicAndConcreteResults
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import kotlin.io.path.Path
import kotlin.test.Test

class SavelistTest {
    private val ctx = TvmContext(TvmComponents())

    private val savelistFiftPath: String = "/continuations/Savelist.fif"

    @Test
    fun testSavelist(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(savelistFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $savelistFiftPath")

        val symbolicResult = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..1).toSet()
        compareSymbolicAndConcreteResults(methodIds, symbolicResult) { methodId ->
            runFiftMethod(fiftResourcePath, methodId)
        }
    }
}
