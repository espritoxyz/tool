package org.usvm.machine.types

import kotlinx.collections.immutable.PersistentList
import org.ton.TlbAtomicLabel
import org.ton.TlbBuiltinLabel
import org.ton.TlbCompositeLabel
import org.ton.TlbStructure
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
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
    struct: TlbStructure,
    path: PersistentList<Int>,
): TlbStackFrame? {
    val tlbLevel = path.size
    return when (struct) {
        is TlbStructure.Unknown -> {
            check(tlbLevel == 0) {
                "`Unknown` is possible only on zero tlb level, but got tlb level $tlbLevel"
            }
            StackFrameOfUnknown
        }

        is TlbStructure.Empty -> {
            null
        }

        is TlbStructure.LoadRef -> {
            buildFrameForStructure(
                ctx,
                struct.rest,
                path,
            )
        }

        is TlbStructure.KnownTypePrefix -> {
            KnownTypeTlbStackFrame(struct, path)
        }

        is TlbStructure.SwitchPrefix -> {
            SwitchTlbStackFrame(struct, ctx.zeroSizeExpr, path)
        }
    }
}

sealed interface TlbStackFrame {
    val path: List<Int>
    fun step(state: TvmState, loadData: LimitedLoadData): Map<UBoolExpr, StackFrameStepResult>
    fun expandNewStackFrame(ctx: TvmContext): TlbStackFrame?
    val isSkippable: Boolean
    fun skipLabel(ctx: TvmContext): TlbStackFrame?
}

data class KnownTypeTlbStackFrame(
    val struct: TlbStructure.KnownTypePrefix,
    override val path: PersistentList<Int>,
) : TlbStackFrame {
    override fun step(
        state: TvmState,
        loadData: LimitedLoadData
    ): Map<UBoolExpr, StackFrameStepResult> = with(state.ctx) {
        if (struct.typeLabel !is TlbBuiltinLabel) {
            return mapOf(trueExpr to StepError(error = null))
        }

        val args = struct.typeArgs(state, loadData.cellAddress, path)

        val frameIsEmpty = struct.typeLabel.isEmptyLabel(this, args)

        val passPartOfLoadDataFurther = if (loadData.type is TvmCellDataBitArrayRead) {
            struct.typeLabel.passBitArrayRead(this, args, loadData.type.sizeBits)
        } else {
            null
        }

        val continueReadOnNextFrame = passPartOfLoadDataFurther?.let {
            passPartOfLoadDataFurther.guard or frameIsEmpty
        } ?: frameIsEmpty

        val accept = struct.typeLabel.accepts(state.ctx, args, loadData.type)
        val nextFrame = buildFrameForStructure(
            state.ctx,
            struct.rest,
            path,
        )?.let {
            NextFrame(it)
        } ?: EndOfStackFrame

        val error = TvmMethodResult.TvmStructuralError(
            TvmReadingOfUnexpectedType(
                struct.typeLabel,
                args,
                loadData.type,
            )
        )

        val result = hashMapOf(
            frameIsEmpty to PassLoadToNextFrame(loadData),
            (continueReadOnNextFrame.not() and accept) to nextFrame,
            (continueReadOnNextFrame.not() and accept.not()) to StepError(error),
        )

        if (passPartOfLoadDataFurther != null) {
            result[passPartOfLoadDataFurther.guard] = PassLoadToNextFrame(
                LimitedLoadData(
                    cellAddress = loadData.cellAddress,
                    type = TvmCellDataBitArrayRead(passPartOfLoadDataFurther.leftBits),
                    offset = mkSizeAddExpr(loadData.offset, mkSizeSubExpr(loadData.type.sizeBits, passPartOfLoadDataFurther.leftBits)),
                )
            )
        }

        result
    }

    override fun expandNewStackFrame(ctx: TvmContext): TlbStackFrame? =
        when (struct.typeLabel) {
            is TlbAtomicLabel -> {
                null
            }
            is TlbCompositeLabel -> {
                buildFrameForStructure(
                    ctx,
                    struct.typeLabel.internalStructure,
                    path.add(struct.id),
                )
            }
        }

    override val isSkippable: Boolean = true

    override fun skipLabel(ctx: TvmContext) = buildFrameForStructure(ctx, struct.rest, path)
}

