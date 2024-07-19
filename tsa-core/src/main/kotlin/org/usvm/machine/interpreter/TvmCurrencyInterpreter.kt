package org.usvm.machine.interpreter

import org.ton.bytecode.TvmAppCurrencyInst
import org.ton.bytecode.TvmAppCurrencyLdgramsInst
import org.ton.bytecode.TvmAppCurrencyStgramsInst
import org.usvm.machine.types.TvmBuilderType
import org.usvm.machine.types.TvmIntegerType
import org.usvm.machine.types.TvmSliceType
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmStepScope
import org.usvm.machine.state.addOnStack
import org.usvm.machine.state.builderCopy
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.doWithStateCtx
import org.usvm.machine.state.loadDataBitsFromCellWithoutChecks
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.sliceCopy
import org.usvm.machine.state.sliceMoveDataPtr
import org.usvm.machine.state.slicePreloadDataBits
import org.usvm.machine.state.slicePreloadInt
import org.usvm.machine.state.takeLastBuilder
import org.usvm.machine.state.takeLastIntOrNull
import org.usvm.machine.state.takeLastSlice
import org.usvm.machine.types.TvmSymbolicCellDataCoins
import org.usvm.machine.types.makeSliceTypeLoad
import org.usvm.mkSizeExpr
import org.usvm.sizeSort

class TvmCurrencyInterpreter(private val ctx: TvmContext) {
    fun visitCurrencyInst(scope: TvmStepScope, stmt: TvmAppCurrencyInst) {
        scope.consumeDefaultGas(stmt)

        when (stmt) {
            is TvmAppCurrencyLdgramsInst -> visitLoadGramsInst(scope, stmt)
            is TvmAppCurrencyStgramsInst -> visitStoreGrams(scope, stmt)
            else -> TODO("$stmt")
        }
    }

    private fun visitLoadGramsInst(scope: TvmStepScope, stmt: TvmAppCurrencyLdgramsInst) {
        scope.doWithStateCtx {
            val slice = stack.takeLastSlice()
            if (slice == null) {
                throwTypeCheckError(this)
                return@doWithStateCtx
            }

            val updatedSlice = memory.allocConcrete(TvmSliceType).also { sliceCopy(slice, it) }

            val lengthPeek = scope.calcOnState { loadDataBitsFromCellWithoutChecks(slice, sizeBits = 4) }
            scope.makeSliceTypeLoad(slice, TvmSymbolicCellDataCoins(ctx, lengthPeek.zeroExtendToSort(sizeSort)))
                ?: return@doWithStateCtx

            val length = scope.slicePreloadDataBits(updatedSlice, bits = 4)?.zeroExtendToSort(sizeSort)
                ?: return@doWithStateCtx

            sliceMoveDataPtr(updatedSlice, bits = 4)

            val extendedLength = mkBvShiftLeftExpr(length, shift = mkSizeExpr(3))
            val grams = scope.slicePreloadInt(updatedSlice, extendedLength.zeroExtendToSort(int257sort), isSigned = false)
                ?: return@doWithStateCtx

            sliceMoveDataPtr(updatedSlice, extendedLength)

            addOnStack(grams, TvmIntegerType)
            addOnStack(updatedSlice, TvmSliceType)

            newStmt(stmt.nextStmt())
        }
    }

    private fun visitStoreGrams(scope: TvmStepScope, stmt: TvmAppCurrencyStgramsInst) {
        scope.doWithStateCtx {
            val (grams, builder) = stack.takeLastIntOrNull() to stack.takeLastBuilder()
            if (builder == null) {
                throwTypeCheckError(this)
                return@doWithStateCtx
            }

            val updatedBuilder = memory.allocConcrete(TvmBuilderType).also { builderCopy(builder, it) }

            // TODO make a real implementation
            addOnStack(updatedBuilder, TvmBuilderType)
            newStmt(stmt.nextStmt())
        }
    }
}
