package org.usvm.machine.state

import org.usvm.UBoolExpr
import org.usvm.machine.TvmStepScope

fun checkOutOfRange(notOutOfRangeExpr: UBoolExpr, scope: TvmStepScope): Unit? = scope.fork(
    condition = notOutOfRangeExpr,
    blockOnFalseState = { ctx.throwIntegerOutOfRangeError(this) }
)

fun checkOverflow(noOverflowExpr: UBoolExpr, scope: TvmStepScope): Unit? = scope.fork(
    noOverflowExpr,
    blockOnFalseState = { ctx.throwIntegerOverflowError(this) }
)

fun checkUnderflow(noUnderflowExpr: UBoolExpr, scope: TvmStepScope): Unit? = scope.fork(
    noUnderflowExpr,
    blockOnFalseState = { ctx.throwIntegerOverflowError(this) }
)