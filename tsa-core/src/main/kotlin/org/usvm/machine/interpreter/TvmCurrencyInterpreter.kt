package org.usvm.machine.interpreter

import org.ton.bytecode.TvmAppCurrencyInst
import org.ton.bytecode.TvmAppCurrencyLdgramsInst
import org.ton.bytecode.TvmAppCurrencyStgramsInst
import org.ton.bytecode.TvmBuilderType
import org.ton.bytecode.TvmIntegerType
import org.ton.bytecode.TvmSliceType
import org.usvm.api.makeSymbolicPrimitive
import org.usvm.machine.TvmContext
import org.usvm.machine.state.builderCopy
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.doWithStateCtx
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.sliceCopy
import org.usvm.machine.state.sliceMoveDataPtr
import org.usvm.machine.state.slicePreloadDataBits
import org.usvm.machine.state.slicePreloadInt
import org.usvm.machine.state.takeLastBuilder
import org.usvm.machine.state.takeLastInt
import org.usvm.machine.state.takeLastSlice
import org.usvm.machine.state.throwTypeCheckError
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

            val length = scope.slicePreloadDataBits(updatedSlice, bits = 4)?.zeroExtendToSort(sizeSort)
                ?: return@doWithStateCtx
            sliceMoveDataPtr(updatedSlice, bits = 4)

            val extendedLength = mkBvShiftLeftExpr(length, shift = mkSizeExpr(3))
            scope.slicePreloadInt(updatedSlice, extendedLength.zeroExtendToSort(int257sort), isSigned = false)
                ?: return@doWithStateCtx

            // TODO read the real value
            val grams = makeSymbolicPrimitive(int257sort)
            scope.assert(mkBvSignedGreaterOrEqualExpr(grams, zeroValue))
                ?: error("Cannot make grams >= 0")

            sliceMoveDataPtr(updatedSlice, extendedLength)

            stack.add(grams, TvmIntegerType)
            stack.add(updatedSlice, TvmSliceType)

            newStmt(stmt.nextStmt())
        }
    }

    private fun visitStoreGrams(scope: TvmStepScope, stmt: TvmAppCurrencyStgramsInst) {
        scope.doWithStateCtx {
            val (grams, builder) = stack.takeLastInt() to stack.takeLastBuilder()
            if (builder == null) {
                throwTypeCheckError(this)
                return@doWithStateCtx
            }

            val updatedBuilder = memory.allocConcrete(TvmBuilderType).also { builderCopy(builder, it) }

            // TODO make a real implementation
            stack.add(updatedBuilder, TvmBuilderType)
            newStmt(stmt.nextStmt())
        }
    }
}
