package org.usvm.machine.state

import io.ksmt.utils.powerOfTwo
import org.ton.bytecode.TvmInst
import org.usvm.NULL_ADDRESS
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.api.readField
import org.usvm.api.writeField
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.TvmInt257Sort
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.TvmStepScope
import org.usvm.machine.types.TvmBuilderType
import org.usvm.machine.types.TvmCellType
import org.usvm.machine.types.TvmDataCellType
import org.usvm.machine.types.TvmNullType
import org.usvm.machine.types.TvmSliceType
import org.usvm.machine.types.TvmType
import org.usvm.memory.GuardedExpr
import org.usvm.memory.foldHeapRef
import org.usvm.mkSizeAddExpr
import org.usvm.mkSizeExpr
import org.usvm.mkSizeGeExpr
import org.usvm.mkSizeLeExpr
import org.usvm.sizeSort
import org.usvm.types.USingleTypeStream
import java.math.BigInteger

val TvmState.lastStmt get() = pathNode.statement
fun TvmState.newStmt(stmt: TvmInst) {
    pathNode += stmt
}

fun TvmInst.nextStmt(): TvmInst = location.codeBlock.instList.getOrNull(location.index + 1)
    ?: error("Unexpected end of the code block ${location.codeBlock}")

fun setFailure(failure: TvmMethodResult.TvmExit, level: TvmFailureType = TvmFailureType.UnknownError): (TvmState) -> Unit = { state ->
    state.consumeGas(IMPLICIT_EXCEPTION_THROW_GAS)
    state.methodResult = TvmMethodResult.TvmFailure(failure, level)

    // Throwing exception clears the current stack and pushes its parameter and exit code
    state.stack.clear()
    // TODO push the real parameter, not always 0
    state.stack.addInt(state.ctx.zeroValue)
    with(state.ctx) {
        state.stack.addInt(failure.exitCode.toInt().toBv257())
    }
}

val throwTypeCheckError: (TvmState) -> Unit = setFailure(TvmTypeCheckError)
val throwIntegerOverflowError: (TvmState) -> Unit = setFailure(TvmIntegerOverflowError)
val throwIntegerOutOfRangeError: (TvmState) -> Unit = setFailure(TvmIntegerOutOfRangeError)
val throwCellOverflowError: (TvmState) -> Unit = setFailure(TvmCellOverflowError)
val throwUnknownCellUnderflowError: (TvmState) -> Unit = setFailure(TvmCellUnderflowError, TvmFailureType.UnknownError)
val throwStructuralCellUnderflowError: (TvmState) -> Unit =
    setFailure(TvmCellUnderflowError, TvmFailureType.FixedStructuralError)
val throwSymbolicStructuralCellUnderflowError: (TvmState) -> Unit =
    setFailure(TvmCellUnderflowError, TvmFailureType.SymbolicStructuralError)
val throwRealCellUnderflowError: (TvmState) -> Unit = setFailure(TvmCellUnderflowError, TvmFailureType.RealError)

fun <R> TvmStepScope.calcOnStateCtx(block: context(TvmContext) TvmState.() -> R): R = calcOnState {
    block(ctx, this)
}

fun TvmStepScope.doWithStateCtx(block: context(TvmContext) TvmState.() -> Unit) = doWithState {
    block(ctx, this)
}

fun TvmState.generateSymbolicCell(): UHeapRef = generateSymbolicRef(TvmCellType).also { initializeSymbolicCell(it) }

fun TvmState.ensureSymbolicCellInitialized(ref: UHeapRef) =
    ensureSymbolicRefInitialized(ref, TvmCellType) { initializeSymbolicCell(it) }

fun TvmState.generateSymbolicSlice(): UHeapRef =
    generateSymbolicRef(TvmSliceType).also { initializeSymbolicSlice(it) }

fun TvmState.ensureSymbolicSliceInitialized(ref: UHeapRef) =
    ensureSymbolicRefInitialized(ref, TvmSliceType) { initializeSymbolicSlice(it) }

fun TvmState.initializeSymbolicCell(cell: UConcreteHeapRef) = with(ctx) {
    val dataLength = memory.readField(cell, TvmContext.cellDataLengthField, sizeSort)
    val refsLength = memory.readField(cell, TvmContext.cellRefsLengthField, sizeSort)

    // We can add these constraints manually to path constraints because default values (0) in models are valid
    // for these fields

    pathConstraints += mkSizeLeExpr(dataLength, maxDataLengthSizeExpr)
    pathConstraints += mkSizeGeExpr(dataLength, zeroSizeExpr)

    pathConstraints += mkSizeLeExpr(refsLength, maxRefsLengthSizeExpr)
    pathConstraints += mkSizeGeExpr(refsLength, zeroSizeExpr)
}

