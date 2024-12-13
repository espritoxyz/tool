package org.usvm.machine.state

import org.ton.Endian
import org.usvm.StateId
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.api.readField
import org.usvm.api.writeField
import org.usvm.machine.TvmContext.Companion.cellDataLengthField
import org.usvm.machine.TvmContext.Companion.cellRefsLengthField
import org.usvm.machine.TvmContext.Companion.sliceCellField
import org.usvm.machine.TvmContext.Companion.sliceDataPosField
import org.usvm.machine.TvmContext.Companion.sliceRefPosField
import org.usvm.machine.TvmContext.TvmInt257Sort
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.TvmStepScopeManager
import org.usvm.machine.types.TvmCellDataMsgAddrRead
import org.usvm.machine.types.TvmSliceType
import org.usvm.machine.types.loadCoinLabelToBuilder
import org.usvm.machine.types.loadIntLabelToBuilder
import org.usvm.machine.types.makeSliceRefLoad
import org.usvm.machine.types.makeSliceTypeLoad
import org.usvm.sizeSort

fun builderStoreIntTlb(
    scope: TvmStepScopeManager,
    builder: UConcreteHeapRef,
    int: UExpr<TvmInt257Sort>,
    sizeBits: UExpr<TvmSizeSort>,
    isSigned: Boolean = false,
): Unit? = scope.doWithCtx {
    scope.builderStoreInt(builder, int, sizeBits.signedExtendToInteger(), isSigned)
        ?: return@doWithCtx null

    scope.calcOnStateCtx {
        loadIntLabelToBuilder(builder, builder, sizeBits, int, isSigned, Endian.BigEndian)
    }
}

fun builderStoreGramsTlb(
    scope: TvmStepScopeManager,
    builder: UConcreteHeapRef,
    grams: UExpr<TvmInt257Sort>,
): Unit? = scope.doWithCtx {
    scope.doWithState {
        loadCoinLabelToBuilder(builder, builder)
    }

    return@doWithCtx scope.builderStoreGrams(builder, grams)
}

fun builderStoreSliceTlb(
    scope: TvmStepScopeManager,
    builder: UConcreteHeapRef,
    slice: UHeapRef,
): Unit? = scope.doWithCtx { scope.builderStoreSlice(builder, slice) }

fun sliceLoadIntTlb(
    scope: TvmStepScopeManager,
    slice: UHeapRef,
    sizeBits: Int,
    isSigned: Boolean = false,
): Pair<UHeapRef, UExpr<TvmInt257Sort>>? = scope.doWithCtx {
    val updatedSliceAddress = scope.calcOnState {
        memory.allocConcrete(TvmSliceType).also {
            sliceCopy(slice, it)
            sliceMoveDataPtr(it, sizeBits)
        }
    }

    val value = scope.slicePreloadDataBits(slice, sizeBits)
        ?: return@doWithCtx null

    val extendedValue = if (isSigned) value.signedExtendToInteger() else value.unsignedExtendToInteger()

    updatedSliceAddress to extendedValue
}

fun sliceLoadAddrTlb(
    scope: TvmStepScopeManager,
    slice: UHeapRef,
): Pair<UHeapRef, UHeapRef>? = scope.doWithCtx {
    val updatedSlice = scope.calcOnState {
        memory.allocConcrete(TvmSliceType).also { sliceCopy(slice, it) }
    }
    val addrSlice = scope.calcOnState {
        memory.allocConcrete(TvmSliceType).also { sliceDeepCopy(slice, it) }
    }

    var error = false
    val ctx = scope.calcOnState { ctx }
    val originalStateId = scope.calcOnState { id }
    scope.makeSliceTypeLoad(slice, TvmCellDataMsgAddrRead(ctx), updatedSlice) {

        // hide the original [scope] from this closure
        @Suppress("NAME_SHADOWING", "UNUSED_VARIABLE")
        val scope = Unit

        validateSliceLoadState(originalStateId)

        calcOnStateCtx {
            val addrLength = slicePreloadAddrLength(slice)
                ?: return@calcOnStateCtx null
            sliceMoveDataPtr(updatedSlice, addrLength)

            val addrDataPos = memory.readField(addrSlice, sliceDataPosField, sizeSort)
            val addrRefPos = memory.readField(addrSlice, sliceRefPosField, sizeSort)
            val addrCell = memory.readField(addrSlice, sliceCellField, addressSort)
            // new data length to ensure that the remaining slice bits count is equal to [addrLength]
            val addrDataLength = mkBvAddExpr(addrDataPos, addrLength)
            memory.writeField(addrCell, cellDataLengthField, sizeSort, addrDataLength, guard = trueExpr)
            // new refs length to ensure that the remaining slice refs count is equal to 0
            memory.writeField(addrCell, cellRefsLengthField, sizeSort, addrRefPos, guard = trueExpr)

            val originalCell = memory.readField(slice, sliceCellField, addressSort)
            checkCellDataUnderflow(this@makeSliceTypeLoad, originalCell, addrDataLength)
        } ?: run { error = true; return@makeSliceTypeLoad }
    }

    if (!error) updatedSlice to addrSlice else null
}

fun sliceLoadGramsTlb(
    scope: TvmStepScopeManager,
    slice: UHeapRef,
): Pair<UHeapRef, UExpr<TvmInt257Sort>>? {
    val updatedSlice = scope.calcOnState {
        memory.allocConcrete(TvmSliceType).also { sliceCopy(slice, it) }
    }

    var resGrams: UExpr<TvmInt257Sort>? = null
    val originalStateId = scope.calcOnState { id }
    sliceLoadGrams(scope, slice, updatedSlice) { grams ->
        validateSliceLoadState(originalStateId)

        resGrams = grams
    }

    return resGrams?.let { updatedSlice to it }
}

fun sliceLoadRefTlb(
    scope: TvmStepScopeManager,
    slice: UHeapRef
): Pair<UHeapRef, UHeapRef>? {
    var ref: UHeapRef? = null
    val updatedSlice = scope.calcOnState {
        memory.allocConcrete(TvmSliceType).also { sliceCopy(slice, it) }
    }

    val originalStateId = scope.calcOnState { id }
    scope.makeSliceRefLoad(slice, updatedSlice) {

        // hide the original [scope] from this closure
        @Suppress("NAME_SHADOWING", "UNUSED_VARIABLE")
        val scope = Unit

        validateSliceLoadState(originalStateId)

        doWithState {
            ref = slicePreloadNextRef(slice) ?: return@doWithState
            sliceMoveRefPtr(updatedSlice)
        }
    }

    return ref?.let { updatedSlice to it }
}

private fun TvmStepScopeManager.validateSliceLoadState(originalStateId: StateId) = doWithState {
    require(id == originalStateId) {
        "Forks are not supported here"
    }
}
