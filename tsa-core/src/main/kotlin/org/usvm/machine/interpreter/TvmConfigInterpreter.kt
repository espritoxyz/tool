package org.usvm.machine.interpreter

import kotlinx.collections.immutable.persistentListOf
import org.ton.bytecode.TvmAppConfigGetparamInst
import org.ton.bytecode.TvmAppConfigInst
import org.usvm.machine.types.TvmNullType
import org.usvm.machine.types.TvmSliceType
import org.usvm.api.makeSymbolicPrimitive
import org.usvm.api.writeField
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.Companion.cellDataLengthField
import org.usvm.machine.TvmStepScope
import org.usvm.machine.state.TvmStack
import org.usvm.machine.state.TvmStack.TvmStackTupleValueConcreteNew
import org.usvm.machine.state.addInt
import org.usvm.machine.state.addOnStack
import org.usvm.machine.state.allocSliceFromCell
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.doWithStateCtx
import org.usvm.machine.state.ensureSymbolicCellInitialized
import org.usvm.machine.state.generateSymbolicCell
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.toStackEntry
import org.usvm.mkSizeExpr
import org.usvm.sizeSort

class TvmConfigInterpreter(private val ctx: TvmContext) {
    fun visitConfigInst(scope: TvmStepScope, stmt: TvmAppConfigInst) {
        scope.consumeDefaultGas(stmt)

        when (stmt) {
            is TvmAppConfigGetparamInst -> visitGetParamInst(scope, stmt)
            else -> TODO("$stmt")
        }
    }

    private fun visitGetParamInst(scope: TvmStepScope, stmt: TvmAppConfigGetparamInst) {
        scope.doWithStateCtx {
            val i = stmt.i

            when (i) {
                3 -> { // NOW
                    val now = scope.calcOnState { makeSymbolicPrimitive(int257sort) }

                    val c7 = scope.calcOnState { registers.c7 }
                    val previousValue = c7.now ?: unitTimeMinValue

                    scope.assert(
                        mkBvSignedGreaterExpr(now, previousValue),
                        unsatBlock = { error("Cannot make NOW > $previousValue") }
                    ) ?: return@doWithStateCtx

                    c7.now = now
                    stack.addInt(now)
                }
                5 -> { // LTIME
                    val logicalTime = scope.calcOnState { makeSymbolicPrimitive(int257sort) }
                    scope.assert(
                        mkBvSignedGreaterExpr(logicalTime, zeroValue),
                        unsatBlock = { error("Cannot make positive LTIME") }
                    ) ?: return@doWithStateCtx

                    stack.addInt(logicalTime)
                }
                7 -> { // BALANCE
                    val c7 = scope.calcOnState { registers.c7 }
                    val balanceValue = c7.balance

                    // TODO read the real value
                    val (balance, maybeCell) = if (balanceValue != null) {
                        val balanceCell = balanceValue.entries[0].cell
                            ?: error("No balance value found")
                        val extraCurrencies = balanceValue.entries[1].cell
                            ?: error("No extra currencies value found")

                        balanceCell.intValue to (extraCurrencies.sliceValue ?: ctx.nullValue)
                    } else {
                        val balance = makeSymbolicPrimitive(int257sort)
                        scope.assert(
                            mkBvSignedGreaterOrEqualExpr(balance, zeroValue),
                            unsatBlock = { error("Cannot make balance >= 0") }
                        ) ?: return@doWithStateCtx

                        c7.balance = TvmStackTupleValueConcreteNew(
                            ctx,
                            persistentListOf(
                                TvmStack.TvmStackIntValue(balance).toStackEntry(),
                                TvmStack.TvmStackSliceValue(ctx.nullValue).toStackEntry()
                            )
                        )
                        balance to ctx.nullValue
                    }

                    stack.addInt(balance)
                    addOnStack(maybeCell, TvmNullType)
                }
                8 -> { // MYADDR
                    // TODO really make a slice with MsgAddressInt from the real contract address?
                    // MsgAddressInt contains at least (2 + 1 + 8 + 256 = 267 bits) -
                    // 2 for constructor, 1 for Nothing Anycast, int8 for workchain id and 256 bits for address
                    val cell = scope.calcOnState {
                        generateSymbolicCell().also { ensureSymbolicCellInitialized(it) }
                    }
                    memory.writeField(cell, cellDataLengthField, sizeSort, mkSizeExpr(267), guard = trueExpr)
                    // TODO write 10 for constructor and 0 for Nothing Anycast to the cell data?

                    val slice = scope.calcOnState { scope.allocSliceFromCell(cell) }
                    addOnStack(slice, TvmSliceType)
                }
                else -> TODO("$i GETPARAM")
            }

            newStmt(stmt.nextStmt())
        }
    }
}