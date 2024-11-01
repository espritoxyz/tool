package org.usvm.machine.types

import org.ton.TvmAtomicDataCellLabel
import org.ton.TvmBuiltinDataCellLabel
import org.ton.TvmCoinsLabel
import org.ton.TvmInputInfo
import org.ton.TvmIntegerLabel
import org.ton.TvmInternalShortStdMsgAddrLabel
import org.ton.TvmInternalStdMsgAddrLabel
import org.ton.TvmMaybeRefLabel
import org.ton.TvmMsgAddrLabel
import org.ton.TvmParameterInfo
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.api.readField
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.Companion.sliceCellField
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.state.TvmStack
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.generateSymbolicCell
import org.usvm.machine.state.generateSymbolicSlice
import org.usvm.machine.state.loadIntFromCellWithoutChecks
import org.usvm.mkSizeAddExpr
import org.usvm.mkSizeExpr
import org.usvm.types.USingleTypeStream

fun <AccType> foldOnTvmType(type: TvmType, init: AccType, f: (AccType, TvmType) -> AccType): AccType {
    var acc = init
    var curType: TvmType? = type
    while (curType != null) {
        acc = f(acc, curType)
        curType = curType.parentType
    }
    return acc
}

fun TvmState.getPossibleTypes(ref: UConcreteHeapRef): Sequence<TvmType> {
    val stream = memory.types.getTypeStream(ref)
    check(stream is USingleTypeStream)
    val type = stream.commonSuperType
    return typeSystem.findSubtypes(type)
}

context(TvmContext)
fun TvmBuiltinDataCellLabel.accepts(symbolicType: TvmSymbolicCellDataType): UBoolExpr =
    when (this) {
        is TvmIntegerLabel -> {
            if (symbolicType !is TvmSymbolicCellDataInteger || isSigned != symbolicType.isSigned || endian != symbolicType.endian) {
                falseExpr
            } else {
                symbolicType.sizeBits eq mkBv(bitSize)
            }
        }
        is TvmMsgAddrLabel -> {
            (symbolicType is TvmSymbolicCellDataMsgAddr).expr
        }
        is TvmCoinsLabel -> {
            (symbolicType is TvmSymbolicCellDataCoins).expr
        }
        is TvmMaybeRefLabel -> {
            when (symbolicType) {
                is TvmSymbolicCellMaybeConstructorBit -> trueExpr
                is TvmSymbolicCellDataInteger -> symbolicType.sizeBits eq oneSizeExpr
                else -> falseExpr
            }
        }
    }

context(TvmContext)
fun TvmAtomicDataCellLabel.offset(
    state: TvmState,
    address: UHeapRef,
    prefixSize: UExpr<TvmSizeSort>,
): UExpr<TvmSizeSort> =
    when (this) {
        is TvmIntegerLabel -> {
            mkSizeExpr(bitSize)
        }
        is TvmInternalStdMsgAddrLabel -> {
            mkSizeExpr(internalStdMsgAddrSize)
        }
        is TvmInternalShortStdMsgAddrLabel -> {
            mkSizeExpr(internalShortStdMsgAddrSize)
        }
        is TvmCoinsLabel -> {
            val prefix = state.loadIntFromCellWithoutChecks(
                address,
                prefixSize,
                fourValue,
                isSigned = false
            ).extractToSizeSort()
            val length = mkBvShiftLeftExpr(prefix, shift = threeSizeExpr)

            mkSizeAddExpr(fourSizeExpr, length)
        }
    }

private const val internalStdMsgAddrSize = 8 + 256
private const val internalShortStdMsgAddrSize = 256

private val defaultInternalMsgAddr = "0".repeat(internalStdMsgAddrSize)
private val defaultInternalShortMsgAddr = "0".repeat(internalShortStdMsgAddrSize)

fun TvmAtomicDataCellLabel.defaultCellValueOfMinimalLength(): String =
    when (this) {
        is TvmIntegerLabel -> "0".repeat(bitSize)
        is TvmInternalStdMsgAddrLabel -> defaultInternalMsgAddr
        is TvmInternalShortStdMsgAddrLabel -> defaultInternalShortMsgAddr
        is TvmCoinsLabel -> "0000"
    }

fun TvmAtomicDataCellLabel.maximumLength(): Int =
    when (this) {
        is TvmIntegerLabel -> bitSize
        is TvmInternalStdMsgAddrLabel -> internalStdMsgAddrSize
        is TvmCoinsLabel -> 4 + TvmContext.MAX_GRAMS_BITS.toInt()
        is TvmInternalShortStdMsgAddrLabel -> internalShortStdMsgAddrSize
    }

data class InputParametersStructure(
    val cellToInfo: Map<UConcreteHeapRef, TvmParameterInfo.CellInfo>,
    val sliceToCell: Map<UConcreteHeapRef, UConcreteHeapRef>,
)

fun extractInputParametersAddresses(
    initialState: TvmState,
    inputInfo: TvmInputInfo,
): InputParametersStructure {
    val cells = hashMapOf<UConcreteHeapRef, TvmParameterInfo.CellInfo>()
    val slices = hashMapOf<UConcreteHeapRef, UConcreteHeapRef>()
    inputInfo.parameterInfos.entries.forEach { (param, paramInfo) ->
        val entry = initialState.stack.peekStackEntry(param)
        check(entry is TvmStack.TvmInputStackEntry) {
            "During TvmAddressToLabelMapper building stack must consist only of input entries, but $entry found"
        }

        when (paramInfo) {
            is TvmParameterInfo.CellInfo -> {
                val stackValue = initialState.stack.getStackValue(entry, TvmCellType) {
                    initialState.generateSymbolicCell()
                }
                // At this point stack should be empty (since TvmState is the initial state)
                // => stackValue is from input stack entry
                // => stackValue.cellValue must be UConcreteHeapRef
                val address = stackValue.cellValue as UConcreteHeapRef
                cells[address] = paramInfo
            }

            is TvmParameterInfo.SliceInfo -> {
                val stackValue = initialState.stack.getStackValue(entry, TvmSliceType) {
                    initialState.generateSymbolicSlice()
                }
                val sliceAddress = stackValue.sliceValue
                    ?: error("Could not extract slice address while building TvmDataCellInfoStorage")
                // At this point stack should be empty (since TvmState is the initial state)
                // => stackValue is from input stack entry
                // => sliceAddress must be UConcreteHeapRef
                // => the corresponding cell address must also be concrete
                val address =
                    initialState.memory.readField(sliceAddress, sliceCellField, initialState.ctx.addressSort) as UConcreteHeapRef
                cells[address] = paramInfo.cellInfo
                // sliceAddress is concrete for the same reason as cell's address
                slices[sliceAddress as UConcreteHeapRef] = address
            }

            is TvmParameterInfo.NoInfo -> {
                // do nothing
            }
        }
    }

    return InputParametersStructure(cells, slices)
}
