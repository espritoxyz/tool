package org.usvm.machine.state

import org.ton.bytecode.TvmContBasicRetInst
import org.ton.bytecode.TvmContinuationValue
import org.ton.bytecode.TvmContractCode
import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmInstLambdaLocation
import org.ton.bytecode.TvmInstMethodLocation
import org.ton.bytecode.TvmReferenceType
import org.usvm.UHeapRef
import org.usvm.machine.TvmContext
import org.usvm.machine.interpreter.TvmStepScope

val TvmState.lastStmt get() = pathNode.statement
fun TvmState.newStmt(stmt: TvmInst) {
    pathNode += stmt
}

fun TvmInst.nextStmt(contractCode: TvmContractCode, currentContinuationValue: TvmContinuationValue): TvmInst =
    when (location) {
        is TvmInstMethodLocation -> (location as TvmInstMethodLocation).methodId.let {
            contractCode.methods[it]!!.instList.getOrNull(location.index + 1)
                ?: TvmContBasicRetInst(TvmInstMethodLocation(it, location.index + 1))
        }
        is TvmInstLambdaLocation -> currentContinuationValue.codeBlock.instList.getOrNull(location.index + 1)
            ?: TvmContBasicRetInst(TvmInstLambdaLocation(location.index + 1))
    }


fun TvmState.returnFromMethod() {
    val returnFromMethod = callStack.lastMethod()
    // TODO: think about it later
    val returnSite = callStack.pop()

    // TODO do we need it?
//    if (callStack.isNotEmpty()) {
//        memory.stack.pop()
//    }

    methodResult = TvmMethodResult.TvmSuccess(returnFromMethod, stack)

    if (returnSite != null) {
        currentContinuation = TvmContinuationValue(returnFromMethod, stack, registers)
        newStmt(returnSite)
    }
}

fun <R> TvmStepScope.calcOnStateCtx(block: context(TvmContext) TvmState.() -> R): R = calcOnState {
    block(ctx, this)
}

fun TvmStepScope.doWithStateCtx(block: context(TvmContext) TvmState.() -> Unit) = doWithState {
    block(ctx, this)
}

fun TvmState.generateSymbolicRef(referenceType: TvmReferenceType): UHeapRef = memory.allocStatic(referenceType)
