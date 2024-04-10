package org.usvm.machine.interpreter

import org.ton.bytecode.TvmAppConfigGetparamInst
import org.ton.bytecode.TvmAppConfigInst
import org.ton.bytecode.TvmIntegerType
import org.ton.bytecode.TvmNullType
import org.usvm.api.makeSymbolicPrimitive
import org.usvm.machine.TvmContext
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.doWithStateCtx
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt

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
                    scope.assert(mkBvSignedGreaterExpr(now, unitTimeMinValue))
                        ?: error("Cannot make NOW > $unitTimeMinValue")

                    stack.add(now, TvmIntegerType)
                }
                7 -> { // BALANCE
                    // TODO read the real value
                    val balance = makeSymbolicPrimitive(int257sort)
                    scope.assert(mkBvSignedGreaterOrEqualExpr(balance, zeroValue))
                        ?: error("Cannot make balance >= 0")
                    val maybeCell = ctx.nullValue

                    stack.add(balance, TvmIntegerType)
                    stack.add(maybeCell, TvmNullType)
                }
                else -> TODO("$i GETPARAM")
            }

            newStmt(stmt.nextStmt())
        }
    }
}