package org.usvm.machine

import org.ton.bytecode.TvmContractCode
import org.ton.bytecode.TvmDictSpecialDictpushconstInst
import org.ton.bytecode.TvmMethod
import org.ton.cell.Cell
import org.usvm.machine.state.TvmState
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
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

/**
 * [codeBlocks] -- blocks of FIFT instructions, surrounded with <{ ... }>
 * */
fun compileFiftCodeBlocksContract(
    fiftWorkDir: Path,
    codeBlocks: List<String>,
): TvmContractCode {
    val tmpBocFile = createTempFile(suffix = ".boc")
    try {
        compileFiftCodeBlocks(fiftWorkDir, codeBlocks, tmpBocFile)
        return loadContractFromBoc(tmpBocFile)
    } finally {
        tmpBocFile.deleteIfExists()
    }
}

fun analyzeAllMethods(bytecodePath: String, contractDataHex: String? = null): Map<TvmMethod, List<TvmState>> {
    val contract = loadContractFromBoc(Path(bytecodePath))
    return analyzeAllMethods(contract, contractDataHex)
}

fun analyzeAllMethods(contract: TvmContractCode, contractDataHex: String? = null): Map<TvmMethod, List<TvmState>> {
    val contractData = Cell.Companion.of(contractDataHex ?: DEFAULT_CONTRAACT_DATA_HEX)
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

private fun loadContractFromBoc(bocFilePath: Path): TvmContractCode {
    val disasmArgs = DISASSEMBLER_RUN_COMMAND.split(" ") + bocFilePath.absolutePathString()
    val disasmProcess = ProcessBuilder(disasmArgs)
        .directory(Paths.get(DISASSEMBLER_PATH).toFile())
        .start()

    disasmProcess.waitFor(DISASSEMBLER_TIMEOUT, TimeUnit.SECONDS)

    val bytecodeJson = disasmProcess.inputStream.bufferedReader().readText()
    return TvmContractCode.fromJson(bytecodeJson)
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
    compileFiftCodeToBoc(fiftPath.parent, fiftPath.readText(), bocFilePath)
}

/**
 * [codeBlocks] -- blocks of FIFT instructions, surrounded with <{ ... }>
 * */
fun compileFiftCodeBlocks(fiftWorkDir: Path, codeBlocks: List<String>, bocFilePath: Path) {
    check(fiftWorkDir.resolve("Asm.fif").exists()) { "No Asm.fif" }
    check(fiftWorkDir.resolve("Fift.fif").exists()) { "No Fift.fif" }

    val methodIds = codeBlocks.indices.map { "$it DECLMETHOD cb_$it" }
    val blocks = codeBlocks.mapIndexed { index, block -> "cb_$index PROC:$block" }

    val fiftCode = """
        "Fift.fif" include
        "Asm.fif" include
        
        PROGRAM{
          ${methodIds.joinToString("\n")}
          
          ${blocks.joinToString("\n")}
        }END>c
    """.trimIndent()

    compileFiftCodeToBoc(fiftWorkDir, fiftCode, bocFilePath)
}

private fun compileFiftCodeToBoc(fiftWorkDir: Path, fiftCode: String, bocFilePath: Path) {
    val fiftTextWithOutputCommand = """
        $fiftCode
        2 boc+>B "$bocFilePath" B>file
    """.trimIndent()

    val fiftCommand = "echo '$fiftTextWithOutputCommand' | $FIFT_COMMAND -n"
    val compilerProcess = ProcessBuilder(listOf("/bin/sh", "-c", fiftCommand))
        .directory(fiftWorkDir.toFile())
        .start()
    compilerProcess.waitFor(COMPILER_TIMEOUT, TimeUnit.SECONDS)

    check(compilerProcess.exitValue() == 0 && bocFilePath.exists() && bocFilePath.readBytes().isNotEmpty()) {
        "Compilation failed, error: ${compilerProcess.errorStream.bufferedReader().readText()}"
    }
}

/**
 * Run method with [methodId].
 *
 * Note: the result Gas usage includes additional runvmx cost.
 * */
fun runFiftMethod(fiftPath: Path, methodId: Int): FiftInterpreterResult {
    val fiftTextWithOutputCommand = """
        ${fiftPath.readText()}
        <s $methodId swap 0x41 runvmx $FINAL_STACK_STATE_MARKER .s
    """.trimIndent()

    return runFiftInterpreter(fiftPath.parent, fiftTextWithOutputCommand)
}

/**
 * [codeBlock] -- block of FIFT instructions, surrounded with <{ ... }>
 * */
fun runFiftCodeBlock(fiftWorkDir: Path, codeBlock: String): FiftInterpreterResult {
    check(fiftWorkDir.resolve("Asm.fif").exists()) { "No Asm.fif" }
    check(fiftWorkDir.resolve("Fift.fif").exists()) { "No Fift.fif" }

    val fiftTextWithOutputCommand = """
        "Fift.fif" include
        "Asm.fif" include

        ${codeBlock.trim()}s runvmcode $FINAL_STACK_STATE_MARKER .s
    """.trimIndent()

    return runFiftInterpreter(fiftWorkDir, fiftTextWithOutputCommand)
}

private fun runFiftInterpreter(fiftWorkDir: Path, fiftInterpreterCommand: String): FiftInterpreterResult{
    val fiftCommand = "echo '$fiftInterpreterCommand' | $FIFT_COMMAND -n"
    val interpreterProcess = ProcessBuilder(listOf("/bin/sh", "-c", fiftCommand))
        .directory(fiftWorkDir.toFile())
        .start()

    interpreterProcess.waitFor(COMPILER_TIMEOUT, TimeUnit.SECONDS)

    val stdout = interpreterProcess.inputStream.bufferedReader().readText()
    val stderr = interpreterProcess.errorStream.bufferedReader().readText()

    val finalStackState = stdout.lines()
        .lastOrNull { it.trim().endsWith(FINAL_STACK_STATE_MARKER) }
        ?.trim()?.removeSuffix(FINAL_STACK_STATE_MARKER)?.trim()
        ?: error("No final stack state")

    val stackEntries = finalStackState.split(' ').map { it.trim() }
    val exitCode = stackEntries.lastOrNull()?.toIntOrNull()
        ?: error("Incorrect exit code: $finalStackState")

    val stackEntriesWithoutExitCode = stackEntries.dropLast(1)

    val tvmState = stderr.lines()
        .mapNotNull { TVM_EXECUTION_STATUS_PATTERN.matchEntire(it) }
        .lastOrNull()
        ?: error("No TVM state")

    val (_, steps, gasUsage) = tvmState.groupValues

    return FiftInterpreterResult(exitCode, gasUsage.toInt(), steps.toInt(), stackEntriesWithoutExitCode)
}

data class FiftInterpreterResult(
    val exitCode: Int,
    val gasUsage: Int,
    val steps: Int,
    val stack: List<String>
)

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

private const val FINAL_STACK_STATE_MARKER = "\"FINAL STACK STATE\""
private val TVM_EXECUTION_STATUS_PATTERN = Regex(""".*steps: (\d+) gas: used=(\d+).*""")
