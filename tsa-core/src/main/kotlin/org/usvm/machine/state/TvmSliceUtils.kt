package org.usvm.machine.state

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.BvUtils.toBigIntegerSigned
import org.usvm.UBvSort
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.USort
import org.usvm.api.readField
import org.usvm.api.writeField
import org.usvm.machine.TvmContext
import org.usvm.machine.interpreter.TvmStepScope
import org.usvm.memory.UWritableMemory
import org.usvm.mkSizeAddExpr
import org.usvm.mkSizeExpr
import org.usvm.mkSizeLeExpr
import org.usvm.mkSizeLtExpr
import org.usvm.sizeSort

fun TvmStepScope.sliceLoadDataBits(slice: UHeapRef, bits: Int): UExpr<UBvSort>? = calcOnStateCtx {
    val cell = memory.readField(slice, TvmContext.sliceCellField, addressSort)
    val cellDataLength = memory.readField(cell, TvmContext.cellDataLengthField, sizeSort)

    val correctnessConstraint = mkAnd(
        mkSizeLeExpr(mkSizeExpr(0), cellDataLength),
        mkSizeLeExpr(cellDataLength, mkSizeExpr(TvmContext.MAX_DATA_LENGTH)),
    )
    assert(correctnessConstraint)
        ?: error("Cannot ensure correctness for data length in cell $cell")

    val cellData = memory.readField(cell, TvmContext.cellDataField, cellDataSort)
    val dataPosition = memory.readField(slice, TvmContext.sliceDataPosField, sizeSort)
    val bitsSizeExpr = mkSizeExpr(bits)
    val readingEnd = mkSizeAddExpr(dataPosition, bitsSizeExpr)
    val readingConstraint = mkSizeLeExpr(readingEnd, cellDataLength)

    fork(
        readingConstraint,
        blockOnFalseState = setFailure(TvmCellUnderflow)
    ) ?: return@calcOnStateCtx null

    if (dataPosition is KBitVecValue<*>) {
        val pos = dataPosition.toBigIntegerSigned().toInt()
        mkBvExtractExpr(high = pos + bits - 1, low = pos, cellData)
    } else {
        val extensionSize = cellData.sort.sizeBits - dataPosition.sort.sizeBits
        val extendedPos = mkBvZeroExtensionExpr(extensionSize.toInt(), dataPosition)
        val shiftedData = mkBvLogicalShiftRightExpr(cellData, extendedPos)
        mkBvExtractExpr(high = bits - 1, low = 0, shiftedData)
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

    fork(
        readingConstraint,
        blockOnFalseState = setFailure(TvmCellUnderflow)
    ) ?: return@calcOnStateCtx null

    readCellRef(cell, sliceRefPos)
}

fun TvmState.sliceCopy(original: UHeapRef, result: UHeapRef) = with(ctx) {
    memory.copyField(original, result, TvmContext.sliceCellField, addressSort)
    memory.copyField(original, result, TvmContext.sliceDataPosField, sizeSort)
    memory.copyField(original, result, TvmContext.sliceRefPosField, sizeSort)
}

fun TvmState.sliceMoveDataPtr(slice: UHeapRef, bits: Int) = with(ctx) {
    val dataPosition = memory.readField(slice, TvmContext.sliceDataPosField, sizeSort)
    val updatedDataPosition = mkSizeAddExpr(dataPosition, mkSizeExpr(bits))
    memory.writeField(slice, TvmContext.sliceDataPosField, sizeSort, updatedDataPosition, guard = trueExpr)
}

fun TvmState.sliceMoveRefPtr(slice: UHeapRef) = with(ctx) {
    val refPosition = memory.readField(slice, TvmContext.sliceRefPosField, sizeSort)
    val updatedRefPosition = mkSizeAddExpr(refPosition, mkSizeExpr(1))
    memory.writeField(slice, TvmContext.sliceRefPosField, sizeSort, updatedRefPosition, guard = trueExpr)
}

fun TvmState.builderCopy(original: UHeapRef, result: UHeapRef) = with(ctx) {
    memory.copyField(original, result, TvmContext.cellDataField, cellDataSort)
    memory.copyField(original, result, TvmContext.cellDataLengthField, sizeSort)
    memory.copyField(original, result, TvmContext.cellRefsLengthField, sizeSort)

    for (i in 0 until TvmContext.MAX_REFS_NUMBER) {
        val refIdx = mkSizeExpr(i)
        val ref = readCellRef(original, refIdx)
        writeCellRef(result, refIdx, ref)
    }
}

fun TvmState.builderStoreDataBits(builder: UHeapRef, bits: UExpr<UBvSort>) = with(ctx) {
    val builderData = memory.readField(builder, TvmContext.cellDataField, cellDataSort)
    val builderDataLength = memory.readField(builder, TvmContext.cellDataLengthField, sizeSort)

    val updatedData = if (builderDataLength is KBitVecValue<*>) {
        val size = builderDataLength.toBigIntegerSigned().toInt()
        val updatedData = if (size > 0) {
            val oldData = mkBvExtractExpr(high = size - 1, low = 0, builderData)
            mkBvConcatExpr(bits, oldData)
        } else {
            bits
        }

        val extensionSize = cellDataSort.sizeBits.toInt() - updatedData.sort.sizeBits.toInt()
        check(extensionSize >= 0) { "Builder data overflow" }

        mkBvZeroExtensionExpr(extensionSize, updatedData)
    } else {
        val bitsExtensionSize = cellDataSort.sizeBits - bits.sort.sizeBits
        val extendedBits = mkBvZeroExtensionExpr(bitsExtensionSize.toInt(), bits)

        val lengthExtensionSize = cellDataSort.sizeBits - builderDataLength.sort.sizeBits
        val extendedLength = mkBvZeroExtensionExpr(lengthExtensionSize.toInt(), builderDataLength)

        val positionedBits = mkBvShiftLeftExpr(extendedBits, extendedLength)
        mkBvOrExpr(builderData, positionedBits)
    }

    val updatedLength = mkSizeAddExpr(builderDataLength, mkSizeExpr(bits.sort.sizeBits.toInt()))

    memory.writeField(builder, TvmContext.cellDataField, cellDataSort, updatedData, guard = trueExpr)
    memory.writeField(builder, TvmContext.cellDataLengthField, sizeSort, updatedLength, guard = trueExpr)
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
