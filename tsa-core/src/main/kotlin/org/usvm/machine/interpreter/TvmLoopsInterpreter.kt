package org.usvm.machine.interpreter

import org.ton.bytecode.TvmArtificialAgainInst
import org.ton.bytecode.TvmArtificialRepeatInst
import org.ton.bytecode.TvmArtificialUntilInst
import org.ton.bytecode.TvmArtificialWhileEndInst
import org.ton.bytecode.TvmArtificialWhileStartInst
import org.ton.bytecode.TvmContLoopsAgainInst
import org.ton.bytecode.TvmContLoopsAgainbrkInst
import org.ton.bytecode.TvmContLoopsAgainendInst
import org.ton.bytecode.TvmContLoopsAgainendbrkInst
import org.ton.bytecode.TvmContLoopsInst
import org.ton.bytecode.TvmContLoopsRepeatInst
import org.ton.bytecode.TvmContLoopsRepeatbrkInst
import org.ton.bytecode.TvmContLoopsRepeatendInst
import org.ton.bytecode.TvmContLoopsRepeatendbrkInst
import org.ton.bytecode.TvmContLoopsUntilInst
import org.ton.bytecode.TvmContLoopsUntilbrkInst
import org.ton.bytecode.TvmContLoopsUntilendInst
import org.ton.bytecode.TvmContLoopsUntilendbrkInst
import org.ton.bytecode.TvmContLoopsWhileInst
import org.ton.bytecode.TvmContLoopsWhilebrkInst
import org.ton.bytecode.TvmContLoopsWhileendInst
import org.ton.bytecode.TvmContLoopsWhileendbrkInst
import org.ton.bytecode.TvmInst
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmStepScope
import org.usvm.machine.interpreter.TvmLoopsInterpreter.ContinuationExtractor.CC
import org.usvm.machine.interpreter.TvmLoopsInterpreter.ContinuationExtractor.STACK
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.returnFromMethod
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.throwIntegerOutOfRangeError
import org.usvm.machine.state.signedIntegerFitsBits
import org.usvm.machine.state.takeLastContinuation
import org.usvm.machine.state.takeLastInt

class TvmLoopsInterpreter(private val ctx: TvmContext) {
    fun visitTvmContLoopsInst(scope: TvmStepScope, stmt: TvmContLoopsInst) {
        scope.consumeDefaultGas(stmt)

        when (stmt) {
            is TvmContLoopsRepeatInst -> visitRepeatInst(scope, stmt, continuationExtractor = STACK, executeUntilEnd = false)
            is TvmContLoopsRepeatendInst -> visitRepeatInst(scope, stmt, continuationExtractor = CC, executeUntilEnd = true)
            is TvmContLoopsRepeatbrkInst -> TODO()
            is TvmContLoopsRepeatendbrkInst -> TODO()
            is TvmContLoopsUntilInst -> visitUntilInst(scope, stmt, continuationExtractor = STACK, executeUntilEnd = false)
            is TvmContLoopsUntilendInst -> visitUntilInst(scope, stmt, continuationExtractor = CC, executeUntilEnd = true)
            is TvmContLoopsUntilbrkInst -> TODO()
            is TvmContLoopsUntilendbrkInst -> TODO()
            is TvmContLoopsWhileInst -> visitWhileInst(scope, stmt, continuationExtractor = STACK, executeUntilEnd = false)
            is TvmContLoopsWhileendInst -> visitWhileInst(scope, stmt, continuationExtractor = CC, executeUntilEnd = true)
            is TvmContLoopsWhilebrkInst -> TODO()
            is TvmContLoopsWhileendbrkInst -> TODO()
            is TvmContLoopsAgainInst -> visitAgainInst(scope, stmt, continuationExtractor = STACK, executeUntilEnd = false)
            is TvmContLoopsAgainendInst -> visitAgainInst(scope, stmt, continuationExtractor = CC, executeUntilEnd = true)
            is TvmContLoopsAgainbrkInst -> TODO()
            is TvmContLoopsAgainendbrkInst -> TODO()
            is TvmArtificialRepeatInst -> visitArtificialRepeatInst(scope, stmt)
            is TvmArtificialUntilInst -> visitArtificialUntilInst(scope, stmt)
            is TvmArtificialWhileStartInst -> visitArtificialWhileStartInst(scope, stmt)
            is TvmArtificialWhileEndInst -> visitArtificialWhileEndInst(scope, stmt)
            is TvmArtificialAgainInst -> visitArtificialAgainInst(scope, stmt)
        }
    }

