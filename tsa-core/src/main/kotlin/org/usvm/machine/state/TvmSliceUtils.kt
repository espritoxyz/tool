package org.usvm.machine.state

import io.ksmt.KContext
import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.uncheckedCast
import io.ksmt.expr.KInterpretedValue
import io.ksmt.sort.KBvSort
import org.ton.bytecode.TvmCell
import org.ton.bytecode.TvmSubSliceSerializedLoader
import org.usvm.machine.types.TvmCellType
import org.usvm.machine.types.TvmSliceType
import org.usvm.UBoolExpr
import org.usvm.UBvSort
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.UIteExpr
import org.usvm.USort
import org.usvm.api.makeSymbolicPrimitive
import org.usvm.api.readField
import org.usvm.api.writeField
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.Companion.ADDRESS_BITS
import org.usvm.machine.TvmContext.Companion.ADDRESS_TAG_BITS
import org.usvm.machine.TvmContext.Companion.ADDRESS_TAG_LENGTH
import org.usvm.machine.TvmContext.Companion.CELL_DATA_BITS
import org.usvm.machine.TvmContext.Companion.EXTERN_ADDRESS_TAG
import org.usvm.machine.TvmContext.Companion.MAX_DATA_LENGTH
import org.usvm.machine.TvmContext.Companion.NONE_ADDRESS_TAG
import org.usvm.machine.TvmContext.Companion.STD_ADDRESS_TAG
import org.usvm.machine.TvmContext.Companion.STD_WORKCHAIN_BITS
import org.usvm.machine.TvmContext.Companion.VAR_ADDRESS_TAG
import org.usvm.machine.TvmContext.Companion.cellDataField
import org.usvm.machine.TvmContext.Companion.cellDataLengthField
import org.usvm.machine.TvmContext.Companion.cellRefsLengthField
import org.usvm.machine.TvmContext.Companion.sliceCellField
import org.usvm.machine.TvmContext.Companion.sliceDataPosField
import org.usvm.machine.TvmContext.Companion.sliceRefPosField
import org.usvm.machine.TvmContext.TvmCellDataSort
import org.usvm.machine.TvmContext.TvmInt257Sort
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.TvmStepScopeManager
import org.usvm.machine.intValue
import org.usvm.machine.types.TvmSymbolicCellDataCoins
import org.usvm.machine.types.makeSliceTypeLoad
import org.usvm.memory.UWritableMemory
import org.usvm.mkSizeAddExpr
import org.usvm.mkSizeExpr
import org.usvm.mkSizeGeExpr
import org.usvm.mkSizeLeExpr
import org.usvm.mkSizeSubExpr
import org.usvm.sizeSort

private data class GuardedExpr(
    val expr: UExpr<TvmSizeSort>,
    val guard: UBoolExpr
)

/**
 * Split sizeExpr that represents ite into two ite's:
 * first one has concrete leaves, second one has symbolic leaves.
 */
private fun splitSizeExpr(
    sizeExpr: UExpr<TvmSizeSort>
): Pair<GuardedExpr?, GuardedExpr?> {  // (concrete, symbolic)

    /**
     * Merge split ite leaves into one ite.
     * Pair (trueValue, falseValue) is either
     * (trueConcrete, falseConcrete) or (trueSymbolic, falseSymbolic).
     */
    fun KContext.mergeCellExprsIntoIte(
        cond: UBoolExpr,
        trueValue: GuardedExpr?,
        falseValue: GuardedExpr?
    ): GuardedExpr? =
        when {
            trueValue == null && falseValue == null -> {
                null
            }
            trueValue == null && falseValue != null -> {
                GuardedExpr(falseValue.expr, falseValue.guard and cond.not())
            }
            trueValue != null && falseValue == null -> {
                GuardedExpr(trueValue.expr, trueValue.guard and cond)
            }
            trueValue != null && falseValue != null -> {
                GuardedExpr(
                    mkIte(cond, trueValue.expr, falseValue.expr),
                    (cond and trueValue.guard) or (cond.not() and falseValue.guard)
                )
            }
            else -> {
                error("not reachable")
            }
        }

    val ctx = sizeExpr.ctx
    return with(ctx) {
        when (sizeExpr) {
            is KInterpretedValue ->
                GuardedExpr(sizeExpr, ctx.trueExpr) to null
            is UIteExpr<TvmSizeSort> -> {
                val cond = sizeExpr.condition
                val (trueConcrete, trueSymbolic) = splitSizeExpr(sizeExpr.trueBranch)
                val (falseConcrete, falseSymbolic) = splitSizeExpr(sizeExpr.falseBranch)
                val concrete = mergeCellExprsIntoIte(cond, trueConcrete, falseConcrete)
                val symbolic = mergeCellExprsIntoIte(cond, trueSymbolic, falseSymbolic)
                concrete to symbolic
            }
            else -> {
                // Any complex expressions containing symbolic values are considered fully symbolic
                null to GuardedExpr(sizeExpr, ctx.trueExpr)
            }
        }
    }
}

