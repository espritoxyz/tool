package org.usvm.machine.types

import io.ksmt.expr.KBitVecValue
import io.ksmt.expr.KInterpretedValue
import org.ton.Endian
import org.ton.TlbCoinsLabel
import org.ton.TlbIntegerLabelOfConcreteSize
import org.ton.TlbIntegerLabelOfSymbolicSize
import org.ton.TlbLabel
import org.ton.TvmParameterInfo
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.api.readField
import org.usvm.isAllocated
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.TvmStepScopeManager
import org.usvm.machine.TvmStepScopeManager.ActionOnCondition
import org.usvm.machine.intValue
import org.usvm.machine.state.TvmMethodResult
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.calcOnStateCtx
import org.usvm.machine.state.doWithCtx
import org.usvm.mkSizeAddExpr
import org.usvm.sizeSort
import org.usvm.utils.extractAddresses


sealed class TvmCellDataTypeRead(open val sizeBits: UExpr<TvmSizeSort>)

data class TvmCellDataIntegerRead(
    override val sizeBits: UExpr<TvmSizeSort>,
    val isSigned: Boolean,
    val endian: Endian
) : TvmCellDataTypeRead(sizeBits)

data class TvmCellMaybeConstructorBitRead(val ctx: TvmContext) : TvmCellDataTypeRead(ctx.oneSizeExpr)

// TODO: support other types of MsgAddr (now only stdMsgAddr is supported)
data class TvmCellDataMsgAddrRead(val ctx: TvmContext) : TvmCellDataTypeRead(ctx.stdMsgAddrSizeExpr)

data class TvmCellDataBitArrayRead(override val sizeBits: UExpr<TvmSizeSort>) : TvmCellDataTypeRead(sizeBits)

data class TvmCellDataCoinsRead(
    val ctx: TvmContext,
    val coinsPrefix: UExpr<TvmSizeSort>  // 4-bit unsigned integer in front of coins amount
) : TvmCellDataTypeRead(ctx.calculateExtendedCoinsLength(coinsPrefix))

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
    type: TvmCellDataTypeRead,
    newSlice: UConcreteHeapRef,
    restActions: TvmStepScopeManager.() -> Unit,
) {
    val turnOnTLBParsingChecks = doWithCtx { tvmOptions.turnOnTLBParsingChecks }
    val performTlbChecksOnAllocatedCells = doWithCtx { tvmOptions.tlbOptions.performTlbChecksOnAllocatedCells }

    val outcomes = hashMapOf<MakeSliceTypeLoadOutcome, UBoolExpr>()

    calcOnStateCtx {
        val cellAddress = memory.readField(oldSlice, TvmContext.sliceCellField, addressSort)
        val offset = memory.readField(oldSlice, TvmContext.sliceDataPosField, sizeSort)
        val loadList = dataCellLoadedTypeInfo.loadData(cellAddress, offset, type, oldSlice)
        loadList.forEach { load ->
            val tlbStack = dataCellInfoStorage.sliceMapper.getTlbStack(load.sliceAddress)
            tlbStack?.step(this, LimitedLoadData.fromLoadData(load))?.forEach { (guard, stepResult) ->
                when (stepResult) {
                    is TlbStack.Error -> {
                        val outcome =
                            if (turnOnTLBParsingChecks && (!load.cellAddress.isAllocated || performTlbChecksOnAllocatedCells)) {
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
            val noConflictCond = if (it.cellAddress.isAllocated) {
                trueExpr
            } else {
                dataCellInfoStorage.getNoUnexpectedEndOfReadingCondition(this, it)
            }
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
                val noConflictCond = if (load.cellAddress.isAllocated) {
                    trueExpr
                } else {
                    dataCellInfoStorage.getNoUnexpectedLoadRefCondition(this, load)
                }
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
    val possibleLabels = mutableMapOf<TlbLabel?, UBoolExpr>()

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

private fun TvmState.addTlbLabelToBuilder(
    oldBuilder: UConcreteHeapRef,
    newBuilder: UConcreteHeapRef,
    label: TlbLabel,
) {
    val oldTlbBuilder = dataCellInfoStorage.mapper.getTlbBuilder(oldBuilder)
        ?: return
    val newTlbBuilder = oldTlbBuilder.addTlbLabel(label)
    dataCellInfoStorage.mapper.addTlbBuilder(newBuilder, newTlbBuilder)
}

private fun TvmState.addTlbConstantToBuilder(
    oldBuilder: UConcreteHeapRef,
    newBuilder: UConcreteHeapRef,
    constant: String,
) {
    val oldTlbBuilder = dataCellInfoStorage.mapper.getTlbBuilder(oldBuilder)
        ?: return
    val newTlbBuilder = oldTlbBuilder.addConstant(constant)
    dataCellInfoStorage.mapper.addTlbBuilder(newBuilder, newTlbBuilder)
}

fun TvmState.loadIntLabelToBuilder(
    oldBuilder: UConcreteHeapRef,
    newBuilder: UConcreteHeapRef,
    sizeBits: UExpr<TvmSizeSort>,
    value: UExpr<TvmContext.TvmInt257Sort>,
    isSigned: Boolean,
    endian: Endian,
) {

    // special case for storing constants
    if (value is KBitVecValue && sizeBits is KInterpretedValue) {
        val constValue = (value as KBitVecValue<*>).stringValue.takeLast(sizeBits.intValue())
        addTlbConstantToBuilder(oldBuilder, newBuilder, constValue)
        return
    }

    val label = if (sizeBits is KInterpretedValue) {
        TlbIntegerLabelOfConcreteSize(sizeBits.intValue(), isSigned = isSigned, endian = endian)
    } else {
        TlbIntegerLabelOfSymbolicSize(isSigned, endian, arity = 0) { _, _ -> sizeBits }
    }

    addTlbLabelToBuilder(oldBuilder, newBuilder, label)
}

fun TvmState.loadCoinLabelToBuilder(
    oldBuilder: UConcreteHeapRef,
    newBuilder: UConcreteHeapRef,
) {
    addTlbLabelToBuilder(oldBuilder, newBuilder, TlbCoinsLabel)
}
