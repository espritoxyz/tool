package org.ton.examples

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.BvUtils.toBigIntegerSigned
import io.ksmt.utils.toBigInteger
import org.ton.bytecode.TvmIntegerType
import org.ton.bytecode.TvmMethod
import org.usvm.UBvSort
import org.usvm.UExpr
import org.usvm.machine.state.TvmMethodResult
import org.usvm.machine.state.TvmStack
import org.usvm.machine.state.TvmState
import kotlin.test.assertEquals

internal fun TvmStack.loadIntegers(n: Int) = List(n) {
    takeLast(TvmIntegerType) { error("Impossible") }.intValue.intValue()
}.reversed()

internal fun TvmStack.evalBigIntegers(n: Int, state: TvmState) = List(n) {
    takeLast(TvmIntegerType) { error("Impossible") }.intValue
        .let { state.models.first().eval(it) }.bigIntegerValue()
}.reversed()

internal fun UExpr<UBvSort>.intValue() = (this as KBitVecValue<*>).toBigIntegerSigned().toInt()

internal fun UExpr<UBvSort>.bigIntegerValue() =
    (this as? KBitVecValue<*>)?.toBigIntegerSigned() ?: error("Unexpected expr $this")

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

internal fun compareActualEvaluatedAndExpectedStack(
    expectedMethodStackValues: Map<Int, List<Number>>,
    methodStates: Map<TvmMethod, List<TvmState>>
) {
    assertEquals(expectedMethodStackValues.keys, methodStates.keys.mapTo(hashSetOf()) { it.id })

    for ((method, states) in methodStates) {
        val state = states.single()

        val expectedStackState = expectedMethodStackValues.getValue(method.id).map { it.toBigInteger() }
        val actualStackState = state.stack.evalBigIntegers(expectedStackState.size, state)
        assertEquals(expectedStackState, actualStackState, "Method id: ${method.id}")
    }
}

internal fun compareActualAndExpectedMethodResults(
    expectedMethodResults: Map<Int, TvmMethodResult>,
    methodStates: Map<TvmMethod, List<TvmState>>
) {
    assertEquals(expectedMethodResults.keys, methodStates.keys.mapTo(hashSetOf()) { it.id })

    methodStates.forEach { (method, states) ->
        val state = states.first()
        val result = state.methodResult
        val expectedResult = expectedMethodResults[method.id]

        assertEquals(expectedResult, result)
    }
}