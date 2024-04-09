package org.usvm.machine.interpreter

import org.ton.bytecode.TvmBuilderType
import org.ton.bytecode.TvmCellBuildEndcInst
import org.ton.bytecode.TvmCellBuildInst
import org.ton.bytecode.TvmCellBuildNewcInst
import org.ton.bytecode.TvmCellBuildStiInst
import org.ton.bytecode.TvmCellBuildStixInst
import org.ton.bytecode.TvmCellBuildStuInst
import org.ton.bytecode.TvmCellBuildStuxInst
import org.ton.bytecode.TvmCellParseCtosInst
import org.ton.bytecode.TvmCellParseEndsInst
import org.ton.bytecode.TvmCellParseInst
import org.ton.bytecode.TvmCellParseLdiAltInst
import org.ton.bytecode.TvmCellParseLdiInst
import org.ton.bytecode.TvmCellParseLdile4Inst
import org.ton.bytecode.TvmCellParseLdile8Inst
import org.ton.bytecode.TvmCellParseLdixInst
import org.ton.bytecode.TvmCellParseLdrefInst
import org.ton.bytecode.TvmCellParseLdsliceAltInst
import org.ton.bytecode.TvmCellParseLdsliceInst
import org.ton.bytecode.TvmCellParseLdslicexInst
import org.ton.bytecode.TvmCellParseLduAltInst
import org.ton.bytecode.TvmCellParseLduInst
import org.ton.bytecode.TvmCellParseLdule4Inst
import org.ton.bytecode.TvmCellParseLdule8Inst
import org.ton.bytecode.TvmCellParseLduxInst
import org.ton.bytecode.TvmCellParsePldiInst
import org.ton.bytecode.TvmCellParsePldile4Inst
import org.ton.bytecode.TvmCellParsePldile8Inst
import org.ton.bytecode.TvmCellParsePldixInst
import org.ton.bytecode.TvmCellParsePldsliceInst
import org.ton.bytecode.TvmCellParsePldslicexInst
import org.ton.bytecode.TvmCellParsePlduInst
import org.ton.bytecode.TvmCellParsePldule4Inst
import org.ton.bytecode.TvmCellParsePldule8Inst
import org.ton.bytecode.TvmCellParsePlduxInst
import org.ton.bytecode.TvmCellParseSbitrefsInst
import org.ton.bytecode.TvmCellParseSbitsInst
import org.ton.bytecode.TvmCellParseSrefsInst
import org.ton.bytecode.TvmCellType
import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmIntegerType
import org.ton.bytecode.TvmSliceType
import org.usvm.UBoolExpr
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.api.readField
import org.usvm.api.writeField
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.state.TvmIntegerOutOfRange
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.builderCopy
import org.usvm.machine.state.builderStoreDataBits
import org.usvm.machine.state.builderStoreInt
import org.usvm.machine.state.calcOnStateCtx
import org.usvm.machine.state.checkCellUnderflow
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.consumeGas
import org.usvm.machine.state.doWithStateCtx
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.setFailure
import org.usvm.machine.state.signedIntegerFitsBits
import org.usvm.machine.state.sliceCopy
import org.usvm.machine.state.sliceLoadDataBits
import org.usvm.machine.state.sliceLoadInt
import org.usvm.machine.state.sliceLoadNextRef
import org.usvm.machine.state.sliceMoveDataPtr
import org.usvm.machine.state.sliceMoveRefPtr
import org.usvm.machine.state.takeLastBuilder
import org.usvm.machine.state.takeLastCell
import org.usvm.machine.state.takeLastInt
import org.usvm.machine.state.takeLastSlice
import org.usvm.machine.state.unsignedIntegerFitsBits
import org.usvm.mkSizeExpr
import org.usvm.mkSizeGeExpr
import org.usvm.sizeSort

