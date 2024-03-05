package org.usvm.machine.state

import org.example.org.ton.bytecode.TvmContractCode
import org.example.org.ton.bytecode.TvmInstMethodLocation
import org.example.org.usvm.machine.state.TvmMethodResult
import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmReturnInst

val TvmState.lastStmt get() = pathNode.statement
fun TvmState.newStmt(stmt: TvmInst) {
    pathNode += stmt
}

fun TvmInst.nextStmt(contractCode: TvmContractCode): TvmInst =
    when (location) {
        is TvmInstMethodLocation -> (location as TvmInstMethodLocation).methodId.let {
            contractCode.methods[it]!!.instList.getOrNull(location.index + 1)
                ?: TvmReturnInst(TvmInstMethodLocation(it, location.index + 1))
        }
        else -> TODO()
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
        newStmt(returnSite)
    }
}
