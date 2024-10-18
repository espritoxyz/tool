package org.usvm.utils

import io.ksmt.expr.KBitVecValue
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.isAllocated
import org.usvm.isStatic
import org.usvm.machine.TvmContext
import org.usvm.machine.bigIntValue
import org.usvm.memory.foldHeapRef

val UExpr<TvmContext.TvmInt257Sort>.intValueOrNull: Int?
    get() = (this as? KBitVecValue<*>)?.bigIntValue()?.toInt()

context(TvmContext)
fun extractAddresses(
    ref: UHeapRef,
    extractAllocated: Boolean = false,
    extractStatic: Boolean = true,
): List<Pair<UBoolExpr, UConcreteHeapRef>> {
    return foldHeapRef(
        ref,
        initial = emptyList(),
        initialGuard = trueExpr,
        staticIsConcrete = true,
        blockOnSymbolic = { _, (ref, _) -> error("Unexpected ref $ref") },
        blockOnConcrete = { acc, (expr, guard) ->
            if (expr.isStatic && extractStatic || expr.isAllocated && extractAllocated) {
                acc + (guard to expr)
            } else {
                acc
            }
        }
    )
}