/**
 * This is function is used to set cellUnderflow error and to set its type
 * (StructuralError, SymbolicStructuralError, RealError or Unknown).
 */
private fun TvmContext.processCellUnderflowCheck(
    size: UExpr<TvmSizeSort>,
    scope: TvmStepScopeManager,
    minSize: UExpr<TvmSizeSort>? = null,
    maxSize: UExpr<TvmSizeSort>? = null,
    quietBlock: (TvmState.() -> Unit)? = null
): Unit? {
    val ctx = size.ctx as TvmContext
    val noUnderflowExpr = scope.calcOnStateCtx {
        val min = minSize?.let { mkSizeGeExpr(size, minSize) } ?: trueExpr
        val max = maxSize?.let { mkSizeLeExpr(size, maxSize) } ?: trueExpr
        min and max
    }

    // don't bother with concrete and symbolic cases if so
    if (!scope.allowFailuresOnCurrentStep || quietBlock != null) {
        return scope.fork(
            noUnderflowExpr,
            falseStateIsExceptional = quietBlock == null,
            blockOnFalseState = {
                quietBlock?.invoke(this)
                    ?: throwUnknownCellUnderflowError(this)
            }
        )
    }

    // cases for concrete and symbolic sizes are different:
    // this is why we need to split `size` if it represents ite.
    val (concreteSize, symbolicSize) = splitSizeExpr(size)
    val concreteGuard = concreteSize?.guard ?: ctx.falseExpr
    val symbolicGuard = symbolicSize?.guard ?: ctx.falseExpr

    // Case of concrete size: cellUnderflow is always a real error.
    scope.fork(
        with(ctx) { concreteGuard implies noUnderflowExpr},
        falseStateIsExceptional = true,
        blockOnFalseState = {
            throwRealCellUnderflowError(this)
        }
    ) ?: return null

    // Case of symbolic size.
    // First we distinguish StructuralError and SymbolicStructuralError.
    val isConcreteBound = (minSize is KInterpretedValue?) && (maxSize is KInterpretedValue?)
    var symbolicThrow = if (isConcreteBound) {
        throwStructuralCellUnderflowError
    } else {
        throwSymbolicStructuralCellUnderflowError
    }
    // Here cellUnderflow can be either structural, real or unknown.
    // It is structural error if state without cellUnderflow is possible.
    // It is real error if state without cellUnderflow is UNSTAT.
    // If solver returned UNKNOWN, the type of cellUnderflow is unknown.
    return scope.forkWithCheckerStatusKnowledge(
        with(ctx) { symbolicGuard implies noUnderflowExpr},
        blockOnUnknownTrueState = { symbolicThrow = throwUnknownCellUnderflowError },
        blockOnUnsatTrueState = { symbolicThrow = throwRealCellUnderflowError },
        blockOnFalseState = {
            symbolicThrow(this)
        }
    )
}

fun TvmContext.checkCellDataUnderflow(
    scope: TvmStepScopeManager,
    cellRef: UHeapRef,
    minSize: UExpr<TvmSizeSort>? = null,
    maxSize: UExpr<TvmSizeSort>? = null,
    quietBlock: (TvmState.() -> Unit)? = null
): Unit? {
    val cellSize = scope.calcOnStateCtx { memory.readField(cellRef, cellDataLengthField, sizeSort) }
    return processCellUnderflowCheck(cellSize, scope, minSize, maxSize, quietBlock)
}

fun TvmContext.checkCellRefsUnderflow(
    scope: TvmStepScopeManager,
    cellRef: UHeapRef,
    minSize: UExpr<TvmSizeSort>? = null,
    maxSize: UExpr<TvmSizeSort>? = null,
    quietBlock: (TvmState.() -> Unit)? = null
): Unit? {
    val cellSize = scope.calcOnStateCtx { memory.readField(cellRef, cellRefsLengthField, sizeSort) }
    return processCellUnderflowCheck(cellSize, scope, minSize, maxSize, quietBlock)
}

fun checkCellOverflow(
    noOverflowExpr: UBoolExpr,
    scope: TvmStepScopeManager,
    quietBlock: (TvmState.() -> Unit)? = null
): Unit? = scope.fork(
    noOverflowExpr,
    falseStateIsExceptional = (quietBlock == null),
    blockOnFalseState = {
        quietBlock?.invoke(this)
            ?: ctx.throwCellOverflowError(this)
    }
)

fun TvmStepScopeManager.assertDataLengthConstraint(
    cellDataLength: UExpr<TvmSizeSort>,
    unsatBlock: TvmState.() -> Unit,
): Unit? = calcOnStateCtx {
    val correctnessConstraint = mkAnd(
        mkSizeLeExpr(zeroSizeExpr, cellDataLength),
        mkSizeLeExpr(cellDataLength, maxDataLengthSizeExpr),
    )
    assert(correctnessConstraint, unsatBlock = unsatBlock)
}

