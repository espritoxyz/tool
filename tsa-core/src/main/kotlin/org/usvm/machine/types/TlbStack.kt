package org.usvm.machine.types

import kotlinx.collections.immutable.persistentListOf
import org.ton.TlbAtomicLabel
import org.ton.TlbCompositeLabel
import org.ton.TlbLabel
import org.ton.createWrapperStructure
import org.usvm.UBoolExpr
import org.usvm.isFalse
import org.usvm.machine.TvmContext
import org.usvm.machine.state.TvmMethodResult
import org.usvm.machine.state.TvmState

data class TlbStack(
    private val frames: List<TlbStackFrame>,
    private val deepestError: TvmMethodResult.TvmStructuralError? = null,
) {
    fun step(
        state: TvmState,
        loadData: LimitedLoadData,
    ): Map<UBoolExpr, StepResult> = with(state.ctx) {

        val ctx = state.ctx
        val result = hashMapOf<UBoolExpr, StepResult>()

        val emptyRead = ctx.mkEq(loadData.type.sizeBits, ctx.zeroSizeExpr)

        if (frames.isEmpty()) {
            // finished parsing
            return mapOf(
                emptyRead to NewStack(this@TlbStack),
                ctx.mkNot(emptyRead) to Error(TvmMethodResult.TvmStructuralError(TvmUnexpectedDataReading(loadData.type)))
            )
        }

        result[emptyRead] = NewStack(this@TlbStack)

        val lastFrame = frames.last()

        lastFrame.step(state, loadData).forEach { (guard, stackFrameStepResult) ->
            if (guard.isFalse) {
                return@forEach
            }

            when (stackFrameStepResult) {
                is EndOfStackFrame -> {
                    val newFrames = popFrames(ctx, frames.subList(0, frames.size - 1))
                    result[guard and emptyRead.not()] = NewStack(TlbStack(newFrames, deepestError))
                }

                is NextFrame -> {
                    val newStack = TlbStack(
                        frames.subList(0, frames.size - 1) + stackFrameStepResult.frame,
                        deepestError,
                    )
                    result[guard and emptyRead.not()] = NewStack(newStack)
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
                            result[newGuard and emptyRead.not()] = stepResult
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

                        result[guard and emptyRead.not()] = Error(error)
                    }
                }

                is PassLoadToNextFrame -> {
                    val newLoadData = stackFrameStepResult.loadData
                    val newFrames = popFrames(ctx, frames)
                    val newStack = TlbStack(newFrames, deepestError)
                    newStack.step(state, newLoadData).forEach { (innerGuard, stepResult) ->
                        val newGuard = ctx.mkAnd(guard, innerGuard)
                        result[newGuard and emptyRead.not()] = stepResult
                    }
                }
            }
        }

        result.remove(falseExpr)

        return result
    }

    private fun popFrames(ctx: TvmContext, framesToPop: List<TlbStackFrame>): List<TlbStackFrame> {
        if (framesToPop.isEmpty()) {
            return framesToPop
        }
        val prevFrame = framesToPop.last()
        check(prevFrame.isSkippable) {
            "$prevFrame must be skippable, but it is not"
        }
        val newFrame = prevFrame.skipLabel(ctx)
        return if (newFrame == null) {
            popFrames(ctx, framesToPop.subList(0, framesToPop.size - 1))
        } else {
            framesToPop.subList(0, framesToPop.size - 1) + newFrame
        }
    }

    sealed interface StepResult

    data class Error(val error: TvmMethodResult.TvmStructuralError) : StepResult

    data class NewStack(val stack: TlbStack) : StepResult

    companion object {
        fun new(ctx: TvmContext, label: TlbLabel): TlbStack {
            val struct = when (label) {
                is TlbCompositeLabel -> label.internalStructure
                is TlbAtomicLabel -> createWrapperStructure(label)
            }
            val frame = buildFrameForStructure(ctx, struct, persistentListOf())
            val frames = frame?.let { listOf(it) } ?: emptyList()
            return TlbStack(frames)
        }
    }
}
