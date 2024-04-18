package org.usvm.machine

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.BvUtils.toBigIntegerSigned
import org.usvm.UBvSort
import org.usvm.UExpr

fun UExpr<out UBvSort>.intValue() = (this as KBitVecValue<*>).toBigIntegerSigned().toInt()
