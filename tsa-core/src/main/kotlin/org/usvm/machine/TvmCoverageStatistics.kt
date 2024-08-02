package org.usvm.machine

import org.ton.bytecode.TvmArtificialInst
import org.ton.bytecode.TvmContOperand1Inst
import org.ton.bytecode.TvmContOperand2Inst
import org.ton.bytecode.TvmContractCode
import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmInstList
import org.ton.bytecode.TvmInstMethodLocation
import org.ton.bytecode.TvmMethod
import org.usvm.machine.state.TvmState
import org.usvm.statistics.UMachineObserver
import java.util.Collections.newSetFromMap
import java.util.IdentityHashMap

// Tracks coverage of all visited statements for all visited methods from all states.
// Note that one instance should be used only one per method.
class TvmCoverageStatistics(private val contractCode: TvmContractCode) : UMachineObserver<TvmState> {
    private val coveredStatements: MutableSet<TvmInst> = newSetFromMap(IdentityHashMap())
    private val visitedMethods: MutableSet<MethodId> = hashSetOf()
    private val traversedMethodStatements: MutableMap<MethodId, List<TvmInst>> = hashMapOf()

    fun getMethodCoveragePercents(method: TvmMethod): Float {
        val methodStatements = getMethodStatements(method)
        val coveredMethodStatements = methodStatements.count { it in coveredStatements }

        return computeCoveragePercents(coveredMethodStatements, methodStatements.size)
    }

    fun getTransitiveCoveragePercents(): Float {
        val allStatements = visitedMethods.flatMap { methodId ->
            val method = contractCode.methods[methodId]
                ?: error("Unknown method with id $methodId")

            getMethodStatements(method)
        }

        return computeCoveragePercents(coveredStatements.size, allStatements.size)
    }

    private fun computeCoveragePercents(covered: Int, all: Int): Float {
        if (all == 0) {
            return 100f
        }

        return covered.toFloat() / (all.toFloat()) * 100f
    }

    private fun getMethodStatements(method: TvmMethod): List<TvmInst> {
        val methodId = method.id
        val alreadyTraversedStatements = traversedMethodStatements[methodId]
        if (alreadyTraversedStatements != null) {
            return alreadyTraversedStatements
        }

        val methodStatements = mutableListOf<TvmInst>()
        val queue = mutableListOf(TvmInstList(method.instList))

        while (queue.isNotEmpty()) {
            val instList = queue.removeLast()

            instList.list.forEach { stmt ->
                if (stmt is TvmArtificialInst) {
                    return@forEach
                }

                methodStatements.add(stmt)
                extractInstLists(stmt).forEach(queue::add)
            }
        }

        traversedMethodStatements[methodId] = methodStatements
        return methodStatements
    }

    private fun extractInstLists(stmt: TvmInst): Sequence<TvmInstList> =
        when (stmt) {
            is TvmContOperand1Inst -> sequenceOf(stmt.c)
            is TvmContOperand2Inst -> sequenceOf(stmt.c1, stmt.c2)
            else -> emptySequence()
        }

    override fun onStatePeeked(state: TvmState) {
        val stmt = state.currentStatement
        if (stmt is TvmArtificialInst) {
            return
        }

        coveredStatements.add(stmt)

        val location = stmt.location
        if (location is TvmInstMethodLocation) {
            visitedMethods.add(location.methodId)
        }
    }
}