package org.usvm.machine

import org.ton.bytecode.TvmContractCode
import org.ton.bytecode.TvmDictSpecialDictpushconstInst
import org.ton.bytecode.TvmMethod
import org.ton.cell.Cell
import org.usvm.machine.state.TvmState
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.readText

fun compileAndAnalyzeAllMethods(
    funcSourcesPath: Path,
    contractDataHex: String? = null
): Map<TvmMethod, List<TvmState>> {
    val tmpBocFile = createTempFile(suffix = ".boc")
    try {
        compileFuncSourceToBoc(funcSourcesPath, tmpBocFile)
        return analyzeAllMethods(tmpBocFile.absolutePathString(), contractDataHex)
    } finally {
        tmpBocFile.deleteIfExists()
    }
}

fun compileAndAnalyzeFift(
    fiftPath: Path,
    contractDataHex: String? = null
): Map<TvmMethod, List<TvmState>> {
    val tmpBocFile = createTempFile(suffix = ".boc")
    try {
        compileFiftToBoc(fiftPath, tmpBocFile)
        return analyzeAllMethods(tmpBocFile.absolutePathString(), contractDataHex)
    } finally {
        tmpBocFile.deleteIfExists()
    }
}

fun analyzeAllMethods(bytecodePath: String, contractDataHex: String? = null): Map<TvmMethod, List<TvmState>> {
    val contractData = Cell.Companion.of(contractDataHex ?: DEFAULT_CONTRAACT_DATA_HEX)

    val disasmProcess = ProcessBuilder(*(DISASSEMBLER_RUN_COMMAND.split(" ") + bytecodePath).toTypedArray())
        .directory(Paths.get(DISASSEMBLER_PATH).toFile())
        .start()

    disasmProcess.waitFor(DISASSEMBLER_TIMEOUT, TimeUnit.SECONDS)

    val bytecodeJson = disasmProcess.inputStream.bufferedReader().readText()
    val contract = TvmContractCode.fromJson(bytecodeJson)
    val machine = TvmMachine()
    val methodsExceptDictPushConst = contract.methods.filterValues {
        it.instList.none { inst -> inst is TvmDictSpecialDictpushconstInst }
    }
    val methodStates = methodsExceptDictPushConst.values.associateWith { machine.analyze(contract, contractData, it.id) }
    methodStates.forEach {
        println("Method ${it.key}")
        val exceptionalStates = it.value.filter { state -> state.isExceptional }
        println("States: ${it.value.size}, exceptional: ${exceptionalStates.size}")
        exceptionalStates.forEach { state -> println(state.methodResult) }
        println("=====".repeat(20))
    }

    return methodStates
}

fun compileFuncSourceToBoc(funcSourcesPath: Path, bocFilePath: Path) {
    val funcCommand = "$FUNC_COMPILER_COMMAND -W ${bocFilePath.absolutePathString()} ${funcSourcesPath.fileName}"
    val fiftCommand = "$FIFT_COMMAND -I $FIFT_STDLIB_RESOURCE"
    val command = "$funcCommand | $fiftCommand"
    val compilerProcess = ProcessBuilder(listOf("/bin/sh", "-c", command))
        .directory(funcSourcesPath.parent.toFile())
        .start()
    compilerProcess.waitFor(COMPILER_TIMEOUT, TimeUnit.SECONDS)

    check(bocFilePath.exists() && bocFilePath.readBytes().isNotEmpty()) {
        "Compilation failed, error: ${compilerProcess.errorStream.bufferedReader().readText()}"
    }
}

fun compileFiftToBoc(fiftPath: Path, bocFilePath: Path) {
    val fiftTextWithOutputCommand = """
        ${fiftPath.readText()}
        2 boc+>B "$bocFilePath" B>file
    """.trimIndent()

    val fiftCommand = "echo '$fiftTextWithOutputCommand' | $FIFT_COMMAND -n"
    val compilerProcess = ProcessBuilder(listOf("/bin/sh", "-c", fiftCommand))
        .directory(fiftPath.parent.toFile())
        .start()
    compilerProcess.waitFor(COMPILER_TIMEOUT, TimeUnit.SECONDS)

    check(compilerProcess.exitValue() == 0 && bocFilePath.exists() && bocFilePath.readBytes().isNotEmpty()) {
        "Compilation failed, error: ${compilerProcess.errorStream.bufferedReader().readText()}"
    }
}

private const val DISASSEMBLER_PATH = "../tvm-disasm"
private const val DISASSEMBLER_RUN_COMMAND = "node dist/index.js"
private const val DISASSEMBLER_TIMEOUT = 5.toLong() // seconds
private const val DEFAULT_CONTRAACT_DATA_HEX = "b5ee9c7241010101000a00001000000185d258f59ccfc59500"

private const val COMPILER_TIMEOUT = 5.toLong() // seconds
private const val FUNC_COMPILER_COMMAND = "func"
private const val FIFT_COMMAND = "fift"

private const val FIFT_STDLIB_PATH = "/fiftstdlib"
private val FIFT_STDLIB_RESOURCE get() = object {}.javaClass.getResource(FIFT_STDLIB_PATH)?.path
    ?: error("Cannot find fift stdlib in $FIFT_STDLIB_PATH")
