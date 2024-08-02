package org.usvm.machine

import io.ksmt.KContext
import io.ksmt.expr.KBitVecValue
import io.ksmt.expr.KExpr
import io.ksmt.sort.KBvCustomSizeSort
import io.ksmt.sort.KBvSort
import io.ksmt.utils.BvUtils.bvMaxValueUnsigned
import io.ksmt.utils.asExpr
import io.ksmt.utils.toBigInteger
import org.usvm.machine.types.TvmCellType
import org.ton.bytecode.TvmField
import org.ton.bytecode.TvmFieldImpl
import org.ton.bytecode.TvmQuitContinuation
import org.usvm.machine.types.TvmSliceType
import org.usvm.machine.types.TvmType
import org.usvm.NULL_ADDRESS
import org.usvm.UBoolExpr
import org.usvm.UBv32Sort
import org.usvm.UBvSort
import org.usvm.UComponents
import org.usvm.UConcreteHeapRef
import org.usvm.UContext
import org.usvm.UExpr
import org.usvm.machine.state.TvmCellOverflowError
import org.usvm.machine.state.TvmCellUnderflowError
import org.usvm.machine.state.TvmFailureType
import org.usvm.machine.state.TvmIntegerOutOfRangeError
import org.usvm.machine.state.TvmIntegerOverflowError
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.TvmTypeCheckError
import org.usvm.machine.state.bvMaxValueSignedExtended
import org.usvm.machine.state.bvMinValueSignedExtended
import org.usvm.machine.state.setFailure
import org.usvm.mkSizeExpr
import org.usvm.sizeSort

// TODO: There is no size sort in TVM because of absence of arrays, but we need to represent cell data as boolean arrays
//  with size no more than 1023

// TODO make it Bv16
typealias TvmSizeSort = UBv32Sort

