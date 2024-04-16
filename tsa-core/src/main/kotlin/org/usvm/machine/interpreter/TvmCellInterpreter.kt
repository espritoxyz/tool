package org.usvm.machine.interpreter

import org.ton.bytecode.TvmAliasInst
import org.ton.bytecode.TvmBuilderType
import org.ton.bytecode.TvmCellBuildEndcInst
import org.ton.bytecode.TvmCellBuildInst
import org.ton.bytecode.TvmCellBuildNewcInst
import org.ton.bytecode.TvmCellBuildStiInst
import org.ton.bytecode.TvmCellBuildStixInst
import org.ton.bytecode.TvmCellBuildStrefAltInst
import org.ton.bytecode.TvmCellBuildStrefInst
import org.ton.bytecode.TvmCellBuildStrefqInst
import org.ton.bytecode.TvmCellBuildStrefrInst
import org.ton.bytecode.TvmCellBuildStrefrqInst
import org.ton.bytecode.TvmCellBuildStsliceAltInst
import org.ton.bytecode.TvmCellBuildStsliceInst
import org.ton.bytecode.TvmCellBuildStsliceqInst
import org.ton.bytecode.TvmCellBuildStslicerInst
import org.ton.bytecode.TvmCellBuildStslicerqInst
import org.ton.bytecode.TvmCellBuildStuInst
import org.ton.bytecode.TvmCellBuildStuxInst
import org.ton.bytecode.TvmCellParseCtosInst
import org.ton.bytecode.TvmCellParseEndsInst
import org.ton.bytecode.TvmCellParseInst
import org.ton.bytecode.TvmCellParseLdiAltInst
import org.ton.bytecode.TvmCellParseLdiInst
import org.ton.bytecode.TvmCellParseLdile4Inst
import org.ton.bytecode.TvmCellParseLdile8Inst
import org.ton.bytecode.TvmCellParseLdiqInst
import org.ton.bytecode.TvmCellParseLdixInst
import org.ton.bytecode.TvmCellParseLdixqInst
import org.ton.bytecode.TvmCellParseLdrefInst
import org.ton.bytecode.TvmCellParseLdsliceAltInst
import org.ton.bytecode.TvmCellParseLdsliceInst
import org.ton.bytecode.TvmCellParseLdsliceqInst
import org.ton.bytecode.TvmCellParseLdslicexInst
import org.ton.bytecode.TvmCellParseLdslicexqInst
import org.ton.bytecode.TvmCellParseLduAltInst
import org.ton.bytecode.TvmCellParseLduInst
import org.ton.bytecode.TvmCellParseLdule4Inst
import org.ton.bytecode.TvmCellParseLdule4qInst
import org.ton.bytecode.TvmCellParseLdule8Inst
import org.ton.bytecode.TvmCellParseLduqInst
import org.ton.bytecode.TvmCellParseLduxInst
import org.ton.bytecode.TvmCellParseLduxqInst
import org.ton.bytecode.TvmCellParsePldiInst
import org.ton.bytecode.TvmCellParsePldile4Inst
import org.ton.bytecode.TvmCellParsePldile4qInst
import org.ton.bytecode.TvmCellParsePldile8Inst
import org.ton.bytecode.TvmCellParsePldile8qInst
import org.ton.bytecode.TvmCellParsePldiqInst
import org.ton.bytecode.TvmCellParsePldixInst
import org.ton.bytecode.TvmCellParsePldixqInst
import org.ton.bytecode.TvmCellParsePldsliceInst
import org.ton.bytecode.TvmCellParsePldsliceqInst
import org.ton.bytecode.TvmCellParsePldslicexInst
import org.ton.bytecode.TvmCellParsePldslicexqInst
import org.ton.bytecode.TvmCellParsePlduInst
import org.ton.bytecode.TvmCellParsePldule4Inst
import org.ton.bytecode.TvmCellParsePldule4qInst
import org.ton.bytecode.TvmCellParsePldule8Inst
import org.ton.bytecode.TvmCellParsePldule8qInst
import org.ton.bytecode.TvmCellParsePlduqInst
import org.ton.bytecode.TvmCellParsePlduxInst
import org.ton.bytecode.TvmCellParsePlduxqInst
import org.ton.bytecode.TvmCellParseSbitrefsInst
import org.ton.bytecode.TvmCellParseSbitsInst
import org.ton.bytecode.TvmCellParseSdcutfirstInst
import org.ton.bytecode.TvmCellParseSdskipfirstInst
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
import org.usvm.machine.TvmContext.Companion.MAX_REFS_NUMBER
import org.usvm.machine.TvmContext.Companion.cellDataField
import org.usvm.machine.TvmContext.Companion.cellDataLengthField
import org.usvm.machine.TvmContext.Companion.cellRefsLengthField
import org.usvm.machine.TvmContext.Companion.sliceCellField
import org.usvm.machine.TvmContext.Companion.sliceDataPosField
import org.usvm.machine.TvmContext.Companion.sliceRefPosField
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.allocEmptyCell
import org.usvm.machine.state.allocSliceFromCell
import org.usvm.machine.state.assertDataLengthConstraint
import org.usvm.machine.state.builderCopy
import org.usvm.machine.state.builderStoreDataBits
import org.usvm.machine.state.builderStoreInt
import org.usvm.machine.state.builderStoreNextRef
import org.usvm.machine.state.calcOnStateCtx
import org.usvm.machine.state.checkCellOverflow
import org.usvm.machine.state.checkCellUnderflow
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.consumeGas
import org.usvm.machine.state.doPop
import org.usvm.machine.state.doWithStateCtx
import org.usvm.machine.state.doXchg
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.readCellRef
import org.usvm.machine.state.signedIntegerFitsBits
import org.usvm.machine.state.sliceCopy
import org.usvm.machine.state.sliceMoveDataPtr
import org.usvm.machine.state.sliceMoveRefPtr
import org.usvm.machine.state.slicePreloadDataBits
import org.usvm.machine.state.slicePreloadInt
import org.usvm.machine.state.slicePreloadNextRef
import org.usvm.machine.state.takeLastBuilder
import org.usvm.machine.state.takeLastCell
import org.usvm.machine.state.takeLastInt
import org.usvm.machine.state.takeLastSlice
import org.usvm.machine.state.throwIntegerOutOfRangeError
import org.usvm.machine.state.throwTypeCheckError
import org.usvm.machine.state.unsignedIntegerFitsBits
import org.usvm.machine.state.writeCellRef
import org.usvm.mkSizeAddExpr
import org.usvm.mkSizeExpr
import org.usvm.mkSizeGeExpr
import org.usvm.mkSizeLeExpr
import org.usvm.mkSizeLtExpr
import org.usvm.mkSizeSubExpr
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
            is TvmCellParseLduInst -> visitLoadIntInst(scope, stmt, stmt.c + 1, isSigned = false, preload = false, quiet = false)
            is TvmCellParseLduqInst -> visitLoadIntInst(scope, stmt, stmt.c + 1, isSigned = false, preload = false, quiet = true)
            is TvmCellParseLduAltInst -> visitLoadIntInst(scope, stmt, stmt.c + 1, isSigned = false, preload = false, quiet = false)
            is TvmCellParseLdiInst -> visitLoadIntInst(scope, stmt, stmt.c + 1, isSigned = true, preload = false, quiet = false)
            is TvmCellParseLdiqInst -> visitLoadIntInst(scope, stmt, stmt.c + 1, isSigned = true, preload = false, quiet = true)
            is TvmCellParseLdiAltInst -> visitLoadIntInst(scope, stmt, stmt.c + 1, isSigned = true, preload = false, quiet = false)
            is TvmCellParsePlduInst -> visitLoadIntInst(scope, stmt, stmt.c + 1, isSigned = false, preload = true, quiet = false)
            is TvmCellParsePlduqInst -> visitLoadIntInst(scope, stmt, stmt.c + 1, isSigned = false, preload = true, quiet = true)
            is TvmCellParsePldiInst -> visitLoadIntInst(scope, stmt, stmt.c + 1, isSigned = true, preload = true, quiet = false)
            is TvmCellParsePldiqInst -> visitLoadIntInst(scope, stmt, stmt.c + 1, isSigned = true, preload = true, quiet = true)
            is TvmCellParseLdule4Inst -> visitLoadIntLEInst(scope, stmt, sizeBytes = 4, isSigned = false, preload = false, quiet = false)
            is TvmCellParseLdule4qInst -> visitLoadIntLEInst(scope, stmt, sizeBytes = 4, isSigned = false, preload = false, quiet = true)
            is TvmCellParseLdile4Inst -> visitLoadIntLEInst(scope, stmt, sizeBytes = 4, isSigned = true, preload = false, quiet = false)
            is TvmCellParseLdule8Inst -> visitLoadIntLEInst(scope, stmt, sizeBytes = 8, isSigned = false, preload = false, quiet = false)
            is TvmCellParseLdile8Inst -> visitLoadIntLEInst(scope, stmt, sizeBytes = 8, isSigned = true, preload = false, quiet = false)
            is TvmCellParsePldule4Inst -> visitLoadIntLEInst(scope, stmt, sizeBytes = 4, isSigned = false, preload = true, quiet = false)
            is TvmCellParsePldule4qInst -> visitLoadIntLEInst(scope, stmt, sizeBytes = 4, isSigned = false, preload = true, quiet = true)
            is TvmCellParsePldile4Inst -> visitLoadIntLEInst(scope, stmt, sizeBytes = 4, isSigned = true, preload = true, quiet = false)
            is TvmCellParsePldile4qInst -> visitLoadIntLEInst(scope, stmt, sizeBytes = 4, isSigned = true, preload = true, quiet = true)
            is TvmCellParsePldule8Inst -> visitLoadIntLEInst(scope, stmt, sizeBytes = 8, isSigned = false, preload = true, quiet = false)
            is TvmCellParsePldule8qInst -> visitLoadIntLEInst(scope, stmt, sizeBytes = 8, isSigned = false, preload = true, quiet = true)
            is TvmCellParsePldile8Inst -> visitLoadIntLEInst(scope, stmt, sizeBytes = 8, isSigned = true, preload = true, quiet = false)
            is TvmCellParsePldile8qInst -> visitLoadIntLEInst(scope, stmt, sizeBytes = 8, isSigned = true, preload = true, quiet = true)
            is TvmCellParseLduxInst -> visitLoadIntXInst(scope, stmt, isSigned = false, preload = false, quiet = false)
            is TvmCellParseLduxqInst -> visitLoadIntXInst(scope, stmt, isSigned = false, preload = false, quiet = true)
            is TvmCellParseLdixInst -> visitLoadIntXInst(scope, stmt, isSigned = true, preload = false, quiet = false)
            is TvmCellParseLdixqInst -> visitLoadIntXInst(scope, stmt, isSigned = true, preload = false, quiet = true)
            is TvmCellParsePlduxInst -> visitLoadIntXInst(scope, stmt, isSigned = false, preload = true, quiet = false)
            is TvmCellParsePlduxqInst -> visitLoadIntXInst(scope, stmt, isSigned = false, preload = true, quiet = true)
            is TvmCellParsePldixInst -> visitLoadIntXInst(scope, stmt, isSigned = true, preload = true, quiet = false)
            is TvmCellParsePldixqInst -> visitLoadIntXInst(scope, stmt, isSigned = true, preload = true, quiet = true)
            is TvmCellParseLdsliceInst -> visitLoadSliceInst(scope, stmt, stmt.c + 1, preload = false, quiet = false)
            is TvmCellParseLdsliceqInst -> visitLoadSliceInst(scope, stmt, stmt.c + 1, preload = false, quiet = true)
            is TvmCellParseLdsliceAltInst -> visitLoadSliceInst(scope, stmt, stmt.c + 1, preload = false, quiet = false)
            is TvmCellParsePldsliceInst -> visitLoadSliceInst(scope, stmt, stmt.c + 1, preload = true, quiet = false)
            is TvmCellParsePldsliceqInst -> visitLoadSliceInst(scope, stmt, stmt.c + 1, preload = true, quiet = true)
            is TvmCellParseLdslicexInst -> visitLoadSliceXInst(scope, stmt, preload = false, quiet = false)
            is TvmCellParseLdslicexqInst -> visitLoadSliceXInst(scope, stmt, preload = false, quiet = true)
            is TvmCellParsePldslicexInst, is TvmCellParseSdcutfirstInst -> visitLoadSliceXInst(scope, stmt, preload = true, quiet = false)
            is TvmCellParsePldslicexqInst -> visitLoadSliceXInst(scope, stmt, preload = true, quiet = true)
            is TvmCellParseSrefsInst -> visitSizeRefsInst(scope, stmt)
            is TvmCellParseSbitsInst -> visitSizeBitsInst(scope, stmt)
            is TvmCellParseSbitrefsInst -> visitSizeBitRefsInst(scope, stmt)
            is TvmCellParseSdskipfirstInst -> {
                visitLoadSliceXInst(scope, stmt, preload = false, quiet = false)
                    ?: return
                doPop(scope, 1)
            }
            is TvmAliasInst -> visitCellParseInst(scope, stmt.resolveAlias() as TvmCellParseInst)
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
            is TvmCellBuildStsliceInst -> {
                scope.consumeDefaultGas(stmt)

                scope.doStoreSlice(stmt, quiet = false)
            }
            is TvmCellBuildStsliceAltInst -> {
                scope.consumeDefaultGas(stmt)

                scope.doStoreSlice(stmt, quiet = false)
            }
            is TvmCellBuildStslicerInst -> {
                scope.consumeDefaultGas(stmt)

                doXchg(scope, first = 0, second = 1)
                scope.doStoreSlice(stmt, quiet = false)
            }
            is TvmCellBuildStsliceqInst -> {
                scope.consumeDefaultGas(stmt)

                scope.doStoreSlice(stmt, quiet = true)
            }
            is TvmCellBuildStslicerqInst -> {
                scope.consumeDefaultGas(stmt)

                doXchg(scope, first = 0, second = 1)
                scope.doStoreSlice(stmt, quiet = true)
            }
            is TvmCellBuildStrefInst -> visitStoreRefInst(scope, stmt, quiet = false)
            is TvmCellBuildStrefqInst -> visitStoreRefInst(scope, stmt, quiet = true)
            is TvmCellBuildStrefAltInst -> visitStoreRefInst(scope, stmt, quiet = false)
            is TvmCellBuildStrefrInst -> {
                doXchg(scope, first = 1, second = 0)
                visitStoreRefInst(scope, stmt, quiet = false)
            }
            is TvmCellBuildStrefrqInst -> {
                doXchg(scope, first = 1, second = 0)
                visitStoreRefInst(scope, stmt, quiet = true)
            }
            else -> TODO("$stmt")
        }
    }

    private fun checkOutOfRange(notOutOfRangeExpr: UBoolExpr, scope: TvmStepScope): Unit? = scope.fork(
        condition = notOutOfRangeExpr,
        blockOnFalseState = throwIntegerOutOfRangeError
    )

    private fun visitLoadRefInst(scope: TvmStepScope, stmt: TvmCellParseLdrefInst) {
        scope.consumeDefaultGas(stmt)

        val slice = scope.calcOnState { stack.takeLastSlice() }
        if (slice == null) {
            scope.doWithState(throwTypeCheckError)
            return
        }

        val updatedSlice = scope.calcOnState {
            memory.allocConcrete(TvmSliceType).also { sliceCopy(slice, it) }
        }

        scope.doWithState {
            val ref = scope.slicePreloadNextRef(slice) ?: return@doWithState
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
            if (slice == null) {
                scope.doWithState(throwTypeCheckError)
                return
            }

            val cell = scope.calcOnState { memory.readField(slice, sliceCellField, addressSort) }

            val dataLength = scope.calcOnState { memory.readField(cell, cellDataLengthField, sizeSort) }
            val refsLength = scope.calcOnState { memory.readField(cell, cellRefsLengthField, sizeSort) }
            val dataPos = scope.calcOnState { memory.readField(slice, sliceDataPosField, sizeSort) }
            val refsPos = scope.calcOnState { memory.readField(slice, sliceRefPosField, sizeSort) }

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
        quiet: Boolean,
    ) {
        if (!preload) {
            val updatedSlice = memory.allocConcrete(TvmSliceType).also { sliceCopy(slice, it) }
            sliceMoveDataPtr(updatedSlice, sizeBits)
            stack.add(updatedSlice, TvmSliceType)
        }

        if (quiet) {
            stack.add(ctx.oneValue, TvmIntegerType)
        }

        newStmt(stmt.nextStmt())
    }

    private fun visitLoadIntInst(
        scope: TvmStepScope,
        stmt: TvmCellParseInst,
        sizeBits: Int,
        isSigned: Boolean,
        preload: Boolean,
        quiet: Boolean,
    ): Unit = with(ctx) {
        scope.consumeDefaultGas(stmt)

        check(sizeBits in 1..256) { "Unexpected bits size $sizeBits" }

        val slice = scope.calcOnState { stack.takeLastSlice() }
        if (slice == null) {
            scope.doWithState(throwTypeCheckError)
            return
        }

        val value = scope.slicePreloadDataBits(slice, sizeBits) ?: return
        val extendedValue = if (isSigned) {
            value.signedExtendToInteger()
        } else {
            value.unsignedExtendToInteger()
        }

        scope.doWithState {
            stack.add(extendedValue, TvmIntegerType)
            visitLoadInstEnd(stmt, slice, mkSizeExpr(sizeBits), preload, quiet)
        }
    }

    private fun visitLoadIntXInst(
        scope: TvmStepScope,
        stmt: TvmCellParseInst,
        isSigned: Boolean,
        preload: Boolean,
        quiet: Boolean,
    ): Unit = with(ctx) {
        scope.consumeDefaultGas(stmt)

        val (sizeBits, slice) = scope.calcOnState { stack.takeLastInt() to stack.takeLastSlice() }
        if (slice == null) {
            scope.doWithState(throwTypeCheckError)
            return
        }

        val bitsUpperBound = if (isSigned) TvmContext.INT_BITS else TvmContext.INT_BITS - 1u
        val notOutOfRangeExpr = mkAnd(
            mkBvSignedLessOrEqualExpr(zeroValue, sizeBits),
            mkBvSignedLessOrEqualExpr(sizeBits, bitsUpperBound.toInt().toBv257())
        )
        checkOutOfRange(notOutOfRangeExpr, scope) ?: return

        val value = scope.slicePreloadInt(slice, sizeBits, isSigned) ?: return

        scope.doWithState {
            stack.add(value, TvmIntegerType)
            visitLoadInstEnd(stmt, slice, sizeBits.extractToSizeSort(), preload, quiet)
        }
    }

    private fun visitLoadIntLEInst(
        scope: TvmStepScope,
        stmt: TvmCellParseInst,
        sizeBytes: Int,
        isSigned: Boolean,
        preload: Boolean,
        quiet: Boolean,
    ) = with(ctx) {
        scope.consumeDefaultGas(stmt)

        val slice = scope.calcOnState { stack.takeLastSlice() }
        if (slice == null) {
            scope.doWithState(throwTypeCheckError)
            return
        }

        val sizeBits = sizeBytes * Byte.SIZE_BITS

        val value = scope.slicePreloadDataBits(slice, sizeBits) ?: return
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
            visitLoadInstEnd(stmt, slice, mkSizeExpr(sizeBits), preload, quiet)
        }
    }

    private fun visitLoadSliceInst(
        scope: TvmStepScope,
        stmt: TvmCellParseInst,
        sizeBits: Int,
        preload: Boolean,
        quiet: Boolean,
    ) = with(ctx) {
        scope.consumeDefaultGas(stmt)

        check(sizeBits in 1..256) { "Unexpected bits size $sizeBits" }

        val slice = scope.calcOnState { stack.takeLastSlice() }
        if (slice == null) {
            scope.doWithState(throwTypeCheckError)
            return
        }

        val bits = scope.slicePreloadDataBits(slice, sizeBits) ?: return
        val cell = scope.allocEmptyCell()

        scope.builderStoreDataBits(cell, bits, mkSizeExpr(bits.sort.sizeBits.toInt())) ?: return

        val resultSlice = scope.allocSliceFromCell(cell)

        scope.doWithState {
            stack.add(resultSlice, TvmSliceType)
            visitLoadInstEnd(stmt, slice, mkSizeExpr(sizeBits), preload, quiet)
        }
    }

    private fun visitLoadSliceXInst(
        scope: TvmStepScope,
        stmt: TvmCellParseInst,
        preload: Boolean,
        quiet: Boolean
    ): Unit? = with(ctx) {
        scope.consumeDefaultGas(stmt)

        val (sizeBits, slice) = scope.calcOnState { stack.takeLastInt() to stack.takeLastSlice() }
        if (slice == null) {
            scope.doWithState(throwTypeCheckError)
            return@with
        }

        val notOutOfRangeExpr = unsignedIntegerFitsBits(sizeBits, bits = 10u)
        checkOutOfRange(notOutOfRangeExpr, scope)
            ?: return null

        val quietBlock: (TvmState.() -> Unit)? = if (!quiet) null else fun TvmState.() {
            stack.add(slice, TvmSliceType)
            stack.add(zeroValue, TvmIntegerType)
            newStmt(stmt.nextStmt())
        }
        val bits = scope.slicePreloadDataBits(
            slice,
            sizeBits.extractToSizeSort(),
            quietBlock = quietBlock
        ) ?: return null

        val cell = scope.allocEmptyCell()
        scope.builderStoreDataBits(cell, bits, sizeBits.extractToSizeSort())
            ?: error("Cannot write $sizeBits bits to the empty builder")
        val resultSlice = scope.allocSliceFromCell(cell)

        scope.doWithState {
            stack.add(resultSlice, TvmSliceType)
            visitLoadInstEnd(stmt, slice, sizeBits.extractToSizeSort(), preload, quiet)
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
        if (cell == null) {
            scope.doWithState(throwTypeCheckError)
            return
        }

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
        val cell = memory.readField(slice, sliceCellField, addressSort)
        val refsLength = memory.readField(cell, cellRefsLengthField, sizeSort)
        val refsPos = memory.readField(slice, sliceRefPosField, sizeSort)

        mkBvSubExpr(refsLength, refsPos)
    }

    private fun getSliceRemainingBitsCount(
        scope: TvmStepScope,
        slice: UHeapRef
    ): UExpr<TvmSizeSort> = scope.calcOnStateCtx {
        val cell = memory.readField(slice, sliceCellField, addressSort)
        val dataLength = memory.readField(cell, cellDataLengthField, sizeSort)
        val dataPos = memory.readField(slice, sliceDataPosField, sizeSort)

        mkBvSubExpr(dataLength, dataPos)
    }

    private fun visitSizeRefsInst(
        scope: TvmStepScope,
        stmt: TvmCellParseSrefsInst
    ) {
        scope.consumeDefaultGas(stmt)

        scope.doWithStateCtx {
            val slice = stack.takeLastSlice()
            if (slice == null) {
                throwTypeCheckError(this)
                return@doWithStateCtx
            }

            val result = getSliceRemainingRefsCount(scope, slice)

            stack.add(result.signedExtendToInteger(), TvmIntegerType)
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitSizeBitsInst(scope: TvmStepScope, stmt: TvmCellParseSbitsInst) {
        scope.consumeDefaultGas(stmt)

        scope.doWithStateCtx {
            val slice = stack.takeLastSlice()
            if (slice == null) {
                throwTypeCheckError(this)
                return@doWithStateCtx
            }

            val result = getSliceRemainingBitsCount(scope, slice)

            stack.add(result.signedExtendToInteger(), TvmIntegerType)
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitSizeBitRefsInst(scope: TvmStepScope, stmt: TvmCellParseSbitrefsInst) {
        scope.consumeDefaultGas(stmt)

        scope.doWithStateCtx {
            val slice = stack.takeLastSlice()
            if (slice == null) {
                throwTypeCheckError(this)
                return@doWithStateCtx
            }

            val sizeBits = getSliceRemainingBitsCount(scope, slice)
            val sizeRefs = getSliceRemainingRefsCount(scope, slice)

            stack.add(sizeBits.signedExtendToInteger(), TvmIntegerType)
            stack.add(sizeRefs.signedExtendToInteger(), TvmIntegerType)
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitStoreIntInst(scope: TvmStepScope, stmt: TvmInst, bits: Int, isSigned: Boolean) = with(ctx) {
        scope.consumeDefaultGas(stmt)

        val builder = scope.calcOnState { stack.takeLastBuilder() }
        if (builder == null) {
            scope.doWithState(throwTypeCheckError)
            return
        }

        val intValue = scope.calcOnState { stack.takeLastInt() }

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
        if (builder == null) {
            scope.doWithState(throwTypeCheckError)
            return
        }

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
        if (builder == null) {
            scope.doWithState(throwTypeCheckError)
            return
        }

        val cell = scope.calcOnState {
            // TODO static or concrete
            memory.allocConcrete(TvmCellType).also { builderCopy(builder, it) }
        }

        scope.doWithState {
            stack.add(cell, TvmCellType)
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitStoreRefInst(scope: TvmStepScope, stmt: TvmCellBuildInst, quiet: Boolean) {
        scope.consumeDefaultGas(stmt)

        val builder = scope.calcOnState { stack.takeLastBuilder() }
        if (builder == null) {
            scope.doWithState(throwTypeCheckError)
            return
        }

        val cell = scope.calcOnState { stack.takeLastCell() }
        if (cell == null) {
            scope.doWithState(throwTypeCheckError)
            return
        }

        with(ctx) {
            val builderRefsLength = scope.calcOnState { memory.readField(builder, cellRefsLengthField, ctx.sizeSort) }
            val canWriteRefConstraint = mkSizeLtExpr(builderRefsLength, maxRefsLengthSizeExpr)
            val quietBlock: (TvmState.() -> Unit)? = if (!quiet) null else fun TvmState.() {
                stack.add(cell, TvmCellType)
                stack.add(builder, TvmBuilderType)
                stack.add(minusOneValue, TvmIntegerType)

                newStmt(stmt.nextStmt())
            }
            checkCellOverflow(canWriteRefConstraint, scope, quietBlock)
                ?: return

            scope.doWithState {
                val updatedBuilder = memory.allocConcrete(TvmBuilderType).also { builderCopy(builder, it) }
                builderStoreNextRef(updatedBuilder, cell)

                stack.add(updatedBuilder, TvmBuilderType)
                if (quiet) {
                    stack.add(zeroValue, TvmIntegerType)
                }

                newStmt(stmt.nextStmt())
            }
        }
    }

    private fun TvmStepScope.doStoreSlice(stmt: TvmCellBuildInst, quiet: Boolean) {
        val builder = calcOnState { stack.takeLastBuilder() }
        if (builder == null) {
            doWithState(throwTypeCheckError)
            return
        }

        val slice = calcOnState { stack.takeLastSlice() }
        if (slice == null) {
            doWithState(throwTypeCheckError)
            return
        }

        val resultBuilder = calcOnState { memory.allocConcrete(TvmBuilderType).also { builderCopy(builder, it) } }

        with(ctx) {
            val cell = calcOnState { memory.readField(slice, sliceCellField, addressSort) }
            val cellDataLength = calcOnState { memory.readField(cell, cellDataLengthField, sizeSort) }

            assertDataLengthConstraint(cellDataLength)
                ?: error("Cannot ensure correctness for data length in cell $cell")

            val cellData = calcOnState { memory.readField(cell, cellDataField, cellDataSort) }
            val dataPosition = calcOnState { memory.readField(slice, sliceDataPosField, sizeSort) }

            val bitsToWriteLength = mkSizeSubExpr(cellDataLength, dataPosition)

            val cellRefsSize = calcOnState { memory.readField(cell, cellRefsLengthField, sizeSort) }
            val refsPosition = calcOnState { memory.readField(slice, sliceRefPosField, sizeSort) }
            val builderRefsSize = calcOnState { memory.readField(builder, cellRefsLengthField, sizeSort) }

            val refsToWriteSize = mkBvSubExpr(cellRefsSize, refsPosition)
            val resultingRefsSize = mkBvAddExpr(builderRefsSize, refsToWriteSize)
            val canWriteRefsConstraint = mkSizeLeExpr(resultingRefsSize, maxRefsLengthSizeExpr)

            val quietBlock: (TvmState.() -> Unit)? = if (!quiet) null else fun TvmState.() {
                stack.add(slice, TvmSliceType)
                stack.add(builder, TvmBuilderType)
                stack.add(minusOneValue, TvmIntegerType)

                newStmt(stmt.nextStmt())
            }

            checkCellOverflow(canWriteRefsConstraint, this@doStoreSlice, quietBlock)
                ?: return

            builderStoreDataBits(resultBuilder, cellData, bitsToWriteLength, quietBlock)
                ?: return

            doWithState {
                for (i in 0 until MAX_REFS_NUMBER) {
                    val sliceRef = readCellRef(cell, mkSizeExpr(i))
                    writeCellRef(resultBuilder, mkSizeAddExpr(builderRefsSize, mkSizeExpr(i)), sliceRef)
                }

                memory.writeField(resultBuilder, cellRefsLengthField, sizeSort, resultingRefsSize, guard = trueExpr)

                stack.add(resultBuilder, TvmBuilderType)
                if (quiet) {
                    stack.add(zeroValue, TvmIntegerType)
                }

                newStmt(stmt.nextStmt())
            }
        }
    }
}
