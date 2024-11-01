package org.usvm.machine.types

import org.ton.Endian
import org.ton.TvmDataCellLabel
import org.ton.TvmParameterInfo
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.api.readField
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.TvmStepScopeManager
import org.usvm.machine.TvmStepScopeManager.ActionOnCondition
import org.usvm.machine.state.TvmMethodResult
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.calcOnStateCtx
import org.usvm.machine.state.doWithCtx
import org.usvm.mkSizeAddExpr
import org.usvm.sizeSort
import org.usvm.utils.extractAddresses


sealed class TvmSymbolicCellDataType(open val sizeBits: UExpr<TvmSizeSort>)

data class TvmSymbolicCellDataInteger(
    override val sizeBits: UExpr<TvmSizeSort>,
    val isSigned: Boolean,
    val endian: Endian
) : TvmSymbolicCellDataType(sizeBits)

data class TvmSymbolicCellMaybeConstructorBit(val ctx: TvmContext) : TvmSymbolicCellDataType(ctx.oneSizeExpr)

// TODO: support other types of MsgAddr (now only stdMsgAddr is supported)
data class TvmSymbolicCellDataMsgAddr(val ctx: TvmContext) : TvmSymbolicCellDataType(ctx.stdMsgAddrSizeExpr)

data class TvmSymbolicCellDataBitArray(override val sizeBits: UExpr<TvmSizeSort>) : TvmSymbolicCellDataType(sizeBits)

data class TvmSymbolicCellDataCoins(
    val ctx: TvmContext,
    val coinsPrefix: UExpr<TvmSizeSort>  // 4-bit unsigned integer in front of coins amount
) : TvmSymbolicCellDataType(ctx.calculateExtendedCoinsLength(coinsPrefix))

private fun TvmContext.calculateExtendedCoinsLength(coinsPrefix: UExpr<TvmSizeSort>): UExpr<TvmSizeSort> {
    val extendedLength = mkBvShiftLeftExpr(coinsPrefix, shift = threeSizeExpr)
    return mkSizeAddExpr(extendedLength, fourSizeExpr)
}

private sealed interface MakeSliceTypeLoadOutcome

private data class NewTlbStack(val stack: TlbStack) : MakeSliceTypeLoadOutcome

private data class Error(val error: TvmMethodResult.TvmStructuralError) : MakeSliceTypeLoadOutcome

private data object NoTlbStack : MakeSliceTypeLoadOutcome

fun TvmStepScopeManager.makeSliceTypeLoad(
    oldSlice: UHeapRef,
    type: TvmSymbolicCellDataType,
    newSlice: UConcreteHeapRef,
    restActions: TvmStepScopeManager.() -> Unit,
) {
    val turnOnTLBParsingChecks = doWithCtx { tvmOptions.turnOnTLBParsingChecks }

    val outcomes = hashMapOf<MakeSliceTypeLoadOutcome, UBoolExpr>()

    calcOnStateCtx {
        val cellAddress = memory.readField(oldSlice, TvmContext.sliceCellField, addressSort)
        val offset = memory.readField(oldSlice, TvmContext.sliceDataPosField, sizeSort)
        val loadList = dataCellLoadedTypeInfo.loadData(cellAddress, offset, type, oldSlice)
        loadList.forEach { load ->
            val tlbStack = dataCellInfoStorage.sliceMapper.getTlbStack(load.sliceAddress)
            tlbStack?.step(this, load)?.forEach { (guard, stepResult) ->
                when (stepResult) {
                    is TlbStack.Error -> {
                        val outcome =
                            if (turnOnTLBParsingChecks) {
                                Error(stepResult.error)
                            } else {
                                NoTlbStack
                            }
                        val oldValue = outcomes[outcome] ?: falseExpr
                        outcomes[outcome] = oldValue or (guard and load.guard)
                    }
                    is TlbStack.NewStack -> {
                        val outcome = NewTlbStack(stepResult.stack)
                        val oldValue = outcomes[outcome] ?: falseExpr
                        outcomes[outcome] = oldValue or (guard and load.guard)
                    }
                }
            } ?: run {
                val oldValue = outcomes[NoTlbStack] ?: falseExpr
                outcomes[NoTlbStack] = oldValue or load.guard
            }
        }
    }

    doWithConditions(
        givenConditionsWithActions = outcomes.entries.map { (outcome, guard) ->
            val action = processMakeSliceTypeLoadOutcome(newSlice, outcome)
            ActionOnCondition(
                action = action,
                condition = guard,
                caseIsExceptional = outcome is Error,
            )
        },
        doForAllBlock = {
            // we execute [restActions] only on states that haven't terminated yet
            if (calcOnState { methodResult == TvmMethodResult.NoCall }) {
                restActions()
            }
        }
    )
}

