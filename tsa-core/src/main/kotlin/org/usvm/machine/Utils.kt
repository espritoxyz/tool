package org.usvm.machine

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.BvUtils.toBigIntegerSigned
import org.usvm.UBvSort
import org.usvm.UExpr

@Suppress("NOTHING_TO_INLINE")
inline fun UExpr<out UBvSort>.bigIntValue() = (this as KBitVecValue<*>).toBigIntegerSigned()
@Suppress("NOTHING_TO_INLINE")
inline fun UExpr<out UBvSort>.intValue() = bigIntValue().toInt()
