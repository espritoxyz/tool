package org.usvm.machine.interpreter

import org.ton.bytecode.TvmExceptionsInst
import org.ton.bytecode.TvmExceptionsThrowShortInst
import org.ton.bytecode.TvmExceptionsThrowargInst
import org.ton.bytecode.TvmExceptionsThrowifInst
import org.ton.bytecode.TvmExceptionsThrowifShortInst
import org.ton.bytecode.TvmExceptionsThrowifnotInst
import org.ton.bytecode.TvmExceptionsThrowifnotShortInst
import org.usvm.machine.TvmContext
import org.usvm.machine.state.TvmUnknownFailure
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.consumeGas
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.setFailure
import org.usvm.machine.state.takeLastInt

class TvmExceptionsInterpreter(private val ctx: TvmContext) {
    fun visitExceptionInst(scope: TvmStepScope, stmt: TvmExceptionsInst) {
        when (stmt) {
            is TvmExceptionsThrowargInst -> scope.doWithState {
                scope.consumeDefaultGas(stmt)

                // TODO push parameter to the stack
                methodResult = TvmUnknownFailure(stmt.n.toUInt())
            }
            is TvmExceptionsThrowShortInst -> scope.doWithState {
                scope.consumeDefaultGas(stmt)

                // TODO push parameter to the stack
                methodResult = TvmUnknownFailure(stmt.n.toUInt())
            }
            is TvmExceptionsThrowifInst -> {
                scope.doWithState { consumeGas(34) }

                doThrowIfInst(scope, stmt, stmt.n, invertCondition = false)
            }
            is TvmExceptionsThrowifShortInst -> {
                scope.doWithState { consumeGas(26) }

                doThrowIfInst(scope, stmt, stmt.n, invertCondition = false)
            }
            is TvmExceptionsThrowifnotInst -> {
                scope.doWithState { consumeGas(34) }

                doThrowIfInst(scope, stmt, stmt.n, invertCondition = true)
            }
            is TvmExceptionsThrowifnotShortInst -> {
                scope.doWithState { consumeGas(26) }

                doThrowIfInst(scope, stmt, stmt.n, invertCondition = true)
            }
            else -> TODO("Unknown stmt: $stmt")
        }
    }

    private fun doThrowIfInst(scope: TvmStepScope, stmt: TvmExceptionsInst, code: Int, invertCondition: Boolean) {
        with(ctx) {
            val flag = scope.calcOnState { stack.takeLastInt() }
            val throwCondition = (flag eq zeroValue).let {
                if (invertCondition) it.not() else it
            }

            scope.fork(
                throwCondition,
                blockOnFalseState = {
                    setFailure(TvmUnknownFailure(code.toUInt()))(this)
                    consumeGas(50)
                }
            ) ?: return

            scope.doWithState { newStmt(stmt.nextStmt()) }
        }
    }
}