fun TvmStepScopeManager.assertRefsLengthConstraint(
    cellRefsLength: UExpr<TvmSizeSort>,
    unsatBlock: TvmState.() -> Unit,
): Unit? = calcOnStateCtx {
    val correctnessConstraint = mkAnd(
        mkSizeLeExpr(zeroSizeExpr, cellRefsLength),
        mkSizeLeExpr(cellRefsLength, maxRefsLengthSizeExpr),
    )
    assert(correctnessConstraint, unsatBlock = unsatBlock)
}

private fun TvmState.preloadDataBitsFromCellWithoutChecks(
    cell: UHeapRef,
    offset: UExpr<TvmSizeSort>,
    sizeBits: UExpr<TvmSizeSort>
): UExpr<TvmCellDataSort> = with(ctx) {
    val cellData = memory.readField(cell, cellDataField, cellDataSort)
    val endOffset = mkSizeAddExpr(offset, sizeBits)
    val offsetDataPos = mkSizeSubExpr(maxDataLengthSizeExpr, endOffset)
    mkBvLogicalShiftRightExpr(cellData, offsetDataPos.zeroExtendToSort(cellDataSort))
}

fun TvmState.preloadDataBitsFromCellWithoutChecks(
    cell: UHeapRef,
    offset: UExpr<TvmSizeSort>,
    sizeBits: Int,
): UExpr<KBvSort> = with(ctx) {
    val shiftedData = preloadDataBitsFromCellWithoutChecks(cell, offset, mkSizeExpr(sizeBits))
    return mkBvExtractExpr(high = sizeBits - 1, low = 0, shiftedData)
}

fun TvmState.slicePreloadDataBitsWithoutChecks(
    slice: UHeapRef,
    sizeBits: Int,
): UExpr<KBvSort> = with(ctx) {
    val cell = memory.readField(slice, sliceCellField, addressSort)
    val dataPosition = memory.readField(slice, sliceDataPosField, sizeSort)
   return preloadDataBitsFromCellWithoutChecks(cell, dataPosition, sizeBits)
}

/**
 * @return bv 1023 with undefined high-order bits
 */
fun TvmStepScopeManager.slicePreloadDataBits(
    slice: UHeapRef,
    sizeBits: UExpr<TvmSizeSort>,
    quietBlock: (TvmState.() -> Unit)? = null
): UExpr<TvmCellDataSort>? = calcOnStateCtx {
    val cell = memory.readField(slice, sliceCellField, addressSort)
    val cellDataLength = memory.readField(cell, cellDataLengthField, sizeSort)

    assertDataLengthConstraint(
        cellDataLength,
        unsatBlock = { error("Cannot ensure correctness for data length in cell $cell") }
    ) ?: return@calcOnStateCtx  null

    val dataPosition = memory.readField(slice, sliceDataPosField, sizeSort)
    val readingEnd = mkBvAddExpr(dataPosition, sizeBits)

    checkCellDataUnderflow(this@slicePreloadDataBits, cell, minSize = readingEnd, quietBlock = quietBlock)
        ?: return@calcOnStateCtx null

    preloadDataBitsFromCellWithoutChecks(cell, dataPosition, sizeBits)
}

fun TvmStepScopeManager.slicePreloadDataBits(
    slice: UHeapRef,
    bits: Int,
    quietBlock: (TvmState.() -> Unit)? = null
): UExpr<UBvSort>? = doWithCtx {
    val data = slicePreloadDataBits(slice, mkSizeExpr(bits), quietBlock)
        ?: return@doWithCtx null

    mkBvExtractExpr(high = bits - 1, low = 0, data)
}

private fun TvmContext.extractIntFromShiftedData(
    shiftedData: UExpr<TvmCellDataSort>,
    sizeBits: UExpr<TvmInt257Sort>,
    isSigned: Boolean,
): UExpr<TvmInt257Sort> {
    val extractedBits = shiftedData.extractToInt257Sort()
    val trashBits = mkBvSubExpr(intBitsValue, sizeBits)
    val shiftedBits = mkBvShiftLeftExpr(extractedBits, trashBits)

    return if (!isSigned) {
        mkBvLogicalShiftRightExpr(shiftedBits, trashBits)
    } else {
        mkBvArithShiftRightExpr(shiftedBits, trashBits)
    }
}

fun TvmState.loadIntFromCellWithoutChecks(
    cell: UHeapRef,
    offset: UExpr<TvmSizeSort>,
    sizeBits: UExpr<TvmInt257Sort>,
    isSigned: Boolean
): UExpr<TvmInt257Sort> = with(ctx) {
    val shiftedData = preloadDataBitsFromCellWithoutChecks(cell, offset, sizeBits.extractToSizeSort())
    return extractIntFromShiftedData(shiftedData, sizeBits, isSigned)
}

/**
 * 0 <= bits <= 257
 */
