package org.ton.examples.stack

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.BvUtils.toBigIntegerSigned
import org.ton.bytecode.TvmIntegerType
import org.ton.bytecode.TvmMethod
import org.usvm.UBvSort
import org.usvm.UExpr
import org.usvm.machine.state.TvmStack
import org.usvm.machine.state.TvmState
import kotlin.test.assertEquals

internal fun TvmStack.loadIntegers(n: Int) = List(n) {
    takeLast(TvmIntegerType) { error("Impossible") }.intValue.intValue()
}.reversed()

internal fun UExpr<UBvSort>.intValue() = (this as KBitVecValue<*>).toBigIntegerSigned().toInt()

internal fun compareActualAndExpectedStack(
    expectedMethodStackValues: Map<Int, List<Int>>,
    methodStates: Map<TvmMethod, List<TvmState>>
) {
    assertEquals(expectedMethodStackValues.keys, methodStates.keys.mapTo(hashSetOf()) { it.id })

    for ((method, states) in methodStates) {
        val state = states.single()

        val expectedStackState = expectedMethodStackValues.getValue(method.id)
        val actualStackState = state.stack.loadIntegers(expectedStackState.size)
        assertEquals(expectedStackState, actualStackState, "Method id: ${method.id}")
    }
}