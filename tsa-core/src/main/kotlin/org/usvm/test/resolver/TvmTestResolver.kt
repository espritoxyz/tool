package org.usvm.test.resolver

import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmMethod
import org.usvm.machine.state.TvmMethodResult.TvmFailure
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.lastStmt
import java.math.BigInteger

data object TvmTestResolver {
    fun resolve(method: TvmMethod, state: TvmState): TvmSymbolicTest {
        val model = state.models.first()
        val ctx = state.ctx
        val stateResolver = TvmTestStateResolver(ctx, model, state)

        val usedParameters = stateResolver.resolveParameters()
        val result = stateResolver.resolveResultStack()
        val gasUsage = stateResolver.resolveGasUsage()

        return TvmSymbolicTest(
            methodId = method.id,
            usedParameters = usedParameters,
            result = result,
            stackTrace = listOf(state.lastStmt) + state.callStack.mapNotNull { it.returnSite },
            gasUsage = gasUsage
        )
    }

    fun resolve(methodStates: Map<TvmMethod, List<TvmState>>): TvmContractSymbolicTestResult = TvmContractSymbolicTestResult(
        methodStates.map {
            val method = it.key
            TvmSymbolicTestSuite(method.id, it.value.map { state -> resolve(method, state) })
        }
    )
}

data class TvmContractSymbolicTestResult(val testSuites: List<TvmSymbolicTestSuite>) : List<TvmSymbolicTestSuite> by testSuites

data class TvmSymbolicTestSuite(
    val methodId: BigInteger,
    val tests: List<TvmSymbolicTest>
) : List<TvmSymbolicTest> by tests

data class TvmSymbolicTest(
    val methodId: BigInteger,
    val usedParameters: List<TvmTestValue>,
    val result: TvmMethodSymbolicResult,
    val stackTrace: List<TvmInst>,
    val gasUsage: Int
)

sealed interface TvmMethodSymbolicResult {
    val stack: List<TvmTestValue>
}

data class TvmMethodFailure(
    val failure: TvmFailure,
    val lastStmt: TvmInst,
    override val stack: List<TvmTestValue>
) : TvmMethodSymbolicResult

data class TvmSuccessfulExecution(override val stack: List<TvmTestValue>) : TvmMethodSymbolicResult