fun TvmStepScopeManager.slicePreloadInt(
    slice: UHeapRef,
    sizeBits: UExpr<TvmInt257Sort>,
    isSigned: Boolean,
    quietBlock: (TvmState.() -> Unit)? = null
): UExpr<TvmInt257Sort>? {
    val shiftedData = calcOnStateCtx { slicePreloadDataBits(slice, sizeBits.extractToSizeSort(), quietBlock) }
        ?: return null

    return calcOnStateCtx {
        extractIntFromShiftedData(shiftedData, sizeBits, isSigned)
    }
}

private fun TvmStepScopeManager.slicePreloadInternalAddrLengthConstraint(
    slice: UHeapRef
): Pair<UBoolExpr, UExpr<TvmSizeSort>>? = calcOnStateCtx {
    // addr_var$11 anycast:(Maybe Anycast) addr_len:(## 9) workchain_id:int32
    // addr_std$10 anycast:(Maybe Anycast) workchain_id:int8 ...
    val prefixLen = 44
    val data = slicePreloadDataBitsWithoutChecks(slice, sizeBits = prefixLen)

    // addr_std$10
    // addr_var$11
    val tag = mkBvExtractExpr(high = prefixLen - 1, low = prefixLen - 2, data)

    // anycast:(Maybe Anycast)
    val anycastBit = mkBvExtractExpr(high = prefixLen - 3, low = prefixLen - 3, data)
    val noAnycastConstraint = anycastBit eq zeroBit

    // addr_std$10
    val stdConstraint = tag eq mkBv(STD_ADDRESS_TAG, ADDRESS_TAG_BITS)
    // workchain_id:int8
    val stdWorkchain = mkBvExtractExpr(high = prefixLen - 4, low = prefixLen - 11, data).signedExtendToInteger()
    val stdWorkchainConstraint = (stdWorkchain eq baseChain) or (stdWorkchain eq masterchain)
    val stdLength = mkSizeExpr(ADDRESS_TAG_LENGTH + 1 + STD_WORKCHAIN_BITS + ADDRESS_BITS)

    // addr_var$11
    val varConstraint = tag eq mkBv(VAR_ADDRESS_TAG, ADDRESS_TAG_BITS)
    // addr_len:(## 9)
    val varAddrLength = mkBvExtractExpr(high = prefixLen - 4, low = prefixLen - 12, data).zeroExtendToSort(sizeSort)
    // workchain_id:int32
    val varWorkchain = mkBvExtractExpr(high = prefixLen - 13, low = prefixLen - 44, data).signedExtendToInteger()
    val varWorkchainConstraint = (varWorkchain eq baseChain) or (varWorkchain eq masterchain)
    val varLength = mkSizeAddExpr(mkSizeExpr(ADDRESS_TAG_LENGTH + 1 + 9 + 32), varAddrLength)

    val (constraint, addrLength) = if (tvmOptions.enableVarAddress) {
        Pair(
            stdConstraint or varConstraint,
            mkIte(
                stdConstraint,
                stdLength,
                varLength
            )
        )
    } else {
        stdConstraint to stdLength
    }

    // TODO assume that there is no `anycast`, since we don't support it
    assert(
        mkAnd(
            constraint implies noAnycastConstraint,
            stdConstraint implies stdWorkchainConstraint,
            varConstraint implies varWorkchainConstraint
        ),
        unsatBlock = {
            error("Cannot assume no anycast")
        }
    ) ?: return@calcOnStateCtx null

    constraint to addrLength
}

private fun TvmStepScopeManager.slicePreloadExternalAddrLengthConstraint(
    slice: UHeapRef
): Pair<UBoolExpr, UExpr<TvmSizeSort>> = calcOnStateCtx {
    // addr_extern$01 len:(## 9)
    // addr_none$00 ...
    val prefixLen = 11
    val data = slicePreloadDataBitsWithoutChecks(slice, sizeBits = prefixLen)

    val tag = mkBvExtractExpr(high = prefixLen - 1, low = prefixLen - 2, data)

    // addr_none$00
    val noneConstraint = tag eq mkBv(NONE_ADDRESS_TAG, ADDRESS_TAG_BITS)
    val noneLength = mkSizeExpr(ADDRESS_TAG_LENGTH)

    // addr_extern$01
    val externConstraint = tag eq mkBv(EXTERN_ADDRESS_TAG, ADDRESS_TAG_BITS)
    // len:(## 9)
    val externAddrLength = mkBvExtractExpr(high = prefixLen - 3, low = prefixLen - 11, data).zeroExtendToSort(sizeSort)
    val externLength = mkSizeAddExpr(mkSizeExpr(ADDRESS_TAG_LENGTH + 9), externAddrLength)

    val addrLength = mkIte(
        noneConstraint,
        noneLength,
        externLength
    )

    (noneConstraint or externConstraint) to addrLength
}

fun TvmStepScopeManager.slicePreloadInternalAddrLength(slice: UHeapRef): UExpr<TvmSizeSort>? {
    val (constraint, length) = slicePreloadInternalAddrLengthConstraint(slice) ?: return null

    fork(
        constraint,
        falseStateIsExceptional = true,
        blockOnFalseState = {
            // TODO tl-b parsing failure
            ctx.throwUnknownCellUnderflowError(this)
        }
    ) ?: return null

    return length
}


