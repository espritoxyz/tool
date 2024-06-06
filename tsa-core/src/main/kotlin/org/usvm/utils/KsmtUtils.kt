package org.usvm.utils

import io.ksmt.expr.KBitVecValue
import org.usvm.UExpr
import org.usvm.machine.TvmContext
import org.usvm.machine.bigIntValue

val UExpr<TvmContext.TvmInt257Sort>.intValueOrNull: Int?
    get() = (this as? KBitVecValue<*>)?.bigIntValue()?.toInt()