    private fun visitRepeatInst(
        scope: TvmStepScope,
        stmt: TvmContLoopsInst,
        continuationExtractor: ContinuationExtractor,
        executeUntilEnd: Boolean,
    ) {
        with(ctx) {
            val continuation = with(scope) { stmt.extractContinuation(continuationExtractor) }
            val loopRepeatTimes = scope.takeLastInt()
            val inRangeConstraint = signedIntegerFitsBits(loopRepeatTimes, bits = 32u)

            scope.fork(
                inRangeConstraint,
                blockOnFalseState = throwIntegerOutOfRangeError
            ) ?: return

            val artificialRepeatInst = TvmArtificialRepeatInst(stmt, continuation, loopRepeatTimes, executeUntilEnd)
            visitArtificialRepeatInst(scope, artificialRepeatInst)
        }
    }

    private fun visitUntilInst(
        scope: TvmStepScope,
        stmt: TvmContLoopsInst,
        continuationExtractor: ContinuationExtractor,
        executeUntilEnd: Boolean,
    ) {
        val continuation = with(scope) { stmt.extractContinuation(continuationExtractor) }

        scope.doWithState {
            val artificialUntilInst = TvmArtificialUntilInst(stmt, continuation, executeUntilEnd)
            callStack.push(continuation.codeBlock, artificialUntilInst)

            currentContinuation = continuation // TODO use these stack and registers?
            newStmt(continuation.takeCurrentStmt())
        }
    }

    private fun visitWhileInst(
        scope: TvmStepScope,
        stmt: TvmContLoopsInst,
        continuationExtractor: ContinuationExtractor,
        executeUntilEnd: Boolean,
    ) {
        val continuation = with(scope) { stmt.extractContinuation(continuationExtractor) }
        val conditionContinuation = scope.calcOnState { stack.takeLastContinuation() }

        val artificialWhileStartInst = TvmArtificialWhileStartInst(
            originalInst = stmt,
            continuationValue = continuation,
            conditionContinuation = conditionContinuation,
            executeUntilEnd = executeUntilEnd
        )
        visitArtificialWhileStartInst(scope, artificialWhileStartInst)
    }

    private fun visitAgainInst(
        scope: TvmStepScope,
        stmt: TvmContLoopsInst,
        continuationExtractor: ContinuationExtractor,
        executeUntilEnd: Boolean,
    ) {
        val continuation = with(scope) { stmt.extractContinuation(continuationExtractor) }

        val artificialAgainInst = TvmArtificialAgainInst(stmt, continuation, executeUntilEnd)
        visitArtificialAgainInst(scope, artificialAgainInst)
    }

    private fun visitArtificialAgainInst(scope: TvmStepScope, stmt: TvmArtificialAgainInst) {
        val continuation = stmt.continuationValue

        scope.doWithState {
            // AGAIN executes the loop infinitely many times,
            // and can be exited only by an exception, or a RETALT (or an explicit JMPX)
            callStack.push(continuation.codeBlock, stmt)

            currentContinuation = continuation // TODO use these stack and registers?
            newStmt(continuation.takeCurrentStmt())
        }
    }