private fun processMakeSliceTypeLoadOutcome(
    newSlice: UConcreteHeapRef,
    outcome: MakeSliceTypeLoadOutcome
): TvmState.() -> Unit =
    when (outcome) {
        is NoTlbStack -> {
            // nothing
            {}
        }
        is Error -> {
            { methodResult = outcome.error }
        }
        is NewTlbStack -> {
            { dataCellInfoStorage.sliceMapper.mapSliceToTlbStack(newSlice, outcome.stack) }
        }
    }

fun TvmStepScopeManager.assertEndOfCell(
    slice: UHeapRef
): Unit? {
    val turnOnTLBParsingChecks = doWithCtx { tvmOptions.turnOnTLBParsingChecks }
    if (!turnOnTLBParsingChecks) {
        return Unit
    }
    return calcOnStateCtx {
        val cellAddress = memory.readField(slice, TvmContext.sliceCellField, addressSort)
        val offset = memory.readField(slice, TvmContext.sliceDataPosField, sizeSort)
        val refNumber = memory.readField(slice, TvmContext.sliceRefPosField, sizeSort)
        val actions = dataCellLoadedTypeInfo.makeEndOfCell(cellAddress, offset, refNumber)
        actions.forEach {
            val noConflictCond = dataCellInfoStorage.getNoUnexpectedEndOfReadingCondition(this, it)
            fork(
                noConflictCond,
                falseStateIsExceptional = true,
                blockOnFalseState = {
                    methodResult = TvmMethodResult.TvmStructuralError(TvmUnexpectedEndOfReading)
                }
            ) ?: return@calcOnStateCtx null
        }
    }
}

fun TvmStepScopeManager.makeSliceRefLoad(
    oldSlice: UHeapRef,
    newSlice: UConcreteHeapRef,
    restActions: TvmStepScopeManager.() -> Unit,
) {
    val turnOnTLBParsingChecks = doWithCtx { tvmOptions.turnOnTLBParsingChecks }
    if (turnOnTLBParsingChecks) {
        calcOnStateCtx {
            val cellAddress = memory.readField(oldSlice, TvmContext.sliceCellField, addressSort)
            val refNumber =
                mkSizeAddExpr(memory.readField(oldSlice, TvmContext.sliceRefPosField, sizeSort), oneSizeExpr)
            val loadList = dataCellLoadedTypeInfo.loadRef(cellAddress, refNumber)
            loadList.forEach { load ->
                val noConflictCond = dataCellInfoStorage.getNoUnexpectedLoadRefCondition(this, load)
                fork(
                    noConflictCond,
                    falseStateIsExceptional = true,
                    blockOnFalseState = {
                        methodResult = TvmMethodResult.TvmStructuralError(TvmUnexpectedRefReading)
                    }
                ) ?: return@calcOnStateCtx null
            }
        } ?: return
    }

    // One cell on a concrete address might both have and not have TL-B scheme for different constraints.
    // This is why absence of TL-B stack is a separate situation on which we have to fork.
    // This is why type of the key is [TlbStack?]
    val possibleTlbStacks = mutableMapOf<TlbStack?, UBoolExpr>()

    calcOnStateCtx {
        val concreteSlices = extractAddresses(oldSlice, extractAllocated = true)
        concreteSlices.forEach { (guard, slice) ->
            val stack = dataCellInfoStorage.sliceMapper.getTlbStack(slice)
            val oldValue = possibleTlbStacks[stack] ?: falseExpr
            possibleTlbStacks[stack] = oldValue or guard
        }
    }

    doWithConditions(
        possibleTlbStacks.map { (stack, guard) ->
            ActionOnCondition(
                action = { stack?.let { dataCellInfoStorage.sliceMapper.mapSliceToTlbStack(newSlice, it) } },
                condition = guard,
                caseIsExceptional = false,
            )
        },
        doForAllBlock = restActions
    )
}

fun TvmStepScopeManager.makeCellToSlice(
    cellAddress: UHeapRef,
    sliceAddress: UConcreteHeapRef,
    restActions: TvmStepScopeManager.() -> Unit
) {
    // One cell on a concrete address might both have and not have TL-B scheme for different constraints.
    // This is why absence of TL-B stack is a separate situation on which we have to fork.
    // This is why type of the key is [TlbStack?]
    val possibleLabels = mutableMapOf<TvmDataCellLabel?, UBoolExpr>()

    calcOnStateCtx {
        val infoVariants = dataCellInfoStorage.getLabelForFreshSlice(cellAddress)
        infoVariants.forEach { (cellInfo, guard) ->
            val label = (cellInfo as? TvmParameterInfo.DataCellInfo)?.dataCellStructure
            val oldValue = possibleLabels[label] ?: falseExpr
            possibleLabels[label] = oldValue or guard
        }
    }

    doWithConditions(
        possibleLabels.map { (label, guard) ->
            ActionOnCondition(
                action = { label?.let { dataCellInfoStorage.sliceMapper.allocateInitialSlice(ctx, sliceAddress, label) } },
                condition = guard,
                caseIsExceptional = false,
            )
        },
        doForAllBlock = restActions
    )
}
