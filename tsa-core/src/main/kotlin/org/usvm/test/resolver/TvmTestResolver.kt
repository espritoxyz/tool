package org.usvm.test.resolver

import org.ton.TlbResolvedBuiltinLabel
import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmMethod
import org.usvm.machine.MethodId
import org.usvm.machine.interpreter.TvmInterpreter.Companion.logger
import org.usvm.machine.tryCatchIf
import org.usvm.machine.state.TvmMethodResult.TvmFailure
import org.usvm.machine.state.TvmState
import org.usvm.machine.types.TvmStructuralExit

data object TvmTestResolver {
    fun resolve(method: TvmMethod, state: TvmState): TvmSymbolicTest {
        val model = state.models.first()
        val ctx = state.ctx
        val stateResolver = TvmTestStateResolver(ctx, model, state)

        val usedParameters = stateResolver.resolveParameters()
        val contractAddress = stateResolver.resolveContractAddress()
        val initialData = stateResolver.resolveInitialData()
        val result = stateResolver.resolveResultStack()
        val gasUsage = stateResolver.resolveGasUsage()

        return TvmSymbolicTest(
            methodId = method.id,
            contractAddress = contractAddress,
            initialData = initialData,
            usedParameters = usedParameters,
            result = result,
            stackTrace = state.continuationStack,
            gasUsage = gasUsage
        )
    }

    fun resolve(
        methodStates: Map<TvmMethod, Pair<List<TvmState>, TvmMethodCoverage>>
    ): TvmContractSymbolicTestResult = TvmContractSymbolicTestResult(
        methodStates.mapNotNull {
            val method = it.key
            val coverage = it.value.second
            val states = it.value.first
            val tests = states.mapNotNull { state ->
                tryCatchIf(
                    condition = state.ctx.tvmOptions.quietMode,
                    body = { resolve(method, state) },
                    exceptionHandler = { exception ->
                        logger.debug(exception) { "Exception is thrown during the resolve of state $state" }
                        null
                    }
                )
            }

            TvmSymbolicTestSuite(
                method.id,
                coverage,
                tests,
            ).takeIf { tests.isNotEmpty() }
        }
    )
}

data class TvmContractSymbolicTestResult(val testSuites: List<TvmSymbolicTestSuite>) : List<TvmSymbolicTestSuite> by testSuites

data class TvmSymbolicTestSuite(
    val methodId: MethodId,
    val methodCoverage: TvmMethodCoverage,
    val tests: List<TvmSymbolicTest>,
) : List<TvmSymbolicTest> by tests

data class TvmMethodCoverage(
    val coverage: Float,
    val transitiveCoverage: Float,
)

data class TvmSymbolicTest(
    val methodId: MethodId,
    val contractAddress: TvmTestDataCellValue,
    val initialData: TvmTestCellValue,
    val usedParameters: List<TvmTestValue>,
    val result: TvmMethodSymbolicResult,
    val stackTrace: List<TvmInst>,
    val gasUsage: Int
)

sealed interface TvmMethodSymbolicResult {
    val stack: List<TvmTestValue>
}

sealed interface TvmTerminalMethodSymbolicResult : TvmMethodSymbolicResult {
    val exitCode: UInt
}

data class TvmMethodFailure(
    val failure: TvmFailure,
    val lastStmt: TvmInst,
    override val exitCode: UInt,
    override val stack: List<TvmTestValue>
) : TvmTerminalMethodSymbolicResult

data class TvmSuccessfulExecution(
    override val exitCode: UInt,
    override val stack: List<TvmTestValue>,
) : TvmTerminalMethodSymbolicResult

data class TvmExecutionWithStructuralError(
    val lastStmt: TvmInst,
    override val stack: List<TvmTestValue>,
    val exit: TvmStructuralExit<TvmTestCellDataTypeRead, TlbResolvedBuiltinLabel>,
) : TvmMethodSymbolicResult