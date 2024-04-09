package org.usvm.machine.state

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.BvUtils.toBigIntegerSigned
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
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.interpreter.TvmStepScope
import org.usvm.memory.UWritableMemory
import org.usvm.mkSizeAddExpr
import org.usvm.mkSizeExpr
import org.usvm.mkSizeLeExpr
import org.usvm.mkSizeLtExpr
import org.usvm.mkSizeSubExpr
import org.usvm.sizeSort

fun checkCellUnderflow(noUnderflowExpr: UBoolExpr, scope: TvmStepScope): Unit? = scope.fork(
    noUnderflowExpr,
    blockOnFalseState = setFailure(TvmCellUnderflow)
)

fun checkCellOverflow(noOverflowExpr: UBoolExpr, scope: TvmStepScope): Unit? = scope.fork(
    noOverflowExpr,
    blockOnFalseState = setFailure(TvmCellOverflow)
)

/**
 * @return bv 1023 with undefined high-order bits
 */
fun TvmStepScope.sliceLoadDataBits(slice: UHeapRef, sizeBits: UExpr<TvmSizeSort>): UExpr<UBvSort>? = calcOnStateCtx {
    val cell = memory.readField(slice, TvmContext.sliceCellField, addressSort)
    val cellDataLength = memory.readField(cell, TvmContext.cellDataLengthField, sizeSort)

    val correctnessConstraint = mkAnd(
        mkSizeLeExpr(mkSizeExpr(0), cellDataLength),
        mkSizeLeExpr(cellDataLength, mkSizeExpr(MAX_DATA_LENGTH)),
    )
    assert(correctnessConstraint) ?: error("Cannot ensure correctness for data length in cell $cell")

    val cellData = memory.readField(cell, TvmContext.cellDataField, cellDataSort)
    val dataPosition = memory.readField(slice, TvmContext.sliceDataPosField, sizeSort)
    val offset = mkBvAddExpr(dataPosition, sizeBits)
    val offsetDataPos = mkBvSubExpr(cellDataLength, offset)
    val readingEnd = mkBvAddExpr(dataPosition, sizeBits)
    val readingConstraint = mkBvSignedLessOrEqualExpr(readingEnd, cellDataLength)

    checkCellUnderflow(readingConstraint, this@sliceLoadDataBits) ?: return@calcOnStateCtx null

    mkBvLogicalShiftRightExpr(cellData, offsetDataPos.zeroExtendToSort(cellDataSort))
}

fun TvmStepScope.sliceLoadDataBits(slice: UHeapRef, bits: Int): UExpr<UBvSort>? {
    val data = calcOnStateCtx { sliceLoadDataBits(slice, mkSizeExpr(bits)) } ?: return null

    return calcOnStateCtx { mkBvExtractExpr(high = bits - 1, low = 0, data) }
}

/**
 * 0 <= bits <= 257
 */
fun TvmStepScope.sliceLoadInt(
    slice: UHeapRef,
    sizeBits: UExpr<UBvSort>,
    isSigned: Boolean,
): UExpr<UBvSort>? {
    val shiftedData = calcOnStateCtx { sliceLoadDataBits(slice, sizeBits.extractToSizeSort()) } ?: return null

    return calcOnStateCtx {
        val extractedBits = mkBvExtractExpr(high = TvmContext.INT_BITS.toInt() - 1, low = 0, shiftedData)
        val trashBits = mkBvSubExpr(intBitsValue, sizeBits)
        val shiftedBits = mkBvShiftLeftExpr(extractedBits, trashBits)

        if (!isSigned) {
            mkBvLogicalShiftRightExpr(shiftedBits, trashBits)
        } else {
            mkBvArithShiftRightExpr(shiftedBits, trashBits)
        }
    }
}

fun TvmStepScope.sliceLoadNextRef(slice: UHeapRef): UHeapRef? = calcOnStateCtx {
    val cell = memory.readField(slice, TvmContext.sliceCellField, addressSort)
    val refsLength = memory.readField(cell, TvmContext.cellRefsLengthField, sizeSort)

    val correctnessConstraint = mkAnd(
        mkSizeLeExpr(mkSizeExpr(0), refsLength),
        mkSizeLeExpr(refsLength, mkSizeExpr(TvmContext.MAX_REFS_NUMBER)),
    )
    assert(correctnessConstraint)
        ?: error("Cannot ensure correctness for number of refs in cell $cell")

    val sliceRefPos = memory.readField(slice, TvmContext.sliceRefPosField, sizeSort)
    val readingConstraint = mkSizeLtExpr(sliceRefPos, refsLength)

    checkCellUnderflow(readingConstraint, this@sliceLoadNextRef) ?: return@calcOnStateCtx null

    readCellRef(cell, sliceRefPos)
}

fun TvmState.sliceCopy(original: UHeapRef, result: UHeapRef) = with(ctx) {
    memory.copyField(original, result, TvmContext.sliceCellField, addressSort)
    memory.copyField(original, result, TvmContext.sliceDataPosField, sizeSort)
    memory.copyField(original, result, TvmContext.sliceRefPosField, sizeSort)
}

fun TvmState.sliceMoveDataPtr(slice: UHeapRef, bits: UExpr<TvmSizeSort>) = with(ctx) {
    val dataPosition = memory.readField(slice, TvmContext.sliceDataPosField, sizeSort)
    val updatedDataPosition = mkSizeAddExpr(dataPosition, bits)
    memory.writeField(slice, TvmContext.sliceDataPosField, sizeSort, updatedDataPosition, guard = trueExpr)
}

