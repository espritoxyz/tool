package org.usvm.machine.state

import io.ksmt.utils.powerOfTwo
import org.ton.bytecode.TvmBuilderType
import org.ton.bytecode.TvmCellType
import org.ton.bytecode.TvmContinuationValue
import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmIntegerType
import org.ton.bytecode.TvmSliceType
import org.ton.bytecode.TvmTupleType
import org.usvm.UBoolExpr
import org.usvm.UBvSort
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.api.writeField
import org.usvm.machine.TvmContext
import org.usvm.machine.interpreter.TvmStepScope
import org.usvm.mkSizeExpr
import org.usvm.sizeSort
import java.math.BigInteger

val TvmState.lastStmt get() = pathNode.statement
fun TvmState.newStmt(stmt: TvmInst) {
    pathNode += stmt
}

fun TvmInst.nextStmt(): TvmInst = location.codeBlock.instList.getOrNull(location.index + 1)
    ?: error("Unexpected end of the code block ${location.codeBlock}")

fun setFailure(failure: TvmMethodResult.TvmFailure): (TvmState) -> Unit = { state ->
    state.consumeGas(IMPLICIT_EXCEPTION_THROW_GAS)
    state.methodResult = failure
    state.stack.add(state.ctx.zeroValue, TvmIntegerType) // Push default zero parameter to the stack
}

// TODO support RETALT
fun TvmState.returnFromMethod() {
    val returnFromMethod = callStack.lastMethod()
    // TODO: think about it later
    val returnSite = callStack.pop()

    // TODO do we need it?
//    if (callStack.isNotEmpty()) {
//        memory.stack.pop()
//    }

    methodResult = TvmMethodResult.TvmSuccess(returnFromMethod, stack)

    if (returnSite != null) {
        currentContinuation = TvmContinuationValue(returnFromMethod, stack, registers)
        newStmt(returnSite)
    }
}

fun <R> TvmStepScope.calcOnStateCtx(block: context(TvmContext) TvmState.() -> R): R = calcOnState {
    block(ctx, this)
}

fun TvmStepScope.doWithStateCtx(block: context(TvmContext) TvmState.() -> Unit) = doWithState {
    block(ctx, this)
}

fun TvmState.generateSymbolicCell(): UHeapRef = generateSymbolicRef(TvmCellType)

fun TvmState.ensureSymbolicCellInitialized(ref: UHeapRef) =
    ensureSymbolicRefInitialized(ref, TvmCellType)

fun TvmState.generateSymbolicSlice(): UHeapRef =
    generateSymbolicRef(TvmSliceType).also { initializeSymbolicSlice(it) }

fun TvmState.ensureSymbolicSliceInitialized(ref: UHeapRef) =
    ensureSymbolicRefInitialized(ref, TvmSliceType) { initializeSymbolicSlice(it) }

fun TvmState.initializeSymbolicSlice(ref: UConcreteHeapRef) = with(ctx) {
    // TODO hack! Assume that all input slices were not read, that means dataPos == 0 and refsPos == 0
    memory.writeField(ref, TvmContext.sliceDataPosField, sizeSort, mkSizeExpr(0), guard = trueExpr)
    memory.writeField(ref, TvmContext.sliceRefPosField, sizeSort, mkSizeExpr(0), guard = trueExpr)

    // Cell in input slices must be represented with static refs to be correctly processed in TvmCellRefsRegion
    val cell = generateSymbolicCell()
    memory.writeField(ref, TvmContext.sliceCellField, addressSort, cell, guard = trueExpr)
}

fun TvmState.generateSymbolicBuilder(): UHeapRef =
    generateSymbolicRef(TvmBuilderType).also { initializeSymbolicBuilder(it) }

fun TvmState.ensureSymbolicBuilderInitialized(ref: UHeapRef) =
    ensureSymbolicRefInitialized(ref, TvmBuilderType) { initializeSymbolicBuilder(it) }

fun TvmState.initializeSymbolicBuilder(ref: UConcreteHeapRef) = with(ctx) {
//    // TODO hack! Assume that all input builder were not written, that means dataLength == 0 and refsLength == 0
//    memory.writeField(ref, TvmContext.cellDataLengthField, sizeSort, mkSizeExpr(0), guard = trueExpr)
//    memory.writeField(ref, TvmContext.cellRefsLengthField, sizeSort, mkSizeExpr(0), guard = trueExpr)
}

