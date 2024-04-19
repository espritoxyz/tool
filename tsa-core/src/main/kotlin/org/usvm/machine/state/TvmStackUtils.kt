package org.usvm.machine.state

import org.ton.bytecode.TvmBuilderType
import org.ton.bytecode.TvmCellType
import org.ton.bytecode.TvmContinuationType
import org.ton.bytecode.TvmContinuationValue
import org.ton.bytecode.TvmIntegerType
import org.ton.bytecode.TvmReferenceType
import org.ton.bytecode.TvmSliceType
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.machine.TvmContext.TvmInt257Sort
import org.usvm.machine.interpreter.TvmStepScope
import org.usvm.machine.state.TvmStack.TvmConcreteStackEntry
import org.usvm.machine.state.TvmStack.TvmInputStackEntry
import org.usvm.machine.state.TvmStack.TvmStackTupleValue
import org.usvm.machine.state.TvmStack.TvmStackValue

fun TvmStackValue.toStackEntry(): TvmConcreteStackEntry = TvmConcreteStackEntry(this)

fun TvmStack.takeLastInt(): UExpr<TvmInt257Sort> {
    val intStackValue = takeLast(TvmIntegerType) { id ->
        ctx.mkRegisterReading(id, ctx.int257sort)
    }

    return intStackValue.intValue
}

context(TvmState)
fun TvmStack.takeLastCell(): UHeapRef? =
    takeLastRef(this, TvmCellType, TvmStackValue::cellValue) {
        generateSymbolicCell()
    }?.also { ensureSymbolicCellInitialized(it) }

context(TvmState)
fun TvmStack.takeLastSlice(): UHeapRef? =
    takeLastRef(this, TvmSliceType, TvmStackValue::sliceValue) {
        generateSymbolicSlice()
    }?.also { ensureSymbolicSliceInitialized(it) }

context(TvmState)
fun TvmStack.takeLastBuilder(): UHeapRef? =
    takeLastRef(this, TvmBuilderType, TvmStackValue::builderValue) {
        generateSymbolicBuilder()
    }?.also { ensureSymbolicBuilderInitialized(it) }

fun TvmStepScope.takeLastTuple(): TvmStackTupleValue? = calcOnStateCtx {
    val lastEntry = stack.takeLastEntry()

    when (lastEntry) {
        is TvmConcreteStackEntry -> lastEntry.cell as? TvmStackTupleValue
        is TvmInputStackEntry -> {
            val cell = lastEntry.cell
            if (cell != null) {
                return@calcOnStateCtx cell as? TvmStackTupleValue
            }

            val size = ctx.mkRegisterReading(lastEntry.id, ctx.int257sort)
            val sizeConstraint = mkAnd(
                mkBvSignedLessOrEqualExpr(zeroValue, size),
                mkBvSignedLessOrEqualExpr(size, maxTupleSizeValue)
            )
            assert(sizeConstraint)
                ?: error("Cannot assert tuple size constraints")

            val symbolicTuple = TvmStack.TvmStackTupleValueInputNew(entries = mutableMapOf(), size = size)
            symbolicTuple.also { lastEntry.cell = it }
        }
    }
}

fun TvmStack.takeLastContinuation(): TvmContinuationValue {
    val continuationStackValue = takeLast(TvmContinuationType) { _ ->
        error("Unexpected continuation as an input")
    }

    return continuationStackValue.continuationValue
}

context(TvmState)
private fun takeLastRef(
    stack: TvmStack,
    referenceType: TvmReferenceType,
    extractValue: TvmStackValue.() -> UHeapRef?,
    generateSymbolicRef: (Int) -> UHeapRef
): UHeapRef? {
    val lastRefValue = stack.takeLast(referenceType, generateSymbolicRef)
    return lastRefValue.extractValue()
}

fun doXchg(scope: TvmStepScope, first: Int, second: Int) {
    scope.doWithState {
        stack.swap(first, second)
    }
}

fun doSwap(scope: TvmStepScope) = doXchg(scope, first = 0, second = 1)

fun doPop(scope: TvmStepScope, i: Int) {
    scope.doWithState {
        stack.pop(i)
    }
}

fun doPush(scope: TvmStepScope, i: Int) {
    scope.doWithState {
        stack.push(i)
    }
}

fun TvmStack.doBlkSwap(i: Int, j: Int) {
    reverse(i + 1, j + 1)
    reverse(j + 1, 0)
    reverse(i + j + 2, 0)
}

fun TvmStack.doPuxc(i: Int, j: Int) {
    push(i)
    swap(0, 1)
    swap(0, j + 1)
}

fun TvmStack.doXchg2(i: Int, j: Int) {
    swap(1, i)
    swap(0, j)
}

fun TvmStack.doXchg3(i: Int, j: Int, k: Int) {
    swap(2, i)
    swap(1, j)
    swap(0, k)
}
