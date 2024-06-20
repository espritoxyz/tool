package org.usvm.machine.types

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.usvm.*
import org.usvm.api.readField
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.TvmStepScope
import org.usvm.machine.state.TvmDataCellTypesError
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.calcOnStateCtx
import org.usvm.machine.state.setFailure
import org.usvm.memory.GuardedExpr
import org.usvm.memory.foldHeapRef
import org.usvm.test.resolver.TvmExecutionWithDataCellTypesError

class TvmDataCellLoadedTypeInfo(
    var addressToActions: PersistentMap<UConcreteHeapRef, PersistentList<Load>>
) {
    class Load(
        val guard: UBoolExpr,
        val type: TvmSymbolicCellDataType,
        val offset: UExpr<TvmSizeSort>,
        val address: UConcreteHeapRef,
    )

    fun makeLoad(
        cellAddress: UHeapRef,
        offset: UExpr<TvmSizeSort>,
        type: TvmSymbolicCellDataType
    ): List<Load> {
        val ctx = cellAddress.ctx
        val loadList = mutableListOf<Load>()
        val blockOnConcreteAddress = { map: PersistentMap<UConcreteHeapRef, PersistentList<Load>>, guardedExpr: GuardedExpr<UConcreteHeapRef> ->
            val ref = guardedExpr.expr
            val oldList = map.getOrDefault(ref, persistentListOf())
            val load = Load(guardedExpr.guard, type, offset, ref)
            loadList.add(load)
            val newList = oldList.add(load)
            map.put(ref, newList)
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

        return loadList
    }

    fun clone(): TvmDataCellLoadedTypeInfo =
        TvmDataCellLoadedTypeInfo(addressToActions)

    companion object {
        fun empty() = TvmDataCellLoadedTypeInfo(persistentMapOf())
    }
}


sealed class TvmSymbolicCellDataType(val sizeBits: UExpr<TvmSizeSort>)

class TvmSymbolicCellDataInteger(sizeBits: UExpr<TvmSizeSort>, val isSigned: Boolean, val endian: Endian): TvmSymbolicCellDataType(sizeBits)
class TvmSymbolicCellMaybeConstructorBit(ctx: TvmContext): TvmSymbolicCellDataType(ctx.mkBv(1))
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

enum class Endian {
    LittleEndian,
    BigEndian
}

fun TvmStepScope.makeSliceTypeLoad(slice: UHeapRef, type: TvmSymbolicCellDataType): Unit? {
    return calcOnStateCtx {
        val cellAddress = memory.readField(slice, TvmContext.sliceCellField, addressSort)
        val offset = memory.readField(slice, TvmContext.sliceDataPosField, sizeSort)
        val loadList = tvmDataCellLoadedTypeInfo.makeLoad(cellAddress, offset, type)
        loadList.forEach { load ->
            val noConflictCond = tvmDataCellInfoStorage.getNoConflictCondition(load)
            fork(
                noConflictCond,
                blockOnFalseState = {
                    methodResult = TvmDataCellTypesError(load.type, load.type) // TODO
                }
            ) ?: return@calcOnStateCtx null
        }
    }
}