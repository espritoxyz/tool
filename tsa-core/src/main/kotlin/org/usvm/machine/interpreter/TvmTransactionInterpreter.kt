package org.usvm.machine.interpreter

import org.ton.TvmContractHandlers
import org.ton.bytecode.ADDRESS_PARAMETER_IDX
import org.usvm.UBoolExpr
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.api.makeSymbolicPrimitive
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.Companion.OP_BITS
import org.usvm.machine.TvmContext.TvmInt257Sort
import org.usvm.machine.TvmStepScopeManager
import org.usvm.machine.state.ContractId
import org.usvm.machine.state.allocCellFromData
import org.usvm.machine.state.allocEmptyBuilder
import org.usvm.machine.state.allocSliceFromCell
import org.usvm.machine.state.builderStoreGramsTlb
import org.usvm.machine.state.builderStoreIntTlb
import org.usvm.machine.state.builderStoreSlice
import org.usvm.machine.state.builderStoreSliceTlb
import org.usvm.machine.state.builderToCell
import org.usvm.machine.state.getCellContractInfoParam
import org.usvm.machine.state.getSliceRemainingRefsCount
import org.usvm.machine.state.sliceLoadAddrTlb
import org.usvm.machine.state.sliceLoadGramsTlb
import org.usvm.machine.state.sliceLoadIntTlb
import org.usvm.machine.state.sliceLoadRefTlb
import org.usvm.machine.state.sliceMoveDataPtr
import org.usvm.machine.state.sliceMoveRefPtr
import org.usvm.machine.state.slicePreloadAddrLength
import org.usvm.machine.state.slicePreloadDataBits
import org.usvm.machine.state.slicePreloadExternalAddrLength
import org.usvm.machine.state.slicePreloadNextRef
import org.usvm.mkSizeAddExpr
import org.usvm.mkSizeExpr
import org.usvm.sizeSort