fun TvmState.sliceMoveDataPtr(slice: UHeapRef, bits: Int) = with(ctx) {
    sliceMoveDataPtr(slice, mkSizeExpr(bits))
}

fun TvmState.sliceMoveRefPtr(slice: UHeapRef) = with(ctx) {
    val refPosition = memory.readField(slice, TvmContext.sliceRefPosField, sizeSort)
    val updatedRefPosition = mkSizeAddExpr(refPosition, mkSizeExpr(1))
    memory.writeField(slice, TvmContext.sliceRefPosField, sizeSort, updatedRefPosition, guard = trueExpr)
}

fun TvmState.builderCopy(original: UHeapRef, result: UConcreteHeapRef) = with(ctx) {
    memory.copyField(original, result, TvmContext.cellDataField, cellDataSort)
    memory.copyField(original, result, TvmContext.cellDataLengthField, sizeSort)
    memory.copyField(original, result, TvmContext.cellRefsLengthField, sizeSort)
    copyCellRefs(original, result)
}

fun TvmState.builderStoreDataBits(builder: UHeapRef, bits: UExpr<UBvSort>) = with(ctx) {
    val builderData = memory.readField(builder, TvmContext.cellDataField, cellDataSort)
    val builderDataLength = memory.readField(builder, TvmContext.cellDataLengthField, sizeSort)

    val updatedData = if (builderDataLength is KBitVecValue<*>) {
        val size = builderDataLength.toBigIntegerSigned().toInt()
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

    memory.writeField(builder, TvmContext.cellDataField, cellDataSort, updatedData, guard = trueExpr)
    memory.writeField(builder, TvmContext.cellDataLengthField, sizeSort, updatedLength, guard = trueExpr)
}


context(TvmContext)
fun TvmStepScope.builderStoreDataBits(
    builder: UHeapRef,
    bits: UExpr<UBvSort>,
    sizeBits: UExpr<TvmSizeSort>
): Unit? {
    val builderData = calcOnState { memory.readField(builder, TvmContext.cellDataField, cellDataSort) }
    val builderDataLength = calcOnState { memory.readField(builder, TvmContext.cellDataLengthField, sizeSort) }
    val newDataLength = mkSizeAddExpr(builderDataLength, sizeBits)
    val extendedBits = bits.zeroExtendToSort(cellDataSort)

    val canWriteConstraint = mkSizeLeExpr(newDataLength, mkSizeExpr(MAX_DATA_LENGTH))
    checkCellOverflow(canWriteConstraint, this) ?: return null

    val trashBits = mkSizeSubExpr(mkSizeExpr(MAX_DATA_LENGTH), sizeBits).zeroExtendToSort(cellDataSort)
    val normalizedBits = mkBvLogicalShiftRightExpr(mkBvShiftLeftExpr(extendedBits, trashBits), trashBits)

    val shiftedData = mkBvShiftLeftExpr(builderData, sizeBits.zeroExtendToSort(builderData.sort))
    val updatedData = mkBvOrExpr(shiftedData, normalizedBits)

    return doWithState {
        memory.writeField(builder, TvmContext.cellDataField, cellDataSort, updatedData, guard = trueExpr)
        memory.writeField(builder, TvmContext.cellDataLengthField, sizeSort, newDataLength, guard = trueExpr)
    }
}

context(TvmContext)
fun TvmStepScope.builderStoreInt(
    builder: UHeapRef,
    value: UExpr<UBvSort>,
    sizeBits: UExpr<UBvSort>,
    isSigned: Boolean
): Unit? {
    require(value.sort == int257sort) { "Expected int value, but got: $value" }

    val builderData = calcOnState { memory.readField(builder, TvmContext.cellDataField, cellDataSort) }
    val builderDataLength = calcOnState { memory.readField(builder, TvmContext.cellDataLengthField, sizeSort) }
    val updatedLength = mkSizeAddExpr(builderDataLength, sizeBits.extractToSizeSort())

    val canWriteConstraint = mkSizeLeExpr(updatedLength, mkSizeExpr(MAX_DATA_LENGTH))
    checkCellOverflow(canWriteConstraint, this) ?: return null

    val normalizedValue = if (isSigned) {
        val trashBits = mkBvSubExpr(intBitsValue, sizeBits)
        mkBvLogicalShiftRightExpr(mkBvShiftLeftExpr(value, trashBits), trashBits)
    } else {
        value
    }

    val shiftedData = mkBvShiftLeftExpr(builderData, sizeBits)
    val updatedData = mkBvOrExpr(shiftedData, normalizedValue.zeroExtendToSort(shiftedData.sort))

    return doWithState {
        memory.writeField(builder, TvmContext.cellDataField, cellDataSort, updatedData, guard = trueExpr)
        memory.writeField(builder, TvmContext.cellDataLengthField, sizeSort, updatedLength, guard = trueExpr)
    }
}

fun TvmState.builderStoreNextRef(builder: UHeapRef, ref: UHeapRef) = with(ctx) {
    val builderRefsLength = memory.readField(builder, TvmContext.cellRefsLengthField, sizeSort)
    writeCellRef(builder, builderRefsLength, ref)
    val updatedLength = mkSizeAddExpr(builderRefsLength, mkSizeExpr(1))
    memory.writeField(builder, TvmContext.cellRefsLengthField, sizeSort, updatedLength, guard = trueExpr)
}

private fun <Field, Sort : USort> UWritableMemory<*>.copyField(from: UHeapRef, to: UHeapRef, field: Field, sort: Sort) {
    writeField(to, field, sort, readField(from, field, sort), guard = from.ctx.trueExpr)
}
