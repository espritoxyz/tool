package org.usvm.machine

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.toBigInteger
import org.ton.bytecode.TvmType
import org.usvm.UBv32Sort
import org.usvm.UBvSort
import org.usvm.UComponents
import org.usvm.UContext

// TODO: There is no size sort in TVM because of absence of arrays, but we need to represent cell data as boolean arrays
//  with size no more than 1023

// TODO make it Bv16
typealias TvmSizeSort = UBv32Sort

class TvmContext(components : UComponents<TvmType, TvmSizeSort>) : UContext<TvmSizeSort>(components) {
    val int257sort: UBvSort = mkBvSort(INT_BITS)
    val cellDataSort: UBvSort = mkBvSort(MAX_DATA_LENGTH.toUInt())

    val trueValue: KBitVecValue<UBvSort> = (-1).toBv257()
    val falseValue: KBitVecValue<UBvSort> = 0.toBv257()
    val oneValue: KBitVecValue<UBvSort> = 1.toBv257()
    val zeroValue: KBitVecValue<UBvSort> = falseValue

    fun Number.toBv257(): KBitVecValue<UBvSort> = mkBv(toBigInteger(), int257sort)

    companion object {
        const val MAX_DATA_LENGTH: Int = 1023
        const val MAX_REFS_NUMBER: Int = 4

        const val INT_BITS = 257u
    }
}