class TvmCellInterpreter(private val ctx: TvmContext) {
    fun visitCellParseInst(
        scope: TvmStepScope,
        stmt: TvmCellParseInst
    ) {
        when (stmt) {
            is TvmCellParseCtosInst -> visitCellToSliceInst(scope, stmt)
            is TvmCellParseEndsInst -> visitEndSliceInst(scope, stmt)
            is TvmCellParseLdrefInst -> visitLoadRefInst(scope, stmt)
            is TvmCellParseLduInst -> visitLoadIntInst(scope, stmt, stmt.c + 1, isSigned = false, preload = false)
            is TvmCellParseLduAltInst -> visitLoadIntInst(scope, stmt, stmt.c + 1, isSigned = false, preload = false)
            is TvmCellParseLdiInst -> visitLoadIntInst(scope, stmt, stmt.c + 1, isSigned = true, preload = false)
            is TvmCellParseLdiAltInst -> visitLoadIntInst(scope, stmt, stmt.c + 1, isSigned = true, preload = false)
            is TvmCellParsePlduInst -> visitLoadIntInst(scope, stmt, stmt.c + 1, isSigned = false, preload = true)
            is TvmCellParsePldiInst -> visitLoadIntInst(scope, stmt, stmt.c + 1, isSigned = true, preload = true)
            is TvmCellParseLdule4Inst -> visitLoadIntLEInst(
                scope,
                stmt,
                sizeBytes = 4,
                isSigned = false,
                preload = false
            )

            is TvmCellParseLdile4Inst -> visitLoadIntLEInst(
                scope,
                stmt,
                sizeBytes = 4,
                isSigned = true,
                preload = false
            )

            is TvmCellParseLdule8Inst -> visitLoadIntLEInst(
                scope,
                stmt,
                sizeBytes = 8,
                isSigned = false,
                preload = false
            )

            is TvmCellParseLdile8Inst -> visitLoadIntLEInst(
                scope,
                stmt,
                sizeBytes = 8,
                isSigned = true,
                preload = false
            )

            is TvmCellParsePldule4Inst -> visitLoadIntLEInst(
                scope,
                stmt,
                sizeBytes = 4,
                isSigned = false,
                preload = true
            )

            is TvmCellParsePldile4Inst -> visitLoadIntLEInst(
                scope,
                stmt,
                sizeBytes = 4,
                isSigned = true,
                preload = true
            )

            is TvmCellParsePldule8Inst -> visitLoadIntLEInst(
                scope,
                stmt,
                sizeBytes = 8,
                isSigned = false,
                preload = true
            )

            is TvmCellParsePldile8Inst -> visitLoadIntLEInst(
                scope,
                stmt,
                sizeBytes = 8,
                isSigned = true,
                preload = true
            )

            is TvmCellParseLduxInst -> visitLoadIntXInst(scope, stmt, isSigned = false, preload = false)
            is TvmCellParseLdixInst -> visitLoadIntXInst(scope, stmt, isSigned = true, preload = false)
            is TvmCellParsePlduxInst -> visitLoadIntXInst(scope, stmt, isSigned = false, preload = true)
            is TvmCellParsePldixInst -> visitLoadIntXInst(scope, stmt, isSigned = true, preload = true)
            is TvmCellParseLdsliceInst -> visitLoadSliceInst(scope, stmt, stmt.c + 1, preload = false)
            is TvmCellParseLdsliceAltInst -> visitLoadSliceInst(scope, stmt, stmt.c + 1, preload = false)
            is TvmCellParsePldsliceInst -> visitLoadSliceInst(scope, stmt, stmt.c + 1, preload = true)
            is TvmCellParseLdslicexInst -> visitLoadSliceXInst(scope, stmt, preload = false)
            is TvmCellParsePldslicexInst -> visitLoadSliceXInst(scope, stmt, preload = true)
            is TvmCellParseSrefsInst -> visitSizeRefsInst(scope, stmt)
            is TvmCellParseSbitsInst -> visitSizeBitsInst(scope, stmt)
            is TvmCellParseSbitrefsInst -> visitSizeBitRefsInst(scope, stmt)
            else -> TODO("Unknown stmt: $stmt")
        }
    }