fun TvmStepScopeManager.slicePreloadExternalAddrLength(slice: UHeapRef): UExpr<TvmSizeSort>? {
    val (constraint, length) = slicePreloadExternalAddrLengthConstraint(slice) ?: return null

    fork(
        constraint,
        falseStateIsExceptional = true,
        blockOnFalseState = {
            // TODO tl-b parsing failure
            ctx.throwUnknownCellUnderflowError(this)
        }
    ) ?: return null

    return length
}

fun TvmStepScopeManager.slicePreloadAddrLength(slice: UHeapRef): UExpr<TvmSizeSort>? = calcOnStateCtx {
    val (intConstraint, intLength) = slicePreloadInternalAddrLengthConstraint(slice) ?: return@calcOnStateCtx null
    val (extConstraint, extLength) = slicePreloadExternalAddrLengthConstraint(slice) ?: return@calcOnStateCtx null

    assert(intConstraint or extConstraint)
        ?: return@calcOnStateCtx null

    val length = mkIte(
        intConstraint,
        intLength,
        extLength
    )

    length
}

fun sliceLoadGrams(
    scope: TvmStepScopeManager,
    oldSlice: UHeapRef,
    newSlice: UConcreteHeapRef,
    doWithGrams: TvmStepScopeManager.(UExpr<TvmInt257Sort>) -> Unit
) = scope.calcOnStateCtx {
    val peekedLength = slicePreloadDataBitsWithoutChecks(newSlice, sizeBits = 4).zeroExtendToSort(sizeSort)
    scope.makeSliceTypeLoad(oldSlice, TvmSymbolicCellDataCoins(ctx, peekedLength), newSlice) {

        // hide the original [scope] from this closure
        @Suppress("NAME_SHADOWING", "UNUSED_VARIABLE")
        val scope = Unit

        val length = slicePreloadDataBits(newSlice, bits = 4)?.zeroExtendToSort(sizeSort)
            ?: return@makeSliceTypeLoad

        sliceMoveDataPtr(newSlice, bits = 4)

        val extendedLength = mkBvShiftLeftExpr(length, shift = threeSizeExpr)
        val grams = slicePreloadInt(newSlice, extendedLength.zeroExtendToSort(int257sort), isSigned = false)
            ?: return@makeSliceTypeLoad

        sliceMoveDataPtr(newSlice, extendedLength)

        doWithGrams(grams)
    }
}

fun TvmStepScopeManager.slicePreloadRef(
    slice: UHeapRef,
    idx: UExpr<TvmSizeSort>,
    quietBlock: (TvmState.() -> Unit)? = null
): UHeapRef? = calcOnStateCtx {
    val cell = memory.readField(slice, sliceCellField, addressSort)
    val refsLength = memory.readField(cell, cellRefsLengthField, sizeSort)

    assertRefsLengthConstraint(
        refsLength,
        unsatBlock = { error("Cannot ensure correctness for number of refs in cell $cell") }
    ) ?: return@calcOnStateCtx null

    val sliceRefPos = memory.readField(slice, sliceRefPosField, sizeSort)
    val refIdx = mkSizeAddExpr(sliceRefPos, idx)

    val minSize = mkBvAddExpr(refIdx, mkBv(1))
    checkCellRefsUnderflow(this@slicePreloadRef, cell, minSize = minSize, quietBlock = quietBlock)
        ?: return@calcOnStateCtx null

    readCellRef(cell, refIdx)
}

fun TvmStepScopeManager.slicePreloadNextRef(
    slice: UHeapRef,
    quietBlock: (TvmState.() -> Unit)? = null
): UHeapRef? = calcOnStateCtx { slicePreloadRef(slice, zeroSizeExpr, quietBlock) }

fun TvmState.sliceCopy(original: UHeapRef, result: UHeapRef) = with(ctx) {
    memory.copyField(original, result, sliceCellField, addressSort)
    memory.copyField(original, result, sliceDataPosField, sizeSort)
    memory.copyField(original, result, sliceRefPosField, sizeSort)
}

fun TvmState.sliceMoveDataPtr(slice: UHeapRef, bits: UExpr<TvmSizeSort>) = with(ctx) {
    val dataPosition = memory.readField(slice, sliceDataPosField, sizeSort)
    val updatedDataPosition = mkSizeAddExpr(dataPosition, bits)
    memory.writeField(slice, sliceDataPosField, sizeSort, updatedDataPosition, guard = trueExpr)
}

fun TvmState.sliceMoveDataPtr(slice: UHeapRef, bits: Int) = with(ctx) {
    sliceMoveDataPtr(slice, mkSizeExpr(bits))
}

