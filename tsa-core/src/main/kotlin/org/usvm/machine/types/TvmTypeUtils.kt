package org.usvm.machine.types

import org.ton.TvmCoinsLabel
import org.ton.TvmDataCellLabel
import org.ton.TvmIntegerLabel
import org.ton.TvmInternalStdMsgAddrLabel
import org.ton.TvmMaybeLabel
import org.ton.TvmMsgAddrLabel
import org.ton.TvmPrependingSwitchDataCellLabel
import org.ton.TvmRealDataCellLabel
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.loadIntFromCellWithoutChecks
import org.usvm.mkSizeAddExpr
import org.usvm.mkSizeExpr
import org.usvm.mkSizeLeExpr
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
fun TvmRealDataCellLabel.accepts(symbolicType: TvmSymbolicCellDataType): UBoolExpr =
    when (this) {
        is TvmIntegerLabel -> {
            if (symbolicType !is TvmSymbolicCellDataInteger || isSigned != symbolicType.isSigned || endian != symbolicType.endian) {
                falseExpr
            } else {
                symbolicType.sizeBits eq mkBv(bitSize)
            }
        }
        is TvmMsgAddrLabel -> {
            when (symbolicType) {
                is TvmSymbolicCellDataMsgAddr -> trueExpr
                is TvmSymbolicCellDataInteger -> mkSizeLeExpr(symbolicType.sizeBits, mkSizeExpr(stdMsgAddrSize))
                else -> falseExpr
            }
        }
        is TvmCoinsLabel -> {
            if (symbolicType is TvmSymbolicCellDataCoins) {
                trueExpr
            } else {
                falseExpr
            }
        }
        is TvmMaybeLabel -> {
            when (symbolicType) {
                is TvmSymbolicCellMaybeConstructorBit -> trueExpr
                is TvmSymbolicCellDataInteger -> symbolicType.sizeBits eq mkBv(1)
                else -> falseExpr
            }
        }
    }

context(TvmContext)
fun TvmDataCellLabel.offset(
    state: TvmState,
    address: UConcreteHeapRef,
    prefixSize: UExpr<TvmSizeSort>,
): UExpr<TvmSizeSort> =
    when (this) {
        is TvmPrependingSwitchDataCellLabel -> {
            zeroSizeExpr
        }
        is TvmIntegerLabel -> {
            mkSizeExpr(bitSize)
        }
        is TvmInternalStdMsgAddrLabel -> {
            mkSizeExpr(internalStdMsgAddrSize)
        }
        is TvmCoinsLabel -> {
            val prefix = state.loadIntFromCellWithoutChecks(
                address,
                prefixSize,
                4.toBv257(),
                isSigned = false
            ).extractToSizeSort()
            val length = mkBvShiftLeftExpr(prefix, shift = threeSizeExpr)

            mkSizeAddExpr(fourSizeExpr, length)
        }
    }

private const val internalStdMsgAddrSize = 8 + 256
