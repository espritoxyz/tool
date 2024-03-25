package org.ton.examples.stack

import org.ton.bytecode.TvmIntegerType
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