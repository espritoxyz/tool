package org.usvm.machine.interpreter

import org.ton.bytecode.ACTIONS_PARAMETER_IDX
import org.ton.bytecode.ADDRESS_PARAMETER_IDX
import org.ton.bytecode.BALANCE_PARAMETER_IDX
import org.ton.bytecode.BLOCK_TIME_PARAMETER_IDX
import org.ton.bytecode.CONFIG_PARAMETER_IDX
import org.ton.bytecode.MSGS_SENT_PARAMETER_IDX
import org.ton.bytecode.SEED_PARAMETER_IDX
import org.ton.bytecode.TAG_PARAMETER_IDX
import org.ton.bytecode.TIME_PARAMETER_IDX
import org.ton.bytecode.TRANSACTION_TIME_PARAMETER_IDX
import org.ton.bytecode.TvmAppConfigConfigoptparamInst
import org.ton.bytecode.TvmAppConfigGetparamInst
import org.ton.bytecode.TvmAppConfigInst
import org.usvm.api.makeSymbolicPrimitive
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmStepScopeManager
import org.usvm.machine.state.TvmStack.TvmStackIntValue
import org.usvm.machine.state.addInt
import org.usvm.machine.state.addOnStack
import org.usvm.machine.state.addTuple
import org.usvm.machine.state.allocSliceFromCell
import org.usvm.machine.state.configContainsParam
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.doWithStateCtx
import org.usvm.machine.state.getConfigParam
import org.usvm.machine.state.getContractInfoParam
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.setContractInfoParam
import org.usvm.machine.state.takeLastIntOrThrowTypeError
import org.usvm.machine.state.toStackEntry
import org.usvm.machine.types.TvmCellType
import org.usvm.machine.types.TvmSliceType

class TvmConfigInterpreter(private val ctx: TvmContext) {
    fun visitConfigInst(scope: TvmStepScopeManager, stmt: TvmAppConfigInst) {
        scope.consumeDefaultGas(stmt)

        when (stmt) {
            is TvmAppConfigGetparamInst -> visitGetParamInst(scope, stmt)
            is TvmAppConfigConfigoptparamInst -> visitConfigParamInst(scope, stmt)
            else -> TODO("$stmt")
        }
    }

    private fun visitGetParamInst(scope: TvmStepScopeManager, stmt: TvmAppConfigGetparamInst) {
        scope.doWithStateCtx {
            val i = stmt.i

            when (i) {
                TAG_PARAMETER_IDX -> { // TAG
                    val tag = getContractInfoParam(i).intValue
                        ?: return@doWithStateCtx ctx.throwTypeCheckError(this)

                    stack.addInt(tag)
                }
                ACTIONS_PARAMETER_IDX -> { // ACTIONS
                    val actionNum = getContractInfoParam(i).intValue
                        ?: return@doWithStateCtx ctx.throwTypeCheckError(this)

                    stack.addInt(actionNum)
                }
                MSGS_SENT_PARAMETER_IDX -> { // MSGS_SENT
                    val messagesSent = getContractInfoParam(i).intValue
                        ?: return@doWithStateCtx ctx.throwTypeCheckError(this)

                    stack.addInt(messagesSent)
                }
                TIME_PARAMETER_IDX -> { // NOW
                    val now = makeSymbolicPrimitive(int257sort)
                    val previousValue = getContractInfoParam(i).intValue
                        ?: return@doWithStateCtx ctx.throwTypeCheckError(this)

                    scope.assert(
                        mkBvSignedGreaterExpr(now, previousValue),
                        unsatBlock = { error("Cannot make NOW > $previousValue") }
                    ) ?: return@doWithStateCtx

                    scope.assert(
                        mkBvSignedGreaterExpr(maxTimestampValue, now),
                        unsatBlock = { error("Cannot make NOW less than 2^64") }
                    ) ?: return@doWithStateCtx

                    setContractInfoParam(i, TvmStackIntValue(now).toStackEntry())
                    stack.addInt(now)
                }
                BLOCK_TIME_PARAMETER_IDX -> { // BLOCK_LTIME
                    val blockLogicalTime = getContractInfoParam(i).intValue
                        ?: return@doWithStateCtx ctx.throwTypeCheckError(this)

                    scope.assert(
                        mkBvSignedGreaterExpr(maxTimestampValue, blockLogicalTime),
                        unsatBlock = { error("Cannot make BLOCK_LTIME less than 2^64") }
                    ) ?: return@doWithStateCtx

                    stack.addInt(blockLogicalTime)
                }
                TRANSACTION_TIME_PARAMETER_IDX -> { // LTIME
                    val logicalTime = getContractInfoParam(i).intValue
                        ?: return@doWithStateCtx ctx.throwTypeCheckError(this)

                    scope.assert(
                        mkBvSignedGreaterExpr(maxTimestampValue, logicalTime),
                        unsatBlock = { error("Cannot make LTIME less than 2^64") }
                    ) ?: return@doWithStateCtx

                    stack.addInt(logicalTime)
                }
                SEED_PARAMETER_IDX -> { // RAND_SEED
                    val randomSeed = getContractInfoParam(i).intValue
                        ?: return@doWithStateCtx ctx.throwTypeCheckError(this)

                    stack.addInt(randomSeed)
                }
                BALANCE_PARAMETER_IDX -> { // BALANCE
                    val balanceValue = getContractInfoParam(i).tupleValue
                        ?: return@doWithStateCtx ctx.throwTypeCheckError(this)

                    stack.addTuple(balanceValue)
                }
                ADDRESS_PARAMETER_IDX -> { // MYADDR
                    val cell = getContractInfoParam(i).cellValue
                        ?: return@doWithStateCtx ctx.throwTypeCheckError(this)

                    val slice = scope.calcOnState { allocSliceFromCell(cell) }
                    addOnStack(slice, TvmSliceType)
                }
                CONFIG_PARAMETER_IDX -> { // GLOBAL_CONFIG
                    val cell = getContractInfoParam(i).cellValue
                        ?: return@doWithStateCtx ctx.throwTypeCheckError(this)

                    addOnStack(cell, TvmCellType)
                }
                else -> TODO("$i GETPARAM")
            }

            newStmt(stmt.nextStmt())
        }
    }

    private fun visitConfigParamInst(scope: TvmStepScopeManager, stmt: TvmAppConfigConfigoptparamInst) = with(ctx) {
        val idx = scope.takeLastIntOrThrowTypeError() ?: return@with

        val absIdx = mkIte(mkBvSignedGreaterOrEqualExpr(idx, zeroValue), idx, mkBvNegationExpr(idx))

        val configContainsIdx = scope.calcOnState { configContainsParam(absIdx) }
        scope.assert(
            configContainsIdx,
            unsatBlock = { error("Config doesn't contain idx: $absIdx") },
        ) ?: return@with

        val result = scope.getConfigParam(absIdx)
            ?: return@with

        scope.doWithState {
            scope.addOnStack(result, TvmCellType)
            newStmt(stmt.nextStmt())
        }
    }
}