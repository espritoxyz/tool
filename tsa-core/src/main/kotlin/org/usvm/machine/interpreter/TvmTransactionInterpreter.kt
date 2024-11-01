package org.usvm.machine.interpreter

import org.ton.bytecode.TvmCellValue
import org.usvm.UBoolExpr
import org.usvm.UHeapRef
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmStepScopeManager
import org.usvm.machine.state.ContractId
import org.usvm.machine.state.allocCellFromData
import org.usvm.machine.state.allocEmptyCell
import org.usvm.machine.state.allocSliceFromCell
import org.usvm.machine.state.builderStoreSlice
import org.usvm.machine.state.calcOnStateCtx
import org.usvm.machine.state.getSliceRemainingRefsCount
import org.usvm.machine.state.sliceMoveDataPtr
import org.usvm.machine.state.sliceMoveRefPtr
import org.usvm.machine.state.slicePreloadAddrLength
import org.usvm.machine.state.slicePreloadDataBits
import org.usvm.machine.state.slicePreloadExternalAddrLength
import org.usvm.machine.state.slicePreloadInternalAddrLength
import org.usvm.machine.state.slicePreloadNextRef
import org.usvm.mkSizeAddExpr
import org.usvm.sizeSort

class TvmTransactionInterpreter(val ctx: TvmContext) {

    fun executeActions(scope: TvmStepScopeManager, contractId: ContractId): List<OutMessage>? = with(ctx) {
        val commitedState = scope.calcOnState { lastCommitedStateOfContracts[contractId] }
        val commitedActions = scope.calcOnState { commitedState?.c5?.value?.value }
        if (commitedActions == null) {
            return@with null
        }

        val actions = extractActions(scope, commitedActions) ?: return null
        val outMessages = mutableListOf<OutMessage>()

        for (action in actions) {
            val tag = scope.slicePreloadDataBits(action, 32) ?: return null
            scope.doWithState { sliceMoveDataPtr(action, 32) }

            val isSendMsgAction = scope.checkSat(tag eq sendMsgActionTag)
            val isReserveAction = scope.checkSat(tag eq reserveActionTag)

            require(isSendMsgAction == null || isReserveAction == null) {
                "Symbolic actions are not supported"
            }

            when {
                isReserveAction != null -> visitReserveAction(scope, action) ?: return null
                isSendMsgAction != null -> {
                    val msg = visitSendMessageAction(scope, action) ?: return null
                    outMessages.add(msg)
                }
                else -> TODO()
            }
        }

        return outMessages
    }

    private fun extractActions(scope: TvmStepScopeManager, actions: UHeapRef): List<UHeapRef>? = with(ctx) {
        var cur = actions
        val actionList = mutableListOf<UHeapRef>()

        while (true) {
            val slice = scope.calcOnState { allocSliceFromCell(cur) }
            val remainingRefs = scope.calcOnState { getSliceRemainingRefsCount(slice) }

            val isEnd = scope.checkCondition(remainingRefs eq zeroSizeExpr) ?: return null
            if (isEnd) {
                // TODO check that `remainingBits` is also zero
                break
            }

            cur = scope.slicePreloadNextRef(slice) ?: return null
            scope.doWithState { sliceMoveRefPtr(slice) }
            actionList.add(slice)

            if (actionList.size > TvmContext.MAX_ACTIONS) {
                // TODO set error code
                return null
            }
        }

        return actionList.reversed()
    }

    private fun visitSendMessageAction(scope: TvmStepScopeManager, slice: UHeapRef): OutMessage? = with(ctx) {
        val msg = scope.slicePreloadNextRef(slice) ?: return null
        val msgSlice = scope.calcOnState { allocSliceFromCell(msg) }

        val dest = parseCommonMsgInfoRelaxed(scope, msgSlice) ?: return null
        val hasStateInit = parseStateInit(scope, msgSlice) ?: return null
        val body = parseBody(scope, msgSlice) ?: return null

        OutMessage(dest, body, hasStateInit)
    }

