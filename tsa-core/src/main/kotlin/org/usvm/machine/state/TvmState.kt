package org.usvm.machine.state

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.persistentListOf
import org.ton.bytecode.TvmCodeBlock
import org.ton.bytecode.TvmContinuationValue
import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmReferenceType
import org.ton.bytecode.TvmType
import org.ton.targets.TvmTarget
import org.usvm.PathNode
import org.usvm.UBv32Sort
import org.usvm.UCallStack
import org.usvm.UConcreteHeapAddress
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.UState
import org.usvm.constraints.UPathConstraints
import org.usvm.isStaticHeapRef
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
    val emptyRefValue: TvmRefEmptyValue,
    private var symbolicRefs: PersistentSet<UConcreteHeapAddress> = persistentHashSetOf(),
    var gasUsage: PersistentList<UExpr<UBv32Sort>>,
    // TODO codepage
    callStack: UCallStack<TvmCodeBlock, TvmInst> = UCallStack(),
    pathConstraints: UPathConstraints<TvmType>,
    memory: UMemory<TvmType, TvmCodeBlock>,
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
            emptyRefValue,
            symbolicRefs,
            gasUsage,
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

    fun generateSymbolicRef(referenceType: TvmReferenceType): UConcreteHeapRef =
        memory.allocStatic(referenceType).also { symbolicRefs = symbolicRefs.add(it.address) }

    fun ensureSymbolicRefInitialized(
        ref: UHeapRef,
        referenceType: TvmReferenceType,
        initializer: TvmState.(UConcreteHeapRef) -> Unit = {}
    ) {
        check(isStaticHeapRef(ref)) { "Symbolic ref expected, but $ref received" }

        val refs = symbolicRefs.add(ref.address)
        if (refs === symbolicRefs) return

        memory.types.allocate(ref.address, referenceType)
        initializer(ref)
    }
}
