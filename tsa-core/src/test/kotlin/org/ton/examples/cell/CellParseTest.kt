package org.ton.examples.cell

import org.ton.examples.compareMethodStateStack
import org.usvm.machine.compileAndAnalyzeFift
import org.usvm.machine.runFiftMethod
import kotlin.io.path.Path
import kotlin.test.Test

class CellParseTest {
    private val cellParseFiftPath: String = "/cell/CellParse.fif"
    private val cellParseFiftFailurePath: String = "/cell/CellParseFailure.fif"

    @Test
    fun cellLoadIntTest() {
        val fiftResourcePath = this::class.java.getResource(cellParseFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $cellParseFiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)
        val methodIds = (0..6).toSet()

        compareMethodStateStack(methodIds, methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }

    @Test
    fun cellLoadIntFailureTest() {
        val fiftResourcePath = this::class.java.getResource(cellParseFiftFailurePath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $cellParseFiftFailurePath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)
        val methodIds = (0..6).toSet()

        compareMethodStateStack(methodIds, methodStates) { method ->
            runFiftMethod(fiftResourcePath, method.id)
        }
    }
}