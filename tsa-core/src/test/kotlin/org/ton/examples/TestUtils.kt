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

internal fun TvmStack.loadBigIntegers(n: Int) = List(n) {
    takeLast(TvmIntegerType) { error("Impossible") }.intValue.bigIntegerValue()
}.reversed()

internal fun TvmStack.evalBigIntegers(n: Int, state: TvmState) = List(n) {
    takeLast(TvmIntegerType) { error("Impossible") }.intValue
        .let { state.models.first().eval(it) }.bigIntegerValue()
}.reversed()

internal fun UExpr<UBvSort>.intValue() = (this as? KBitVecValue<*>)?.toBigIntegerSigned()?.toInt()
    ?: error("Could not cast $this to concrete integer")

internal fun UExpr<UBvSort>.bigIntegerValue() =
    (this as? KBitVecValue<*>)?.toBigIntegerSigned() ?: error("Unexpected expr $this")

internal fun <GivenTestInfo, ExtractedInfo> compareActualAndExpectedStack(
    expectedMethodStackValues: Map<Int, GivenTestInfo>,
    methodStates: Map<TvmMethod, List<TvmState>>,
    extractExpected: (GivenTestInfo) -> ExtractedInfo,
    extractActual: (TvmState, /* expected */ ExtractedInfo) -> ExtractedInfo
) {
    assertEquals(expectedMethodStackValues.keys, methodStates.keys.mapTo(hashSetOf()) { it.id })

    for ((method, states) in methodStates) {
        val state = states.single()

        val expectedStackState = extractExpected(expectedMethodStackValues.getValue(method.id))
        val actualStackState = extractActual(state, expectedStackState)
        assertEquals(expectedStackState, actualStackState, "Method id: ${method.id}")
    }
}

internal fun compareActualAndExpectedStack(
    expectedMethodStackValues: Map<Int, List<Number>>,
    methodStates: Map<TvmMethod, List<TvmState>>
) {
    compareActualAndExpectedStack(
        expectedMethodStackValues,
        methodStates,
        extractExpected = { it.map { value -> value.toBigInteger() } },
        extractActual = { state, exp -> state.stack.loadBigIntegers(exp.size) }
    )
}

internal fun compareActualEvaluatedAndExpectedStack(
    expectedMethodStackValues: Map<Int, List<Number>>,
    methodStates: Map<TvmMethod, List<TvmState>>
) {
    compareActualAndExpectedStack(
        expectedMethodStackValues,
        methodStates,
        extractExpected = { it.map { value -> value.toBigInteger() } },
        extractActual = { state, exp -> state.stack.evalBigIntegers(exp.size, state) }
    )
}

internal fun compareActualAndExpectedMethodResults(
    expectedMethodResults: Map<Int, TvmMethodResult>,
    methodStates: Map<TvmMethod, List<TvmState>>
) {
    compareActualAndExpectedStack(
        expectedMethodResults,
        methodStates,
        extractExpected = { it },
        extractActual = { state, _ -> state.methodResult }
    )
}