    fun visitCellBuildInst(
        scope: TvmStepScope,
        stmt: TvmCellBuildInst
    ) {
        when (stmt) {
            is TvmCellBuildEndcInst -> visitEndCellInst(scope, stmt)
            is TvmCellBuildNewcInst -> visitNewCellInst(scope, stmt)
            is TvmCellBuildStuInst -> visitStoreIntInst(scope, stmt, stmt.c + 1, false)
            is TvmCellBuildStiInst -> visitStoreIntInst(scope, stmt, stmt.c + 1, true)
            is TvmCellBuildStuxInst -> visitStoreIntXInst(scope, stmt, false)
            is TvmCellBuildStixInst -> visitStoreIntXInst(scope, stmt, true)
            else -> TODO("$stmt")
        }
    }

    private fun checkOutOfRange(notOutOfRangeExpr: UBoolExpr, scope: TvmStepScope): Unit? = scope.fork(
        condition = notOutOfRangeExpr,
        blockOnFalseState = setFailure(TvmIntegerOutOfRange)
    )

    private fun visitLoadRefInst(scope: TvmStepScope, stmt: TvmCellParseLdrefInst) {
        scope.consumeDefaultGas(stmt)

        val slice = scope.calcOnState { stack.takeLastSlice() }
        val updatedSlice = scope.calcOnState {
            memory.allocConcrete(TvmSliceType).also { sliceCopy(slice, it) }
        }

        scope.doWithState {
            val ref = scope.sliceLoadNextRef(slice) ?: return@doWithState
            sliceMoveRefPtr(updatedSlice)

            stack.add(ref, TvmCellType)
            stack.add(updatedSlice, TvmSliceType)

            newStmt(stmt.nextStmt())
        }
    }

    private fun visitEndSliceInst(scope: TvmStepScope, stmt: TvmCellParseEndsInst) {
        scope.doWithState { consumeGas(18) } // complex gas

        with(ctx) {
            val slice = scope.calcOnState { stack.takeLastSlice() }

            val cell = scope.calcOnState { memory.readField(slice, TvmContext.sliceCellField, addressSort) }

            val dataLength = scope.calcOnState { memory.readField(cell, TvmContext.cellDataLengthField, sizeSort) }
            val refsLength = scope.calcOnState { memory.readField(cell, TvmContext.cellRefsLengthField, sizeSort) }
            val dataPos = scope.calcOnState { memory.readField(slice, TvmContext.sliceDataPosField, sizeSort) }
            val refsPos = scope.calcOnState { memory.readField(slice, TvmContext.sliceRefPosField, sizeSort) }

            val isRemainingDataEmptyConstraint = mkSizeGeExpr(dataPos, dataLength)
            val areRemainingRefsEmpty = mkSizeGeExpr(refsPos, refsLength)

            checkCellUnderflow(mkAnd(isRemainingDataEmptyConstraint, areRemainingRefsEmpty), scope) ?: return

            scope.doWithState {
                newStmt(stmt.nextStmt())
            }
        }
    }

    private fun TvmState.visitLoadInstEnd(
        stmt: TvmCellParseInst,
        slice: UHeapRef,
        sizeBits: UExpr<TvmSizeSort>,
        preload: Boolean,
    ) {
        if (!preload) {
            val updatedSlice = memory.allocConcrete(TvmSliceType).also { sliceCopy(slice, it) }
            sliceMoveDataPtr(updatedSlice, sizeBits)
            stack.add(updatedSlice, TvmSliceType)
        }

        newStmt(stmt.nextStmt())
    }