    private fun visitArtificialRepeatInst(scope: TvmStepScope, stmt: TvmArtificialRepeatInst) {
        with(ctx) {
            val loopRepeatTimes = stmt.loopRepeats

            val isPositive = mkBvSignedLessExpr(zeroValue, loopRepeatTimes)
            scope.fork(
                isPositive,
                blockOnFalseState = { lastIteration(stmt, stmt.executeUntilEnd) }
            ) ?: return

            val decreasedLoopRepeatTimes = mkBvSubExpr(loopRepeatTimes, oneValue)
            scope.doWithState {
                val continuationValue = stmt.continuationValue
                val updatedArtificialRepeatInst = stmt.copy(loopRepeats = decreasedLoopRepeatTimes)
                callStack.push(continuationValue.codeBlock, updatedArtificialRepeatInst)

                currentContinuation = continuationValue // TODO use these stack and registers?
                newStmt(continuationValue.takeCurrentStmt())
            }
        }
    }

    private fun visitArtificialUntilInst(scope: TvmStepScope, stmt: TvmArtificialUntilInst) {
        val x = scope.takeLastInt()
        val continueLoopCondition = ctx.mkEq(x, ctx.zeroValue)

        scope.fork(
            continueLoopCondition,
            blockOnFalseState = { lastIteration(stmt, stmt.executeUntilEnd) }
        ) ?: return

        val continuation = stmt.continuationValue

        scope.doWithState {
            callStack.push(continuation.codeBlock, stmt)

            currentContinuation = continuation // TODO use these stack and registers?
            newStmt(continuation.takeCurrentStmt())
        }
    }

    private fun visitArtificialWhileStartInst(scope: TvmStepScope, stmt: TvmArtificialWhileStartInst) {
        scope.doWithState {
            val continuationValue = stmt.continuationValue
            val conditionContinuation = stmt.conditionContinuation
            val artificialWhileEndInst = TvmArtificialWhileEndInst(
                originalInst = stmt.originalInst,
                continuationValue = continuationValue,
                conditionContinuation = conditionContinuation,
                executeUntilEnd = stmt.executeUntilEnd
            )
            callStack.push(conditionContinuation.codeBlock, artificialWhileEndInst)

            currentContinuation = conditionContinuation // TODO use these stack and registers?
            newStmt(conditionContinuation.takeCurrentStmt())
        }
    }

    private fun visitArtificialWhileEndInst(scope: TvmStepScope, stmt: TvmArtificialWhileEndInst) {
        val x = scope.takeLastInt()
        val continueLoopCondition = with(ctx) {
            mkEq(x, zeroValue).not()
        }

        scope.fork(
            continueLoopCondition,
            blockOnFalseState = { lastIteration(stmt, stmt.executeUntilEnd) }
        ) ?: return

        val continuationValue = stmt.continuationValue
        val conditionContinuation = stmt.conditionContinuation
        scope.doWithState {
            val artificialWhileStartInst = TvmArtificialWhileStartInst(
                originalInst = stmt.originalInst,
                continuationValue = continuationValue,
                conditionContinuation = conditionContinuation,
                executeUntilEnd = stmt.executeUntilEnd
            )
            callStack.push(continuationValue.codeBlock, artificialWhileStartInst)

            currentContinuation = continuationValue // TODO use these stack and registers?
            newStmt(continuationValue.takeCurrentStmt())
        }
    }

    private enum class ContinuationExtractor {
        STACK,
        CC
    }

    private fun TvmState.lastIteration(stmt: TvmInst, executeUntilEnd: Boolean) =
        if (executeUntilEnd) {
            returnFromMethod()
        } else {
            newStmt(stmt.nextStmt())
        }

    context(TvmStepScope)
    private fun TvmInst.extractContinuation(continuationExtractor: ContinuationExtractor) = calcOnState {
        when (continuationExtractor) {
            STACK -> stack.takeLastContinuation()
            CC -> currentContinuation.copy(currentInstIndex = location.index + 1)
        }
    }
}
