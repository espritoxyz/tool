package org.usvm

import org.ton.bytecode.TvmContractCode
import org.usvm.checkers.TvmChecker
import org.usvm.machine.BocAnalyzer
import org.usvm.machine.FuncAnalyzer
import org.usvm.machine.getResourcePath
import org.usvm.machine.state.TvmFailureType
import org.usvm.machine.state.TvmMethodResult
import org.usvm.machine.state.TvmState
import org.usvm.statistics.UMachineObserver
import org.usvm.stopstrategies.StopStrategy
import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeBytes

fun getContractFromBytes(bytes: ByteArray): TvmContractCode{
    val tmpBocFile = createTempFile(suffix = ".boc")
    try {
        tmpBocFile.writeBytes(bytes)
        return BocAnalyzer.loadContractFromBoc(tmpBocFile)
    } finally {
        tmpBocFile.deleteIfExists()
    }
}

fun getFuncContract(path: Path, funcStdlibPath: Path, fiftStdlibPath: Path, isTSAChecker: Boolean = false): TvmContractCode {
    val tmpBocFile = createTempFile(suffix = ".boc")
    try {
        FuncAnalyzer(funcStdlibPath, fiftStdlibPath)
            .compileFuncSourceToBoc(path, tmpBocFile)
        return BocAnalyzer.loadContractFromBoc(tmpBocFile).also {
            if (isTSAChecker) {
                setTSACheckerFunctions(it)
            }
        }
    } finally {
        tmpBocFile.deleteIfExists()
    }
}

fun setTSACheckerFunctions(contractCode: TvmContractCode) {
    contractCode.isContractWithTSACheckerFunctions = true
}

const val FUNC_STDLIB_PATH = "/imports"
const val FIFT_STDLIB_PATH = "/fiftstdlib"

class FirstFailureTerminator : StopStrategy, UMachineObserver<TvmState> {
    private var shouldStop: Boolean = false

    override fun shouldStop(): Boolean = shouldStop

    override fun onStateTerminated(state: TvmState, stateReachable: Boolean) {
        if (!stateReachable)
            return
        val result = state.methodResult
        if (result is TvmMethodResult.TvmFailure && result.type !in forbiddenTypes) {
            shouldStop = true
        }
    }

    override fun stopReason(): String = "Found conflicting execution"

    companion object {
        val forbiddenTypes = listOf(
            TvmFailureType.FixedStructuralError,
            TvmFailureType.SymbolicStructuralError,
        )
    }
}

internal fun Path?.resolveResourcePath(resourceName: String): Path =
    this?.resolve(resourceName.substring(startIndex = 1)) ?: getResourcePath<TvmChecker>(resourceName)
