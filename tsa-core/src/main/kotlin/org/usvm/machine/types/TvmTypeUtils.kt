package org.usvm.machine.types

import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
import org.usvm.machine.TvmContext
import org.usvm.machine.state.TvmState
import org.usvm.sizeSort
import org.usvm.test.resolver.TvmCellDataBitArray
import org.usvm.test.resolver.TvmCellDataInteger
import org.usvm.test.resolver.TvmCellDataMaybeConstructorBit
import org.usvm.test.resolver.TvmCellDataMsgAddr
import org.usvm.test.resolver.TvmCellDataType
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
fun TvmCellDataType.accepts(symbolicType: TvmSymbolicCellDataType): UBoolExpr =
    when (this) {
        is TvmCellDataInteger -> {
            if (symbolicType !is TvmSymbolicCellDataInteger || isSigned != symbolicType.isSigned || endian != symbolicType.endian) {
                falseExpr
            } else {
                symbolicType.sizeBits eq mkBv(bitSize)
            }
        }
        is TvmCellDataMaybeConstructorBit -> {
            if (symbolicType is TvmSymbolicCellMaybeConstructorBit) {
                trueExpr
            } else {
                falseExpr
            }
        }
        is TvmCellDataMsgAddr -> {
            if (symbolicType is TvmSymbolicCellDataMsgAddr) {
                trueExpr
            } else {
                falseExpr
            }
        }
        is TvmCellDataBitArray -> {
            if (symbolicType !is TvmSymbolicCellDataBitArray) {
                falseExpr
            } else {
                symbolicType.sizeBits eq mkBv(bitSize)
            }
        }
    }