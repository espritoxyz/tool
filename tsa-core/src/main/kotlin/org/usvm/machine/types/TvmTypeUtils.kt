package org.usvm.machine.types

import org.ton.TvmCoinsLabel
import org.ton.TvmDataCellLabel
import org.ton.TvmIntegerLabel
import org.ton.TvmMsgAddrLabel
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.state.TvmState
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
fun TvmDataCellLabel.accepts(symbolicType: TvmSymbolicCellDataType): UBoolExpr =
    when (this) {
        is TvmIntegerLabel -> {
            if (symbolicType !is TvmSymbolicCellDataInteger || isSigned != symbolicType.isSigned || endian != symbolicType.endian) {
                falseExpr
            } else {
                symbolicType.sizeBits eq mkBv(bitSize)
            }
        }
        is TvmMsgAddrLabel -> {
            if (symbolicType is TvmSymbolicCellDataMsgAddr) {
                trueExpr
            } else {
                falseExpr
            }
        }
        is TvmCoinsLabel -> {
            if (symbolicType is TvmSymbolicCellDataCoins) {
                trueExpr
            } else {
                falseExpr
            }
        }
    }

context(TvmContext)
fun TvmDataCellLabel.offset(state: TvmState, address: UConcreteHeapRef, prefixSize: UExpr<TvmSizeSort>): UExpr<TvmSizeSort> =
    when (this) {
        is TvmIntegerLabel -> {
            mkSizeExpr(bitSize)
        }
        is TvmMsgAddrLabel -> {
            TODO()
        }
        is TvmCoinsLabel -> {
            TODO()
        }
    }
