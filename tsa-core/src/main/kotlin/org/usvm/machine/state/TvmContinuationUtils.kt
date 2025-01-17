package org.usvm.machine.state

import org.ton.bytecode.TvmAgainContinuation
import org.ton.bytecode.TvmArtificialJmpToContInst
import org.ton.bytecode.TvmCellValue
import org.ton.bytecode.TvmContinuation
import org.ton.bytecode.TvmExceptionContinuation
import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmLoopEntranceContinuation
import org.ton.bytecode.TvmMethod
import org.ton.bytecode.TvmMethodReturnContinuation
import org.ton.bytecode.TvmOrdContinuation
import org.ton.bytecode.TvmQuitContinuation
import org.ton.bytecode.TvmRegisterSavelist
import org.ton.bytecode.TvmRepeatContinuation
import org.ton.bytecode.TvmUntilContinuation
import org.ton.bytecode.TvmWhileContinuation
import org.usvm.UHeapRef
import org.usvm.machine.TvmStepScopeManager
import org.usvm.machine.extractMethodId
import org.usvm.machine.state.TvmStack.TvmStackTupleValue
import org.usvm.machine.state.TvmStack.TvmStackTupleValueConcreteNew
import org.usvm.utils.intValueOrNull


fun TvmState.extractCurrentContinuation(
    stmt: TvmInst,
    saveC0: Boolean = false,
    saveC1: Boolean = false,
    saveC2: Boolean = false,
): TvmOrdContinuation = with(ctx) {
    var c0: C0Register? = null
    var c1: C1Register? = null
    var c2: C2Register? = null

    val registers = registersOfCurrentContract

    if (saveC0) {
        c0 = registers.c0
        registers.c0 = C0Register(quit0Cont)
    }

    if (saveC1) {
        c1 = registers.c1
        registers.c1 = C1Register(quit1Cont)
    }

    if (saveC2) {
        c2 = registers.c2

        // this line is commented in the tvm interpreter
        // https://github.com/ton-blockchain/ton/blob/5c392e0f2d946877bb79a09ed35068f7b0bd333a/crypto/vm/vm.cpp#L382
        // registers.c2 = C2Register(TvmExceptionContinuation)
    }

    TvmOrdContinuation(stmt.nextStmt(), TvmRegisterSavelist(c0, c1, c2))
}

fun TvmStepScopeManager.callMethod(
    stmt: TvmInst,
    methodToCall: TvmMethod,
) {
    val nextContinuation = TvmOrdContinuation(methodToCall)

    doWithState {
        val currentContinuation = extractCurrentContinuation(stmt, saveC0 = true)
        val wrappedCC = TvmMethodReturnContinuation(methodToCall.id, currentContinuation)
        registersOfCurrentContract.c0 = C0Register(wrappedCC)

        callStack.push(methodToCall, returnSite = stmt)
    }

    jump(nextContinuation)
}

/**
 * Executes (or jumps to, depending on the value of [returnToTheNextStmt]) the [continuation].
 */
fun TvmStepScopeManager.switchToContinuation(
    stmt: TvmInst,
    continuation: TvmContinuation,
    returnToTheNextStmt: Boolean
) {
    // TODO stack, n', n''

    if (returnToTheNextStmt) {
        doWithState {
            val currentContinuation = extractCurrentContinuation(stmt, saveC0 = true)
            val registers = registersOfCurrentContract
            registers.c0 = C0Register(currentContinuation)
        }
    }

    jump(continuation)
}

fun TvmStepScopeManager.returnFromContinuation() {
    val c0 = calcOnState { registersOfCurrentContract.c0.value }

    doWithStateCtx {
        registersOfCurrentContract.c0 = C0Register(quit0Cont)
    }

    jump(c0)
}

fun TvmStepScopeManager.returnAltFromContinuation() {
    val c1 = calcOnState { registersOfCurrentContract.c1.value }

    doWithStateCtx {
        registersOfCurrentContract.c1 = C1Register(quit1Cont)
    }

    jump(c1)
}

fun TvmContinuation.defineC0(cont: TvmContinuation): TvmContinuation {
    if (savelist.c0 != null) {
        return this
    }

    return updateSavelist(savelist.copy(c0 = C0Register(cont)))
}

fun TvmContinuation.defineC1(cont: TvmContinuation): TvmContinuation {
    if (savelist.c1 != null) {
        return this
    }

    return updateSavelist(savelist.copy(c1 = C1Register(cont)))
}

fun TvmContinuation.defineC2(cont: TvmContinuation): TvmContinuation {
    if (savelist.c2 != null) {
        return this
    }

    return updateSavelist(savelist.copy(c2 = C2Register(cont)))
}

fun TvmContinuation.defineC3(cont: TvmContinuation): TvmContinuation {
    if (savelist.c3 != null) {
        return this
    }

    return updateSavelist(savelist.copy(c3 = C3Register(cont)))
}

fun TvmContinuation.defineC4(cell: UHeapRef): TvmContinuation {
    if (savelist.c4 != null) {
        return this
    }

    return updateSavelist(savelist.copy(c4 = C4Register(TvmCellValue(cell))))
}

fun TvmContinuation.defineC5(cell: UHeapRef): TvmContinuation {
    if (savelist.c5 != null) {
        return this
    }

    return updateSavelist(savelist.copy(c5 = C5Register(TvmCellValue(cell))))
}

fun TvmContinuation.defineC7(tuple: TvmStackTupleValue): TvmContinuation {
    if (savelist.c7 != null) {
        return this
    }

    require(tuple is TvmStackTupleValueConcreteNew) {
        TODO("Support non-concrete tuples")
    }

    return updateSavelist(savelist.copy(c7 = C7Register(tuple)))
}

