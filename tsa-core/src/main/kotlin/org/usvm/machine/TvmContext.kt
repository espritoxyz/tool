package org.usvm.machine

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.toBigInteger
import org.ton.bytecode.TvmCellType
import org.ton.bytecode.TvmField
import org.ton.bytecode.TvmFieldImpl
import org.ton.bytecode.TvmSliceType
import org.ton.bytecode.TvmType
import org.usvm.NULL_ADDRESS
import org.usvm.UBv32Sort
import org.usvm.UBvSort
import org.usvm.UComponents
import org.usvm.UConcreteHeapRef
import org.usvm.UContext
import org.usvm.UExpr

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
    val minusOneValue = trueValue
    val intBitsValue = INT_BITS.toInt().toBv257()

    val nullValue: UConcreteHeapRef = mkConcreteHeapRef(NULL_ADDRESS)

    fun Number.toBv257(): KBitVecValue<UBvSort> = mkBv(toBigInteger(), int257sort)

    fun UExpr<UBvSort>.signedExtendToInteger(): UExpr<UBvSort> {
        val extensionSize = int257sort.sizeBits - sort.sizeBits
        check(extensionSize <= int257sort.sizeBits) {
            "Cannot extend $this to bits more than ${int257sort.sizeBits}"
        }

        val extendedValue = mkBvSignExtensionExpr(extensionSize.toInt(), this)
        return extendedValue
    }

    fun UExpr<UBvSort>.unsignedExtendToInteger(): UExpr<UBvSort> {
        val extensionSize = int257sort.sizeBits - sort.sizeBits
        check(extensionSize <= int257sort.sizeBits) {
            "Cannot extend $this to bits more than ${int257sort.sizeBits}"
        }

        val extendedValue = mkBvZeroExtensionExpr(extensionSize.toInt(), this)
        return extendedValue
    }

    companion object {
        const val MAX_DATA_LENGTH: Int = 1023
        const val MAX_REFS_NUMBER: Int = 4

        const val INT_BITS = 257u

        val cellDataField: TvmField = TvmFieldImpl(TvmCellType, "data")
        val cellDataLengthField: TvmField = TvmFieldImpl(TvmCellType, "dataLength")
        val cellRefsLengthField: TvmField = TvmFieldImpl(TvmCellType, "refsLength")

        val sliceDataPosField: TvmField = TvmFieldImpl(TvmSliceType, "dataPos")
        val sliceRefPosField: TvmField = TvmFieldImpl(TvmSliceType, "refPos")
        val sliceCellField: TvmField = TvmFieldImpl(TvmSliceType, "cell")
    }
}
