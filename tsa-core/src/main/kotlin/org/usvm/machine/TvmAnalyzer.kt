package org.usvm.machine

import org.ton.bytecode.TvmContractCode
import org.ton.bytecode.TvmMethod
import org.ton.cell.Cell
import org.usvm.machine.FuncAnalyzer.Companion.FIFT_EXECUTABLE
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

sealed interface TvmAnalyzer {
    fun analyzeAllMethods(
        sourcesPath: Path,
        contractDataHex: String? = null,
        methodsBlackList: Set<Int> = hashSetOf(Int.MAX_VALUE)
    ): Map<TvmMethod, List<TvmState>>
}

class FuncAnalyzer(
    private val funcStdlibPath: Path,
    private val fiftStdlibPath: Path,
) : TvmAnalyzer {
    private val funcExecutablePath: Path = Paths.get(FUNC_EXECUTABLE)
    private val fiftExecutablePath: Path = Paths.get(FIFT_EXECUTABLE)

    override fun analyzeAllMethods(
        sourcesPath: Path,
        contractDataHex: String?,
        methodsBlackList: Set<Int>
    ): Map<TvmMethod, List<TvmState>> {
        val tmpBocFile = createTempFile(suffix = ".boc")
        try {
            compileFuncSourceToBoc(sourcesPath, tmpBocFile)
            return BocAnalyzer.analyzeAllMethods(tmpBocFile, contractDataHex, methodsBlackList)
        } finally {
            tmpBocFile.deleteIfExists()
        }
    }

    private fun compileFuncSourceToBoc(funcSourcesPath: Path, bocFilePath: Path) {
        val funcCommand = "$funcExecutablePath -W ${bocFilePath.absolutePathString()} $funcStdlibPath ${funcSourcesPath.absolutePathString()}"
        val fiftCommand = "$fiftExecutablePath -I $fiftStdlibPath"
        val command = "$funcCommand | $fiftCommand"
        val compilerProcess = ProcessBuilder(listOf("/bin/sh", "-c", command))
            .start()
        compilerProcess.waitFor(COMPILER_TIMEOUT, TimeUnit.SECONDS)

        check(bocFilePath.exists() && bocFilePath.readBytes().isNotEmpty()) {
            "Compilation failed, error: ${compilerProcess.errorStream.bufferedReader().readText()}"
        }
    }

    companion object {
        const val FUNC_EXECUTABLE = "func"
        const val FIFT_EXECUTABLE = "fift"
    }
}

