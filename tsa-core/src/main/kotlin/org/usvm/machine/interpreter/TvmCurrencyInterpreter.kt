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
import org.usvm.machine.state.builderStoreGrams
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.doWithStateCtx
import org.usvm.machine.state.loadDataBitsFromCellWithoutChecks
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.sliceCopy
import org.usvm.machine.state.sliceLoadGrams
import org.usvm.machine.state.takeLastBuilder
import org.usvm.machine.state.takeLastIntOrThrowTypeError
import org.usvm.machine.state.takeLastSlice

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
            val grams = scope.sliceLoadGrams(updatedSlice) ?: return@doWithStateCtx

            addOnStack(grams, TvmIntegerType)
            addOnStack(updatedSlice, TvmSliceType)

            newStmt(stmt.nextStmt())
        }
    }

    private fun visitStoreGrams(scope: TvmStepScope, stmt: TvmAppCurrencyStgramsInst) = with(ctx) {
        val grams = scope.takeLastIntOrThrowTypeError()
            ?: return
        val builder = scope.calcOnState { stack.takeLastBuilder() }
            ?: return scope.calcOnState(throwTypeCheckError)

        val updatedBuilder = scope.calcOnState {
            memory.allocConcrete(TvmBuilderType).also { builderCopy(builder, it) }
        }
        scope.builderStoreGrams(updatedBuilder, grams) ?: return@with

        scope.doWithStateCtx {
            addOnStack(updatedBuilder, TvmBuilderType)
            newStmt(stmt.nextStmt())
        }
    }
}