    private fun visitLoadIntInst(
        scope: TvmStepScope,
        stmt: TvmCellParseInst,
        sizeBits: Int,
        isSigned: Boolean,
        preload: Boolean,
    ): Unit = with(ctx) {
        scope.consumeDefaultGas(stmt)

        val slice = scope.calcOnState { stack.takeLastSlice() }
        check(sizeBits in 1..256) { "Unexpected bits size $sizeBits" }

        val value = scope.sliceLoadDataBits(slice, sizeBits) ?: return
        val extendedValue = if (isSigned) {
            value.signedExtendToInteger()
        } else {
            value.unsignedExtendToInteger()
        }

        scope.doWithState {
            stack.add(extendedValue, TvmIntegerType)
            visitLoadInstEnd(stmt, slice, mkSizeExpr(sizeBits), preload)
        }
    }

    private fun visitLoadIntXInst(
        scope: TvmStepScope,
        stmt: TvmCellParseInst,
        isSigned: Boolean,
        preload: Boolean,
    ): Unit = with(ctx) {
        scope.consumeDefaultGas(stmt)

        val (sizeBits, slice) = scope.calcOnState { stack.takeLastInt() to stack.takeLastSlice() }
        val bitsUpperBound = if (isSigned) TvmContext.INT_BITS else TvmContext.INT_BITS - 1u
        val notOutOfRangeExpr = mkAnd(
            mkBvSignedLessOrEqualExpr(zeroValue, sizeBits),
            mkBvSignedLessOrEqualExpr(sizeBits, bitsUpperBound.toInt().toBv257())
        )
        checkOutOfRange(notOutOfRangeExpr, scope) ?: return

        val value = scope.sliceLoadInt(slice, sizeBits, isSigned) ?: return

        scope.doWithState {
            stack.add(value, TvmIntegerType)
            visitLoadInstEnd(stmt, slice, sizeBits.extractToSizeSort(), preload)
        }
    }

    private fun visitLoadIntLEInst(
        scope: TvmStepScope,
        stmt: TvmCellParseInst,
        sizeBytes: Int,
        isSigned: Boolean,
        preload: Boolean,
    ) = with(ctx) {
        scope.consumeDefaultGas(stmt)

        val slice = scope.calcOnState { stack.takeLastSlice() }
        val sizeBits = sizeBytes * Byte.SIZE_BITS

        val value = scope.sliceLoadDataBits(slice, sizeBits) ?: return
        val bytes = List(sizeBytes) { byteIdx ->
            val high = sizeBits - 1 - byteIdx * Byte.SIZE_BITS
            val low = sizeBits - (byteIdx + 1) * Byte.SIZE_BITS

            mkBvExtractExpr(high, low, value)
        }
        val res = bytes.reduce { acc, el ->
            mkBvConcatExpr(el, acc)
        }

        val extendedRes = if (isSigned) {
            res.signedExtendToInteger()
        } else {
            res.unsignedExtendToInteger()
        }

        scope.doWithState {
            stack.add(extendedRes, TvmIntegerType)
            visitLoadInstEnd(stmt, slice, mkSizeExpr(sizeBits), preload)
        }
    }

    private fun TvmStepScope.allocEmptyCell() = calcOnStateCtx {
        memory.allocConcrete(TvmCellType).also { cell ->
            memory.writeField(cell, TvmContext.cellDataField, cellDataSort, mkBv(0, cellDataSort), trueExpr)
            memory.writeField(cell, TvmContext.cellDataLengthField, sizeSort, zeroSizeExpr, trueExpr)
            memory.writeField(cell, TvmContext.cellRefsLengthField, sizeSort, zeroSizeExpr, trueExpr)
        }
    }

    private fun TvmStepScope.allocSliceFromCell(cell: UHeapRef) = calcOnStateCtx {
        memory.allocConcrete(TvmSliceType).also { slice ->
            memory.writeField(slice, TvmContext.sliceCellField, addressSort, cell, trueExpr)
            memory.writeField(slice, TvmContext.sliceDataPosField, sizeSort, zeroSizeExpr, trueExpr)
            memory.writeField(slice, TvmContext.sliceRefPosField, sizeSort, zeroSizeExpr, trueExpr)
        }
    }

