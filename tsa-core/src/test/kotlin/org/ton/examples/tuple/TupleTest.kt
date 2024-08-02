package org.ton.examples.tuple

import org.ton.examples.compareSymbolicAndConcreteResults
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmOptions
import kotlin.io.path.Path
import kotlin.test.Test

class TupleTest {
    private val ctx = TvmContext(TvmOptions(), TvmComponents())

    private val tupleSuccessFiftPath: String = "/tuple/TupleSuccess.fif"
    private val tupleFailureFiftPath: String = "/tuple/TupleFailure.fif"

    @Test
    fun testTupleSuccess(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(tupleSuccessFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $tupleSuccessFiftPath")

        val symbolicResult = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..14).toSet()
        compareSymbolicAndConcreteResults(methodIds, symbolicResult) { methodId ->
            runFiftMethod(fiftResourcePath, methodId)
        }
    }

    @Test
    fun testTupleFailure(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(tupleFailureFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $tupleFailureFiftPath")

        val symbolicResult = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..12).toSet()
        compareSymbolicAndConcreteResults(methodIds, symbolicResult) { methodId ->
            runFiftMethod(fiftResourcePath, methodId)
        }
    }
}
