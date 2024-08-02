package org.usvm.machine

import mu.KLogging
import org.ton.TvmInputInfo
import org.ton.bytecode.TvmCodeBlock
import org.ton.bytecode.TvmContractCode
import org.ton.bytecode.TvmInst
import org.ton.cell.Cell
import org.usvm.PathSelectionStrategy
import org.usvm.StateCollectionStrategy
import org.usvm.UMachine
import org.usvm.UMachineOptions
import org.usvm.machine.interpreter.TvmInterpreter
import org.usvm.machine.state.TvmMethodResult
import org.usvm.machine.state.TvmState
import org.usvm.ps.createPathSelector
import org.usvm.statistics.ApplicationGraph
import org.usvm.statistics.CompositeUMachineObserver
import org.usvm.statistics.StepsStatistics
import org.usvm.statistics.collectors.AllStatesCollector
import org.usvm.stopstrategies.StepLimitStopStrategy
import org.usvm.stopstrategies.StopStrategy

class TvmMachine(
    private val options: UMachineOptions = defaultOptions,
    private val tvmOptions: TvmOptions = TvmOptions(),
) : UMachine<TvmState>() {
    override fun close() {
        // Do nothing
    }

    private val components = TvmComponents()
    private val ctx = TvmContext(tvmOptions, components)

    fun analyze(
        contractCode: TvmContractCode,
        contractData: Cell,
        methodId: MethodId,
        inputInfo: TvmInputInfo = TvmInputInfo(),
        coverageStatistics: TvmCoverageStatistics,
    ): List<TvmState> {
        val interpreter = TvmInterpreter(
            ctx,
            contractCode,
            typeSystem = components.typeSystem,
            inputInfo = inputInfo,
            checkDataCellContentTypes = tvmOptions.checkDataCellContentTypes,
            excludeInputsThatDoNotMatchGivenScheme = tvmOptions.excludeInputsThatDoNotMatchGivenScheme,
        )
        logger.debug("{}.analyze({})", this, contractCode)
        val initialState = interpreter.getInitialState(contractCode, contractData, methodId)

        val loopTracker = TvmLoopTracker()
        val pathSelector = createPathSelector(
            initialState = initialState,
            options = options,
            loopStatisticFactory = { loopTracker },
            applicationGraph = object : ApplicationGraph<TvmCodeBlock, TvmInst> {
                override fun callees(node: TvmInst): Sequence<TvmCodeBlock> {
                    TODO("Not yet implemented")
                }

                override fun callers(method: TvmCodeBlock): Sequence<TvmInst> {
                    TODO("Not yet implemented")
                }

                override fun entryPoints(method: TvmCodeBlock): Sequence<TvmInst> {
                    TODO("Not yet implemented")
                }

                override fun exitPoints(method: TvmCodeBlock): Sequence<TvmInst> {
                    TODO("Not yet implemented")
                }

                override fun methodOf(node: TvmInst): TvmCodeBlock {
                    TODO("Not yet implemented")
                }

                override fun predecessors(node: TvmInst): Sequence<TvmInst> {
                    TODO("Not yet implemented")
                }

                override fun statementsOf(method: TvmCodeBlock): Sequence<TvmInst> {
                    TODO("Not yet implemented")
                }

                override fun successors(node: TvmInst): Sequence<TvmInst> {
                    TODO("Not yet implemented")
                }
            }
        )

        val stepLimit = options.stepLimit
        val stepsStatistics = StepsStatistics<TvmCodeBlock, TvmState>()
        val stopStrategy = if (stepLimit != null) {
            StepLimitStopStrategy(stepLimit, stepsStatistics)
        } else {
            StopStrategy { false }
        }
        val statesCollector = when (options.stateCollectionStrategy) {
            StateCollectionStrategy.COVERED_NEW, StateCollectionStrategy.REACHED_TARGET -> TODO("Unsupported strategy ${options.stateCollectionStrategy}")
            StateCollectionStrategy.ALL -> AllStatesCollector<TvmState>()
        }

        val observers = mutableListOf(statesCollector, stepsStatistics, coverageStatistics)
        run(
            interpreter,
            pathSelector,
            observer = CompositeUMachineObserver(observers),
            isStateTerminated = ::isStateTerminated,
            stopStrategy = stopStrategy,
        )

        return statesCollector.collectedStates
    }

    private fun isStateTerminated(state: TvmState): Boolean =
        state.methodResult !is TvmMethodResult.NoCall

    companion object {
        private val logger = object : KLogging() {}.logger

        private const val LOOP_ITERATIONS_LIMIT: Int = 2 // TODO find the best value

        private val defaultOptions: UMachineOptions = UMachineOptions(
            pathSelectionStrategies = listOf(PathSelectionStrategy.DFS),
            stateCollectionStrategy = StateCollectionStrategy.ALL,
            stopOnCoverage = -1,
            loopIterativeDeepening = true,
            loopIterationLimit = LOOP_ITERATIONS_LIMIT,
            stepLimit = null,
        )
    }
}