fun TvmState.sliceMoveRefPtr(slice: UHeapRef, shift: UExpr<TvmSizeSort> = ctx.mkSizeExpr(1)) = with(ctx) {
    val refPosition = memory.readField(slice, sliceRefPosField, sizeSort)
    val updatedRefPosition = mkSizeAddExpr(refPosition, shift)
    memory.writeField(slice, sliceRefPosField, sizeSort, updatedRefPosition, guard = trueExpr)
}

fun TvmState.builderCopy(original: UHeapRef, result: UConcreteHeapRef) = with(ctx) {
    memory.copyField(original, result, cellDataField, cellDataSort)
    memory.copyField(original, result, cellDataLengthField, sizeSort)
    memory.copyField(original, result, cellRefsLengthField, sizeSort)
    copyCellRefs(original, result)
}

fun TvmState.builderStoreDataBits(builder: UHeapRef, bits: UExpr<UBvSort>) = with(ctx) {
    val builderData = memory.readField(builder, cellDataField, cellDataSort)
    val builderDataLength = memory.readField(builder, cellDataLengthField, sizeSort)

    val updatedLength = mkSizeAddExpr(builderDataLength, mkSizeExpr(bits.sort.sizeBits.toInt()))

    val updatedData: UExpr<TvmCellDataSort> = if (builderDataLength is KBitVecValue) {
        val size = builderDataLength.intValue()
        val updatedData = if (size > 0) {
            val oldData = mkBvExtractExpr(high = MAX_DATA_LENGTH - 1, low = MAX_DATA_LENGTH - size, builderData)
            mkBvConcatExpr(oldData, bits)
        } else {
            bits
        }

        val updatedDataSizeBits = updatedData.sort.sizeBits

        if (updatedDataSizeBits < CELL_DATA_BITS) {
            mkBvConcatExpr(
                updatedData,
                mkBv(0, CELL_DATA_BITS - updatedDataSizeBits)
            )
        } else {
            updatedData
        }.uncheckedCast()
    } else {
        updateBuilderData(builderData, bits.zeroExtendToSort(builderData.sort), updatedLength)
    }

    memory.writeField(builder, cellDataField, cellDataSort, updatedData, guard = trueExpr)
    memory.writeField(builder, cellDataLengthField, sizeSort, updatedLength, guard = trueExpr)
}


context(TvmContext)
fun <S : UBvSort> TvmStepScopeManager.builderStoreDataBits(
    builder: UHeapRef,
    bits: UExpr<S>,
    sizeBits: UExpr<TvmSizeSort>,
    quietBlock: (TvmState.() -> Unit)? = null
): Unit? {
    val builderData = calcOnState { memory.readField(builder, cellDataField, cellDataSort) }
    val builderDataLength = calcOnState { memory.readField(builder, cellDataLengthField, sizeSort) }
    val newDataLength = mkSizeAddExpr(builderDataLength, sizeBits)
    val extendedBits = bits.zeroExtendToSort(cellDataSort)

    val canWriteConstraint = mkSizeLeExpr(newDataLength, mkSizeExpr(MAX_DATA_LENGTH))
    checkCellOverflow(canWriteConstraint, this, quietBlock)
        ?: return null

    val trashBits = mkSizeSubExpr(mkSizeExpr(MAX_DATA_LENGTH), sizeBits).zeroExtendToSort(cellDataSort)
    val normalizedBits = mkBvLogicalShiftRightExpr(mkBvShiftLeftExpr(extendedBits, trashBits), trashBits)

    val updatedData = updateBuilderData(builderData, normalizedBits, newDataLength)

    return doWithState {
        memory.writeField(builder, cellDataField, cellDataSort, updatedData, guard = trueExpr)
        memory.writeField(builder, cellDataLengthField, sizeSort, newDataLength, guard = trueExpr)
    }
}

context(TvmContext)
fun TvmStepScopeManager.builderStoreInt(
    builder: UHeapRef,
    value: UExpr<TvmInt257Sort>,
    sizeBits: UExpr<TvmInt257Sort>,
    isSigned: Boolean,
    quietBlock: (TvmState.() -> Unit)? = null
): Unit? {
    val builderData = calcOnState { memory.readField(builder, cellDataField, cellDataSort) }
    val builderDataLength = calcOnState { memory.readField(builder, cellDataLengthField, sizeSort) }
    val updatedLength = mkSizeAddExpr(builderDataLength, sizeBits.extractToSizeSort())

    val canWriteConstraint = mkSizeLeExpr(updatedLength, mkSizeExpr(MAX_DATA_LENGTH))
    checkCellOverflow(canWriteConstraint, this, quietBlock)
        ?: return null

    val normalizedValue = if (isSigned) {
        val trashBits = mkBvSubExpr(intBitsValue, sizeBits)
        mkBvLogicalShiftRightExpr(mkBvShiftLeftExpr(value, trashBits), trashBits)
    } else {
        value
    }

    val updatedData = updateBuilderData(builderData, normalizedValue.zeroExtendToSort(builderData.sort), updatedLength)

    return doWithState {
        memory.writeField(builder, cellDataField, cellDataSort, updatedData, guard = trueExpr)
        memory.writeField(builder, cellDataLengthField, sizeSort, updatedLength, guard = trueExpr)
    }
}

