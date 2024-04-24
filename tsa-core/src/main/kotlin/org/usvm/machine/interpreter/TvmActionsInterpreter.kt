package org.usvm.machine.interpreter

import org.ton.bytecode.TvmAppActionsInst
import org.ton.bytecode.TvmAppActionsRawreserveInst
import org.ton.bytecode.TvmAppActionsSendrawmsgInst
import org.usvm.machine.TvmContext
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.doWithStateCtx
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.takeLastCell
import org.usvm.machine.state.takeLastInt

class TvmActionsInterpreter(private val ctx: TvmContext) {
    fun visitActionsStmt(scope: TvmStepScope, stmt: TvmAppActionsInst) {
        scope.consumeDefaultGas(stmt)

        when (stmt) {
            is TvmAppActionsSendrawmsgInst -> visitSendRawMsgInst(scope, stmt)
            is TvmAppActionsRawreserveInst -> visitRawReserveInst(scope, stmt)
            else -> TODO("$stmt")
        }
    }

    private fun visitSendRawMsgInst(scope: TvmStepScope, stmt: TvmAppActionsSendrawmsgInst) {
        scope.doWithStateCtx {
            val (mode, cell) = stack.takeLastInt() to stack.takeLastCell()

            // TODO make a real implementation
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitRawReserveInst(scope: TvmStepScope, stmt: TvmAppActionsRawreserveInst) {
        scope.doWithState {
            val (mode, value) = stack.takeLastInt() to stack.takeLastInt()

            // TODO make a real implementation
            newStmt(stmt.nextStmt())
        }
    }
}