package org.usvm.machine

import org.ton.bytecode.TvmContractCode
import org.usvm.machine.TvmComponents
import org.usvm.machine.state.TvmMethodResult
import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmCodeBlock
import org.ton.cell.Cell
import org.usvm.PathSelectionStrategy
import org.usvm.UMachine
import org.usvm.UMachineOptions
import org.usvm.api.targets.JcTarget
import org.usvm.machine.interpreter.TvmInterpreter
import org.usvm.machine.state.TvmState
import org.usvm.ps.createPathSelector
import org.usvm.statistics.ApplicationGraph
import org.usvm.statistics.CompositeUMachineObserver
import org.usvm.statistics.collectors.StatesCollector
import org.usvm.stopstrategies.StopStrategy

class TvmMachine : UMachine<TvmState>() {
    override fun close() {
        // Do nothing
    }

    private val components = TvmComponents()
    private val ctx = TvmContext(components)

    fun analyze(contractCode: TvmContractCode, contractData: Cell, methodId: Int, targets: List<JcTarget> = emptyList()): List<TvmState> {
        val interpreter = TvmInterpreter(ctx, contractCode)
        logger.debug("{}.analyze({})", this, contractCode)
        val initialState = interpreter.getInitialState(contractCode, contractData, methodId)


        val pathSelector = createPathSelector(initialState, UMachineOptions(pathSelectionStrategies = listOf(PathSelectionStrategy.RANDOM_PATH)), object : ApplicationGraph<TvmCodeBlock, TvmInst> {
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
        })

        val stopStrategy = StopStrategy { false }
        val statesCollector = object : StatesCollector<TvmState> {
            private val states: MutableList<TvmState> = mutableListOf()

            override val collectedStates: List<TvmState>
                get() = states

            override fun onStateTerminated(state: TvmState, stateReachable: Boolean) {
                if (stateReachable) {
                    states += state
                }
            }
        }

        run(
            interpreter,
            pathSelector,
            observer = CompositeUMachineObserver(listOf(statesCollector)),
            isStateTerminated = ::isStateTerminated,
            stopStrategy = stopStrategy,
        )

        return statesCollector.collectedStates
    }

    private fun isStateTerminated(state: TvmState): Boolean {
        return state.callStack.isEmpty() || state.methodResult is TvmMethodResult.TvmFailure
    }
}