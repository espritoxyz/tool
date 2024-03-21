package org.usvm.machine.state

import kotlinx.collections.immutable.persistentListOf
import org.ton.bytecode.TvmCodeBlock
import org.ton.bytecode.TvmContinuationValue
import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmType
import org.ton.targets.TvmTarget
import org.usvm.PathNode
import org.usvm.UCallStack
import org.usvm.UState
import org.usvm.constraints.UPathConstraints
import org.usvm.machine.TvmContext
import org.usvm.memory.UMemory
import org.usvm.model.UModelBase
import org.usvm.targets.UTargetsSet

class TvmState(
    ctx: TvmContext,
    override val entrypoint: TvmCodeBlock,
//    val registers: TvmRegisters, // TODO do we really need keep the registers this way?
    var currentContinuation: TvmContinuationValue,
    var stack: TvmStack = TvmStack(ctx, persistentListOf()),
    var registers: TvmRegisters,
    // TODO codepage and gas
    callStack: UCallStack<TvmCodeBlock, TvmInst> = UCallStack(),
    pathConstraints: UPathConstraints<TvmType> = UPathConstraints(ctx),
    memory: UMemory<TvmType, TvmCodeBlock> = UMemory(ctx, pathConstraints.typeConstraints),
    models: List<UModelBase<TvmType>> = listOf(),
    pathNode: PathNode<TvmInst> = PathNode.root(),
    var methodResult: TvmMethodResult = TvmMethodResult.NoCall,
    targets: UTargetsSet<TvmTarget, TvmInst> = UTargetsSet.empty(),
) : UState<TvmType, TvmCodeBlock, TvmInst, TvmContext, TvmTarget, TvmState>(
    ctx,
    callStack,
    pathConstraints,
    memory,
    models,
    pathNode,
    targets
) {
    override val isExceptional: Boolean
        get() = methodResult is TvmMethodResult.TvmFailure

    override fun clone(newConstraints: UPathConstraints<TvmType>?): TvmState {
        val clonedConstraints = newConstraints ?: pathConstraints.clone()

        return TvmState(
            ctx,
            entrypoint,
//            registers, // TODO clone?
            currentContinuation, // TODO clone?
            stack.clone(), // TODO clone?
            registers.copy(),
            callStack.clone(),
            clonedConstraints,
            memory.clone(clonedConstraints.typeConstraints),
            models,
            pathNode,
            methodResult,
            targets.clone()
        )
    }

    override fun toString(): String = buildString {
        appendLine("Instruction: $lastStmt")
        if (isExceptional) appendLine("Exception: $methodResult")
        appendLine(callStack)
    }
}
