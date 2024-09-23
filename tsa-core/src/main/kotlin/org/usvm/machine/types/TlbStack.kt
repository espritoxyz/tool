package org.usvm.machine.types

import org.ton.TvmAtomicDataCellLabel
import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmDataCellLabel
import org.ton.TvmDataCellStructure
import org.usvm.UBoolExpr
import org.usvm.machine.TvmContext
import org.usvm.machine.state.TvmMethodResult
import org.usvm.machine.state.TvmState

data class TlbStack(
    private val frames: List<TlbStackFrame>,
    private val deepestError: TvmMethodResult.TvmStructuralError? = null,
) {
    fun step(
        state: TvmState,
        loadData: TvmDataCellLoadedTypeInfo.LoadData,
    ): Map<UBoolExpr, StepResult> {

        val ctx = state.ctx
        val result = hashMapOf<UBoolExpr, StepResult>()

        if (frames.isEmpty()) {
            // finished parsing
            val emptyRead = ctx.mkEq(loadData.type.sizeBits, ctx.zeroSizeExpr)
            return mapOf(
                emptyRead to NewStack(this),
                ctx.mkNot(emptyRead) to Error(TvmMethodResult.TvmStructuralError(TvmUnexpectedDataReading(loadData.type)))
            )
        }

        val lastFrame = frames.last()

        lastFrame.step(state, loadData).forEach { (guard, stackFrameStepResult) ->
            when (stackFrameStepResult) {
                is EndOfStackFrame -> {
                    val newStack = TlbStack(frames.subList(0, frames.size - 1))
                    result[guard] = NewStack(newStack)
                }

                is NextFrame -> {
                    val newStack = TlbStack(frames.subList(0, frames.size - 1) + stackFrameStepResult.frame)
                    result[guard] = NewStack(newStack)
                }

                is StepError -> {
                    val nextLevelFrame = lastFrame.expandNewStackFrame(ctx)
                    if (nextLevelFrame != null) {
                        val newDeepestError = deepestError ?: stackFrameStepResult.error
                        val newStack = TlbStack(
                            frames + nextLevelFrame,
                            newDeepestError,
                        )
                        newStack.step(state, loadData).forEach { (innerGuard, stepResult) ->
                            val newGuard = ctx.mkAnd(guard, innerGuard)
                            result[newGuard] = stepResult
                        }
                    } else {

                        // condition [nextLevelFrame == null] means that we were about to parse TvmAtomicDataLabel

                        // condition [stackFrameStepResult.error == null] means that this TvmAtomicDataLabel
                        // was not builtin (situation A).

                        // condition [deepestError != null] means that there was an unsuccessful attempt to
                        // parse TvmCompositeDataLabel on a previous level (situation B).

                        val error = deepestError ?: stackFrameStepResult.error

                        // If A happened, B must have happened => [error] must be non-null
                        check(error != null) {
                            "Error was not set after unsuccessful TlbStack step."
                        }

                        result[guard] = Error(error)
                    }
                }
            }
        }

        return result
    }

    sealed interface StepResult

    data class Error(val error: TvmMethodResult.TvmStructuralError) : StepResult

    data class NewStack(val stack: TlbStack) : StepResult

    companion object {
        fun new(ctx: TvmContext, label: TvmDataCellLabel): TlbStack {
            val struct = when (label) {
                is TvmCompositeDataCellLabel -> label.internalStructure
                is TvmAtomicDataCellLabel -> TvmDataCellStructure.KnownTypePrefix(label, TvmDataCellStructure.Empty)
            }
            val frames = buildFrameForStructure(ctx, struct, tlbLevel = 0)?.let { listOf(it) } ?: emptyList()
            return TlbStack(frames)
        }
    }
}
