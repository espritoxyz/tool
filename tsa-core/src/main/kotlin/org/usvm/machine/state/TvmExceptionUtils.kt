package org.usvm.machine.state

import org.usvm.UBoolExpr
import org.usvm.machine.TvmStepScopeManager

fun checkOutOfRange(notOutOfRangeExpr: UBoolExpr, scope: TvmStepScopeManager): Unit? = scope.fork(
    condition = notOutOfRangeExpr,
    falseStateIsExceptional = true,
    blockOnFalseState = { ctx.throwIntegerOutOfRangeError(this) }
)

fun checkOverflow(noOverflowExpr: UBoolExpr, scope: TvmStepScopeManager): Unit? = scope.fork(
    noOverflowExpr,
    falseStateIsExceptional = true,
    blockOnFalseState = { ctx.throwIntegerOverflowError(this) }
)

fun checkUnderflow(noUnderflowExpr: UBoolExpr, scope: TvmStepScopeManager): Unit? = scope.fork(
    noUnderflowExpr,
    falseStateIsExceptional = true,
    blockOnFalseState = { ctx.throwIntegerOverflowError(this) }
)
