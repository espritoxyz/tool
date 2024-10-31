package org.usvm.checkers

import org.ton.bytecode.TvmContractCode
import org.usvm.test.resolver.TvmSymbolicTest

interface TvmChecker {
    fun findConflictingExecutions(
        contractUnderTest: TvmContractCode,
        stopWhenFoundOneConflictingExecution: Boolean = false,
    ): List<TvmSymbolicTest>
}