    private fun visitLoadSliceInst(
        scope: TvmStepScope,
        stmt: TvmCellParseInst,
        sizeBits: Int,
        preload: Boolean
    ) = with(ctx) {
        scope.consumeDefaultGas(stmt)

        val slice = scope.calcOnState { stack.takeLastSlice() }
        check(sizeBits in 1..256) { "Unexpected bits size $sizeBits" }

        val bits = scope.sliceLoadDataBits(slice, sizeBits) ?: return
        val cell = scope.allocEmptyCell()

        scope.builderStoreDataBits(cell, bits, mkSizeExpr(bits.sort.sizeBits.toInt())) ?: return

        val resultSlice = scope.allocSliceFromCell(cell)

        scope.doWithState {
            stack.add(resultSlice, TvmSliceType)
            visitLoadInstEnd(stmt, slice, mkSizeExpr(sizeBits), preload)
        }
    }

    private fun visitLoadSliceXInst(
        scope: TvmStepScope,
        stmt: TvmCellParseInst,
        preload: Boolean
    ) = with(ctx) {
        scope.consumeDefaultGas(stmt)

        val (sizeBits, slice) = scope.calcOnState { stack.takeLastInt() to stack.takeLastSlice() }

        val notOutOfRangeExpr = unsignedIntegerFitsBits(sizeBits, bits = 10u)
        checkOutOfRange(notOutOfRangeExpr, scope) ?: return@with

        val bits = scope.sliceLoadDataBits(slice, sizeBits.extractToSizeSort()) ?: return
        val cell = scope.allocEmptyCell()

        scope.builderStoreDataBits(cell, bits, sizeBits.extractToSizeSort()) ?: return

        val resultSlice = scope.allocSliceFromCell(cell)

        scope.doWithState {
            stack.add(resultSlice, TvmSliceType)
            visitLoadInstEnd(stmt, slice, sizeBits.extractToSizeSort(), preload)
        }
    }

    private fun visitCellToSliceInst(
        scope: TvmStepScope,
        stmt: TvmCellParseCtosInst
    ) {
        /**
         * todo: Transforming a cell into a slice costs 100 gas units if the cell is loading
         * for the first time and 25 for subsequent loads during the same transaction
         * */
        scope.doWithState { consumeGas(118) }

        val cell = scope.calcOnStateCtx { stack.takeLastCell() }

        val slice = scope.allocSliceFromCell(cell)

        scope.doWithState {
            stack.add(slice, TvmSliceType)
            newStmt(stmt.nextStmt())
        }
    }

    private fun getSliceRemainingRefsCount(
        scope: TvmStepScope,
        slice: UHeapRef
    ): UExpr<TvmSizeSort> = scope.calcOnStateCtx {
        val cell = memory.readField(slice, TvmContext.sliceCellField, addressSort)
        val refsLength = memory.readField(cell, TvmContext.cellRefsLengthField, sizeSort)
        val refsPos = memory.readField(slice, TvmContext.sliceRefPosField, sizeSort)

        mkBvSubExpr(refsLength, refsPos)
    }

    private fun getSliceRemainingBitsCount(
        scope: TvmStepScope,
        slice: UHeapRef
    ): UExpr<TvmSizeSort> = scope.calcOnStateCtx {
        val cell = memory.readField(slice, TvmContext.sliceCellField, addressSort)
        val dataLength = memory.readField(cell, TvmContext.cellDataLengthField, sizeSort)
        val dataPos = memory.readField(slice, TvmContext.sliceDataPosField, sizeSort)

        mkBvSubExpr(dataLength, dataPos)
    }

