package org.ton.examples.cell

import org.ton.examples.compareSymbolicAndConcreteResults
import org.ton.examples.compileAndAnalyzeFift
import org.ton.examples.runFiftMethod
import kotlin.io.path.Path
import kotlin.test.Test

class CellParseTest {
    private val cellParseFiftPath: String = "/cell/CellParse.fif"
    private val cellParseFiftFailurePath: String = "/cell/CellParseFailure.fif"

    @Test
    fun cellLoadIntTest() {
        val fiftResourcePath = this::class.java.getResource(cellParseFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $cellParseFiftPath")

        val symbolicResult = compileAndAnalyzeFift(fiftResourcePath)
        val methodIds = (0..6).toSet()

        compareSymbolicAndConcreteResults(methodIds, symbolicResult) { methodId ->
            runFiftMethod(fiftResourcePath, methodId)
        }
    }

    @Test
    fun cellLoadIntFailureTest() {
        val fiftResourcePath = this::class.java.getResource(cellParseFiftFailurePath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $cellParseFiftFailurePath")

        val symbolicResult = compileAndAnalyzeFift(fiftResourcePath)
        val methodIds = (0..6).toSet()

        compareSymbolicAndConcreteResults(methodIds, symbolicResult) { methodId ->
            runFiftMethod(fiftResourcePath, methodId)
        }
    }
}