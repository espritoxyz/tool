package org.usvm.machine

import org.ton.bytecode.TvmInst
import org.usvm.ForkCase
import org.usvm.StepResult
import org.usvm.UBoolExpr
import org.usvm.forkblacklists.UForkBlackList
import org.usvm.isFalse
import org.usvm.isTrue
import org.usvm.machine.TvmStepScope.StepScopeState.CANNOT_BE_PROCESSED
import org.usvm.machine.TvmStepScope.StepScopeState.CAN_BE_PROCESSED
import org.usvm.machine.TvmStepScope.StepScopeState.DEAD
import org.usvm.machine.state.TvmState
import org.usvm.machine.types.TvmType
import org.usvm.solver.USatResult
import org.usvm.solver.UUnknownResult
import org.usvm.solver.UUnsatResult
import org.usvm.uctx

/**
 * Modified version of usvm [StepScope] that distinguishes between UNSAT and UNKNOWN solver results at [assert].
 * UNSAT is an unexpected result, while UNKNOWN is not.
 *
 * An auxiliary class, which carefully maintains forks and asserts via [forkWithBlackList] and [assert].
 * It should be created on every step in an interpreter.
 * You can think about an instance of [TvmStepScope] as a monad `ExceptT null TvmState`.
 *
 * This scope is considered as [DEAD], iff the condition in [assert] was unsatisfiable or unknown.
 * The underlying state cannot be processed further (see [CANNOT_BE_PROCESSED]),
 * if the first passed to [forkWithBlackList] or [forkMultiWithBlackList] condition was unsatisfiable or unknown.
 *
 * To execute some function on a state, you should use [doWithState] or [calcOnState]. `null` is returned, when
 * this scope cannot be processed on the current step - see [CANNOT_BE_PROCESSED].
 *
 * @param originalState an initial state.
 */
