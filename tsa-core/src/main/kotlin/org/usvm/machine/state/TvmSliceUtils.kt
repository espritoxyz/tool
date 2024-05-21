package org.usvm.machine.state

import io.ksmt.KContext
import io.ksmt.expr.KBitVecValue
import io.ksmt.expr.KInterpretedValue
import org.usvm.machine.types.TvmBuilderType
import org.usvm.machine.types.TvmCellType
import org.usvm.machine.types.TvmSliceType
import org.usvm.UBoolExpr
import org.usvm.UBvSort
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.UIteExpr
import org.usvm.USort
import org.usvm.api.readField
import org.usvm.api.writeField
import org.usvm.collection.field.UInputFieldReading
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.Companion.MAX_DATA_LENGTH
import org.usvm.machine.TvmContext.Companion.cellDataField
import org.usvm.machine.TvmContext.Companion.cellDataLengthField
import org.usvm.machine.TvmContext.Companion.cellRefsLengthField
import org.usvm.machine.TvmContext.Companion.sliceCellField
import org.usvm.machine.TvmContext.Companion.sliceDataPosField
import org.usvm.machine.TvmContext.Companion.sliceRefPosField
import org.usvm.machine.TvmContext.TvmCellDataSort
import org.usvm.machine.TvmContext.TvmInt257Sort
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.TvmStepScope
import org.usvm.machine.intValue
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
            is UInputFieldReading<*, *> ->
                null to GuardedExpr(sizeExpr, ctx.trueExpr)
            is UIteExpr<TvmSizeSort> -> {
                val cond = sizeExpr.condition
                val (trueConcrete, trueSymbolic) = splitSizeExpr(sizeExpr.trueBranch)
                val (falseConcrete, falseSymbolic) = splitSizeExpr(sizeExpr.falseBranch)
                val concrete = mergeCellExprsIntoIte(cond, trueConcrete, falseConcrete)
                val symbolic = mergeCellExprsIntoIte(cond, trueSymbolic, falseSymbolic)
                concrete to symbolic
            }
            else -> error("Unexpected expr $sizeExpr")
        }
    }
}

/**
 * This is function is used to set cellUnderflow error and to set its type
 * (StructuralError, SymbolicStructuralError, RealError or Unknown).
 */
private fun processCellUnderflowCheck(
    size: UExpr<TvmSizeSort>,
    scope: TvmStepScope,
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

    // cases for concrete and symbolic sizes are different:
    // this is why we need to split `size` if it represents ite.
    val (concreteSize, symbolicSize) = splitSizeExpr(size)
    val concreteGuard = concreteSize?.guard ?: ctx.falseExpr
    val symbolicGuard = symbolicSize?.guard ?: ctx.falseExpr

    // Case of concrete size: cellUnderflow is always a real error.
    scope.fork(
        with(ctx) { concreteGuard implies noUnderflowExpr},
        blockOnFalseState = {
            quietBlock?.invoke(this)
                ?: throwRealCellUnderflowError(this)
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
            quietBlock?.invoke(this)
                ?: symbolicThrow(this)
        }
    )
}

fun checkCellDataUnderflow(
    scope: TvmStepScope,
    cellRef: UHeapRef,
    minSize: UExpr<TvmSizeSort>? = null,
    maxSize: UExpr<TvmSizeSort>? = null,
    quietBlock: (TvmState.() -> Unit)? = null
): Unit? {
    val cellSize = scope.calcOnStateCtx { memory.readField(cellRef, cellDataLengthField, sizeSort) }
    return processCellUnderflowCheck(cellSize, scope, minSize, maxSize, quietBlock)
}

fun checkCellRefsUnderflow(
    scope: TvmStepScope,
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
    scope: TvmStepScope,
    quietBlock: (TvmState.() -> Unit)? = null
): Unit? = scope.fork(
    noOverflowExpr,
    blockOnFalseState = {
        quietBlock?.invoke(this)
            ?: throwCellOverflowError(this)
    }
)

fun TvmStepScope.assertDataLengthConstraint(
    cellDataLength: UExpr<TvmSizeSort>,
    unsatBlock: TvmState.() -> Unit,
): Unit? = calcOnStateCtx {
    val correctnessConstraint = mkAnd(
        mkSizeLeExpr(zeroSizeExpr, cellDataLength),
        mkSizeLeExpr(cellDataLength, maxDataLengthSizeExpr),
    )
    assert(correctnessConstraint, unsatBlock = unsatBlock)
}

fun TvmStepScope.assertRefsLengthConstraint(
    cellRefsLength: UExpr<TvmSizeSort>,
    unsatBlock: TvmState.() -> Unit,
): Unit? = calcOnStateCtx {
    val correctnessConstraint = mkAnd(
        mkSizeLeExpr(zeroSizeExpr, cellRefsLength),
        mkSizeLeExpr(cellRefsLength, maxRefsLengthSizeExpr),
    )
    assert(correctnessConstraint, unsatBlock = unsatBlock)
}

