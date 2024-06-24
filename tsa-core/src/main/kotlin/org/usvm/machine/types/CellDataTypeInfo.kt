package org.usvm.machine.types

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.usvm.*
import org.usvm.api.readField
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.state.TvmState
import org.usvm.memory.GuardedExpr
import org.usvm.memory.foldHeapRef

class CellDataTypeInfo(
    var addressToActions: PersistentMap<UConcreteHeapRef, PersistentList<Load>>
) {
    class Load(val guard: UBoolExpr, val type: TvmSymbolicCellDataType, val offset: UExpr<TvmSizeSort>)

    fun makeLoad(cellAddress: UHeapRef, offset: UExpr<TvmSizeSort>, type: TvmSymbolicCellDataType) {
        val ctx = cellAddress.ctx
        val blockOnConcreteAddress = { map: PersistentMap<UConcreteHeapRef, PersistentList<Load>>, guardedExpr: GuardedExpr<UConcreteHeapRef> ->
            val oldList = map.getOrDefault(guardedExpr.expr, persistentListOf())
            val newList = oldList.add(Load(guardedExpr.guard, type, offset))
            map.put(guardedExpr.expr, newList)
        }
        val newMap = foldHeapRef(
            ref = cellAddress,
            initial = addressToActions,
            initialGuard = ctx.trueExpr,
            collapseHeapRefs = false,
            blockOnConcrete = blockOnConcreteAddress,
            staticIsConcrete = true,
            blockOnSymbolic = { _, ref -> error("Unexpected symbolic ref ${ref.expr}") }
        )

        addressToActions = newMap
    }

    fun clone(): CellDataTypeInfo =
        CellDataTypeInfo(addressToActions)

    companion object {
        fun empty() = CellDataTypeInfo(persistentMapOf())
    }
}


sealed class TvmSymbolicCellDataType(val sizeBits: UExpr<TvmSizeSort>)

class TvmSymbolicCellDataInteger(sizeBits: UExpr<TvmSizeSort>, val isSigned: Boolean, val endian: Endian): TvmSymbolicCellDataType(sizeBits)
class TvmSymbolicCellMaybeDictConstructorBit(ctx: TvmContext): TvmSymbolicCellDataType(ctx.mkBv(1))
class TvmSymbolicCellDataMsgAddr(ctx: TvmContext): TvmSymbolicCellDataType(ctx.mkBv(2))
class TvmSymbolicCellDataBitArray(sizeBits: UExpr<TvmSizeSort>): TvmSymbolicCellDataType(sizeBits)
class TvmSymbolicCellDataCoins(
    ctx: TvmContext,
    val coinsPrefix: UExpr<TvmSizeSort>  // 4-bit unsigned integer in front of coins amount
) : TvmSymbolicCellDataType(ctx.calculateExtendedCoinsLength(coinsPrefix))

private fun TvmContext.calculateExtendedCoinsLength(coinsPrefix: UExpr<TvmSizeSort>): UExpr<TvmSizeSort> {
    val extendedLength = mkBvShiftLeftExpr(coinsPrefix, shift = mkSizeExpr(3))
    return mkBvAddExpr(extendedLength, mkSizeExpr(4))
}

fun TvmState.makeSliceTypeLoad(slice: UHeapRef, type: TvmSymbolicCellDataType) = with(ctx) {
    val cellAddress = memory.readField(slice, TvmContext.sliceCellField, addressSort)
    val offset = memory.readField(slice, TvmContext.sliceDataPosField, sizeSort)
    cellDataTypeInfo.makeLoad(cellAddress, offset, type)
}

enum class Endian {
    LittleEndian,
    BigEndian
}