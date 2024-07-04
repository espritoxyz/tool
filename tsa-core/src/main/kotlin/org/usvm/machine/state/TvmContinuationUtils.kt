package org.usvm.machine.state

import org.ton.bytecode.TvmAgainContinuation
import org.ton.bytecode.TvmArtificialJmpToContInst
import org.ton.bytecode.TvmContinuation
import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmLoopEntranceContinuation
import org.ton.bytecode.TvmOrdContinuation
import org.ton.bytecode.TvmQuitContinuation
import org.ton.bytecode.TvmRegisterSavelist
import org.ton.bytecode.TvmRepeatContinuation
import org.ton.bytecode.TvmUntilContinuation
import org.ton.bytecode.TvmWhileContinuation
import org.usvm.machine.TvmStepScope


fun TvmState.extractCurrentContinuation(
    stmt: TvmInst,
    saveC0: Boolean = false,
    saveC1: Boolean = false
): TvmContinuation {
    var c0: C0Register? = null
    var c1: C1Register? = null

    if (saveC0) {
        c0 = C0Register(registers.c0.value)
        registers.c0 = C0Register(TvmQuitContinuation)
    }

    if (saveC1) {
        c1 = registers.c1
        // TODO set failure continuation
        registers.c1 = C1Register(TvmQuitContinuation)
    }

    return TvmOrdContinuation(stmt.nextStmt(), TvmRegisterSavelist(c0, c1))
}

/**
 * Executes (or jumps to, depending on the value of [returnToTheNextStmt]) the [continuation].
 */
fun TvmStepScope.switchToContinuation(
    stmt: TvmInst,
    continuation: TvmContinuation,
    returnToTheNextStmt: Boolean
) {
    // TODO stack, n', n''

    if (returnToTheNextStmt) {
        doWithState {
            val currentContinuation = extractCurrentContinuation(stmt, saveC0 = true)
            registers.c0 = C0Register(currentContinuation)
        }
    }

    jump(continuation)
}

fun TvmStepScope.returnFromContinuation() {
    val c0 = calcOnState { registers.c0 }

    doWithState {
        registers.c0 = C0Register(TvmQuitContinuation)
    }

    jump(c0.value)
}

fun TvmContinuation.defineC0(cont: TvmContinuation?): TvmContinuation {
    if (savelist.c0 != null || cont == null) {
        return this
    }

    return updateSavelist(savelist.copy(c0 = C0Register(cont)))
}

fun TvmContinuation.defineC1(cont: TvmContinuation?): TvmContinuation {
    if (savelist.c1 != null || cont == null) {
        return this
    }

    return updateSavelist(savelist.copy(c1 = C1Register(cont)))
}

fun TvmContinuation.defineC2(cont: TvmContinuation?): TvmContinuation {
    if (savelist.c1 != null || cont == null) {
        return this
    }

    return updateSavelist(savelist.copy(c2 = C2Register(cont)))
}

fun TvmStepScope.jump(cont: TvmContinuation) {
    when (cont) {
        is TvmOrdContinuation -> doOrdJump(cont)
        is TvmQuitContinuation -> doQuitJump(cont)
        is TvmLoopEntranceContinuation -> doLoopEntranceJump(cont)
        is TvmUntilContinuation -> doUntilJump(cont)
        is TvmRepeatContinuation -> doRepeatJump(cont)
        is TvmWhileContinuation -> doWhileJump(cont)
        is TvmAgainContinuation -> doAgainJump(cont)
    }
}

private fun TvmState.adjustRegisters(cont: TvmContinuation) = with(cont.savelist) {
    c0?.let { registers.c0 = it }
    c1?.let { registers.c1 = it }
    c2?.let { registers.c2 = it }
    c3?.let { registers.c3 = it }
    c4?.let { registers.c4 = it }
    c5?.let { registers.c5 = it }
    c7?.let { registers.c7 = it.copy() }
}

private fun TvmStepScope.doOrdJump(cont: TvmOrdContinuation) = doWithState {
    adjustRegisters(cont)

    newStmt(cont.stmt)
}

private fun TvmStepScope.doQuitJump(cont: TvmQuitContinuation) = doWithState {
    methodResult = TvmMethodResult.TvmSuccess(stack)
}

private fun TvmStepScope.doLoopEntranceJump(cont: TvmLoopEntranceContinuation) {
    doWithState {
        newStmt(cont.codeBlock.instList.first())
    }
}

private fun TvmStepScope.doUntilJump(cont: TvmUntilContinuation) {
    doWithState { adjustRegisters(cont) }

    val x = takeLastIntOrThrowTypeError() ?: return
    val continueLoopCondition = calcOnStateCtx { mkEq(x, ctx.zeroValue) }

    fork(
        continueLoopCondition,
        blockOnFalseState = {
            newStmt(TvmArtificialJmpToContInst(cont.after, lastStmt.location))
        }
    ) ?: return

    doWithState {
        registers.c0 = C0Register(cont.updateSavelist())
    }

    jump(cont.body)
}

private fun TvmStepScope.doRepeatJump(cont: TvmRepeatContinuation) {
    doWithState { adjustRegisters(cont) }

    val count = cont.count

    val isPositive = calcOnStateCtx { mkBvSignedLessExpr(zeroValue, count) }

    fork(
        isPositive,
        blockOnFalseState = {
            newStmt(TvmArtificialJmpToContInst(cont.after, lastStmt.location))
        }
    ) ?: return

    doWithStateCtx {
        val newCont = cont.copy(count = mkBvSubExpr(cont.count, oneValue), savelist = TvmRegisterSavelist())
        registers.c0 = C0Register(newCont)
    }

    jump(cont.body)
}

private fun TvmStepScope.doWhileJump(cont: TvmWhileContinuation) {
    doWithState { adjustRegisters(cont) }

    val newCont = cont.copy(isCondition = !cont.isCondition, savelist = TvmRegisterSavelist())
    doWithState { registers.c0 = C0Register(newCont) }

    if (!cont.isCondition) {
        return jump(cont.condition)
    }

    val cond = takeLastIntOrThrowTypeError() ?: return
    val continueLoopCondition = calcOnStateCtx { mkEq(cond, zeroValue).not() }

    fork(
        continueLoopCondition,
        blockOnFalseState = {
            newStmt(TvmArtificialJmpToContInst(cont.after, lastStmt.location))
        }
    ) ?: return

    jump(cont.body)
}

private fun TvmStepScope.doAgainJump(cont: TvmAgainContinuation) {
    doWithState {
        adjustRegisters(cont)
        registers.c0 = C0Register(cont.updateSavelist())
    }

    jump(cont.body)
}