class TvmStepScope(
    private val originalState: TvmState,
    private val forkBlackList: UForkBlackList<TvmState, TvmInst>
) {
    private val forkedStates = mutableListOf<TvmState>()

    private inline val alive: Boolean get() = stepScopeState != DEAD
    private inline val canProcessFurtherOnCurrentStep: Boolean get() = stepScopeState == CAN_BE_PROCESSED
    private inline val ctx: TvmContext get() = originalState.ctx

    /**
     * Determines whether we interact this scope on the current step.
     * @see [StepScopeState].
     */
    private var stepScopeState: StepScopeState = CAN_BE_PROCESSED

    /**
     * @return forked states and the status of initial state.
     */
    fun stepResult() = StepResult(forkedStates.asSequence(), alive)

    val isDead: Boolean get() = stepScopeState === DEAD

    /**
     * Executes [block] on a state.
     *
     * @return `null` if the underlying state is `null`.
     */
    fun doWithState(block: TvmState.() -> Unit) {
        check(canProcessFurtherOnCurrentStep) { "Caller should check before processing the current hop further" }
        return originalState.block()
    }

    /**
     * Executes [block] on a state.
     *
     * @return `null` if the underlying state is `null`, otherwise returns result of calling [block].
     */
    fun <R> calcOnState(block: TvmState.() -> R): R {
        check(canProcessFurtherOnCurrentStep) { "Caller should check before processing the current hop further" }
        return originalState.block()
    }

    /**
     * Forks on a [condition], performing [blockOnTrueState] on a state satisfying [condition] and
     * [blockOnFalseState] on a state satisfying [condition].not().
     *
     * If the [condition] is unsatisfiable or unknown, sets the scope state to the [CANNOT_BE_PROCESSED].
     *
     * @return `null` if the [condition] is unsatisfiable or unknown.
     */
    fun fork(
        condition: UBoolExpr,
        blockOnTrueState: TvmState.() -> Unit = {},
        blockOnFalseState: TvmState.() -> Unit = {},
    ): Unit? {
        check(canProcessFurtherOnCurrentStep)

        val possibleForkPoint = originalState.pathNode

        val (posState, negState) = ctx.statesForkProvider.fork(originalState, condition)

        posState?.blockOnTrueState()

        if (posState == null) {
            stepScopeState = CANNOT_BE_PROCESSED
            check(negState === originalState)
        } else {
            check(posState === originalState)
        }

        if (negState != null) {
            negState.blockOnFalseState()
            if (negState !== originalState) {
                forkedStates += negState

                originalState.forkPoints += possibleForkPoint
                negState.forkPoints += possibleForkPoint
            }
        }

        // conversion of ExecutionState? to Unit?
        return posState?.let { }
    }

    // TODO: I don't like this implementation, but that was the fastest way to do it without patching usvm
    fun forkWithCheckerStatusKnowledge(
        condition: UBoolExpr,
        blockOnUnknownTrueState: TvmState.() -> Unit = {},
        blockOnUnsatTrueState: TvmState.() -> Unit = {},
        blockOnFalseState: TvmState.() -> Unit = {},
    ): Unit? {
        val clonedState = originalState.clone()
        val clonedStepScope = TvmStepScope(clonedState, forkBlackList)

        val result = assert(condition, unsatBlock = blockOnUnsatTrueState, unknownBlock = blockOnUnknownTrueState)

        clonedStepScope.assert(ctx.mkNot(condition))
        if (clonedStepScope.alive) {
            clonedStepScope.originalState.blockOnFalseState()
            forkedStates += clonedStepScope.originalState
        }

        return result
    }

    /**
     * Forks on a few disjoint conditions using `forkMulti` in `State.kt`
     * and executes the corresponding block on each not-null state.
     *
     * NOTE: always sets the [stepScopeState] to the [CANNOT_BE_PROCESSED] value.
     */
    fun forkMulti(conditionsWithBlockOnStates: List<Pair<UBoolExpr, TvmState.() -> Unit>>) =
        forkMulti(conditionsWithBlockOnStates, skipForkPointIfPossible = true)

    /**
     * @param skipForkPointIfPossible determines whether it is allowed to skip fork point registration.
     * */
    private fun forkMulti(
        conditionsWithBlockOnStates: List<Pair<UBoolExpr, TvmState.() -> Unit>>,
        skipForkPointIfPossible: Boolean
    ) {
        check(canProcessFurtherOnCurrentStep)

        val possibleForkPoint = originalState.pathNode

        val conditions = conditionsWithBlockOnStates.map { it.first }

        val conditionStates = ctx.statesForkProvider.forkMulti(originalState, conditions)

        val forkedStates = conditionStates.mapIndexedNotNull { idx, positiveState ->
            val block = conditionsWithBlockOnStates[idx].second

            positiveState?.apply(block)
        }

        stepScopeState = CANNOT_BE_PROCESSED
        if (forkedStates.isEmpty()) {
            stepScopeState = DEAD
            return
        }

        val firstForkedState = forkedStates.first()
        require(firstForkedState == originalState) {
            "The original state $originalState was expected to become the first of forked states but $firstForkedState found"
        }

        // Interpret the first state as original and others as forked
        this.forkedStates += forkedStates.subList(1, forkedStates.size)

        if (skipForkPointIfPossible && forkedStates.size < 2) return

        forkedStates.forEach { it.forkPoints += possibleForkPoint }
    }

    fun assert(
        constraint: UBoolExpr,
        satBlock: TvmState.() -> Unit = {},
        unsatBlock: TvmState.() -> Unit = {},
        unknownBlock: TvmState.() -> Unit = {},
    ): Unit? = assert(
        constraint,
        registerForkPoint = false,
        satBlock,
        unsatBlock,
        unknownBlock
    )

    /**
     * @param registerForkPoint register a fork point if assert was successful.
     * */
    @Suppress("MoveVariableDeclarationIntoWhen")
    private fun assert(
        constraint: UBoolExpr,
        registerForkPoint: Boolean,
        satBlock: TvmState.() -> Unit = {},
        unsatBlock: TvmState.() -> Unit = {},
        unknownBlock: TvmState.() -> Unit = {},
    ): Unit? {
        check(canProcessFurtherOnCurrentStep)

        val possibleForkPoint = originalState.pathNode

        val trueModels = originalState.models.filter { it.eval(constraint).isTrue }

        if (trueModels.isNotEmpty()) {
            originalState.models = trueModels
        } else {
            val constraints = originalState.pathConstraints.clone()
            constraints += constraint

            val solver = originalState.ctx.solver<TvmType>()
            val solverResult = solver.check(constraints)

            when (solverResult) {
                is USatResult -> {
                    originalState.models = listOf(solverResult.model)
                }

                is UUnsatResult -> {
                    originalState.unsatBlock()
                    stepScopeState = DEAD
                    return null
                }

                is UUnknownResult -> {
                    originalState.pathConstraints += constraint
                    originalState.unknownBlock()
                    stepScopeState = DEAD
                    return null
                }
            }
        }

        originalState.pathConstraints += constraint

        if (registerForkPoint) {
            originalState.forkPoints += possibleForkPoint
        }

        originalState.satBlock()

        return Unit
    }

    /**
     * [forkWithBlackList] version which doesn't fork to the branches with statements
     * banned by underlying [forkBlackList].
     *
     * @param trueStmt statement to fork on [condition].
     * @param falseStmt statement to fork on ![condition].
     */
    fun forkWithBlackList(
        condition: UBoolExpr,
        trueStmt: TvmInst,
        falseStmt: TvmInst,
        blockOnTrueState: TvmState.() -> Unit = {},
        blockOnFalseState: TvmState.() -> Unit = {},
    ): Unit? {
        check(canProcessFurtherOnCurrentStep)

        val shouldForkOnTrue = forkBlackList.shouldForkTo(originalState, trueStmt)
        val shouldForkOnFalse = forkBlackList.shouldForkTo(originalState, falseStmt)

        if (!shouldForkOnTrue && !shouldForkOnFalse) {
            stepScopeState = DEAD
            // TODO: should it be null?
            return null
        }

        if (shouldForkOnTrue && shouldForkOnFalse) {
            return fork(condition, blockOnTrueState, blockOnFalseState)
        }

        // If condition is concrete there is no fork point possibility
        val registerForkPoint = !condition.isConcrete

        if (shouldForkOnTrue) {
            return assert(condition, registerForkPoint, satBlock = blockOnTrueState)
        }

        return assert(condition.uctx.mkNot(condition), registerForkPoint, satBlock = blockOnFalseState)
    }

    /**
     * [forkMultiWithBlackList] version which doesn't fork to the branches with statements
     * banned by underlying [forkBlackList].
     */
    fun forkMultiWithBlackList(forkCases: List<ForkCase<TvmState, TvmInst>>) {
        check(canProcessFurtherOnCurrentStep)

        val filteredConditionsWithBlockOnStates = forkCases
            .mapNotNull { case ->
                if (!forkBlackList.shouldForkTo(originalState, case.stmt)) {
                    return@mapNotNull null
                }
                case.condition to case.block
            }

        if (filteredConditionsWithBlockOnStates.isEmpty()) {
            stepScopeState = DEAD
            return
        }

        // If all conditions are concrete there is no fork point possibility
        val skipForkPoint = forkCases.all { it.condition.isConcrete }
        return forkMulti(filteredConditionsWithBlockOnStates, skipForkPoint)
    }

    private val UBoolExpr.isConcrete get() = isTrue || isFalse

    /**
     * [assert]s the [condition] on the scope with the cloned [originalState]. Returns this cloned state,
     * if this [condition] is satisfiable, and returns `null` otherwise.
     */
    @Suppress("MoveVariableDeclarationIntoWhen")
    fun checkSat(condition: UBoolExpr): TvmState? {
        val conditionalState = originalState.clone()
        conditionalState.pathConstraints += condition

        // If this state did not fork at all or was sat at the last fork point, it must be still sat, so we can just
        // check this condition with presented models
        if (conditionalState.models.isNotEmpty()) {
            val trueModels = conditionalState.models.filter { it.eval(condition).isTrue }

            if (trueModels.isNotEmpty()) {
                return conditionalState
            }
        }

        val solver = conditionalState.ctx.solver<TvmType>()
        val solverResult = solver.check(conditionalState.pathConstraints)

        return when (solverResult) {
            is USatResult -> {
                conditionalState.models += solverResult.model

                // If state with the added condition is satisfiable, it means that the original state is satisfiable too,
                // and we can save a model from the solver
                originalState.models += solverResult.model

                conditionalState
            }
            is UUnknownResult, is UUnsatResult -> null
        }
    }

    /**
     * Represents the current state of this [TvmStepScope].
     */
    private enum class StepScopeState {
        /**
         * Cannot be processed further with any actions.
         */
        DEAD,
        /**
         * Cannot be forked or asserted using [forkWithBlackList], [forkMultiWithBlackList] or [assert],
         * but is considered as alive from the Machine's point of view.
         */
        CANNOT_BE_PROCESSED,
        /**
         * Can be forked using [forkWithBlackList] or [forkMultiWithBlackList] and asserted using [assert].
         */
        CAN_BE_PROCESSED;
    }
}
