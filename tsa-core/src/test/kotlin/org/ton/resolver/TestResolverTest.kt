package org.ton.resolver

import java.math.BigInteger
import org.usvm.test.TvmTestResolver
import org.ton.bigint.plus
import org.ton.bytecode.TvmArithmDivInst
import org.ton.bytecode.TvmMethod
import org.usvm.machine.compileAndAnalyzeAllMethods
import org.usvm.machine.state.TvmIntegerOverflow
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.lastStmt
import org.usvm.test.TvmTestBuilderValue
import org.usvm.test.TvmTestCellValue
import org.usvm.test.TvmTestIntegerValue
import org.usvm.test.TvmTestSliceValue
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestResolverTest {
    private val divisionByIntZeroSource = "/division/div-by-zero/div_by_zero.fc"
    private val divisionByCellRefZeroSource = "/division/div-by-cell-ref-number/div-by-cell-ref-number.fc"
    private val divisionBySliceRefZeroSource = "/division/div-by-cell-ref-number/div-by-slice-ref-number.fc"
    private val divisionByBuilderRefZeroSource = "/division/div-by-cell-ref-number/div-by-builder-ref-number.fc"

    @Test
    fun testDivisionByIntZero() {
        val methodStates = compileAndAnalyzeAllMethods(divisionByIntZeroSource)
        val (method, state) = findMethodState(methodStates) { state ->
            state.methodResult is TvmIntegerOverflow && state.lastStmt is TvmArithmDivInst
        }

        val params = TvmTestResolver.resolve(method, state)
        assertTrue(params.size == 2)

        val arg0 = (params[0] as TvmTestIntegerValue).value
        val arg1 = (params[1] as TvmTestIntegerValue).value

        assertEquals(BigInteger.ZERO, arg0 - (arg1 + 3))
    }

    @Test
    fun testDivisionByCellRefZero() {
        val methodStates = compileAndAnalyzeAllMethods(divisionByCellRefZeroSource)
        val (method, state) = findMethodState(methodStates) { state ->
            state.methodResult is TvmIntegerOverflow && state.lastStmt is TvmArithmDivInst
        }

        val params = TvmTestResolver.resolve(method, state)
        assertTrue(params.size == 1)

        val arg = (params.first() as TvmTestCellValue).refs.first()
        val extractedInt = arg.data.dropLast(3).takeLast(8)

        assertEquals("00000011", extractedInt)
    }

    @Test
    fun testDivisionBySliceRefZero() {
        val methodStates = compileAndAnalyzeAllMethods(divisionBySliceRefZeroSource)
        val (method, state) = findMethodState(methodStates) { state ->
            state.methodResult is TvmIntegerOverflow && state.lastStmt is TvmArithmDivInst
        }

        val params = TvmTestResolver.resolve(method, state)
        assertTrue(params.size == 1)

        val arg = (params.first() as TvmTestSliceValue).cell.refs.first()
        val extractedInt = arg.data.dropLast(3).takeLast(8)

        assertEquals("00000011", extractedInt)
    }

    @Test
    fun testDivisionByBuilderRefZero() {
        val methodStates = compileAndAnalyzeAllMethods(divisionByBuilderRefZeroSource)
        val (method, state) = findMethodState(methodStates) { state ->
            state.methodResult is TvmIntegerOverflow && state.lastStmt is TvmArithmDivInst
        }

        val params = TvmTestResolver.resolve(method, state)
        assertTrue(params.size == 1)

        val arg = (params.first() as TvmTestBuilderValue).refs.first()
        val extractedInt = arg.data.dropLast(3).takeLast(8)

        assertEquals("00000011", extractedInt)
    }

    private fun compileAndAnalyzeAllMethods(sourcePath: String): Map<TvmMethod, List<TvmState>> {
        val bytecodeResourcePath = this::class.java.getResource(sourcePath)?.path?.let { Path(it) }
            ?: error("Cannot find resource bytecode $sourcePath")

        return compileAndAnalyzeAllMethods(bytecodeResourcePath)
    }

    private fun findMethodState(
        methodStates: Map<TvmMethod, List<TvmState>>,
        predicate: (TvmState) -> Boolean
    ): Pair<TvmMethod, TvmState> = methodStates.entries.firstNotNullOf { (method, states) ->
        val state = states.firstOrNull(predicate) ?: return@firstNotNullOf null

        method to state
    }
}