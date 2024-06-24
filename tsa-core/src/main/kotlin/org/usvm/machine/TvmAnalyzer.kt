package org.usvm.machine

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import mu.KLogging
import org.ton.TvmInputInfo
import org.ton.bytecode.TvmContractCode
import org.ton.cell.Cell
import org.usvm.machine.FuncAnalyzer.Companion.FIFT_EXECUTABLE
import org.usvm.test.resolver.TvmContractSymbolicTestResult
import org.usvm.test.resolver.TvmTestResolver
import org.usvm.utils.FileUtils
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyTo
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.outputStream
import kotlin.io.path.readBytes
import kotlin.io.path.readText
import kotlin.io.path.walk

sealed interface TvmAnalyzer {
    fun analyzeAllMethods(
        sourcesPath: Path,
        contractDataHex: String? = null,
        methodsBlackList: Set<Int> = hashSetOf(Int.MAX_VALUE),
        inputInfo: Map<Int, TvmInputInfo> = emptyMap(),
        checkDataCellContentTypes: Boolean = true,
    ): TvmContractSymbolicTestResult
}

data object TactAnalyzer : TvmAnalyzer {
    @OptIn(ExperimentalPathApi::class)
    override fun analyzeAllMethods(
        sourcesPath: Path,
        contractDataHex: String?,
        methodsBlackList: Set<Int>,
        inputInfo: Map<Int, TvmInputInfo>,
        checkDataCellContentTypes: Boolean,
    ): TvmContractSymbolicTestResult {
        val outputDir = createTempDirectory(CONFIG_OUTPUT_PREFIX)
        val sourcesInOutputDir = sourcesPath.copyTo(outputDir.resolve(sourcesPath.fileName))
        val configFile = createTactConfig(sourcesInOutputDir, outputDir)

        try {
            compileTact(configFile)

            val bocFile = outputDir.walk().singleOrNull { it.toFile().extension == "boc" }
                ?: error("Cannot find .boc file after compiling the Tact source $sourcesPath")

            return BocAnalyzer.analyzeAllMethods(
                bocFile,
                contractDataHex,
                methodsBlackList,
                inputInfo,
                checkDataCellContentTypes
            )
        } finally {
            outputDir.deleteRecursively()
            configFile.deleteIfExists()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun createTactConfig(sourcesPath: Path, outputDir: Path): Path {
        val config = TactConfig(
            listOf(
                TactProject(
                    name = CONFIG_NAME_OPTION,
                    path = sourcesPath.absolutePathString(),
                    output = outputDir.absolutePathString(),
                )
            )
        )

        val configFile = createTempFile("tact_config")
        configFile.outputStream().use { Json.encodeToStream(config, it) }

        return configFile
    }

    private fun compileTact(configFile: Path) {
        val command = "$TACT_EXECUTABLE --config ${configFile.absolutePathString()}"
        val compilerProcess = ProcessBuilder(listOf("/bin/sh", "-c", command))
            .start()
        val exited = compilerProcess.waitFor(COMPILER_TIMEOUT, TimeUnit.SECONDS)

        check(exited) {
            compilerProcess.destroyForcibly()
            "Tact compilation process has not finished in $COMPILER_TIMEOUT seconds"
        }

        check(compilerProcess.exitValue() == 0) {
            "Compilation failed, error: ${compilerProcess.errorStream.bufferedReader().readText()}"
        }
    }

    @Serializable
    private data class TactConfig(val projects: List<TactProject>)

    @Serializable
    private data class TactProject(
        val name: String,
        val path: String,
        val output: String,
    )

    private const val CONFIG_NAME_OPTION: String = "sample"
    private const val CONFIG_OUTPUT_PREFIX: String = "output"
    private const val TACT_EXECUTABLE: String = "tact"
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
        methodsBlackList: Set<Int>,
        inputInfo: Map<Int, TvmInputInfo>,
        checkDataCellContentTypes: Boolean,
    ): TvmContractSymbolicTestResult {
        val tmpBocFile = createTempFile(suffix = ".boc")
        try {
            compileFuncSourceToBoc(sourcesPath, tmpBocFile)
            return BocAnalyzer.analyzeAllMethods(
                tmpBocFile,
                contractDataHex,
                methodsBlackList,
                inputInfo,
                checkDataCellContentTypes,
            )
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
        val exited = compilerProcess.waitFor(COMPILER_TIMEOUT, TimeUnit.SECONDS)
        check(exited) {
            compilerProcess.destroyForcibly()
            "Compiler process has not finished in $COMPILER_TIMEOUT seconds"
        }

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
        methodsBlackList: Set<Int>,
        inputInfo: Map<Int, TvmInputInfo>,
        checkDataCellContentTypes: Boolean,
    ): TvmContractSymbolicTestResult {
        val tmpBocFile = createTempFile(suffix = ".boc")
        try {
            compileFiftToBoc(sourcesPath, tmpBocFile)
            return BocAnalyzer.analyzeAllMethods(
                tmpBocFile,
                contractDataHex,
                methodsBlackList,
                inputInfo,
                checkDataCellContentTypes,
            )
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
        val exited = compilerProcess.waitFor(COMPILER_TIMEOUT, TimeUnit.SECONDS)
        check(exited) {
            compilerProcess.destroyForcibly()
            "Compiler process has not finished in $COMPILER_TIMEOUT seconds"
        }

        check(compilerProcess.exitValue() == 0 && bocFilePath.exists() && bocFilePath.readBytes().isNotEmpty()) {
            "Compilation failed, error: ${compilerProcess.errorStream.bufferedReader().readText()}"
        }
    }

    private fun ProcessBuilder.addFiftStdlib(): ProcessBuilder {
        val env = environment()
        env["FIFTPATH"] = fiftStdlibPath.toString()
        return this
    }

    private fun runFiftInterpreter(
        fiftWorkDir: Path,
        fiftInterpreterCommand: String
    ): FiftInterpreterResult{
        val fiftCommand = "echo '$fiftInterpreterCommand' | $fiftExecutablePath -n"
        val interpreterProcess = ProcessBuilder(listOf("/bin/sh", "-c", fiftCommand))
            .directory(fiftWorkDir.toFile())
            .addFiftStdlib()
            .start()

        val stdout = interpreterProcess.inputStream.bufferedReader().readText()
        val stderr = interpreterProcess.errorStream.bufferedReader().readText()

        val exited = interpreterProcess.waitFor(COMPILER_TIMEOUT, TimeUnit.SECONDS)
        check(exited) {
            interpreterProcess.destroyForcibly()
            "`fift` process has not finished in $COMPILER_TIMEOUT seconds"
        }

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
        methodsBlackList: Set<Int>,
        inputInfo: Map<Int, TvmInputInfo>,
        checkDataCellContentTypes: Boolean,
    ): TvmContractSymbolicTestResult {
        val contract = loadContractFromBoc(sourcesPath)
        return analyzeAllMethods(contract, methodsBlackList, contractDataHex, inputInfo, checkDataCellContentTypes)
    }

    fun loadContractFromBoc(bocFilePath: Path): TvmContractCode {
        val disasmArgs = DISASSEMBLER_RUN_COMMAND.split(" ") + bocFilePath.absolutePathString()
        val disasmProcess = ProcessBuilder(disasmArgs)
            .directory(Paths.get(DISASSEMBLER_PATH).toFile())
            .start()

        val bytecodeJson = disasmProcess.inputStream.bufferedReader().readText()
        val stderr = disasmProcess.errorStream.bufferedReader().readText()

        val exited = disasmProcess.waitFor(DISASSEMBLER_TIMEOUT, TimeUnit.SECONDS)
        check(exited) {
            disasmProcess.destroyForcibly()
            "Disassembler process has not finished in $DISASSEMBLER_TIMEOUT seconds"
        }
        check(disasmProcess.exitValue() == 0) {
            "Disassembler process finished with an error:\n$stderr"
        }

        return TvmContractCode.fromJson(bytecodeJson)
    }

    private val DISASSEMBLER_PATH: String by lazy {
        val disassemblerPath = FileUtils.tvmTempDirectory.resolve("tvm-disasm")
        FileUtils.extractZipFromResource("lib/tvm-disasm.zip", disassemblerPath)
        disassemblerPath.absolutePath
    }
    private const val DISASSEMBLER_RUN_COMMAND = "node dist/index.js"
    private const val DISASSEMBLER_TIMEOUT = 5.toLong() // seconds
}

fun analyzeAllMethods(
    contract: TvmContractCode,
    methodsBlackList: Set<Int> = hashSetOf(Int.MAX_VALUE),
    contractDataHex: String? = null,
    inputInfo: Map<Int, TvmInputInfo> = emptyMap(),
    checkDataCellContentTypes: Boolean = true,
): TvmContractSymbolicTestResult {
    val contractData = Cell.Companion.of(contractDataHex ?: DEFAULT_CONTRACT_DATA_HEX)
    val machine = TvmMachine(checkDataCellContentTypes = checkDataCellContentTypes)
    val methodsExceptDictPushConst = contract.methods.filterKeys { it.toInt() !in methodsBlackList }
    val methodStates = methodsExceptDictPushConst.values.associateWith { method ->
        runCatching {
            machine.analyze(
                contract,
                contractData,
                method.id,
                inputInfo[method.id.toInt()] ?: TvmInputInfo()
            )
        }.getOrElse {
            logger.error(it) {
                "Failed analyzing $method"
            }
            emptyList()
        }
    }
    methodStates.forEach {
        logger.debug("Method {}", it.key)
        val exceptionalStates = it.value.filter { state -> state.isExceptional }
        logger.debug("States: ${it.value.size}, exceptional: ${exceptionalStates.size}")
        exceptionalStates.forEach { state -> logger.debug(state.methodResult.toString()) }
        logger.debug("=====".repeat(20))
    }

    return TvmTestResolver.resolve(methodStates)
}

data class FiftInterpreterResult(
    val exitCode: Int,
    val gasUsage: Int,
    val steps: Int,
    val stack: List<String>
)

private const val DEFAULT_CONTRACT_DATA_HEX = "b5ee9c7241010101000a00001000000185d258f59ccfc59500"
private const val COMPILER_TIMEOUT = 5.toLong() // seconds

private val logger = object : KLogging() {}.logger