class FiftAnalyzer(
    private val fiftStdlibPath: Path,
) : TvmAnalyzer {
    private val fiftExecutablePath: Path = Paths.get(FIFT_EXECUTABLE)

    override fun analyzeAllMethods(
        sourcesPath: Path,
        contractDataHex: String?,
        methodsBlackList: Set<Int>
    ): Map<TvmMethod, List<TvmState>> {
        val tmpBocFile = createTempFile(suffix = ".boc")
        try {
            compileFiftToBoc(sourcesPath, tmpBocFile)
            return BocAnalyzer.analyzeAllMethods(tmpBocFile, contractDataHex, methodsBlackList)
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
            return BocAnalyzer.loadContractFromBoc(tmpBocFile)
        } finally {
            tmpBocFile.deleteIfExists()
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

    private fun compileFiftToBoc(fiftPath: Path, bocFilePath: Path) {
        compileFiftCodeToBoc(fiftPath.readText(), bocFilePath)
    }

    /**
     * [codeBlocks] -- blocks of FIFT instructions, surrounded with <{ ... }>
     * */
    private fun compileFiftCodeBlocks(fiftWorkDir: Path, codeBlocks: List<String>, bocFilePath: Path) {
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

        compileFiftCodeToBoc(fiftCode, bocFilePath)
    }

    private fun compileFiftCodeToBoc(fiftCode: String, bocFilePath: Path) {
        val fiftTextWithOutputCommand = """
        $fiftCode
        2 boc+>B "$bocFilePath" B>file
    """.trimIndent()

        val fiftCommand = "echo '$fiftTextWithOutputCommand' | $fiftExecutablePath -n"
        val compilerProcess = ProcessBuilder(listOf("/bin/sh", "-c", fiftCommand))
            .directory(fiftStdlibPath.toFile())
            .start()
        compilerProcess.waitFor(COMPILER_TIMEOUT, TimeUnit.SECONDS)

        check(compilerProcess.exitValue() == 0 && bocFilePath.exists() && bocFilePath.readBytes().isNotEmpty()) {
            "Compilation failed, error: ${compilerProcess.errorStream.bufferedReader().readText()}"
        }
    }

    private fun runFiftInterpreter(fiftWorkDir: Path, fiftInterpreterCommand: String): FiftInterpreterResult{
        val fiftCommand = "echo '$fiftInterpreterCommand' | $fiftExecutablePath -n"
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

    companion object {
        private const val FINAL_STACK_STATE_MARKER = "\"FINAL STACK STATE\""
        private val TVM_EXECUTION_STATUS_PATTERN = Regex(""".*steps: (\d+) gas: used=(\d+).*""")
    }
}

data object BocAnalyzer : TvmAnalyzer {
    override fun analyzeAllMethods(
        sourcesPath: Path,
        contractDataHex: String?,
        methodsBlackList: Set<Int>
    ): Map<TvmMethod, List<TvmState>> {
        val contract = loadContractFromBoc(sourcesPath)
        return analyzeAllMethods(contract, methodsBlackList, contractDataHex)
    }

    fun loadContractFromBoc(bocFilePath: Path): TvmContractCode {
        val disasmArgs = DISASSEMBLER_RUN_COMMAND.split(" ") + bocFilePath.absolutePathString()
        val disasmProcess = ProcessBuilder(disasmArgs)
            .directory(Paths.get(DISASSEMBLER_PATH).toFile())
            .start()

        disasmProcess.waitFor(DISASSEMBLER_TIMEOUT, TimeUnit.SECONDS)

        val bytecodeJson = disasmProcess.inputStream.bufferedReader().readText()
        return TvmContractCode.fromJson(bytecodeJson)
    }

    private const val DISASSEMBLER_PATH = "../tvm-disasm"
    private const val DISASSEMBLER_RUN_COMMAND = "node dist/index.js"
    private const val DISASSEMBLER_TIMEOUT = 5.toLong() // seconds
}

fun analyzeAllMethods(contract: TvmContractCode, methodsBlackList: Set<Int> = hashSetOf(Int.MAX_VALUE), contractDataHex: String? = null): Map<TvmMethod, List<TvmState>> {
    val contractData = Cell.Companion.of(contractDataHex ?: DEFAULT_CONTRACT_DATA_HEX)
    val machine = TvmMachine()
    val methodsExceptDictPushConst = contract.methods.filterKeys { it !in methodsBlackList }
    val methodStates = methodsExceptDictPushConst.values.associateWith { method ->
        runCatching {
            machine.analyze(
                contract,
                contractData,
                method.id
            )
        }.getOrElse {
            logger.error(it) {
                "Failed analyzing $method"
            }
            emptyList()
        }
    }
    methodStates.forEach {
        println("Method ${it.key}")
        val exceptionalStates = it.value.filter { state -> state.isExceptional }
        println("States: ${it.value.size}, exceptional: ${exceptionalStates.size}")
        exceptionalStates.forEach { state -> println(state.methodResult) }
        println("=====".repeat(20))
    }

    return methodStates
}

data class FiftInterpreterResult(
    val exitCode: Int,
    val gasUsage: Int,
    val steps: Int,
    val stack: List<String>
)

private const val DEFAULT_CONTRACT_DATA_HEX = "b5ee9c7241010101000a00001000000185d258f59ccfc59500"
private const val COMPILER_TIMEOUT = 5.toLong() // seconds
