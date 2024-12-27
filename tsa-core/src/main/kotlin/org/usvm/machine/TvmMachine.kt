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
import org.usvm.machine.state.ContractId
import org.usvm.machine.state.TvmMethodResult
import org.usvm.machine.state.TvmState
import org.usvm.ps.createPathSelector
import org.usvm.statistics.ApplicationGraph
import org.usvm.statistics.CompositeUMachineObserver
import org.usvm.statistics.StepsStatistics
import org.usvm.statistics.TimeStatistics
import org.usvm.statistics.UMachineObserver
import org.usvm.statistics.collectors.AllStatesCollector
import org.usvm.stopstrategies.GroupedStopStrategy
import org.usvm.stopstrategies.StepLimitStopStrategy
import org.usvm.stopstrategies.StopStrategy
import java.math.BigInteger
import kotlin.time.Duration.Companion.INFINITE
import kotlin.time.Duration.Companion.seconds

class TvmMachine(
    private val options: UMachineOptions = defaultOptions,
    private val tvmOptions: TvmOptions = TvmOptions(),
) : UMachine<TvmState>() {
    private val components = TvmComponents(options)
    private val ctx = TvmContext(tvmOptions, components)

    fun analyze(
        contractCode: TvmContractCode,
        contractData: Cell,
        coverageStatistics: TvmCoverageStatistics,
        methodId: BigInteger,
        inputInfo: TvmInputInfo = TvmInputInfo()
    ): List<TvmState> =
        analyze(listOf(contractCode), startContractId = 0, contractData, coverageStatistics, methodId, inputInfo)

    fun analyze(
        contractsCode: List<TvmContractCode>,
        startContractId: ContractId,
        contractData: Cell,
        coverageStatistics: TvmCoverageStatistics,  // TODO: adapt for several contracts
        methodId: BigInteger,
        inputInfo: TvmInputInfo = TvmInputInfo(),
        additionalStopStrategy: StopStrategy = StopStrategy { false },
        additionalObserver: UMachineObserver<TvmState>? = null,
    ): List<TvmState> {
        val interpreter = TvmInterpreter(
            ctx,
            contractsCode,
            typeSystem = components.typeSystem,
            inputInfo = inputInfo,
        )
        logger.debug("{}.analyze({})", this, contractsCode)
        val initialState = interpreter.getInitialState(startContractId, contractData, methodId)

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
        val timeStatistics = TimeStatistics<TvmCodeBlock, TvmState>()
        val stopStrategy = if (stepLimit != null) {
            StepLimitStopStrategy(stepLimit, stepsStatistics)
        } else {
            StopStrategy { false }
        }

        val integrativeStopStrategy = GroupedStopStrategy(listOf(stopStrategy, additionalStopStrategy))

        val statesCollector = when (options.stateCollectionStrategy) {
            StateCollectionStrategy.COVERED_NEW, StateCollectionStrategy.REACHED_TARGET -> TODO("Unsupported strategy ${options.stateCollectionStrategy}")
            StateCollectionStrategy.ALL -> AllStatesCollector<TvmState>()
        }

        val observers = mutableListOf(statesCollector, stepsStatistics, coverageStatistics, timeStatistics)
        additionalObserver?.let { observers.add(it) }

        if (logger.isDebugEnabled && contractsCode.size == 1) {
            val code = contractsCode.single()
            val profiler = getTvmDebugProfileObserver(code)
            observers.add(profiler)
        }

        run(
            interpreter,
            pathSelector,
            observer = CompositeUMachineObserver(observers),
            isStateTerminated = ::isStateTerminated,
            stopStrategy = integrativeStopStrategy,
        )

        return interpreter.postProcessStates(statesCollector.collectedStates)
    }

    private fun isStateTerminated(state: TvmState): Boolean =
        state.methodResult !is TvmMethodResult.NoCall

    companion object {
        private val logger = object : KLogging() {}.logger

        const val DEFAULT_MAX_RECURSION_DEPTH: Int = 2
        private const val LOOP_ITERATIONS_LIMIT: Int = 2 // TODO find the best value
        const val DEFAULT_MAX_TLB_DEPTH = 10
        const val DEFAULT_MAX_CELL_DEPTH_FOR_DEFAULT_CELLS_CONSISTENT_WITH_TLB = 10

        val defaultOptions: UMachineOptions = UMachineOptions(
            pathSelectionStrategies = listOf(PathSelectionStrategy.DFS),
            stateCollectionStrategy = StateCollectionStrategy.ALL,
            timeout = INFINITE,
            stopOnCoverage = -1,
            loopIterativeDeepening = true,
            loopIterationLimit = LOOP_ITERATIONS_LIMIT,
            stepLimit = null,
            solverTimeout = 1.seconds
        )
    }

    override fun close() {
        components.close()
    }
}