class TvmContext(
    val tvmOptions: TvmOptions,
    components: UComponents<TvmType, TvmSizeSort>,
) : UContext<TvmSizeSort>(components) {
    val int257sort = TvmInt257Sort(this)
    val cellDataSort = TvmCellDataSort(this)

    // Utility sorts for arith operations
    val int257Ext1Sort = TvmInt257Ext1Sort(this)
    val int257Ext256Sort = TvmInt257Ext256Sort(this)

    val trueValue: KBitVecValue<TvmInt257Sort> = (-1).toBv257()
    val falseValue: KBitVecValue<TvmInt257Sort> = 0.toBv257()
    val oneValue: KBitVecValue<TvmInt257Sort> = 1.toBv257()
    val twoValue: KBitVecValue<TvmInt257Sort> = 2.toBv257()
    val eightValue: KBitVecValue<TvmInt257Sort> = 8.toBv257()
    val zeroValue: KBitVecValue<TvmInt257Sort> = falseValue
    val minusOneValue: KBitVecValue<TvmInt257Sort> = trueValue
    val intBitsValue: KBitVecValue<TvmInt257Sort> = INT_BITS.toInt().toBv257()
    val maxTupleSizeValue: KBitVecValue<TvmInt257Sort> = MAX_TUPLE_SIZE.toBv257()
    val unitTimeMinValue: KBitVecValue<TvmInt257Sort> = UNIX_TIME_MIN.toBv257()
    val min257BitValue: KExpr<TvmInt257Sort> = bvMinValueSignedExtended(intBitsValue)
    val max257BitValue: KExpr<TvmInt257Sort> = bvMaxValueSignedExtended(intBitsValue)
    val maxGramsValue: KExpr<TvmInt257Sort> = bvMaxValueUnsigned<UBvSort>(MAX_GRAMS_BITS).unsignedExtendToInteger()
    val maxTimestampValue = mkBvShiftLeftExpr(oneValue, 64.toBv257())

    val masterchain: KBitVecValue<TvmInt257Sort> = minusOneValue
    val baseChain: KBitVecValue<TvmInt257Sort> = zeroValue

    val zeroSizeExpr: UExpr<TvmSizeSort> = mkSizeExpr(0)
    val oneSizeExpr: UExpr<TvmSizeSort> = mkSizeExpr(1)
    val threeSizeExpr: UExpr<TvmSizeSort> = mkSizeExpr(3)
    val fourSizeExpr: UExpr<TvmSizeSort> = mkSizeExpr(4)
    val sixSizeExpr: UExpr<TvmSizeSort> = mkSizeExpr(6)
    val maxDataLengthSizeExpr: UExpr<TvmSizeSort> = mkSizeExpr(MAX_DATA_LENGTH)
    val maxRefsLengthSizeExpr: UExpr<TvmSizeSort> = mkSizeExpr(MAX_REFS_NUMBER)

    val zeroBit = mkBv(0, 1u)
    val oneBit = mkBv(1, 1u)

    private var inputStackEntryCounter: Int = 0
    fun nextInputStackEntryId(): Int = inputStackEntryCounter++

    val nullValue: UConcreteHeapRef = mkConcreteHeapRef(NULL_ADDRESS)

    val quit0Cont = TvmQuitContinuation(0u)
    val quit1Cont = TvmQuitContinuation(1u)

    val throwTypeCheckError: (TvmState) -> Unit = setFailure(TvmTypeCheckError)
    val throwIntegerOverflowError: (TvmState) -> Unit = setFailure(TvmIntegerOverflowError)
    val throwIntegerOutOfRangeError: (TvmState) -> Unit = setFailure(TvmIntegerOutOfRangeError)
    val throwCellOverflowError: (TvmState) -> Unit = setFailure(TvmCellOverflowError)
    val throwUnknownCellUnderflowError: (TvmState) -> Unit = setFailure(TvmCellUnderflowError, TvmFailureType.UnknownError)
    val throwStructuralCellUnderflowError: (TvmState) -> Unit =
        setFailure(TvmCellUnderflowError, TvmFailureType.FixedStructuralError)
    val throwSymbolicStructuralCellUnderflowError: (TvmState) -> Unit =
        setFailure(TvmCellUnderflowError, TvmFailureType.SymbolicStructuralError)
    val throwRealCellUnderflowError: (TvmState) -> Unit = setFailure(TvmCellUnderflowError, TvmFailureType.RealError)

    val sendMsgActionTag = mkBvHex("0ec3c86d", 32u)
    val reserveActionTag = mkBvHex("36e6b809", 32u)

    fun UBoolExpr.toBv257Bool(): UExpr<TvmInt257Sort> = with(ctx) {
        mkIte(
            condition = this@toBv257Bool,
            trueBranch = trueValue,
            falseBranch = falseValue,
        )
    }

    fun Number.toBv257(): KBitVecValue<TvmInt257Sort> = mkBv(toBigInteger(), int257sort)

    fun <Sort : UBvSort> UExpr<Sort>.signedExtendToInteger(): UExpr<TvmInt257Sort> =
        signExtendToSort(int257sort)

    fun <Sort : UBvSort> UExpr<Sort>.unsignedExtendToInteger(): UExpr<TvmInt257Sort> =
        zeroExtendToSort(int257sort)

    fun <InSort : UBvSort, OutSort: UBvSort> UExpr<InSort>.zeroExtendToSort(sort: OutSort): UExpr<OutSort> {
        require(this.sort.sizeBits <= sort.sizeBits)
        val extensionSize = sort.sizeBits - this.sort.sizeBits
        return mkBvZeroExtensionExpr(extensionSize.toInt(), this).asExpr(sort)
    }

    fun <InSort : UBvSort, OutSort: UBvSort> UExpr<InSort>.signExtendToSort(sort: OutSort): UExpr<OutSort> {
        require(this.sort.sizeBits <= sort.sizeBits)
        val extensionSize = sort.sizeBits - this.sort.sizeBits
        return mkBvSignExtensionExpr(extensionSize.toInt(), this).asExpr(sort)
    }

    fun <Sort : UBvSort> UExpr<Sort>.extractToSizeSort(): UExpr<TvmSizeSort> =
        extractToSort(sizeSort)

    fun <Sort : UBvSort> UExpr<Sort>.extractToInt257Sort(): UExpr<TvmInt257Sort> =
        extractToSort(int257sort)

    fun <InSort : UBvSort, OutSort: UBvSort> UExpr<InSort>.extractToSort(sort: OutSort): UExpr<OutSort> {
        require(this.sort.sizeBits >= sort.sizeBits)

        return mkBvExtractExpr(sort.sizeBits.toInt() - 1, 0, this).asExpr(sort)
    }

    override fun mkBvSort(sizeBits: UInt): KBvSort = when (sizeBits) {
        INT_BITS -> int257sort
        CELL_DATA_BITS -> cellDataSort
        INT_EXT1_BITS -> int257Ext1Sort
        INT_EXT256_BITS -> int257Ext256Sort
        else -> super.mkBvSort(sizeBits)
    }

    companion object {
        const val MAX_DATA_LENGTH: Int = 1023
        const val MAX_REFS_NUMBER: Int = 4

        const val MAX_TUPLE_SIZE: Int = 255

        const val INT_BITS: UInt = 257u
        val CELL_DATA_BITS: UInt = MAX_DATA_LENGTH.toUInt()

        const val UNIX_TIME_MIN: Int = 1712318909

        const val MAX_GRAMS_BITS: UInt = 120u

        const val MAX_ACTIONS = 255

        const val CONFIG_KEY_LENGTH: Int = 32

        const val STD_WORKCHAIN_BITS: Int = 8
        const val ADDRESS_BITS: Int = 256

        // Utility bit sizes for arith operations
        val INT_EXT1_BITS: UInt = INT_BITS + 1u
        val INT_EXT256_BITS: UInt = INT_BITS + 256u

        val cellDataField: TvmField = TvmFieldImpl(TvmCellType, "data")
        val cellDataLengthField: TvmField = TvmFieldImpl(TvmCellType, "dataLength")
        val cellRefsLengthField: TvmField = TvmFieldImpl(TvmCellType, "refsLength")

        val sliceDataPosField: TvmField = TvmFieldImpl(TvmSliceType, "dataPos")
        val sliceRefPosField: TvmField = TvmFieldImpl(TvmSliceType, "refPos")
        val sliceCellField: TvmField = TvmFieldImpl(TvmSliceType, "cell")
    }

    class TvmInt257Sort(ctx: KContext) : KBvCustomSizeSort(ctx, INT_BITS)
    class TvmCellDataSort(ctx: KContext) : KBvCustomSizeSort(ctx, CELL_DATA_BITS)

    // Utility sorts for arith operations
    class TvmInt257Ext1Sort(ctx: KContext) : KBvCustomSizeSort(ctx, INT_EXT1_BITS)
    class TvmInt257Ext256Sort(ctx: KContext) : KBvCustomSizeSort(ctx, INT_EXT256_BITS)
}