/**
 * @return bv 1023 with undefined high-order bits
 */
fun TvmStepScope.slicePreloadDataBits(
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

    val cellData = memory.readField(cell, cellDataField, cellDataSort)
    val dataPosition = memory.readField(slice, sliceDataPosField, sizeSort)
    val offset = mkBvAddExpr(dataPosition, sizeBits)
    val offsetDataPos = mkBvSubExpr(cellDataLength, offset)
    val readingEnd = mkBvAddExpr(dataPosition, sizeBits)

    checkCellDataUnderflow(this@slicePreloadDataBits, cell, minSize = readingEnd, quietBlock = quietBlock)
        ?: return@calcOnStateCtx null

    mkBvLogicalShiftRightExpr(cellData, offsetDataPos.zeroExtendToSort(cellDataSort))
}

fun TvmStepScope.slicePreloadDataBits(
    slice: UHeapRef,
    bits: Int,
    quietBlock: (TvmState.() -> Unit)? = null
): UExpr<UBvSort>? {
    val data = calcOnStateCtx { slicePreloadDataBits(slice, mkSizeExpr(bits), quietBlock) }
        ?: return null

    return calcOnStateCtx { mkBvExtractExpr(high = bits - 1, low = 0, data) }
}

/**
 * 0 <= bits <= 257
 */
fun TvmStepScope.slicePreloadInt(
    slice: UHeapRef,
    sizeBits: UExpr<TvmInt257Sort>,
    isSigned: Boolean,
    quietBlock: (TvmState.() -> Unit)? = null
): UExpr<TvmInt257Sort>? {
    val shiftedData = calcOnStateCtx { slicePreloadDataBits(slice, sizeBits.extractToSizeSort(), quietBlock) }
        ?: return null

    return calcOnStateCtx {
        val extractedBits = shiftedData.extractToInt257Sort()
        val trashBits = mkBvSubExpr(intBitsValue, sizeBits)
        val shiftedBits = mkBvShiftLeftExpr(extractedBits, trashBits)

        if (!isSigned) {
            mkBvLogicalShiftRightExpr(shiftedBits, trashBits)
        } else {
            mkBvArithShiftRightExpr(shiftedBits, trashBits)
        }
    }
}

