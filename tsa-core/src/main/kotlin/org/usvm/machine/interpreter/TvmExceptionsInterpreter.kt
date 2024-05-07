package org.usvm.machine.interpreter

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.BvUtils.bigIntValue
import org.ton.bytecode.TvmExceptionsInst
import org.ton.bytecode.TvmExceptionsThrowShortInst
import org.ton.bytecode.TvmExceptionsThrowanyifInst
import org.ton.bytecode.TvmExceptionsThrowanyifnotInst
import org.ton.bytecode.TvmExceptionsThrowargInst
import org.ton.bytecode.TvmExceptionsThrowifInst
import org.ton.bytecode.TvmExceptionsThrowifShortInst
import org.ton.bytecode.TvmExceptionsThrowifnotInst
import org.ton.bytecode.TvmExceptionsThrowifnotShortInst
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmStepScope
import org.usvm.machine.state.TvmState
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

                doThrowIfInst(scope, stmt, EmbeddedCodeExtractor(stmt.n), invertCondition = false)
            }
            is TvmExceptionsThrowifShortInst -> {
                scope.doWithState { consumeGas(26) }

                doThrowIfInst(scope, stmt, EmbeddedCodeExtractor(stmt.n), invertCondition = false)
            }
            is TvmExceptionsThrowifnotInst -> {
                scope.doWithState { consumeGas(34) }

                doThrowIfInst(scope, stmt, EmbeddedCodeExtractor(stmt.n), invertCondition = true)
            }
            is TvmExceptionsThrowifnotShortInst -> {
                scope.doWithState { consumeGas(26) }

                doThrowIfInst(scope, stmt, EmbeddedCodeExtractor(stmt.n), invertCondition = true)
            }
            is TvmExceptionsThrowanyifInst -> {
                scope.doWithState { consumeGas(26) }

                doThrowIfInst(scope, stmt, StackCodeExtractor, invertCondition = false)
            }
            is TvmExceptionsThrowanyifnotInst -> {
                scope.doWithState { consumeGas(26) }

                doThrowIfInst(scope, stmt, StackCodeExtractor, invertCondition = true)
            }
            else -> TODO("Unknown stmt: $stmt")
        }
    }

    private fun doThrowIfInst(
        scope: TvmStepScope,
        stmt: TvmExceptionsInst,
        exceptionCodeExtractor: ExceptionCodeExtractor,
        invertCondition: Boolean
    ) {
        with(ctx) {
            val flag = scope.takeLastInt()
            val throwCondition = (flag eq zeroValue).let {
                if (invertCondition) it.not() else it
            }
            val exceptionCode = scope.calcOnState { exceptionCodeExtractor.code(this) }

            scope.fork(
                throwCondition,
                blockOnFalseState = {
                    setFailure(TvmUnknownFailure(exceptionCode.toUInt()))(this)
                    consumeGas(50)
                }
            ) ?: return

            scope.doWithState { newStmt(stmt.nextStmt()) }
        }
    }

    private sealed interface ExceptionCodeExtractor {
        fun code(state: TvmState): Int
    }

    private data class EmbeddedCodeExtractor(val code: Int) : ExceptionCodeExtractor {
        override fun code(state: TvmState): Int = code
    }
    private data object StackCodeExtractor : ExceptionCodeExtractor {
        override fun code(state: TvmState): Int = (state.takeLastInt() as? KBitVecValue<*>)?.bigIntValue()?.toInt()
            ?: error("Cannot extract concrete code exception from the stack")
    }
}