fun TvmState.generateSymbolicTuple(): UHeapRef = generateSymbolicRef(TvmTupleType)

fun TvmState.ensureSymbolicTupleInitialized(ref: UHeapRef) =
    ensureSymbolicRefInitialized(ref, TvmTupleType)

fun TvmStepScope.assertIfSat(
    constraint: UBoolExpr
): Boolean {
    val originalState = calcOnState { this }
    val (stateWithConstraint) = originalState.ctx.statesForkProvider.forkMulti(originalState, listOf(constraint))
    return stateWithConstraint != null
}

fun TvmContext.signedIntegerFitsBits(value: UExpr<UBvSort>, bits: UInt): UBoolExpr =
    when {
        bits == 0u -> value eq zeroValue
        bits >= TvmContext.INT_BITS -> trueExpr
        else -> mkAnd(
            mkBvSignedLessOrEqualExpr(value, powerOfTwo(bits - 1u).minus(BigInteger.ONE).toBv257()),
            mkBvSignedGreaterOrEqualExpr(value, powerOfTwo(bits - 1u).negate().toBv257()),
        )
    }

/**
 * Since TVM integers have a signed representation only, every non-negative integer fits in 256 bits
 */
fun TvmContext.unsignedIntegerFitsBits(value: UExpr<UBvSort>, bits: UInt): UBoolExpr =
    when {
        bits == 0u -> value eq zeroValue
        bits >= TvmContext.INT_BITS - 1u -> mkBvSignedGreaterOrEqualExpr(value, zeroValue)
        else -> mkAnd(
            mkBvSignedLessOrEqualExpr(value, powerOfTwo(bits).minus(BigInteger.ONE).toBv257()),
            mkBvSignedGreaterOrEqualExpr(value, zeroValue),
        )
    }

/**
 * 0 <= [sizeBits] <= 257
 */
fun TvmContext.signedIntegerFitsBits(value: UExpr<UBvSort>, bits: UExpr<UBvSort>): UBoolExpr =
    mkAnd(
        mkBvSignedLessOrEqualExpr(bvMinValueSignedExtended(bits), value),
        mkBvSignedLessOrEqualExpr(value, bvMaxValueSignedExtended(bits)),
    )


/**
 * 0 <= [sizeBits] <= 256
 *
 * @see unsignedIntegerFitsBits
 */
fun TvmContext.unsignedIntegerFitsBits(value: UExpr<UBvSort>, bits: UExpr<UBvSort>): UBoolExpr =
    mkAnd(
        mkBvSignedLessOrEqualExpr(zeroValue, value),
        mkBvSignedLessOrEqualExpr(value, bvMaxValueUnsignedExtended(bits)),
    )


/**
 * 0 <= [sizeBits] <= 257
 */
fun TvmContext.bvMinValueSignedExtended(sizeBits: UExpr<UBvSort>): UExpr<UBvSort> =
    mkIte(
        condition = sizeBits eq zeroValue,
        trueBranch = zeroValue,
        falseBranch = mkBvNegationExpr(mkBvShiftLeftExpr(oneValue, mkBvSubExpr(sizeBits, oneValue)))
    )


/**
 * 0 <= [sizeBits] <= 257
 */
fun TvmContext.bvMaxValueSignedExtended(sizeBits: UExpr<UBvSort>): UExpr<UBvSort> =
    mkIte(
        condition = sizeBits eq zeroValue,
        trueBranch = zeroValue,
        falseBranch = mkBvSubExpr(mkBvShiftLeftExpr(oneValue, mkBvSubExpr(sizeBits, oneValue)), oneValue)
    )

/**
 * 0 <= [sizeBits] <= 256
 *
 * @see unsignedIntegerFitsBits
 */
fun TvmContext.bvMaxValueUnsignedExtended(sizeBits: UExpr<UBvSort>): UExpr<UBvSort> =
    mkBvSubExpr(mkBvShiftLeftExpr(oneValue, sizeBits), oneValue)