    private fun visitSizeRefsInst(
        scope: TvmStepScope,
        stmt: TvmCellParseSrefsInst
    ) {
        scope.consumeDefaultGas(stmt)

        scope.doWithStateCtx {
            val slice = stack.takeLastSlice()
            val result = getSliceRemainingRefsCount(scope, slice)

            stack.add(result.signedExtendToInteger(), TvmIntegerType)
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitSizeBitsInst(scope: TvmStepScope, stmt: TvmCellParseSbitsInst) {
        scope.consumeDefaultGas(stmt)

        scope.doWithStateCtx {
            val slice = stack.takeLastSlice()
            val result = getSliceRemainingBitsCount(scope, slice)

            stack.add(result.signedExtendToInteger(), TvmIntegerType)
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitSizeBitRefsInst(scope: TvmStepScope, stmt: TvmCellParseSbitrefsInst) {
        scope.consumeDefaultGas(stmt)

        scope.doWithStateCtx {
            val slice = stack.takeLastSlice()
            val sizeBits = getSliceRemainingBitsCount(scope, slice)
            val sizeRefs = getSliceRemainingRefsCount(scope, slice)

            stack.add(sizeBits.signedExtendToInteger(), TvmIntegerType)
            stack.add(sizeRefs.signedExtendToInteger(), TvmIntegerType)
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitStoreIntInst(scope: TvmStepScope, stmt: TvmInst, bits: Int, isSigned: Boolean) = with(ctx) {
        scope.consumeDefaultGas(stmt)

        val (builder, intValue) = scope.calcOnState { stack.takeLastBuilder() to stack.takeLastInt() }
        val updatedBuilder = scope.calcOnState {
            memory.allocConcrete(TvmBuilderType).also { builderCopy(builder, it) }
        }

        val notOutOfRangeExpr = if (isSigned) {
            signedIntegerFitsBits(intValue, bits.toUInt())
        } else {
            unsignedIntegerFitsBits(intValue, bits.toUInt())
        }
        checkOutOfRange(notOutOfRangeExpr, scope) ?: return

        scope.builderStoreInt(updatedBuilder, intValue, bits.toBv257(), isSigned) ?: return@with

        scope.doWithState {
            stack.add(updatedBuilder, TvmBuilderType)
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitStoreIntXInst(scope: TvmStepScope, stmt: TvmInst, isSigned: Boolean) = with(ctx) {
        scope.consumeDefaultGas(stmt)

        val (bits, builder) = scope.calcOnState { stack.takeLastInt() to stack.takeLastBuilder() }
        val updatedBuilder = scope.calcOnState {
            memory.allocConcrete(TvmBuilderType).also { builderCopy(builder, it) }
        }
        val bitsUpperBound = if (isSigned) TvmContext.INT_BITS else TvmContext.INT_BITS - 1u
        val bitsNotOutOfRangeExpr = mkAnd(
            mkBvSignedLessOrEqualExpr(zeroValue, bits),
            mkBvSignedLessOrEqualExpr(bits, bitsUpperBound.toInt().toBv257()),
        )

        checkOutOfRange(bitsNotOutOfRangeExpr, scope) ?: return

        val intValue = scope.calcOnState { stack.takeLastInt() }

        val valueNotOutOfRangeExpr = if (isSigned) {
            signedIntegerFitsBits(intValue, bits)
        } else {
            unsignedIntegerFitsBits(intValue, bits)
        }
        checkOutOfRange(valueNotOutOfRangeExpr, scope) ?: return

        scope.builderStoreInt(updatedBuilder, intValue, bits, isSigned) ?: return@with

        scope.doWithState {
            stack.add(updatedBuilder, TvmBuilderType)
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitNewCellInst(scope: TvmStepScope, stmt: TvmCellBuildNewcInst) {
        scope.consumeDefaultGas(stmt)

        scope.doWithStateCtx {
            val builder = emptyRefValue.emptyBuilder

            stack.add(builder, TvmBuilderType)
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitEndCellInst(scope: TvmStepScope, stmt: TvmCellBuildEndcInst) {
        scope.consumeDefaultGas(stmt)

        val builder = scope.calcOnState { stack.takeLastBuilder() }
        val cell = scope.calcOnState {
            // TODO static or concrete
            memory.allocConcrete(TvmCellType).also { builderCopy(builder, it) }
        }

        scope.doWithState {
            stack.add(cell, TvmCellType)
            newStmt(stmt.nextStmt())
        }
    }
}