    private fun parseCommonMsgInfoRelaxed(scope: TvmStepScopeManager, msgSlice: UHeapRef): TvmCellValue? = with(ctx) {
        val tag = scope.slicePreloadDataBits(msgSlice, 1) ?: return null

        val isInternalCond = tag eq zeroBit
        val isInternal = scope.checkCondition(isInternalCond) ?: return null

        val dest = if (isInternal) {
            // int_msg_info$0 ihr_disabled:Bool bounce:Bool bounced:Bool
            scope.doWithState { sliceMoveDataPtr(msgSlice, 4) }

            // src:MsgAddress
            val srcAddrLength = scope.slicePreloadAddrLength(msgSlice) ?: return null
            scope.doWithState { sliceMoveDataPtr(msgSlice, srcAddrLength) }

            // dest:MsgAddressInt
            val destAddrLength = scope.slicePreloadInternalAddrLength(msgSlice) ?: return null
            val destAddr = scope.slicePreloadDataBits(msgSlice, destAddrLength) ?: return null
            scope.doWithState { sliceMoveDataPtr(msgSlice, destAddrLength) }

            // value:CurrencyCollection
            scope.skipGrams(msgSlice) ?: return null
            val extraCurrenciesBit = scope.slicePreloadDataBits(msgSlice, 1) ?: return null
            scope.doWithState { sliceMoveDataPtr(msgSlice, 1) }
            val extraCurrenciesEmptyConstraint = extraCurrenciesBit eq zeroBit
            val isExtraCurrenciesEmpty = scope.checkCondition(extraCurrenciesEmptyConstraint) ?: return null
            if (!isExtraCurrenciesEmpty) {
                scope.slicePreloadNextRef(msgSlice) ?: return null
                scope.doWithState { sliceMoveRefPtr(msgSlice) }
            }

            // ihr_fee:Grams fwd_fee:Grams created_lt:uint64 created_at:uint32
            scope.skipGrams(msgSlice) ?: return null
            scope.skipGrams(msgSlice) ?: return null
            scope.doWithState { sliceMoveDataPtr(msgSlice, 64 + 32) }

            scope.allocCellFromData(destAddr, destAddrLength) ?: return null
        } else {
            val externalTag = scope.slicePreloadDataBits(msgSlice, 2) ?: return null

            scope.fork(
                externalTag eq mkBv(value = 3, sizeBits = 2u),
                falseStateIsExceptional = true,
                // TODO set cell deserialization failure
                blockOnFalseState = throwStructuralCellUnderflowError
            ) ?: return null

            // ext_out_msg_info$11
            scope.doWithState { sliceMoveDataPtr(msgSlice, 2) }

            // src:MsgAddress
            val srcAddrLength = scope.slicePreloadAddrLength(msgSlice) ?: return null
            scope.doWithState { sliceMoveDataPtr(msgSlice, srcAddrLength) }

            // dest:MsgAddressExt
            val destAddrLength = scope.slicePreloadExternalAddrLength(msgSlice) ?: return null
            val destAddr = scope.slicePreloadDataBits(msgSlice, destAddrLength) ?: return null
            scope.doWithState { sliceMoveDataPtr(msgSlice, destAddrLength) }

            // created_lt:uint64 created_at:uint32
            scope.doWithState { sliceMoveDataPtr(msgSlice, 64 + 32) }

            scope.allocCellFromData(destAddr, destAddrLength) ?: return null
        }

        TvmCellValue(dest)
    }

