package org.usvm.test

import kotlinx.serialization.Serializable
import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmMethod
import org.usvm.machine.state.TvmMethodResult.TvmFailure
import org.usvm.machine.state.TvmState

data object TvmTestResolver {
    fun resolve(method: TvmMethod, state: TvmState): TvmSymbolicTest {
        val model = state.models.first()
        val ctx = state.ctx
        val stateResolver = TvmTestStateResolver(ctx, model, state)

        val usedParameters = stateResolver.resolveParameters()
        val result = stateResolver.resolveResult()
        val gasUsage = stateResolver.resolveGasUsage()

        return TvmSymbolicTest(method.id, usedParameters, result, gasUsage)
    }

    fun resolve(methodStates: Map<TvmMethod, List<TvmState>>): TvmContractSymbolicTestResult = TvmContractSymbolicTestResult(
        methodStates.map {
            val method = it.key
            TvmSymbolicTestSuite(method.id, it.value.map { state -> resolve(method, state) })
        }
    )
}

@Serializable
data class TvmContractSymbolicTestResult(val testSuites: List<TvmSymbolicTestSuite>) : List<TvmSymbolicTestSuite> by testSuites

@Serializable
data class TvmSymbolicTestSuite(
    val methodId: Int,
    val tests: List<TvmSymbolicTest>
) : List<TvmSymbolicTest> by tests

@Serializable
data class TvmSymbolicTest(
    val methodId: Int,
    val usedParameters: List<TvmTestValue>,
    val result: TvmMethodSymbolicResult,
    val gasUsage: Int
)

@Serializable
sealed interface TvmMethodSymbolicResult {
    val stack: List<TvmTestValue>
}

@Serializable
data class TvmMethodFailure(
    val failure: TvmFailure,
    val lastStmt: TvmInst,
    override val stack: List<TvmTestValue>
) : TvmMethodSymbolicResult

@Serializable
data class TvmSuccessfulExecution(override val stack: List<TvmTestValue>) : TvmMethodSymbolicResult
