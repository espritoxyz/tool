package org.usvm.machine.state

import io.ksmt.expr.KBitVecValue
import org.usvm.machine.types.TvmBuilderType
import org.usvm.machine.types.TvmCellType
import org.usvm.machine.types.TvmSliceType
import org.usvm.UBoolExpr
import org.usvm.UBvSort
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.USort
import org.usvm.api.readField
import org.usvm.api.writeField
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
import org.usvm.mkSizeLeExpr
import org.usvm.mkSizeLtExpr
import org.usvm.mkSizeSubExpr
import org.usvm.sizeSort

fun checkCellUnderflow(
    noUnderflowExpr: UBoolExpr,
    scope: TvmStepScope,
    quietBlock: (TvmState.() -> Unit)? = null
): Unit? = scope.fork(
    noUnderflowExpr,
    blockOnFalseState = {
        quietBlock?.invoke(this)
            ?: throwCellUnderflowError(this)
    }
)

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
    val readingConstraint = mkBvSignedLessOrEqualExpr(readingEnd, cellDataLength)

    checkCellUnderflow(readingConstraint, this@slicePreloadDataBits, quietBlock)
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

fun TvmStepScope.slicePreloadNextRef(
    slice: UHeapRef,
    quietBlock: (TvmState.() -> Unit)? = null
): UHeapRef? = calcOnStateCtx {
    val cell = memory.readField(slice, sliceCellField, addressSort)
    val refsLength = memory.readField(cell, cellRefsLengthField, sizeSort)

    assertRefsLengthConstraint(
        refsLength,
        unsatBlock = { error("Cannot ensure correctness for number of refs in cell $cell") }
    ) ?: return@calcOnStateCtx null

    val sliceRefPos = memory.readField(slice, sliceRefPosField, sizeSort)
    val readingConstraint = mkSizeLtExpr(sliceRefPos, refsLength)

    checkCellUnderflow(readingConstraint, this@slicePreloadNextRef, quietBlock)
        ?: return@calcOnStateCtx null

    readCellRef(cell, sliceRefPos)
}

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