data class SwitchTlbStackFrame(
    private val struct: TlbStructure.SwitchPrefix,
    private val offset: UExpr<TvmSizeSort>,
    override val path: PersistentList<Int>,
) : TlbStackFrame {
    override fun step(
        state: TvmState,
        loadData: LimitedLoadData
    ): Map<UBoolExpr, StackFrameStepResult> = with(state.ctx) {
        val switchSize = mkSizeExpr(struct.switchSize)
        val leftBits = mkSizeSubExpr(switchSize, offset)
        val newOffset = mkSizeAddExpr(offset, loadData.type.sizeBits)

        val passPartOfLoadDataFurther = if (loadData.type is TvmCellDataBitArrayRead && isSkippable) {
            mkSizeGtExpr(loadData.type.sizeBits, leftBits)
        } else {
            falseExpr
        }

        val result = hashMapOf(
            (mkSizeLtExpr(loadData.type.sizeBits, leftBits) and passPartOfLoadDataFurther.not()) to NextFrame(
                SwitchTlbStackFrame(struct, newOffset, path)
            ),
            (mkSizeGtExpr(loadData.type.sizeBits, leftBits) and passPartOfLoadDataFurther.not()) to StepError(
                TvmMethodResult.TvmStructuralError(TvmReadingOutOfSwitchBounds(loadData.type))
            ),
            passPartOfLoadDataFurther to PassLoadToNextFrame(
                LimitedLoadData(
                    loadData.cellAddress,
                    TvmCellDataBitArrayRead(mkSizeSubExpr(loadData.type.sizeBits, leftBits)),
                    mkSizeAddExpr(loadData.offset, leftBits),
                )
            )
        )
        struct.variants.forEach { (key, variant) ->
            val guard = generateSwitchGuard(struct.switchSize, key).apply(
                AbstractionForUExpr(loadData.cellAddress, mkSizeSubExpr(loadData.offset, offset), path, state)
            )
            val stepResult = buildFrameForStructure(
                state.ctx,
                variant,
                path,
            )?.let {
                NextFrame(it)
            } ?: EndOfStackFrame
            result[(loadData.type.sizeBits eq leftBits) and guard and passPartOfLoadDataFurther.not()] = stepResult
        }

        result
    }

    override fun expandNewStackFrame(ctx: TvmContext): TlbStackFrame? = null

    override val isSkippable: Boolean
        get() = struct.variants.size == 1

    override fun skipLabel(ctx: TvmContext): TlbStackFrame? {
        check(isSkippable) {
            "Cannot skip switch with several keys: ${struct.variants.keys}"
        }
        val variant = struct.variants.values.single()
        return buildFrameForStructure(ctx, variant, path)
    }
}

data object StackFrameOfUnknown : TlbStackFrame {
    override val path = emptyList<Int>()

    override fun step(
        state: TvmState,
        loadData: LimitedLoadData
    ): Map<UBoolExpr, StackFrameStepResult> =
        mapOf(state.ctx.trueExpr to NextFrame(this))

    override fun expandNewStackFrame(ctx: TvmContext): TlbStackFrame? = null

    override val isSkippable: Boolean = false

    override fun skipLabel(ctx: TvmContext): TlbStackFrame? = null
}

sealed interface StackFrameStepResult

data class StepError(val error: TvmMethodResult.TvmStructuralError?) : StackFrameStepResult

data class NextFrame(val frame: TlbStackFrame) : StackFrameStepResult

data object EndOfStackFrame : StackFrameStepResult

data class PassLoadToNextFrame(
    val loadData: LimitedLoadData,
) : StackFrameStepResult

data class LimitedLoadData(
    val cellAddress: UConcreteHeapRef,
    val type: TvmCellDataTypeRead,
    val offset: UExpr<TvmSizeSort>,
) {
    companion object {
        fun fromLoadData(loadData: TvmDataCellLoadedTypeInfo.LoadData) = LimitedLoadData(
            type = loadData.type,
            cellAddress = loadData.cellAddress,
            offset = loadData.offset,
        )
    }
}
