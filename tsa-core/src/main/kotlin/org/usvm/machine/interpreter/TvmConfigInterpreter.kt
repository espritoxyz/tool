package org.usvm.machine.interpreter

import org.ton.bytecode.TvmAppConfigConfigoptparamInst
import org.ton.bytecode.TvmAppConfigGetparamInst
import org.ton.bytecode.TvmAppConfigInst
import org.usvm.api.makeSymbolicPrimitive
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmStepScope
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
    fun visitConfigInst(scope: TvmStepScope, stmt: TvmAppConfigInst) {
        scope.consumeDefaultGas(stmt)

        when (stmt) {
            is TvmAppConfigGetparamInst -> visitGetParamInst(scope, stmt)
            is TvmAppConfigConfigoptparamInst -> visitConfigParamInst(scope, stmt)
            else -> TODO("$stmt")
        }
    }

    private fun visitGetParamInst(scope: TvmStepScope, stmt: TvmAppConfigGetparamInst) {
        scope.doWithStateCtx {
            val i = stmt.i

            when (i) {
                0 -> { // TAG
                    val tag = getContractInfoParam(i).intValue
                    stack.addInt(tag)
                }
                1 -> { // ACTIONS
                    val actionNum = getContractInfoParam(i).intValue
                    stack.addInt(actionNum)
                }
                2 -> { // MSGS_SENT
                    val messagesSent = getContractInfoParam(i).intValue
                    stack.addInt(messagesSent)
                }
                3 -> { // NOW
                    val now = makeSymbolicPrimitive(int257sort)
                    val previousValue = getContractInfoParam(i).intValue

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
                4 -> { // BLOCK_LTIME
                    val blockLogicalTime = getContractInfoParam(i).intValue

                    scope.assert(
                        mkBvSignedGreaterExpr(maxTimestampValue, blockLogicalTime),
                        unsatBlock = { error("Cannot make BLOCK_LTIME less than 2^64") }
                    ) ?: return@doWithStateCtx

                    stack.addInt(blockLogicalTime)
                }
                5 -> { // LTIME
                    val logicalTime = getContractInfoParam(i).intValue

                    scope.assert(
                        mkBvSignedGreaterExpr(maxTimestampValue, logicalTime),
                        unsatBlock = { error("Cannot make LTIME less than 2^64") }
                    ) ?: return@doWithStateCtx

                    stack.addInt(logicalTime)
                }
                6 -> { // RAND_SEED
                    val randomSeed = getContractInfoParam(i).intValue
                    stack.addInt(randomSeed)
                }
                7 -> { // BALANCE
                    val balanceValue = getContractInfoParam(i).tupleValue

                    stack.addTuple(balanceValue)
                }
                8 -> { // MYADDR
                    val cell = getContractInfoParam(i).cellValue
                        ?: error("Unexpected address value")

                    val slice = scope.calcOnState { allocSliceFromCell(cell) }
                    addOnStack(slice, TvmSliceType)
                }
                9 -> { // GLOBAL_CONFIG
                    val cell = getContractInfoParam(i).cellValue
                        ?: error("Unexpected config value")

                    addOnStack(cell, TvmCellType)
                }
                else -> TODO("$i GETPARAM")
            }

            newStmt(stmt.nextStmt())
        }
    }

    private fun visitConfigParamInst(scope: TvmStepScope, stmt: TvmAppConfigConfigoptparamInst) = with(ctx) {
        val idx = scope.takeLastIntOrThrowTypeError() ?: return@with

        val absIdx = mkIte(mkBvSignedGreaterOrEqualExpr(idx, zeroValue), idx, mkBvNegationExpr(idx))

        val configContainsIdx = scope.calcOnState { configContainsParam(absIdx) }
        scope.assert(
            configContainsIdx,
            unsatBlock = { error("Config doesn't contain idx: $absIdx") },
        ) ?: return@with

        val result = scope.calcOnState { getConfigParam(absIdx) }

        scope.doWithState {
            scope.addOnStack(result, TvmCellType)
            newStmt(stmt.nextStmt())
        }
    }
}