fun TvmStepScopeManager.jump(cont: TvmContinuation) {
    when (cont) {
        is TvmOrdContinuation -> doOrdJump(cont)
        is TvmQuitContinuation -> doQuitJump(cont)
        is TvmLoopEntranceContinuation -> doLoopEntranceJump(cont)
        is TvmUntilContinuation -> doUntilJump(cont)
        is TvmRepeatContinuation -> doRepeatJump(cont)
        is TvmWhileContinuation -> doWhileJump(cont)
        is TvmAgainContinuation -> doAgainJump(cont)
        is TvmExceptionContinuation -> doExceptionJump(cont)
        is TvmMethodReturnContinuation -> doMethodReturnJump(cont)
    }
}

private fun TvmState.adjustRegisters(cont: TvmContinuation) = with(cont.savelist) {
    val registers = registersOfCurrentContract
    c0?.let { registers.c0 = it }
    c1?.let { registers.c1 = it }
    c2?.let { registers.c2 = it }
    c3?.let { registers.c3 = it }
    c4?.let { registers.c4 = it }
    c5?.let { registers.c5 = it }
    c7?.let { registers.c7 = it.copy() }
}

private fun TvmStepScopeManager.doOrdJump(cont: TvmOrdContinuation) = doWithState {
    adjustRegisters(cont)

    newStmt(cont.stmt)
}

private fun TvmStepScopeManager.doMethodReturnJump(cont: TvmMethodReturnContinuation) {
    doWithState {
        if (callStack.lastMethod().extractMethodId() == cont.method) {
            callStack.pop()
        } else {
            // TODO
            // sometimes the invariant can be violated, for example,
            // by manually setting the c0 register
        }
    }

    doOrdJump(cont.returnSite)
}

private fun TvmStepScopeManager.doQuitJump(cont: TvmQuitContinuation) = doWithState {
    val exit = when (cont.exitCode) {
        0u -> TvmNormalExit
        1u -> TvmAlternativeExit
        else -> error("Unexpected exit code ${cont.exitCode}")
    }

    val registers = registersOfCurrentContract
    val commitedState = TvmCommitedState(registers.c4, registers.c5)

    lastCommitedStateOfContracts = lastCommitedStateOfContracts.put(currentContract, commitedState)
    methodResult = TvmMethodResult.TvmSuccess(exit, stack)
}

private fun TvmStepScopeManager.doLoopEntranceJump(cont: TvmLoopEntranceContinuation) {
    doWithState {
        newStmt(cont.codeBlock.instList.first())
    }
}

private fun TvmStepScopeManager.doUntilJump(cont: TvmUntilContinuation) {
    doWithState { adjustRegisters(cont) }

    val x = takeLastIntOrThrowTypeError() ?: return
    val continueLoopCondition = calcOnStateCtx { mkEq(x, ctx.zeroValue) }

    fork(
        continueLoopCondition,
        falseStateIsExceptional = false,
        blockOnFalseState = {
            newStmt(TvmArtificialJmpToContInst(cont.after, lastStmt.location))
        }
    ) ?: return

    doWithState {
        registersOfCurrentContract.c0 = C0Register(cont.updateSavelist())
    }

    jump(cont.body)
}

private fun TvmStepScopeManager.doRepeatJump(cont: TvmRepeatContinuation) {
    doWithState { adjustRegisters(cont) }

    val count = cont.count

    val isPositive = calcOnStateCtx { mkBvSignedLessExpr(zeroValue, count) }

    fork(
        isPositive,
        falseStateIsExceptional = false,
        blockOnFalseState = {
            newStmt(TvmArtificialJmpToContInst(cont.after, lastStmt.location))
        }
    ) ?: return

    doWithStateCtx {
        val newCont = cont.copy(count = mkBvSubExpr(cont.count, oneValue), savelist = TvmRegisterSavelist())
        registersOfCurrentContract.c0 = C0Register(newCont)
    }

    jump(cont.body)
}

private fun TvmStepScopeManager.doWhileJump(cont: TvmWhileContinuation) {
    doWithState { adjustRegisters(cont) }

    val newCont = cont.copy(isCondition = !cont.isCondition, savelist = TvmRegisterSavelist())
    doWithState { registersOfCurrentContract.c0 = C0Register(newCont) }

    if (!cont.isCondition) {
        return jump(cont.condition)
    }

    val cond = takeLastIntOrThrowTypeError() ?: return
    val continueLoopCondition = calcOnStateCtx { mkEq(cond, zeroValue).not() }

    fork(
        continueLoopCondition,
        falseStateIsExceptional = false,
        blockOnFalseState = {
            newStmt(TvmArtificialJmpToContInst(cont.after, lastStmt.location))
        }
    ) ?: return

    jump(cont.body)
}

private fun TvmStepScopeManager.doAgainJump(cont: TvmAgainContinuation) {
    doWithState {
        adjustRegisters(cont)
        registersOfCurrentContract.c0 = C0Register(cont.updateSavelist())
    }

    jump(cont.body)
}

private fun TvmStepScopeManager.doExceptionJump(cont: TvmExceptionContinuation) {
    val exitCode = takeLastIntOrThrowTypeError() ?: return
    val exitCodeValue = exitCode.intValueOrNull ?: error("Cannot extract concrete code exception")
    val failure = TvmUnknownFailure(exitCodeValue.toUInt())

    doWithState {
        stack.addInt(exitCode)

        methodResult = TvmMethodResult.TvmFailure(failure, TvmFailureType.UnknownError)
    }
}