private fun TvmContext.updateBuilderData(
    builderData: UExpr<TvmCellDataSort>,
    bits: UExpr<TvmCellDataSort>,
    updatedBuilderDataLength: UExpr<TvmSizeSort>,
): UExpr<TvmCellDataSort> {
    val shiftedBits: UExpr<TvmCellDataSort> = mkBvShiftLeftExpr(
        bits,
        mkBvSubExpr(maxDataLengthSizeExpr, updatedBuilderDataLength).zeroExtendToSort(builderData.sort)
    )

    return mkBvOrExpr(builderData, shiftedBits)
}

context(TvmContext)
fun TvmStepScopeManager.builderStoreGrams(
    builder: UHeapRef,
    value: UExpr<TvmInt257Sort>,
    quietBlock: (TvmState.() -> Unit)? = null
): Unit? {
    // var_uint$_ {n:#} len:(#< 16) value:(uint (len * 8))
    val lenSizeBits = 4
    val maxValue = 8 * ((1 shl lenSizeBits) - 1)

    val notOutOfRangeValue = unsignedIntegerFitsBits(value, maxValue.toUInt())
    checkOutOfRange(notOutOfRangeValue, this) ?: return null

    // len:(#< 16)
    val lengthValue = calcOnState { makeSymbolicPrimitive(int257sort) }
    val lengthRangeConstraint = unsignedIntegerFitsBits(lengthValue, lenSizeBits.toUInt())

    // (len * 8)
    val valueBits = mkBvShiftLeftExpr(lengthValue, threeValue)
    // ((len - 1) * 8)
    val prevValueBits = mkBvShiftLeftExpr(mkBvSubExpr(lengthValue, oneValue), threeValue)
    // (len = 0 /\ value = 0) \/
    // (len > 0 /\ `value ufits in (len * 8) bits` /\ `value doesn't ufit in ((len - 1) * 8) bits`)
    val lengthValueConstraint = mkOr(
        (lengthValue eq zeroValue) and (value eq zeroValue),
        mkAnd(
            mkBvSignedGreaterExpr(lengthValue, zeroValue),
            mkBvSignedLessOrEqualExpr(value, bvMaxValueUnsignedExtended(valueBits)),
            mkBvSignedGreaterExpr(value, bvMaxValueUnsignedExtended(prevValueBits)),
        )
    )

    assert(
        lengthRangeConstraint and lengthValueConstraint,
        unsatBlock = {
            error("Cannot assert grams length constraints")
        },
    ) ?: return null

    builderStoreInt(
        builder,
        lengthValue,
        lenSizeBits.toBv257(),
        isSigned = false,
        quietBlock
    ) ?: return null

    return builderStoreInt(
        builder,
        value,
        valueBits,
        isSigned = false,
        quietBlock
    )
}

fun TvmState.builderStoreNextRef(builder: UHeapRef, ref: UHeapRef) = with(ctx) {
    val builderRefsLength = memory.readField(builder, cellRefsLengthField, sizeSort)
    writeCellRef(builder, builderRefsLength, ref)
    val updatedLength = mkSizeAddExpr(builderRefsLength, mkSizeExpr(1))
    memory.writeField(builder, cellRefsLengthField, sizeSort, updatedLength, guard = trueExpr)
}

context(TvmContext)
fun TvmStepScopeManager.builderStoreSlice(
    builder: UHeapRef,
    slice: UHeapRef,
    quietBlock: (TvmState.() -> Unit)? = null,
): Unit? {
    val cell = calcOnState { memory.readField(slice, sliceCellField, addressSort) }
    val cellDataLength = calcOnState { memory.readField(cell, cellDataLengthField, sizeSort) }

    assertDataLengthConstraint(
        cellDataLength,
        unsatBlock = { error("Cannot ensure correctness for data length in cell $cell") }
    ) ?: return null
    val dataPosition = calcOnState { memory.readField(slice, sliceDataPosField, sizeSort) }

    val bitsToWriteLength = mkSizeSubExpr(cellDataLength, dataPosition)
    val cellData = slicePreloadDataBits(slice, bitsToWriteLength, quietBlock)
        ?: return null

    val cellRefsSize = calcOnState { memory.readField(cell, cellRefsLengthField, sizeSort) }
    val refsPosition = calcOnState { memory.readField(slice, sliceRefPosField, sizeSort) }
    val builderRefsSize = calcOnState { memory.readField(builder, cellRefsLengthField, sizeSort) }

    val refsToWriteSize = mkBvSubExpr(cellRefsSize, refsPosition)
    val resultingRefsSize = mkBvAddExpr(builderRefsSize, refsToWriteSize)
    val canWriteRefsConstraint = mkSizeLeExpr(resultingRefsSize, maxRefsLengthSizeExpr)

    checkCellOverflow(canWriteRefsConstraint, this, quietBlock)
        ?: return null

    builderStoreDataBits(builder, cellData, bitsToWriteLength, quietBlock)
        ?: return null

    return doWithState {
        for (i in 0 until TvmContext.MAX_REFS_NUMBER) {
            val sliceRef = readCellRef(cell, mkSizeExpr(i))
            writeCellRef(builder, mkSizeAddExpr(builderRefsSize, mkSizeExpr(i)), sliceRef)
        }

        memory.writeField(builder, cellRefsLengthField, sizeSort, resultingRefsSize, guard = trueExpr)
    }
}

