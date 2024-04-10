package org.ton.examples.tuple

import org.ton.examples.compareMethodStateStack
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import kotlin.io.path.Path
import kotlin.test.Test

class TupleTest {
    private val ctx = TvmContext(TvmComponents())

    private val tupleSuccessFiftPath: String = "/tuple/TupleSuccess.fif"
    private val tupleFailureFiftPath: String = "/tuple/TupleFailure.fif"

    @Test
    fun testTupleSuccess(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(tupleSuccessFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $tupleSuccessFiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..14).toSet()
        compareMethodStateStack(methodIds, methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }

    @Test
    fun testTupleFailure(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(tupleFailureFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $tupleFailureFiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..12).toSet()
        compareMethodStateStack(methodIds, methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }
}
