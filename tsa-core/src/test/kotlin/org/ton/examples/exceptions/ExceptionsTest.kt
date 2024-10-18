package org.ton.examples.exceptions

import org.ton.examples.compareSymbolicAndConcreteResults
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmOptions
import kotlin.io.path.Path
import kotlin.test.Test

class ExceptionsTest {
    private val ctx = TvmContext(TvmOptions(), TvmComponents())

    private val exceptionsFiftPath: String = "/exceptions/Exceptions.fif"

    @Test
    fun testExceptions(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(exceptionsFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $exceptionsFiftPath")

        val symbolicResult = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..5).toSet()
        compareSymbolicAndConcreteResults(methodIds, symbolicResult) { methodId ->
            runFiftMethod(fiftResourcePath, methodId)
        }
    }
}