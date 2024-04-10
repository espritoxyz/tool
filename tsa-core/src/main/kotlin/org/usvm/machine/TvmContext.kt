package org.usvm.machine

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.asExpr
import io.ksmt.utils.toBigInteger
import io.ksmt.utils.uncheckedCast
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
import org.usvm.mkSizeExpr
import org.usvm.sizeSort

// TODO: There is no size sort in TVM because of absence of arrays, but we need to represent cell data as boolean arrays
//  with size no more than 1023

// TODO make it Bv16
typealias TvmSizeSort = UBv32Sort

class TvmContext(components: UComponents<TvmType, TvmSizeSort>) : UContext<TvmSizeSort>(components) {
    val int257sort: UBvSort = mkBvSort(INT_BITS)
    val cellDataSort: UBvSort = mkBvSort(MAX_DATA_LENGTH.toUInt())

    val trueValue: KBitVecValue<UBvSort> = (-1).toBv257()
    val falseValue: KBitVecValue<UBvSort> = 0.toBv257()
    val oneValue: KBitVecValue<UBvSort> = 1.toBv257()
    val twoValue: KBitVecValue<UBvSort> = 2.toBv257()
    val zeroValue: KBitVecValue<UBvSort> = falseValue
    val minusOneValue = trueValue
    val intBitsValue = INT_BITS.toInt().toBv257()
    val maxTupleSizeValue = MAX_TUPLE_SIZE.toBv257()
    val unitTimeMinValue = UNIX_TIME_MIN.toBv257()

    val zeroSizeExpr = mkSizeExpr(0)
    val maxDataLengthSizeExpr = mkSizeExpr(MAX_DATA_LENGTH)
    val maxRefsLengthSizeExpr = mkSizeExpr(MAX_REFS_NUMBER)

    private var inputStackEntryCounter: Int = 0
    fun nextInputStackEntryId(): Int = inputStackEntryCounter++

    val nullValue: UConcreteHeapRef = mkConcreteHeapRef(NULL_ADDRESS)

    fun Number.toBv257(): KBitVecValue<UBvSort> = mkBv(toBigInteger(), int257sort)

    fun <Sort : UBvSort> UExpr<Sort>.signedExtendToInteger(): UExpr<UBvSort> {
        val extensionSize = int257sort.sizeBits - sort.sizeBits
        check(extensionSize <= int257sort.sizeBits) {
            "Cannot extend $this to bits more than ${int257sort.sizeBits}"
        }

        val extendedValue = mkBvSignExtensionExpr(extensionSize.toInt(), this)
        return extendedValue
    }

    fun <Sort : UBvSort> UExpr<Sort>.unsignedExtendToInteger(): UExpr<UBvSort> {
        val extensionSize = int257sort.sizeBits - sort.sizeBits
        check(extensionSize <= int257sort.sizeBits) {
            "Cannot extend $this to bits more than ${int257sort.sizeBits}"
        }

        val extendedValue = mkBvZeroExtensionExpr(extensionSize.toInt(), this)
        return extendedValue
    }

    fun <InSort : UBvSort, OutSort: UBvSort> UExpr<InSort>.zeroExtendToSort(sort: OutSort): UExpr<OutSort> {
        require(this.sort.sizeBits <= sort.sizeBits)
        val extensionSize = sort.sizeBits - this.sort.sizeBits
        return mkBvZeroExtensionExpr(extensionSize.toInt(), this).asExpr(sort)
    }

    fun <Sort : UBvSort> UExpr<Sort>.extractToSizeSort(): UExpr<TvmSizeSort> {
        require(sort.sizeBits >= sizeSort.sizeBits)

        return mkBvExtractExpr(sizeSort.sizeBits.toInt() - 1, 0, this).uncheckedCast()
    }

    companion object {
        const val MAX_DATA_LENGTH: Int = 1023
        const val MAX_REFS_NUMBER: Int = 4

        const val MAX_TUPLE_SIZE: Int = 255

        const val INT_BITS: UInt = 257u

        const val UNIX_TIME_MIN: Int = 1712318909

        val cellDataField: TvmField = TvmFieldImpl(TvmCellType, "data")
        val cellDataLengthField: TvmField = TvmFieldImpl(TvmCellType, "dataLength")
        val cellRefsLengthField: TvmField = TvmFieldImpl(TvmCellType, "refsLength")

        val sliceDataPosField: TvmField = TvmFieldImpl(TvmSliceType, "dataPos")
        val sliceRefPosField: TvmField = TvmFieldImpl(TvmSliceType, "refPos")
        val sliceCellField: TvmField = TvmFieldImpl(TvmSliceType, "cell")
    }
}
