package org.ton.examples.ints

import io.ksmt.utils.powerOfTwo
import org.ton.examples.compareActualAndExpectedMethodResults
import org.ton.examples.compareActualEvaluatedAndExpectedStack
import org.usvm.machine.compileAndAnalyzeFift
import org.usvm.machine.state.TvmIntegerOutOfRange
import org.usvm.machine.state.TvmIntegerOverflow
import org.usvm.machine.state.TvmMethodResult
import kotlin.io.path.Path
import kotlin.test.Test

class LogicalArithmeticTest {
    private val logicalArithFiftPath: String = "/ints/logical_arith.fif"
    private val logicalArithFailureFiftPath: String = "/ints/logical_arith_failure.fif"

    @Test
    fun logicalArithResultTest() {
        val fiftResourcePath = this::class.java.getResource(logicalArithFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $logicalArithFiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val expectedMethodStackValues: Map<Int, List<Number>> = mapOf(
            0 to listOf(-1, 9, 9),
            1 to listOf(-6, 9, 8),
            2 to listOf(5, 0, 1),
            3 to listOf(0, -2),
            4 to listOf(11, 11, 0),
            5 to listOf(1),
            6 to listOf(-11),
            7 to listOf(-11, 1, -11, 1),
            8 to listOf(1, powerOfTwo(255u)),
            9 to listOf(-powerOfTwo(256u), powerOfTwo(255u) + powerOfTwo(254u)),
            10 to listOf(-powerOfTwo(256u), powerOfTwo(255u) + powerOfTwo(254u), 1),
            11 to listOf(-3, -1, 2),
            12 to listOf(-3, -1, 2, 5),
            13 to listOf(-1, 3),
            14 to listOf(-1, 3, 0, 0),
            15 to listOf(1, 3),
            16 to listOf(1, 3, 0, 0),
            17 to listOf(0, 3, 1, 257, 257),
            18 to listOf(0, 2, 256)
        )

        compareActualEvaluatedAndExpectedStack(expectedMethodStackValues, methodStates)
    }

    @Test
    fun logicalArithFailureTest() {
        val fiftResourcePath = this::class.java.getResource(logicalArithFailureFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $logicalArithFailureFiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val expectedMethodFailures: Map<Int, TvmMethodResult> = mapOf(
            0 to TvmIntegerOverflow,
            1 to TvmIntegerOverflow,
            2 to TvmIntegerOverflow,
            3 to TvmIntegerOverflow,
            4 to TvmIntegerOverflow,
            5 to TvmIntegerOverflow,
            6 to TvmIntegerOutOfRange,
            7 to TvmIntegerOutOfRange,
            8 to TvmIntegerOverflow,
            9 to TvmIntegerOverflow,
            10 to TvmIntegerOutOfRange,
            11 to TvmIntegerOverflow,
            12 to TvmIntegerOverflow,
            13 to TvmIntegerOutOfRange,
            14 to TvmIntegerOutOfRange,
        )

        compareActualAndExpectedMethodResults(expectedMethodFailures, methodStates)
    }
}