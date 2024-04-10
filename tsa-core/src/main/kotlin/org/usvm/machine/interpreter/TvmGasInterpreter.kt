package org.usvm.machine.interpreter

import org.ton.bytecode.TvmAppGasAcceptInst
import org.ton.bytecode.TvmAppGasCommitInst
import org.ton.bytecode.TvmAppGasGasconsumedInst
import org.ton.bytecode.TvmAppGasInst
import org.ton.bytecode.TvmAppGasSetgaslimitInst
import org.ton.bytecode.TvmIntegerType
import org.usvm.machine.TvmContext
import org.usvm.machine.state.TvmOutOfGas
import org.usvm.machine.state.calcConsumedGas
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.doWithStateCtx
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.setFailure
import org.usvm.machine.state.takeLastInt
import org.usvm.mkSizeAddExpr

class TvmGasInterpreter(private val ctx: TvmContext) {
    fun visitGasInst(scope: TvmStepScope, stmt: TvmAppGasInst) {
        scope.consumeDefaultGas(stmt)

        when (stmt) {
            is TvmAppGasAcceptInst -> {
                // TODO Do nothing for now
                scope.doWithState { newStmt(stmt.nextStmt()) }
            }
            is TvmAppGasCommitInst -> {
                // TODO Do nothing for now
                scope.doWithState { newStmt(stmt.nextStmt()) }
            }
            is TvmAppGasGasconsumedInst -> visitGasConsumedInst(scope, stmt)
            is TvmAppGasSetgaslimitInst -> visitSetGasLimitInst(scope, stmt)
        }
    }

    private fun visitGasConsumedInst(scope: TvmStepScope, stmt: TvmAppGasGasconsumedInst) {
        scope.doWithStateCtx {
            val usedGas = calcConsumedGas()

            stack.add(usedGas, TvmIntegerType)
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitSetGasLimitInst(scope: TvmStepScope, stmt: TvmAppGasSetgaslimitInst) {
        with(ctx) {
            val gasLimit = scope.calcOnState { stack.takeLastInt() }.extractToSizeSort()
            val consumedGas = scope.calcOnState { calcConsumedGas() }

            scope.fork(
                mkBvSignedGreaterOrEqualExpr(gasLimit, consumedGas),
                blockOnFalseState = {
                    setFailure(TvmOutOfGas(consumedGas, gasLimit))(this)
                }
            ) ?: return@with

            scope.doWithState { newStmt(stmt.nextStmt()) }
        }
    }
}