fun TvmState.initializeSymbolicSlice(ref: UConcreteHeapRef) = with(ctx) {
    // TODO hack! Assume that all input slices were not read, that means dataPos == 0 and refsPos == 0
    memory.writeField(ref, TvmContext.sliceDataPosField, sizeSort, mkSizeExpr(0), guard = trueExpr)
    memory.writeField(ref, TvmContext.sliceRefPosField, sizeSort, mkSizeExpr(0), guard = trueExpr)

    // Cell in input slices must be represented with static refs to be correctly processed in TvmCellRefsRegion
    val cell = generateSymbolicCell()
    memory.writeField(ref, TvmContext.sliceCellField, addressSort, cell, guard = trueExpr)
    assertType(cell, TvmDataCellType)
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

fun TvmStepScope.assertIfSat(
    constraint: UBoolExpr
): Boolean {
    val originalState = calcOnState { this }
    val (stateWithConstraint) = originalState.ctx.statesForkProvider.forkMulti(originalState, listOf(constraint))
    return stateWithConstraint != null
}

fun TvmContext.signedIntegerFitsBits(value: UExpr<TvmInt257Sort>, bits: UInt): UBoolExpr =
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
fun TvmContext.unsignedIntegerFitsBits(value: UExpr<TvmInt257Sort>, bits: UInt): UBoolExpr =
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
fun TvmContext.signedIntegerFitsBits(value: UExpr<TvmInt257Sort>, bits: UExpr<TvmInt257Sort>): UBoolExpr =
    mkAnd(
        mkBvSignedLessOrEqualExpr(bvMinValueSignedExtended(bits), value),
        mkBvSignedLessOrEqualExpr(value, bvMaxValueSignedExtended(bits)),
    )


/**
 * 0 <= [sizeBits] <= 256
 *
 * @see unsignedIntegerFitsBits
 */
fun TvmContext.unsignedIntegerFitsBits(value: UExpr<TvmInt257Sort>, bits: UExpr<TvmInt257Sort>): UBoolExpr =
    mkAnd(
        mkBvSignedLessOrEqualExpr(zeroValue, value),
        mkBvSignedLessOrEqualExpr(value, bvMaxValueUnsignedExtended(bits)),
    )


/**
 * 0 <= [sizeBits] <= 257
 */
fun TvmContext.bvMinValueSignedExtended(sizeBits: UExpr<TvmInt257Sort>): UExpr<TvmInt257Sort> =
    mkIte(
        condition = sizeBits eq zeroValue,
        trueBranch = zeroValue,
        falseBranch = mkBvNegationExpr(mkBvShiftLeftExpr(oneValue, mkBvSubExpr(sizeBits, oneValue)))
    )


/**
 * 0 <= [sizeBits] <= 257
 */
fun TvmContext.bvMaxValueSignedExtended(sizeBits: UExpr<TvmInt257Sort>): UExpr<TvmInt257Sort> =
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
fun TvmContext.bvMaxValueUnsignedExtended(sizeBits: UExpr<TvmInt257Sort>): UExpr<TvmInt257Sort> =
    mkBvSubExpr(mkBvShiftLeftExpr(oneValue, sizeBits), oneValue)

fun TvmState.calcConsumedGas(): UExpr<TvmSizeSort> =
    gasUsage.fold(ctx.zeroSizeExpr) { acc, value -> ctx.mkSizeAddExpr(acc, value) }


fun TvmState.assertType(value: UHeapRef, type: TvmType) {
    if (value is UConcreteHeapRef && value.address == NULL_ADDRESS) {
        require(type is TvmNullType)
        return
    }
    val refHandler = { acc: MutableList<Pair<TvmType, UConcreteHeapRef>>, ref: GuardedExpr<UConcreteHeapRef> ->
        val cur = memory.types.getTypeStream(ref.expr)
        require(cur is USingleTypeStream)
        acc += (cur.commonSuperType to ref.expr)
        acc
    }
    val refOldTypes = foldHeapRef(
        ref = value,
        initial = mutableListOf(),
        initialGuard = ctx.trueExpr,
        collapseHeapRefs = false,
        staticIsConcrete = true,
        blockOnConcrete = refHandler,
        blockOnSymbolic =  { _, ref -> error("Unexpected symbolic ref ${ref.expr}") }
    )
    refOldTypes.forEach { (oldType, ref) ->
        if (typeSystem.isSupertype(oldType, type)) {
            memory.types.allocate(ref.address, type)
        } else if (!typeSystem.isSupertype(type, oldType)) {
            throw TypeCastException(oldType, type)
        }
    }
}
