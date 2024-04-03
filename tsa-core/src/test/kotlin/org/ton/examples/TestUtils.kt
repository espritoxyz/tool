package org.ton.examples

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.BvUtils.toBigIntegerSigned
import org.ton.bytecode.TvmIntegerType
import org.ton.bytecode.TvmMethod
import org.usvm.UBvSort
import org.usvm.UExpr
import org.usvm.machine.FiftInterpreterResult
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

internal fun TvmStack.evalBigIntegersAndNulls(n: Int, state: TvmState) = List(n) {
    if (lastIsNull()) {
        pop(0)
        return@List null
    }

    takeLast(TvmIntegerType) { error("Impossible") }.intValue
        .let { state.models.first().eval(it) }.bigIntegerValue()
}.reversed()

internal fun TvmState.executionCode(): Int = when (val res = methodResult) {
    TvmMethodResult.NoCall -> error("Unexpected method result: $res")
    is TvmMethodResult.TvmFailure -> res.exitCode.toInt()
    is TvmMethodResult.TvmSuccess -> 0
}

internal fun TvmState.gasUsageValue(): Int {
    val model = models.first()
    return gasUsage.sumOf { model.eval(it).intValue() }
}

internal fun UExpr<out UBvSort>.intValue() = (this as KBitVecValue<*>).toBigIntegerSigned().toInt()

internal fun UExpr<out UBvSort>.bigIntegerValue() =
    (this as? KBitVecValue<*>)?.toBigIntegerSigned() ?: error("Unexpected expr $this")

internal fun compareMethodStateStackNumbersAndNulls(
    methodIds: Set<Int>,
    methodStates: Map<TvmMethod, List<TvmState>>,
    expectedState: (TvmMethod) -> FiftInterpreterResult,
) = compareMethodStateStack(methodIds, methodStates, expectedState,
    stateStack = { state, expectedStackSize ->
        state.stack.evalBigIntegersAndNulls(expectedStackSize, state)
    },
    concreteStack = { state ->
        state.stack.map { if (it == "(null)") null else it.toBigInteger() }
    }
)

internal fun compareMethodStateStackNumbers(
    methodIds: Set<Int>,
    methodStates: Map<TvmMethod, List<TvmState>>,
    expectedState: (TvmMethod) -> FiftInterpreterResult,
) = compareMethodStateStack(methodIds, methodStates, expectedState,
    stateStack = { state, expectedStackSize ->
        state.stack.evalBigIntegers(expectedStackSize, state)
    },
    concreteStack = { state ->
        state.stack.map { it.toBigInteger() }
    }
)

internal fun compareMethodStateResult(
    methodIds: Set<Int>,
    methodStates: Map<TvmMethod, List<TvmState>>,
    expectedState: (TvmMethod) -> FiftInterpreterResult,
) = compareMethodStates(methodIds, methodStates, expectedState) { method, actualState, concreteState ->
    val actualStatus = actualState.executionCode()
    assertEquals(concreteState.exitCode, actualStatus, "Method id: ${method.id}")
}

internal fun <T> compareMethodStateStack(
    methodIds: Set<Int>,
    methodStates: Map<TvmMethod, List<TvmState>>,
    expectedState: (TvmMethod) -> FiftInterpreterResult,
    stateStack: (TvmState, Int) -> List<T>,
    concreteStack: (FiftInterpreterResult) -> List<T>,
) = compareMethodStates(methodIds, methodStates, expectedState) { method, actualState, concreteState ->
    val concreteStackValue = concreteStack(concreteState)
    val actualStack = stateStack(actualState, concreteStackValue.size)
    assertEquals(concreteStackValue, actualStack, "Method id: ${method.id}")
}

internal fun compareMethodStates(
    methodIds: Set<Int>,
    methodStates: Map<TvmMethod, List<TvmState>>,
    expectedState: (TvmMethod) -> FiftInterpreterResult,
    comparison: (TvmMethod, TvmState, FiftInterpreterResult) -> Unit
) {
    assertEquals(methodIds, methodStates.keys.mapTo(hashSetOf()) { it.id })

    for ((method, states) in methodStates) {
        val state = states.single()
        val concreteState = expectedState(method)
        comparison(method, state, concreteState)
    }
}
