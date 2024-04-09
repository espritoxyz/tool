package org.ton.examples.division

import org.junit.jupiter.api.Test
import org.ton.examples.compareMethodStateStack
import org.usvm.machine.compileAndAnalyzeFift
import org.usvm.machine.runFiftMethod
import kotlin.io.path.Path

class IntDivisionTest {
    private val fiftPath = "/division/int_division_no_throw.fif"
    private val fiftPathOverflow = "/division/int_division_fail.fif"

    @Test
    fun testIntDivisionFiftNoThrow() {
        val fiftResourcePath = this::class.java.getResource(fiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $fiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..143).toSet()
        compareMethodStateStack(methodIds, methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }

    @Test
    fun testIntDivisionFiftFailure() {
        val fiftResourcePath = this::class.java.getResource(fiftPathOverflow)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $fiftPathOverflow")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..45).toSet()
        compareMethodStateStack(methodIds, methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }
}