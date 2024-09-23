package org.usvm.machine.types

import org.ton.TvmAtomicDataCellLabel
import org.ton.TvmBuiltinDataCellLabel
import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmDataCellStructure
import org.usvm.UBoolExpr
import org.usvm.UExpr
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.state.TvmMethodResult
import org.usvm.machine.state.TvmState
import org.usvm.machine.types.dp.AbstractionForUExpr
import org.usvm.machine.types.dp.generateSwitchGuard
import org.usvm.mkSizeAddExpr
import org.usvm.mkSizeExpr
import org.usvm.mkSizeGtExpr
import org.usvm.mkSizeLtExpr
import org.usvm.mkSizeSubExpr

fun buildFrameForStructure(
    ctx: TvmContext,
    struct: TvmDataCellStructure,
    tlbLevel: Int,
): TlbStackFrame? =
    when (struct) {
        is TvmDataCellStructure.Unknown -> {
            check(tlbLevel == 0) {
                "`Unknown` is possible only on zero tlb level, but got tlb level $tlbLevel"
            }
            StackFrameOfUnknown
        }
        is TvmDataCellStructure.Empty -> {
            null
        }
        is TvmDataCellStructure.LoadRef -> {
            buildFrameForStructure(ctx, struct.selfRest, tlbLevel)
        }
        is TvmDataCellStructure.KnownTypePrefix -> {
            KnownTypeTlbStackFrame(struct, tlbLevel)
        }
        is TvmDataCellStructure.SwitchPrefix -> {
            SwitchTlbStackFrame(struct, ctx.zeroSizeExpr, tlbLevel)
        }
    }

sealed interface TlbStackFrame {
    val tlbLevel: Int
    fun step(state: TvmState, loadData: TvmDataCellLoadedTypeInfo.LoadData): Map<UBoolExpr, StackFrameStepResult>
    fun expandNewStackFrame(ctx: TvmContext): TlbStackFrame?
}

data class KnownTypeTlbStackFrame(
    val struct: TvmDataCellStructure.KnownTypePrefix,
    override val tlbLevel: Int,
) : TlbStackFrame {
    override fun step(
        state: TvmState,
        loadData: TvmDataCellLoadedTypeInfo.LoadData
    ): Map<UBoolExpr, StackFrameStepResult> = with(state.ctx) {
        if (struct.typeOfPrefix !is TvmBuiltinDataCellLabel) {
            return mapOf(trueExpr to StepError(error = null))
        }

        val accept = struct.typeOfPrefix.accepts(loadData.type)
        val nextFrame = buildFrameForStructure(state.ctx, struct.rest, tlbLevel)?.let {
            NextFrame(it)
        } ?: EndOfStackFrame
        mapOf(
            accept to nextFrame,
            accept.not() to StepError(TvmMethodResult.TvmStructuralError(TvmReadingOfUnexpectedType(struct.typeOfPrefix, loadData.type))),
        )
    }

    override fun expandNewStackFrame(ctx: TvmContext): TlbStackFrame? =
        when (struct.typeOfPrefix) {
            is TvmAtomicDataCellLabel -> null
            is TvmCompositeDataCellLabel ->
                buildFrameForStructure(ctx, struct.typeOfPrefix.internalStructure, tlbLevel + 1)
        }
}

data class SwitchTlbStackFrame(
    private val struct: TvmDataCellStructure.SwitchPrefix,
    private val offset: UExpr<TvmSizeSort>,
    override val tlbLevel: Int,
) : TlbStackFrame {
    override fun step(
        state: TvmState,
        loadData: TvmDataCellLoadedTypeInfo.LoadData
    ): Map<UBoolExpr, StackFrameStepResult> = with(state.ctx) {
        val switchSize = mkSizeExpr(struct.switchSize)
        val leftBits = mkSizeSubExpr(switchSize, offset)
        val newOffset = mkSizeAddExpr(offset, loadData.type.sizeBits)
        val result = hashMapOf(
            mkSizeLtExpr(loadData.type.sizeBits, leftBits) to NextFrame(SwitchTlbStackFrame(struct, newOffset, tlbLevel)),
            mkSizeGtExpr(loadData.type.sizeBits, leftBits) to StepError(
                TvmMethodResult.TvmStructuralError(TvmReadingOutOfSwitchBounds(loadData.type))
            ),
        )
        struct.variants.forEach { (key, variant) ->
            val guard = generateSwitchGuard(struct.switchSize, key).apply(
                AbstractionForUExpr(loadData.cellAddress, loadData.offset, state)
            )
            val stepResult = buildFrameForStructure(state.ctx, variant, tlbLevel)?.let {
                NextFrame(it)
            } ?: EndOfStackFrame
            result[(loadData.type.sizeBits eq leftBits) and guard] = stepResult
        }
        result
    }

    override fun expandNewStackFrame(ctx: TvmContext): TlbStackFrame? = null
}

data object StackFrameOfUnknown : TlbStackFrame {
    override val tlbLevel: Int = 0

    override fun step(
        state: TvmState,
        loadData: TvmDataCellLoadedTypeInfo.LoadData
    ): Map<UBoolExpr, StackFrameStepResult> =
        mapOf(state.ctx.trueExpr to NextFrame(this))

    override fun expandNewStackFrame(ctx: TvmContext): TlbStackFrame? = null
}

sealed interface StackFrameStepResult

data class StepError(val error: TvmMethodResult.TvmStructuralError?) : StackFrameStepResult

data class NextFrame(val frame: TlbStackFrame) : StackFrameStepResult

data object EndOfStackFrame : StackFrameStepResult
