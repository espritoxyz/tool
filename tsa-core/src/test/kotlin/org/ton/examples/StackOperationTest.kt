package org.ton.examples

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.BvUtils.toBigIntegerSigned
import org.ton.bytecode.TvmIntegerType
import org.usvm.UBvSort
import org.usvm.UExpr
import org.usvm.machine.TvmComponents
import org.usvm.machine.TvmContext
import org.usvm.machine.compileAndAnalyzeFift
import org.usvm.machine.state.TvmStack
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class StackOperationTest {
    private val ctx = TvmContext(TvmComponents())

    private val fiftPath: String = "/stack/StackReverse.fif"

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
    fun testStackReverseFift(): Unit = with(ctx) {
        val fiftResourcePath = this::class.java.getResource(fiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $fiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val expectedMethodStackValues = mapOf(
            0 to listOf(0, 1, 2, 7, 6, 5, 4, 3, 8, 9, 10),
            1 to listOf(0, 1, 2, 8, 9, 10, 3, 4, 5, 6, 7),
            2 to listOf(0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 8),
        )

        assertEquals(expectedMethodStackValues.keys, methodStates.keys.mapTo(hashSetOf()) { it.id })

        for ((method, states) in methodStates) {
            val state = states.single()

            val expectedStackState = expectedMethodStackValues.getValue(method.id)
            val actualStackState = state.stack.loadIntegers(expectedStackState.size)
            assertEquals(expectedStackState, actualStackState, "Method id: ${method.id}")
        }
    }

    private fun TvmStack.loadIntegers(n: Int) = List(n) {
        takeLast(TvmIntegerType) { error("Impossible") }.intValue.intValue()
    }.reversed()

    private fun UExpr<UBvSort>.intValue() = (this as KBitVecValue<*>).toBigIntegerSigned().toInt()
}
