package org.usvm.machine.types

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.ton.Endian
import org.usvm.*
import org.usvm.api.readField
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.TvmStepScope
import org.usvm.machine.state.TvmUnexpectedEndOfReading
import org.usvm.machine.state.TvmUnexpectedRefReading
import org.usvm.machine.state.calcOnStateCtx
import org.usvm.memory.GuardedExpr
import org.usvm.memory.foldHeapRef

class TvmDataCellLoadedTypeInfo(
    var addressToActions: PersistentMap<UConcreteHeapRef, PersistentList<Action>>
) {

    sealed interface Action {
        val guard: UBoolExpr
        val address: UConcreteHeapRef
    }

    class LoadData(
        override val guard: UBoolExpr,
        override val address: UConcreteHeapRef,
        val type: TvmSymbolicCellDataType,
        val offset: UExpr<TvmSizeSort>,
    ) : Action

    class LoadRef(
        override val guard: UBoolExpr,
        override val address: UConcreteHeapRef,
        val refNumber: UExpr<TvmSizeSort>,
    ) : Action

    class EndOfCell(
        override val guard: UBoolExpr,
        override val address: UConcreteHeapRef,
        val offset: UExpr<TvmSizeSort>,
        val refNumber: UExpr<TvmSizeSort>,
    ) : Action

    private fun <ConcreteAction : Action> registerAction(
        cellAddress: UHeapRef,
        action: (GuardedExpr<UConcreteHeapRef>) -> ConcreteAction,
    ): List<ConcreteAction> {
        val ctx = cellAddress.ctx
        val actionList = mutableListOf<ConcreteAction>()
        val newMap = foldHeapRef(
            ref = cellAddress,
            initial = addressToActions,
            initialGuard = ctx.trueExpr,
            collapseHeapRefs = false,
            blockOnConcrete = { map: PersistentMap<UConcreteHeapRef, PersistentList<Action>>,
                                guardedExpr: GuardedExpr<UConcreteHeapRef> ->
                val ref = guardedExpr.expr
                val oldList = map.getOrDefault(ref, persistentListOf())
                val actionInstance = action(guardedExpr)
                actionList.add(actionInstance)
                val newList = oldList.add(actionInstance)
                map.put(ref, newList)
            },
            staticIsConcrete = true,
            blockOnSymbolic = { _, ref -> error("Unexpected symbolic ref ${ref.expr}") }
        )

        addressToActions = newMap

        return actionList
    }

    fun loadData(
        cellAddress: UHeapRef,
        offset: UExpr<TvmSizeSort>,
        type: TvmSymbolicCellDataType,
    ): List<LoadData> = registerAction(cellAddress) { ref ->
        LoadData(ref.guard, ref.expr, type, offset)
    }

    fun loadRef(
        cellAddress: UHeapRef,
        refPos: UExpr<TvmSizeSort>,
    ): List<LoadRef> = registerAction(cellAddress) { ref ->
        LoadRef(ref.guard, ref.expr, refPos)
    }

    fun makeEndOfCell(
        cellAddress: UHeapRef,
        offset: UExpr<TvmSizeSort>,
        refNumber: UExpr<TvmSizeSort>
    ): List<EndOfCell> = registerAction(cellAddress) { ref ->
        EndOfCell(ref.guard, ref.expr, offset, refNumber)
    }

    fun clone(): TvmDataCellLoadedTypeInfo =
        TvmDataCellLoadedTypeInfo(addressToActions)

    companion object {
        fun empty() = TvmDataCellLoadedTypeInfo(persistentMapOf())
    }
}


sealed class TvmSymbolicCellDataType(val sizeBits: UExpr<TvmSizeSort>)

class TvmSymbolicCellDataInteger(
    sizeBits: UExpr<TvmSizeSort>,
    val isSigned: Boolean,
    val endian: Endian
) : TvmSymbolicCellDataType(sizeBits)

class TvmSymbolicCellMaybeConstructorBit(ctx: TvmContext) : TvmSymbolicCellDataType(ctx.mkBv(1))

const val stdMsgAddrSize = 2 + 8 + 256

// TODO: support other types of MsgAddr (now only stdMsgAddr is supported)
class TvmSymbolicCellDataMsgAddr(ctx: TvmContext) : TvmSymbolicCellDataType(ctx.mkBv(stdMsgAddrSize))

class TvmSymbolicCellDataBitArray(sizeBits: UExpr<TvmSizeSort>) : TvmSymbolicCellDataType(sizeBits)

class TvmSymbolicCellDataCoins(
    ctx: TvmContext,
    val coinsPrefix: UExpr<TvmSizeSort>  // 4-bit unsigned integer in front of coins amount
) : TvmSymbolicCellDataType(ctx.calculateExtendedCoinsLength(coinsPrefix))

private fun TvmContext.calculateExtendedCoinsLength(coinsPrefix: UExpr<TvmSizeSort>): UExpr<TvmSizeSort> {
    val extendedLength = mkBvShiftLeftExpr(coinsPrefix, shift = threeSizeExpr)
    return mkSizeAddExpr(extendedLength, fourSizeExpr)
}

fun TvmStepScope.makeSliceTypeLoad(slice: UHeapRef, type: TvmSymbolicCellDataType): Unit? {
    return calcOnStateCtx {
        val cellAddress = memory.readField(slice, TvmContext.sliceCellField, addressSort)
        val offset = memory.readField(slice, TvmContext.sliceDataPosField, sizeSort)
        val loadList = dataCellLoadedTypeInfo.loadData(cellAddress, offset, type)
        loadList.forEach { load ->
            val noConflictCond = dataCellInfoStorage.getNoConflictConditionsForLoadData(load)
            noConflictCond.entries.forEach { (error, cond) ->
                fork(
                    cond,
                    blockOnFalseState = {
                        methodResult = error
                    }
                ) ?: return@calcOnStateCtx null
            }
        }
    }
}

fun TvmStepScope.assertEndOfCell(slice: UHeapRef): Unit? {
    return calcOnStateCtx {
        val cellAddress = memory.readField(slice, TvmContext.sliceCellField, addressSort)
        val offset = memory.readField(slice, TvmContext.sliceDataPosField, sizeSort)
        val refNumber = memory.readField(slice, TvmContext.sliceRefPosField, sizeSort)
        val actions = dataCellLoadedTypeInfo.makeEndOfCell(cellAddress, offset, refNumber)
        actions.forEach {
            val noConflictCond = dataCellInfoStorage.getNoUnexpectedEndOfReadingCondition(it)
            fork(
                noConflictCond,
                blockOnFalseState = {
                    methodResult = TvmUnexpectedEndOfReading
                }
            ) ?: return@calcOnStateCtx null
        }
    }
}


fun TvmStepScope.makeSliceRefLoad(slice: UHeapRef): Unit? {
    return calcOnStateCtx {
        val cellAddress = memory.readField(slice, TvmContext.sliceCellField, addressSort)
        val refNumber = mkSizeAddExpr(memory.readField(slice, TvmContext.sliceRefPosField, sizeSort), oneSizeExpr)
        val loadList = dataCellLoadedTypeInfo.loadRef(cellAddress, refNumber)
        loadList.forEach { load ->
            val noConflictCond = dataCellInfoStorage.getNoUnexpectedLoadRefCondition(load)
            fork(
                noConflictCond,
                blockOnFalseState = {
                    methodResult = TvmUnexpectedRefReading
                }
            ) ?: return@calcOnStateCtx null
        }
    }
}
