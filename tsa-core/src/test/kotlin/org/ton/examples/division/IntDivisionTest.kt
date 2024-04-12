package org.ton.examples.division

import org.junit.jupiter.api.Test
import org.ton.examples.compareMethodStateStack
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import kotlin.io.path.Path

class IntDivisionTest {
    private val fiftPath = "/division/int_division_no_throw.fif"
    private val fiftPathBasic = "/division/int_division_basic.fif"
    private val fiftPathFail = "/division/int_division_fail.fif"

    @Test
    fun testIntDivisionFiftBasic() {
        val fiftResourcePath = this::class.java.getResource(fiftPathBasic)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $fiftPathBasic")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..30).toSet()
        compareMethodStateStack(methodIds, methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }

    @Test
    fun testIntDivisionFiftNoThrow() {
        val fiftResourcePath = this::class.java.getResource(fiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $fiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..190).toSet()
        compareMethodStateStack(methodIds, methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }

    @Test
    fun testIntDivisionFiftFailure() {
        val fiftResourcePath = this::class.java.getResource(fiftPathFail)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $fiftPathFail")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val methodIds = (0..94).toSet()
        compareMethodStateStack(methodIds, methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }
}