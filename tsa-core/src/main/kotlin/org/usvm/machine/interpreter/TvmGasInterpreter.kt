package org.usvm.machine.interpreter

import org.ton.bytecode.TvmAppGasAcceptInst
import org.ton.bytecode.TvmAppGasCommitInst
import org.ton.bytecode.TvmAppGasGasconsumedInst
import org.ton.bytecode.TvmAppGasInst
import org.ton.bytecode.TvmAppGasSetgaslimitInst
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmStepScope
import org.usvm.machine.state.TvmCommitedState
import org.usvm.machine.state.TvmOutOfGas
import org.usvm.machine.state.addInt
import org.usvm.machine.state.calcConsumedGas
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.doWithStateCtx
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.setFailure
import org.usvm.machine.state.takeLastIntOrThrowTypeError

class TvmGasInterpreter(private val ctx: TvmContext) {
    fun visitGasInst(scope: TvmStepScope, stmt: TvmAppGasInst) {
        scope.consumeDefaultGas(stmt)

        when (stmt) {
            is TvmAppGasAcceptInst -> {
                // TODO Do nothing for now
                scope.doWithState { newStmt(stmt.nextStmt()) }
            }
            is TvmAppGasCommitInst -> {
                scope.doWithState {
                    commitedState = TvmCommitedState(registers.c4, registers.c5)
                    newStmt(stmt.nextStmt())
                }
            }
            is TvmAppGasGasconsumedInst -> visitGasConsumedInst(scope, stmt)
            is TvmAppGasSetgaslimitInst -> visitSetGasLimitInst(scope, stmt)
        }
    }

    private fun visitGasConsumedInst(scope: TvmStepScope, stmt: TvmAppGasGasconsumedInst) {
        scope.doWithStateCtx {
            val usedGas = calcConsumedGas()

            stack.addInt(usedGas.extractToInt257Sort())
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitSetGasLimitInst(scope: TvmStepScope, stmt: TvmAppGasSetgaslimitInst) {
        with(ctx) {
            val gasLimit = (scope.takeLastIntOrThrowTypeError() ?: return).extractToSizeSort()
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