class TvmTransactionInterpreter(
    val ctx: TvmContext,
    private val communicationScheme: Map<ContractId, TvmContractHandlers> = mapOf(),
) {
    fun parseActionsToDestinations(scope: TvmStepScopeManager): List<Pair<ContractId, OutMessage>>? = with(ctx) {
        val actions = parseActions(scope)
            ?: return null

        if (actions.isEmpty()) {
            return emptyList()
        }

        val contractId = scope.calcOnState { currentContract }
        val handlers = communicationScheme[contractId]
            ?: error("Contract handlers are not found")

        val msgBody = scope.calcOnState { lastMsgBody }
            ?: error("Unexpected null msg_body")

        // TODO possible underflow
        val op = sliceLoadIntTlb(scope, msgBody, OP_BITS.toInt())?.second
            ?: return null

        val handler = handlers.handlers.firstOrNull { handler ->
            val handlerOp = mkBvHex(handler.op, OP_BITS).zeroExtendToSort(op.sort)

            // TODO maybe use model here ?
            scope.checkCondition(handlerOp eq op)
                ?: return null
        } ?: return null

        check(handler.destinations.size == actions.size) {
            "The number of actual messages is not equal to the number of destinations in the scheme: " +
                    "${actions.size} ${handler.destinations.size}"
        }

        handler.destinations.zip(actions)
    }

    fun parseActions(scope: TvmStepScopeManager): List<OutMessage>? = with(ctx) {
        val contractId = scope.calcOnState { currentContract }
        val commitedState = scope.calcOnState { lastCommitedStateOfContracts[contractId] }
        val commitedActions = scope.calcOnState { commitedState?.c5?.value?.value }
        if (commitedActions == null) {
            return@with null
        }

        val actions = extractActions(scope, commitedActions)
            ?: return null
        val outMessages = mutableListOf<OutMessage>()

        for (action in actions) {
            val (actionBody, tag) = sliceLoadIntTlb(scope, action, 32)
                ?: return null

            val isSendMsgAction = scope.checkSat(tag eq sendMsgActionTag.unsignedExtendToInteger())
            val isReserveAction = scope.checkSat(tag eq reserveActionTag.unsignedExtendToInteger())

            require(isSendMsgAction == null || isReserveAction == null) {
                "Symbolic actions are not supported"
            }

            when {
                isReserveAction != null -> visitReserveAction(scope, actionBody)
                    ?: return null
                isSendMsgAction != null -> {
                    val msg = visitSendMessageAction(scope, actionBody)
                        ?: return null
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

            val isEnd = scope.checkCondition(remainingRefs eq zeroSizeExpr)
                ?: return null
            if (isEnd) {
                // TODO check that `remainingBits` is also zero
                break
            }

            val action = sliceLoadRefTlb(scope, slice)?.let { cur = it.second; it.first }
                ?: return null
            actionList.add(action)

            if (actionList.size > TvmContext.MAX_ACTIONS) {
                // TODO set error code
                return null
            }
        }

        return actionList.reversed()
    }

    private fun visitSendMessageAction(scope: TvmStepScopeManager, slice: UHeapRef): OutMessage? = with(ctx) {
        val msg = scope.slicePreloadNextRef(slice)
            ?: return null
        val msgSlice = scope.calcOnState { allocSliceFromCell(msg) }

        val ptr = ParsingState(msgSlice)
        val (msgFull, msgValue) = parseCommonMsgInfoRelaxed(scope, ptr)
            ?: return null
        parseStateInit(scope, ptr)
            ?: return null
        val bodySlice = parseBody(scope, ptr)
            ?: return null

        OutMessage(msgValue, msgFull, bodySlice)
    }

    private fun parseCommonMsgInfoRelaxed(
        scope: TvmStepScopeManager,
        ptr: ParsingState
    ): Pair<UHeapRef, UExpr<TvmInt257Sort>>? = with(ctx) {
        val msgFull = scope.calcOnState { allocEmptyBuilder() }

        val tag = sliceLoadIntTlb(scope, ptr.slice, 1)?.second
            ?: return@with null

        val isInternalCond = tag eq zeroValue
        val isInternal = scope.checkCondition(isInternalCond)
            ?: return null

        if (isInternal) {
            // int_msg_info$0 ihr_disabled:Bool bounce:Bool bounced:Bool
            val flags = sliceLoadIntTlb(scope, ptr.slice, 4)?.unwrap(ptr)
                ?: return@with null

            builderStoreIntTlb(scope, msgFull, flags, mkSizeExpr(4))
                ?: return@with null

            // src:MsgAddress
            // TODO support std addresses
            val src = sliceLoadIntTlb(scope, ptr.slice, 2)?.unwrap(ptr)
                ?: return@with null

            val isSrcNone = scope.checkCondition(src eq zeroValue)
                ?: return@with null

            require(isSrcNone) {
                "Std source message address is not supported"
            }

            val addrCell = scope.getCellContractInfoParam(ADDRESS_PARAMETER_IDX)
                ?: return null
            val addrSlice = scope.calcOnState { allocSliceFromCell(addrCell) }
            builderStoreSliceTlb(scope, msgFull, addrSlice)
                ?: return null

            // dest:MsgAddressInt
            val destSlice = sliceLoadAddrTlb(scope, ptr.slice)?.unwrap(ptr)
                ?: return@with null
            builderStoreSliceTlb(scope, msgFull, destSlice)
                ?: return null

            // value:CurrencyCollection
            sliceLoadGramsTlb(scope, ptr.slice)?.unwrap(ptr)
                ?: return@with null

            // TODO send correct msg_value
            val symbolicMsgValue = scope.calcOnState { makeSymbolicPrimitive(int257sort) }
            builderStoreGramsTlb(scope, msgFull, symbolicMsgValue)
                ?: return null

            // TODO possible cell overflow
            builderStoreSliceTlb(scope, msgFull, ptr.slice)
                ?: return null

            val extraCurrenciesBit = sliceLoadIntTlb(scope, ptr.slice, 1)?.unwrap(ptr)
                ?: return@with null

            val extraCurrenciesEmptyConstraint = extraCurrenciesBit eq zeroValue
            val isExtraCurrenciesEmpty = scope.checkCondition(extraCurrenciesEmptyConstraint)
                ?: return null
            if (!isExtraCurrenciesEmpty) {
                sliceLoadRefTlb(scope, ptr.slice)?.unwrap(ptr)
                    ?: return@with null
            }

            // ihr_fee:Grams fwd_fee:Grams
            sliceLoadGramsTlb(scope, ptr.slice)?.unwrap(ptr)
                ?: return@with null
            sliceLoadGramsTlb(scope, ptr.slice)?.unwrap(ptr)
                ?: return@with null

            // created_lt:uint64 created_at:uint32
            sliceLoadIntTlb(scope, ptr.slice, 64)?.unwrap(ptr)
                ?: return@with null
            sliceLoadIntTlb(scope, ptr.slice, 32)?.unwrap(ptr)
                ?: return@with null

            return scope.builderToCell(msgFull) to symbolicMsgValue
        }

        TODO("External messages are not supported")

        scope.builderStoreSlice(msgFull, ptr.slice)
            ?: return null

        val externalTag = scope.slicePreloadDataBits(ptr.slice, 2)
            ?: return null

        scope.fork(
            externalTag eq mkBv(value = 3, sizeBits = 2u),
            falseStateIsExceptional = true,
            // TODO set cell deserialization failure
            blockOnFalseState = throwStructuralCellUnderflowError
        ) ?: return null

        // ext_out_msg_info$11
        scope.doWithState { sliceMoveDataPtr(ptr.slice, 2) }

        // src:MsgAddress
        val srcAddrLength = scope.slicePreloadAddrLength(ptr.slice)
            ?: return null
        scope.doWithState { sliceMoveDataPtr(ptr.slice, srcAddrLength) }

        // dest:MsgAddressExt
        val destAddrLength = scope.slicePreloadExternalAddrLength(ptr.slice)
            ?: return null
        val destAddr = scope.slicePreloadDataBits(ptr.slice, destAddrLength)
            ?: return null
        scope.doWithState { sliceMoveDataPtr(ptr.slice, destAddrLength) }

        // created_lt:uint64 created_at:uint32
        scope.doWithState { sliceMoveDataPtr(ptr.slice, 64 + 32) }

        scope.allocCellFromData(destAddr, destAddrLength)

        null
    }

    private fun parseStateInit(scope: TvmStepScopeManager, ptr: ParsingState): Unit? = with(ctx) {
        // init:(Maybe (Either StateInit ^StateInit))
        val initMaybeBit = sliceLoadIntTlb(scope, ptr.slice, 1)?.unwrap(ptr)
            ?: return null
        val noStateInitConstraint = initMaybeBit eq zeroValue
        val noStateInit = scope.checkCondition(noStateInitConstraint)
            ?: return null

        if (noStateInit) {
            return Unit
        }

        val eitherBit = sliceLoadIntTlb(scope, ptr.slice, 1)?.unwrap(ptr)
            ?: return null
        val isEitherRightCond = eitherBit eq oneValue
        val isEitherRight = scope.checkCondition(isEitherRightCond)
            ?: return null

        if (isEitherRight) {
            sliceLoadRefTlb(scope, ptr.slice)?.unwrap(ptr)
                ?: return null
            return Unit
        }

        TODO("Raw state_init is not supported")

        // split_depth:(Maybe (## 5))
        val splitDepthMaybeBit = scope.slicePreloadDataBits(ptr.slice, 1)
            ?: return null
        val splitDepthLen = mkIte(
            splitDepthMaybeBit eq oneBit,
            sixSizeExpr,
            oneSizeExpr
        )
        scope.doWithState { sliceMoveDataPtr(ptr.slice, splitDepthLen) }

        // special:(Maybe TickTock)
        val specialMaybeBit = scope.slicePreloadDataBits(ptr.slice, 1)
            ?: return null
        val specialLen = mkIte(
            specialMaybeBit eq oneBit,
            threeSizeExpr,
            oneSizeExpr
        )
        scope.doWithState { sliceMoveDataPtr(ptr.slice, specialLen) }

        // code:(Maybe ^Cell) data:(Maybe ^Cell) library:(Maybe ^Cell)
        val codeMaybeBit = scope.slicePreloadDataBits(ptr.slice, 1)
            ?: return null
        scope.doWithState { sliceMoveDataPtr(ptr.slice, 1) }
        val dataMaybeBit = scope.slicePreloadDataBits(ptr.slice, 1)
            ?: return null
        scope.doWithState { sliceMoveDataPtr(ptr.slice, 1) }
        val libMaybeBit = scope.slicePreloadDataBits(ptr.slice, 1)
            ?: return null
        scope.doWithState { sliceMoveDataPtr(ptr.slice, 1) }

        val refsToSkip = mkSizeAddExpr(
            mkSizeAddExpr(
                codeMaybeBit.zeroExtendToSort(sizeSort),
                dataMaybeBit.zeroExtendToSort(sizeSort)
            ),
            libMaybeBit.zeroExtendToSort(sizeSort),
        )

        scope.doWithState { sliceMoveRefPtr(ptr.slice, refsToSkip) }

        return Unit
    }

    private fun parseBody(scope: TvmStepScopeManager, ptr: ParsingState): UHeapRef? = with(ctx) {
        //  body:(Either X ^X)
        val bodyEitherBit = sliceLoadIntTlb(scope, ptr.slice, 1)?.unwrap(ptr)
            ?: return null
        val isBodyLeft = scope.checkCondition(bodyEitherBit eq zeroValue)
            ?: return null

        val body = if (isBodyLeft) {
            val bodyBuilder = scope.calcOnState { allocEmptyBuilder() }
            builderStoreSliceTlb(scope, bodyBuilder, ptr.slice)
                ?: return null
            scope.builderToCell(bodyBuilder)
        } else {
            sliceLoadRefTlb(scope, ptr.slice)?.unwrap(ptr)
                ?: return null
        }

        scope.calcOnState { allocSliceFromCell(body) }
    }

    private fun visitReserveAction(scope: TvmStepScopeManager, slice: UHeapRef): Unit? {
        // TODO no implementation, since we don't compute actions fees and balance

        return Unit
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

    private fun <T> Pair<UHeapRef, T>.unwrap(state: ParsingState): T {
        state.slice = first
        return second
    }

    private data class ParsingState(
        var slice: UHeapRef
    )
}

data class OutMessage(
    val msgValue: UExpr<TvmInt257Sort>,
    val fullMsgCell: UHeapRef,
    val msgBodySlice: UHeapRef,
)