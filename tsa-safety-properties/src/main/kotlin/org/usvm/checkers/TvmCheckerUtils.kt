package org.usvm.checkers

import org.ton.TvmInputInfo
import org.ton.bytecode.TvmContractCode
import org.usvm.FirstFailureTerminator
import org.usvm.machine.MethodId
import org.usvm.machine.TvmOptions
import org.usvm.machine.analyzeInterContract
import org.usvm.stopstrategies.StopStrategy
import org.usvm.test.resolver.TvmMethodFailure
import org.usvm.test.resolver.TvmSymbolicTest

fun runAnalysisAndExtractFailingExecutions(
    contracts: List<TvmContractCode>,
    stopWhenFoundOneConflictingExecution: Boolean,
    inputInfo: TvmInputInfo?,
): List<TvmSymbolicTest> {
    val additionalStopStrategy = FirstFailureTerminator()
    val analysisResult = analyzeInterContract(
        contracts,
        startContractId = 0,
        methodId = MethodId.ZERO,
        additionalStopStrategy = if (stopWhenFoundOneConflictingExecution) additionalStopStrategy else StopStrategy { false },
        additionalObserver = if (stopWhenFoundOneConflictingExecution) additionalStopStrategy else null,
        options = TvmOptions(turnOnTLBParsingChecks = false),
        inputInfo = inputInfo ?: TvmInputInfo(),
    )
    check(analysisResult.testSuites.size == 1) {
        "Number of test suites must be 1, but found ${analysisResult.testSuites.size}"
    }
    val foundTests = analysisResult.testSuites.single().tests
    val result = foundTests.filter { it.result is TvmMethodFailure }
    return result
}