fun TvmState.allocCellFromData(data: UExpr<UBvSort>): UHeapRef = with(ctx) {
    check(data.sort.sizeBits <= CELL_DATA_BITS) { "Unexpected data: $data" }

    val cell = allocEmptyCell()
    builderStoreDataBits(cell, data)
    cell
}

fun TvmStepScopeManager.allocCellFromData(
    data: UExpr<TvmCellDataSort>,
    sizeBits: UExpr<TvmSizeSort>,
): UHeapRef? = calcOnStateCtx {
    val cell = calcOnState { allocEmptyCell() }
    builderStoreDataBits(cell, data, sizeBits) ?: return@calcOnStateCtx null

    cell
}

fun TvmState.allocSliceFromData(data: UExpr<UBvSort>): UHeapRef = with(ctx) {
    val sliceCell = allocCellFromData(data)

    return allocSliceFromCell(sliceCell)
}

fun TvmStepScopeManager.allocSliceFromData(data: UExpr<TvmCellDataSort>, sizeBits: UExpr<TvmSizeSort>): UHeapRef? {
    val sliceCell = allocCellFromData(data, sizeBits) ?: return null

    return calcOnStateCtx { allocSliceFromCell(sliceCell) }
}

fun TvmState.allocateCell(cellValue: TvmCell): UConcreteHeapRef = with(ctx) {
    val refsSizeCondition = cellValue.refs.size <= TvmContext.MAX_REFS_NUMBER
    val cellDataSizeCondition = cellValue.data.bits.length <= MAX_DATA_LENGTH
    check(refsSizeCondition && cellDataSizeCondition) { "Unexpected cellValue: $cellValue" }

    if (cellValue.data.bits.isEmpty()) {
        return allocEmptyCell()
    }

    val data = mkBv(cellValue.data.bits, cellValue.data.bits.length.toUInt())
    val cell = allocEmptyCell()

    builderStoreDataBits(cell, data)

    cellValue.refs.forEach { refValue ->
        val ref = allocateCell(refValue)

        builderStoreNextRef(cell, ref)
    }

    cell
}

fun TvmState.allocEmptyCell() = with(ctx) {
    memory.allocConcrete(TvmCellType).also { cell ->
        memory.writeField(cell, cellDataField, cellDataSort, mkBv(0, cellDataSort), trueExpr)
        memory.writeField(cell, cellDataLengthField, sizeSort, zeroSizeExpr, trueExpr)
        memory.writeField(cell, cellRefsLengthField, sizeSort, zeroSizeExpr, trueExpr)
    }
}

fun TvmState.allocSliceFromCell(cell: UHeapRef) = with(ctx) {
    memory.allocConcrete(TvmSliceType).also { slice ->
        memory.writeField(slice, sliceCellField, addressSort, cell, trueExpr)
        memory.writeField(slice, sliceDataPosField, sizeSort, zeroSizeExpr, trueExpr)
        memory.writeField(slice, sliceRefPosField, sizeSort, zeroSizeExpr, trueExpr)
    }
}

fun TvmState.getSliceRemainingRefsCount(slice: UHeapRef): UExpr<TvmSizeSort> = with(ctx) {
    val cell = memory.readField(slice, sliceCellField, addressSort)
    val refsLength = memory.readField(cell, cellRefsLengthField, sizeSort)
    val refsPos = memory.readField(slice, sliceRefPosField, sizeSort)

    mkBvSubExpr(refsLength, refsPos)
}

fun TvmState.getSliceRemainingBitsCount(slice: UHeapRef): UExpr<TvmSizeSort> = with(ctx) {
    val cell = memory.readField(slice, sliceCellField, addressSort)
    val dataLength = memory.readField(cell, cellDataLengthField, sizeSort)
    val dataPos = memory.readField(slice, sliceDataPosField, sizeSort)

    mkBvSubExpr(dataLength, dataPos)
}

context(TvmContext)
fun TvmSubSliceSerializedLoader.bitsToBv(): KBitVecValue<UBvSort> {
    // todo: check bits order
    return mkBv(bits.joinToString(""), bits.size.toUInt())
}

private fun <Field, Sort : USort> UWritableMemory<*>.copyField(from: UHeapRef, to: UHeapRef, field: Field, sort: Sort) {
    writeField(to, field, sort, readField(from, field, sort), guard = from.ctx.trueExpr)
}
