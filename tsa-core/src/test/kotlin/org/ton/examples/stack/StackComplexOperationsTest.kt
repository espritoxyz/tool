package org.ton.examples.stack

import org.ton.bytecode.TvmIntegerType
import org.ton.examples.compareActualAndExpectedStack
import org.ton.examples.intValue
import org.ton.examples.loadIntegers
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import org.usvm.machine.compileAndAnalyzeFift
import org.usvm.machine.state.TvmStack
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class StackComplexOperationsTest {
    private val ctx = TvmContext(TvmComponents())

    private val stackComplexFiftPath: String = "/stack/StackComplex.fif"
    private val stackNullChecksFiftPath: String = "/stack/NullChecks.fif"

    @Test
    fun testStackReverse(): Unit = with(ctx) {
        val stack = TvmStack(ctx)

        val originalOrder = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val reversed53Order = listOf(0, 1, 2, 7, 6, 5, 4, 3, 8, 9, 10)

        originalOrder.forEach { stack.add(it.toBv257(), TvmIntegerType) }

        stack.reverse(5, 3)

        val stackState = stack.loadIntegers(originalOrder.size)
        assertEquals(reversed53Order, stackState)
    }

    @Test
    fun testStackNullChecks(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(stackNullChecksFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $stackNullChecksFiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val expectedMethodStackValues = mapOf(
            0 to listOf(6, null, 7, -1),
            1 to listOf(6, 7, 0),
            2 to listOf(6, null, null, 7, -1),
            3 to listOf(6, 7, 0),
            4 to listOf(6, 7, null, -1),
            5 to listOf(6, 7, 0),
            6 to listOf(6, 7, null, null, -1),
            7 to listOf(6, 7, 0),

            8 to listOf(6, null, 7, 0),
            9 to listOf(6, 7, -1),
            10 to listOf(6, null, null, 7, 0),
            11 to listOf(6, 7, -1),
            12 to listOf(6, 7, null, 0),
            13 to listOf(6, 7, -1),
            14 to listOf(6, 7, null, null, 0),
            15 to listOf(6, 7, -1),
        )

        assertEquals(expectedMethodStackValues.keys, methodStates.keys.mapTo(hashSetOf()) { it.id })

        for ((method, states) in methodStates) {
            val state = states.single()

            val expectedStackState = expectedMethodStackValues.getValue(method.id)
            val actualStackState = state.stack.loadIntegersAndNulls(expectedStackState.size)
            assertEquals(expectedStackState, actualStackState, "Method id: ${method.id}")
        }
    }

    @Test
    fun testStackComplexFift(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(stackComplexFiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $stackComplexFiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val expectedMethodStackValues = mapOf(
            0 to listOf(0, 1, 2, 7, 6, 5, 4, 3, 8, 9, 10),
            1 to listOf(0, 1, 2, 8, 9, 10, 3, 4, 5, 6, 7),
            2 to listOf(0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 8),
            3 to listOf(0, 4, 5),
            4 to listOf(0, 1, 2),
            5 to listOf(0, 4, 5, 1, 2, 3),
            6 to listOf(0, 1, 2, 3, 4, 5, 3, 4, 5),
            7 to listOf(0),
            8 to listOf(0, 1),
            9 to listOf(0, 1, 2, 1, 2),
            10 to listOf(0, 1, 2, 1, 1),
            11 to listOf(0, 1, 2, 0, 1, 2),
            12 to listOf(2, 3, 0, 1),
            13 to listOf(0, 1, 2, 3, 0, 1),
            14 to listOf(0, 3, 4, 1, 2),
            15 to listOf(0, 1, 2, 4, 3, 1),
            16 to listOf(0, 1, 2, 4, 3, 4),
            17 to listOf(0, 1, 4, 2, 3),
            18 to listOf(0, 1, 2, 3, 4, 3),
            19 to listOf(0, 5, 2, 3, 4, 2, 1),
            20 to listOf(0, 1, 4, 3, 2, 5),
            21 to listOf(0, 1, 3, 4, 5, 2),
            22 to listOf(0, 1, 2, 5, 3, 4),
            23 to listOf(0, 4, 2, 3, 1),
            24 to listOf(0, 1, 5, 3, 4, 4, 3, 2),
            25 to listOf(0, 1, 4, 5, 4, 3, 2),
            26 to listOf(0, 1, 2, 5, 4, 4, 3, 2),
            27 to listOf(0, 1, 2, 5, 4, 3, 2),
            28 to listOf(3, 5, 4, 0, 2, 1),
            29 to listOf(5, 1, 2, 3, 4, 0, 2, 1),
            30 to listOf(4, 5, 2, 3, 0, 2, 1),
        )

        compareActualAndExpectedStack(expectedMethodStackValues, methodStates)
    }

    private fun TvmStack.loadIntegersAndNulls(n: Int) = List(n) {
        if (lastIsNull()) {
            pop(0)
            null
        } else {
            takeLast(TvmIntegerType) { error("Impossible") }.intValue.intValue()
        }
    }.reversed()

}
