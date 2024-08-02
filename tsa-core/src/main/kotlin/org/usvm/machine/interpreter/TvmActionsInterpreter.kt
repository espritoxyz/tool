package org.usvm.machine.interpreter

import org.ton.bytecode.TvmAppActionsInst
import org.ton.bytecode.TvmAppActionsRawreserveInst
import org.ton.bytecode.TvmAppActionsSendrawmsgInst
import org.ton.bytecode.TvmAppActionsSetcodeInst
import org.ton.bytecode.TvmCellValue
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmStepScope
import org.usvm.machine.state.C5Register
import org.usvm.machine.state.allocEmptyCell
import org.usvm.machine.state.builderStoreDataBits
import org.usvm.machine.state.builderStoreGrams
import org.usvm.machine.state.builderStoreInt
import org.usvm.machine.state.builderStoreNextRef
import org.usvm.machine.state.checkOutOfRange
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.doWithStateCtx
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.takeLastCell
import org.usvm.machine.state.takeLastIntOrThrowTypeError
import org.usvm.machine.state.unsignedIntegerFitsBits

class TvmActionsInterpreter(private val ctx: TvmContext) {
    fun visitActionsStmt(scope: TvmStepScope, stmt: TvmAppActionsInst) {
        scope.consumeDefaultGas(stmt)

        when (stmt) {
            is TvmAppActionsSendrawmsgInst -> visitSendRawMsgInst(scope, stmt)
            is TvmAppActionsRawreserveInst -> visitRawReserveInst(scope, stmt)
            is TvmAppActionsSetcodeInst -> visitSetCodeInst(scope, stmt)
            else -> TODO("$stmt")
        }
    }

    private fun visitSendRawMsgInst(scope: TvmStepScope, stmt: TvmAppActionsSendrawmsgInst) = with(ctx) {
        val mode = scope.takeLastIntOrThrowTypeError()
            ?: return@with
        val msg = scope.calcOnState { stack.takeLastCell() }
            ?: return@with scope.doWithState(throwTypeCheckError)

        val notOutOfRangeExpr = unsignedIntegerFitsBits(mode, 8u)
        checkOutOfRange(notOutOfRangeExpr, scope) ?: return

        scope.doWithStateCtx {
            val actions = registers.c5.value.value
            val updatedActions = allocEmptyCell()

            builderStoreNextRef(updatedActions, actions)
            builderStoreDataBits(updatedActions, sendMsgActionTag)
            scope.builderStoreInt(updatedActions, mode, sizeBits = eightValue, isSigned = false) {
                error("Unexpected cell overflow during SENDRAWMSG instruction")
            } ?: return@doWithStateCtx
            builderStoreNextRef(updatedActions, msg)

            registers.c5 = C5Register(TvmCellValue(updatedActions))

            newStmt(stmt.nextStmt())
        }
    }

    private fun visitRawReserveInst(scope: TvmStepScope, stmt: TvmAppActionsRawreserveInst) = with(ctx) {
        val mode = scope.takeLastIntOrThrowTypeError()
            ?: return@with
        val grams = scope.takeLastIntOrThrowTypeError()
            ?: return@with

        // TODO 4 or 5 bits depending on the version
        val modeNotOutOfRangeExpr = unsignedIntegerFitsBits(mode, 5u)
        val valueNotOutOfRangeExpr = mkBvSignedLessOrEqualExpr(zeroValue, grams)
        checkOutOfRange(modeNotOutOfRangeExpr and valueNotOutOfRangeExpr, scope) ?: return

        scope.doWithState {
            val actions = registers.c5.value.value
            val updatedActions = allocEmptyCell()

            builderStoreNextRef(updatedActions, actions)
            builderStoreDataBits(updatedActions, reserveActionTag)
            scope.builderStoreInt(updatedActions, mode, sizeBits = eightValue, isSigned = false) {
                error("Unexpected cell overflow during RAWRESERVE instruction")
            } ?: return@doWithState
            scope.builderStoreGrams(updatedActions, grams) ?: return@doWithState
            // empty ExtraCurrencyCollection
            builderStoreDataBits(updatedActions, zeroBit)

            registers.c5 = C5Register(TvmCellValue(updatedActions))

            newStmt(stmt.nextStmt())
        }
    }

    private fun visitSetCodeInst(scope: TvmStepScope, stmt: TvmAppActionsSetcodeInst) {
        scope.doWithState {
            val cell = stack.takeLastCell()

            // TODO make a real implementation
            newStmt(stmt.nextStmt())
        }
    }
}