    private fun parseStateInit(scope: TvmStepScopeManager, msgSlice: UHeapRef): Boolean? = with(ctx) {
        // init:(Maybe (Either StateInit ^StateInit))
        val initMaybeBit = scope.slicePreloadDataBits(msgSlice, 1) ?: return null
        val noStateInitConstraint = initMaybeBit eq zeroBit
        val noStateInit = scope.checkCondition(noStateInitConstraint) ?: return null
        scope.doWithState { sliceMoveDataPtr(msgSlice, 1) }

        if (noStateInit) {
            return false
        }

        val eitherBit = scope.slicePreloadDataBits(msgSlice, 1) ?: return null
        val isEitherRightCond = eitherBit eq oneBit
        val isEitherRight = scope.checkCondition(isEitherRightCond) ?: return null
        scope.doWithState { sliceMoveDataPtr(msgSlice, 1) }

        if (isEitherRight) {
            scope.slicePreloadNextRef(msgSlice) ?: return null
            scope.doWithState { sliceMoveRefPtr(msgSlice) }
            return true
        }

        // split_depth:(Maybe (## 5))
        val splitDepthMaybeBit = scope.slicePreloadDataBits(msgSlice, 1) ?: return null
        val splitDepthLen = mkIte(
            splitDepthMaybeBit eq oneBit,
            sixSizeExpr,
            oneSizeExpr
        )
        scope.doWithState { sliceMoveDataPtr(msgSlice, splitDepthLen) }

        // special:(Maybe TickTock)
        val specialMaybeBit = scope.slicePreloadDataBits(msgSlice, 1) ?: return null
        val specialLen = mkIte(
            specialMaybeBit eq oneBit,
            threeSizeExpr,
            oneSizeExpr
        )
        scope.doWithState { sliceMoveDataPtr(msgSlice, specialLen) }

        // code:(Maybe ^Cell) data:(Maybe ^Cell) library:(Maybe ^Cell)
        val codeMaybeBit = scope.slicePreloadDataBits(msgSlice, 1) ?: return null
        scope.doWithState { sliceMoveDataPtr(msgSlice, 1) }
        val dataMaybeBit = scope.slicePreloadDataBits(msgSlice, 1) ?: return null
        scope.doWithState { sliceMoveDataPtr(msgSlice, 1) }
        val libMaybeBit = scope.slicePreloadDataBits(msgSlice, 1) ?: return null
        scope.doWithState { sliceMoveDataPtr(msgSlice, 1) }

        val refsToSkip = mkSizeAddExpr(
            mkSizeAddExpr(
                codeMaybeBit.zeroExtendToSort(sizeSort),
                dataMaybeBit.zeroExtendToSort(sizeSort)
            ),
            libMaybeBit.zeroExtendToSort(sizeSort),
        )

        scope.doWithState { sliceMoveRefPtr(msgSlice, refsToSkip) }

        return true
    }

    private fun parseBody(scope: TvmStepScopeManager, msgSlice: UHeapRef): TvmCellValue? = with(ctx) {
        //  body:(Either X ^X)
        val bodyEitherBit = scope.slicePreloadDataBits(msgSlice, 1) ?: return null
        val isBodyLeft = scope.checkCondition(bodyEitherBit eq zeroBit) ?: return null
        scope.doWithState { sliceMoveDataPtr(msgSlice, 1) }

        val body = if (isBodyLeft) {
            val bodyBuilder = scope.calcOnState { allocEmptyCell() }
            scope.builderStoreSlice(bodyBuilder, msgSlice) ?: return null
            bodyBuilder
        } else {
            scope.slicePreloadNextRef(msgSlice) ?: return null
        }

        TvmCellValue(body)
    }

    private fun visitReserveAction(scope: TvmStepScopeManager, slice: UHeapRef): Unit? {
        // TODO no implementation, since we don't compute actions fees and balance

        return Unit
    }

    private fun TvmStepScopeManager.skipGrams(slice: UHeapRef) = calcOnStateCtx {
        val length = slicePreloadDataBits(slice, bits = 4)?.zeroExtendToSort(sizeSort)
            ?: return@calcOnStateCtx null

        // TODO: do we need it here?
        // makeSliceTypeLoad(slice, TvmSymbolicCellDataCoins(ctx, length))

        val extendedLength = mkBvShiftLeftExpr(length, shift = threeSizeExpr)
        val bitsToSkip = mkSizeAddExpr(fourSizeExpr, extendedLength)

        sliceMoveDataPtr(slice, bitsToSkip)
    }

    private fun TvmStepScopeManager.checkCondition(cond: UBoolExpr): Boolean? = with(ctx) {
        val checkRes = checkSat(cond)
        val invertedRes = checkSat(cond.not())

        require(checkRes == null || invertedRes == null) {
            error("Symbolic actions are not supported")
        }

        if (checkRes == null && invertedRes == null) {
            return null
        }

        checkRes != null
    }
}

data class OutMessage(
    val addr: TvmCellValue,
    val body: TvmCellValue,
    val hasStateInit: Boolean,
)