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
        val type: TvmSymbolicCellDataType,
        val offset: UExpr<TvmSizeSort>,
        override val address: UConcreteHeapRef,
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

    private fun <ConcreteAction: Action> registerAction(
        cellAddress: UHeapRef,
        action: (GuardedExpr<UConcreteHeapRef>) -> ConcreteAction,
    ): List<ConcreteAction> {
        val ctx = cellAddress.ctx
        val actionList = mutableListOf<ConcreteAction>()
        val blockOnConcreteAddress = {
            map: PersistentMap<UConcreteHeapRef, PersistentList<Action>>,
            guardedExpr: GuardedExpr<UConcreteHeapRef> ->
            val ref = guardedExpr.expr
            val oldList = map.getOrDefault(ref, persistentListOf())
            val actionInstance = action(guardedExpr)
            actionList.add(actionInstance)
            val newList = oldList.add(actionInstance)
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

        return actionList
    }

    fun loadData(
        cellAddress: UHeapRef,
        offset: UExpr<TvmSizeSort>,
        type: TvmSymbolicCellDataType,
    ): List<LoadData> {
        val action = { ref: GuardedExpr<UConcreteHeapRef> -> LoadData(ref.guard, type, offset, ref.expr) }
        return registerAction(cellAddress, action)
    }

    fun loadRef(
        cellAddress: UHeapRef,
        refPos: UExpr<TvmSizeSort>,
    ): List<LoadRef> {
        val action = { ref: GuardedExpr<UConcreteHeapRef> -> LoadRef(ref.guard, ref.expr, refPos) }
        return registerAction(cellAddress, action)
    }

    fun makeEndOfCell(
        cellAddress: UHeapRef,
        offset: UExpr<TvmSizeSort>,
        refNumber: UExpr<TvmSizeSort>
    ): List<EndOfCell> {
        val action = { ref: GuardedExpr<UConcreteHeapRef> -> EndOfCell(ref.guard, ref.expr, offset, refNumber) }
        return registerAction(cellAddress, action)
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
        val loadList = tvmDataCellLoadedTypeInfo.loadData(cellAddress, offset, type)
        loadList.forEach { load ->
            val noConflictCond = tvmDataCellInfoStorage.getNoConflictConditionsForLoadData(this, load)
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
        val actions = tvmDataCellLoadedTypeInfo.makeEndOfCell(cellAddress, offset, refNumber)
        actions.forEach {
            val noConflictCond = tvmDataCellInfoStorage.getNoUnexpectedEndOfReadingCondition(this, it)
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
        val refNumber = mkBvAddExpr(memory.readField(slice, TvmContext.sliceRefPosField, sizeSort), mkSizeExpr(1))
        val loadList = tvmDataCellLoadedTypeInfo.loadRef(cellAddress, refNumber)
        loadList.forEach { load ->
            val noConflictCond = tvmDataCellInfoStorage.getNoUnexpectedLoadRefCondition(this, load)
            fork(
                noConflictCond,
                blockOnFalseState = {
                    methodResult = TvmUnexpectedRefReading
                }
            ) ?: return@calcOnStateCtx null
        }
    }
}