fun TvmStepScope.slicePreloadRef(
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

fun TvmStepScope.slicePreloadNextRef(
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

fun TvmState.sliceMoveRefPtr(slice: UHeapRef) = with(ctx) {
    val refPosition = memory.readField(slice, sliceRefPosField, sizeSort)
    val updatedRefPosition = mkSizeAddExpr(refPosition, mkSizeExpr(1))
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

    val updatedData: UExpr<TvmCellDataSort> = if (builderDataLength is KBitVecValue<*>) {
        val size = builderDataLength.intValue()
        val updatedData = if (size > 0) {
            val oldData = mkBvExtractExpr(high = size - 1, low = 0, builderData)
            mkBvConcatExpr(oldData, bits)
        } else {
            bits
        }

        check(updatedData.sort.sizeBits <= cellDataSort.sizeBits) { "Builder data overflow" }

        updatedData.zeroExtendToSort(cellDataSort)
    } else {
        val bitsLengthValue = bits.sort.sizeBits.toInt().toBv257()
        val shiftedData = mkBvShiftLeftExpr(builderData, bitsLengthValue.zeroExtendToSort(builderData.sort))

        mkBvOrExpr(shiftedData, bits.zeroExtendToSort(shiftedData.sort))
    }

    val updatedLength = mkSizeAddExpr(builderDataLength, mkSizeExpr(bits.sort.sizeBits.toInt()))

    memory.writeField(builder, cellDataField, cellDataSort, updatedData, guard = trueExpr)
    memory.writeField(builder, cellDataLengthField, sizeSort, updatedLength, guard = trueExpr)
}


context(TvmContext)
fun <S : UBvSort> TvmStepScope.builderStoreDataBits(
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

    val shiftedData = mkBvShiftLeftExpr(builderData, sizeBits.zeroExtendToSort(builderData.sort))
    val updatedData = mkBvOrExpr(shiftedData, normalizedBits)

    return doWithState {
        memory.writeField(builder, cellDataField, cellDataSort, updatedData, guard = trueExpr)
        memory.writeField(builder, cellDataLengthField, sizeSort, newDataLength, guard = trueExpr)
    }
}

context(TvmContext)
fun TvmStepScope.builderStoreInt(
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

    val shiftedData = mkBvShiftLeftExpr(builderData, sizeBits.zeroExtendToSort(builderData.sort))
    val updatedData = mkBvOrExpr(shiftedData, normalizedValue.zeroExtendToSort(shiftedData.sort))

    return doWithState {
        memory.writeField(builder, cellDataField, cellDataSort, updatedData, guard = trueExpr)
        memory.writeField(builder, cellDataLengthField, sizeSort, updatedLength, guard = trueExpr)
    }
}

fun TvmState.builderStoreNextRef(builder: UHeapRef, ref: UHeapRef) = with(ctx) {
    val builderRefsLength = memory.readField(builder, cellRefsLengthField, sizeSort)
    writeCellRef(builder, builderRefsLength, ref)
    val updatedLength = mkSizeAddExpr(builderRefsLength, mkSizeExpr(1))
    memory.writeField(builder, cellRefsLengthField, sizeSort, updatedLength, guard = trueExpr)
}

context(TvmContext)
fun TvmStepScope.builderStoreSlice(builder: UHeapRef, slice: UHeapRef, quietBlock: (TvmState.() -> Unit)?): Unit? {
    val resultBuilder = calcOnState { memory.allocConcrete(TvmBuilderType).also { builderCopy(builder, it) } }

    val cell = calcOnState { memory.readField(slice, sliceCellField, addressSort) }
    val cellDataLength = calcOnState { memory.readField(cell, cellDataLengthField, sizeSort) }

    assertDataLengthConstraint(
        cellDataLength,
        unsatBlock = { error("Cannot ensure correctness for data length in cell $cell") }
    ) ?: return null

    val cellData = calcOnState { memory.readField(cell, cellDataField, cellDataSort) }
    val dataPosition = calcOnState { memory.readField(slice, sliceDataPosField, sizeSort) }

    val bitsToWriteLength = mkSizeSubExpr(cellDataLength, dataPosition)

    val cellRefsSize = calcOnState { memory.readField(cell, cellRefsLengthField, sizeSort) }
    val refsPosition = calcOnState { memory.readField(slice, sliceRefPosField, sizeSort) }
    val builderRefsSize = calcOnState { memory.readField(builder, cellRefsLengthField, sizeSort) }

    val refsToWriteSize = mkBvSubExpr(cellRefsSize, refsPosition)
    val resultingRefsSize = mkBvAddExpr(builderRefsSize, refsToWriteSize)
    val canWriteRefsConstraint = mkSizeLeExpr(resultingRefsSize, maxRefsLengthSizeExpr)

    checkCellOverflow(canWriteRefsConstraint, this, quietBlock)
        ?: return null

    builderStoreDataBits(resultBuilder, cellData, bitsToWriteLength, quietBlock)
        ?: return null

    return doWithState {
        for (i in 0 until TvmContext.MAX_REFS_NUMBER) {
            val sliceRef = readCellRef(cell, mkSizeExpr(i))
            writeCellRef(resultBuilder, mkSizeAddExpr(builderRefsSize, mkSizeExpr(i)), sliceRef)
        }

        memory.writeField(resultBuilder, cellRefsLengthField, sizeSort, resultingRefsSize, guard = trueExpr)
    }
}

fun TvmState.makeSliceFromData(data: UExpr<UBvSort>): UHeapRef = with(ctx) {
    val sliceCell = memory.allocConcrete(TvmCellType)
    memory.writeField(sliceCell, cellDataField, cellDataSort, mkBv(0, cellDataSort), trueExpr)
    memory.writeField(sliceCell, cellDataLengthField, sizeSort, zeroSizeExpr, trueExpr)
    memory.writeField(sliceCell, cellRefsLengthField, sizeSort, zeroSizeExpr, trueExpr)

    builderStoreDataBits(sliceCell, data)

    val resultSlice = memory.allocConcrete(TvmSliceType)
    memory.writeField(resultSlice, sliceCellField, addressSort, sliceCell, trueExpr)
    memory.writeField(resultSlice, sliceDataPosField, sizeSort, zeroSizeExpr, trueExpr)
    memory.writeField(resultSlice, sliceRefPosField, sizeSort, zeroSizeExpr, trueExpr)

    return resultSlice
}

fun TvmStepScope.allocEmptyCell() = calcOnStateCtx {
    memory.allocConcrete(TvmCellType).also { cell ->
        memory.writeField(cell, cellDataField, cellDataSort, mkBv(0, cellDataSort), trueExpr)
        memory.writeField(cell, cellDataLengthField, sizeSort, zeroSizeExpr, trueExpr)
        memory.writeField(cell, cellRefsLengthField, sizeSort, zeroSizeExpr, trueExpr)
    }
}

fun TvmStepScope.allocSliceFromCell(cell: UHeapRef) = calcOnStateCtx {
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

private fun <Field, Sort : USort> UWritableMemory<*>.copyField(from: UHeapRef, to: UHeapRef, field: Field, sort: Sort) {
    writeField(to, field, sort, readField(from, field, sort), guard = from.ctx.trueExpr)
}
