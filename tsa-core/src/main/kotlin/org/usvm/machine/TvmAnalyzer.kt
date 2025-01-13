package org.usvm.machine

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import mu.KLogging
import org.ton.TvmInputInfo
import org.ton.bytecode.TvmContractCode
import org.ton.bytecode.TvmMethod
import org.ton.cell.Cell
import org.ton.disasm.TvmDisassembler
import org.usvm.machine.FuncAnalyzer.Companion.FIFT_EXECUTABLE
import org.usvm.machine.state.ContractId
import org.usvm.machine.state.TvmState
import org.usvm.statistics.UMachineObserver
import org.usvm.stopstrategies.StopStrategy
import org.usvm.test.resolver.TvmContractSymbolicTestResult
import org.usvm.test.resolver.TvmMethodCoverage
import org.usvm.test.resolver.TvmTestResolver
import org.usvm.utils.executeCommandWithTimeout
import org.usvm.utils.toText
import java.math.BigInteger
import java.nio.file.Path
import java.nio.file.Paths
import org.ton.TvmContractHandlers
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
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.minutes

sealed interface TvmAnalyzer {
    fun analyzeAllMethods(
        sourcesPath: Path,
        contractDataHex: String? = null,
        methodsBlackList: Set<MethodId> = hashSetOf(),
        methodsWhiteList: Set<MethodId>? = null,
        inputInfo: Map<BigInteger, TvmInputInfo> = emptyMap(),
        tvmOptions: TvmOptions = TvmOptions(quietMode = true, timeout = 10.minutes),
    ): TvmContractSymbolicTestResult

    fun convertToTvmContractCode(sourcesPath: Path): TvmContractCode
}

data object TactAnalyzer : TvmAnalyzer {
    @OptIn(ExperimentalPathApi::class)
    override fun convertToTvmContractCode(sourcesPath: Path): TvmContractCode {
        val outputDir = createTempDirectory(CONFIG_OUTPUT_PREFIX)
        val sourcesInOutputDir = sourcesPath.copyTo(outputDir.resolve(sourcesPath.fileName))
        val configFile = createTactConfig(sourcesInOutputDir, outputDir)

        try {
            compileTact(configFile)
            val bocFile = outputDir.walk().singleOrNull { it.toFile().extension == "boc" }
                ?: error("Cannot find .boc file after compiling the Tact source $sourcesPath")

            return BocAnalyzer.loadContractFromBoc(bocFile)

        } finally {
            outputDir.deleteRecursively()
            configFile.deleteIfExists()
        }
    }

    override fun analyzeAllMethods(
        sourcesPath: Path,
        contractDataHex: String?,
        methodsBlackList: Set<MethodId>,
        methodsWhiteList: Set<MethodId>?,
        inputInfo: Map<BigInteger, TvmInputInfo>,
        tvmOptions: TvmOptions,
    ): TvmContractSymbolicTestResult {
        val contract = convertToTvmContractCode(sourcesPath)
        return analyzeAllMethods(contract, methodsBlackList, methodsWhiteList, contractDataHex, inputInfo, tvmOptions)
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
        val tactCommand = "$TACT_EXECUTABLE --config ${configFile.absolutePathString()}"
        val executionCommand = tactCommand.toExecutionCommand()
        val (exitValue, completedInTime, _, errors) = executeCommandWithTimeout(executionCommand, COMPILER_TIMEOUT)

        check(completedInTime) {
            "Tact compilation process has not finished in $COMPILER_TIMEOUT seconds"
        }

        check(exitValue == 0) {
            "Tact compilation failed with an error, exit code $exitValue, errors: \n${errors.toText()}"
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

    override fun convertToTvmContractCode(sourcesPath: Path): TvmContractCode {
        val tmpBocFile = createTempFile(suffix = ".boc")
        try {
            compileFuncSourceToBoc(sourcesPath, tmpBocFile)
            return BocAnalyzer.loadContractFromBoc(tmpBocFile)
        } finally {
            tmpBocFile.deleteIfExists()
        }
    }

    override fun analyzeAllMethods(
        sourcesPath: Path,
        contractDataHex: String?,
        methodsBlackList: Set<MethodId>,
        methodsWhiteList: Set<MethodId>?,
        inputInfo: Map<BigInteger, TvmInputInfo>,
        tvmOptions: TvmOptions,
    ): TvmContractSymbolicTestResult {
        val contract = convertToTvmContractCode(sourcesPath)
        return analyzeAllMethods(contract, methodsBlackList, methodsWhiteList, contractDataHex, inputInfo, tvmOptions)
    }

    fun compileFuncSourceToFift(funcSourcesPath: Path, fiftFilePath: Path) {
        val funcCommand = "$funcExecutablePath -AP $funcStdlibPath ${funcSourcesPath.absolutePathString()}"
        val executionCommand = funcCommand.toExecutionCommand()
        val (exitValue, completedInTime, output, errors) = executeCommandWithTimeout(executionCommand, COMPILER_TIMEOUT)

        check(completedInTime) {
            "FunC compilation to Fift has not finished in $COMPILER_TIMEOUT seconds"
        }

        check(exitValue == 0) {
            "FunC compilation failed with an error, exit code $exitValue, errors: \n${errors.toText()}"
        }
        val fiftIncludePreamble = """"Fift.fif" include"""
        val fiftCode = "$fiftIncludePreamble\n${output.toText()}"

        fiftFilePath.writeText(fiftCode)
    }

    fun compileFuncSourceToBoc(funcSourcesPath: Path, bocFilePath: Path) {
        val funcCommand = "$funcExecutablePath -W ${bocFilePath.absolutePathString()} $funcStdlibPath ${funcSourcesPath.absolutePathString()}"
        val fiftCommand = "$fiftExecutablePath -I $fiftStdlibPath"
        val command = "$funcCommand | $fiftCommand"
        val executionCommand = command.toExecutionCommand()
        val (exitValue, completedInTime, _, errors) = executeCommandWithTimeout(executionCommand, COMPILER_TIMEOUT)

        check(completedInTime) {
            "FunC compilation to BoC has not finished in $COMPILER_TIMEOUT seconds"
        }

        check(exitValue == 0 && errors.isEmpty()) {
            "FunC compilation to BoC failed with an error, exit code $exitValue, errors: \n${errors.toText()}"
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

    override fun convertToTvmContractCode(sourcesPath: Path): TvmContractCode {
        val tmpBocFile = createTempFile(suffix = ".boc")
        try {
            compileFiftToBoc(sourcesPath, tmpBocFile)
            return BocAnalyzer.loadContractFromBoc(tmpBocFile)
        } finally {
            tmpBocFile.deleteIfExists()
        }
    }

    override fun analyzeAllMethods(
        sourcesPath: Path,
        contractDataHex: String?,
        methodsBlackList: Set<MethodId>,
        methodsWhiteList: Set<MethodId>?,
        inputInfo: Map<BigInteger, TvmInputInfo>,
        tvmOptions: TvmOptions,
    ): TvmContractSymbolicTestResult {
        val contract = convertToTvmContractCode(sourcesPath)
        return analyzeAllMethods(contract, methodsBlackList, methodsWhiteList, contractDataHex, inputInfo, tvmOptions)
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
        val executionCommand = fiftCommand.toExecutionCommand()
        val (exitValue, completedInTime, _, errors) = executeCommandWithTimeout(
            executionCommand,
            COMPILER_TIMEOUT,
            fiftStdlibPath.toFile()
        )

        check(completedInTime) {
            "Fift compilation has not finished in $COMPILER_TIMEOUT seconds"
        }

        check(exitValue == 0 && bocFilePath.exists() && bocFilePath.readBytes().isNotEmpty()) {
            "Fift compilation failed with an error, exit code $exitValue, errors: \n${errors.toText()}"
        }
    }

    private fun runFiftInterpreter(
        fiftWorkDir: Path,
        fiftInterpreterCommand: String
    ): FiftInterpreterResult{
        val fiftCommand = "echo '$fiftInterpreterCommand' | $fiftExecutablePath -n"
        val executionCommand = fiftCommand.toExecutionCommand()
        val (exitValue, completedInTime, output, errors) = executeCommandWithTimeout(
            executionCommand,
            COMPILER_TIMEOUT,
            fiftWorkDir.toFile(),
            mapOf("FIFTPATH" to fiftStdlibPath.toString())
        )

        check(completedInTime) {
            "`fift` process has not has not finished in $COMPILER_TIMEOUT seconds"
        }

        check(exitValue == 0) {
            "`fift` process failed with an error, exit code $exitValue, errors: \n${errors.toText()}"
        }

        val finalStackState = output
            .lastOrNull { it.trim().endsWith(FINAL_STACK_STATE_MARKER) }
            ?.trim()?.removeSuffix(FINAL_STACK_STATE_MARKER)?.trim()
            ?: error("No final stack state")

        val stackEntries = finalStackState.split(' ').map { it.trim() }
        val exitCode = stackEntries.lastOrNull()?.toIntOrNull()
            ?: error("Incorrect exit code: $finalStackState")

        val stackEntriesWithoutExitCode = stackEntries.dropLast(1)

        val tvmState = errors
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
        methodsBlackList: Set<MethodId>,
        methodsWhiteList: Set<MethodId>?,
        inputInfo: Map<BigInteger, TvmInputInfo>,
        tvmOptions: TvmOptions,
    ): TvmContractSymbolicTestResult {
        val contract = loadContractFromBoc(sourcesPath)
        return analyzeAllMethods(contract, methodsBlackList, methodsWhiteList, contractDataHex, inputInfo, tvmOptions)
    }

    override fun convertToTvmContractCode(sourcesPath: Path): TvmContractCode {
        return loadContractFromBoc(sourcesPath)
    }

    fun loadContractFromBoc(bocFilePath: Path): TvmContractCode {
        val boc = bocFilePath.toFile().readBytes()
        val bytecodeJson = TvmDisassembler.disassemble(boc)

        return TvmContractCode.fromJson(bytecodeJson.toString())
    }
}

private fun runAnalysisInCatchingBlock(
    contractIdForCoverageStats: ContractId,
    contractForCoverageStats: TvmContractCode,
    method: TvmMethod,
    logInfoAboutAnalysis: Boolean = true,
    analysisRun: (TvmCoverageStatistics) -> List<TvmState>,
) : Pair<List<TvmState>, TvmMethodCoverage> =
    runCatching {
        val coverageStatistics = TvmCoverageStatistics(contractIdForCoverageStats, contractForCoverageStats)

        val states = analysisRun(coverageStatistics)

        val coverage = TvmMethodCoverage(
            coverageStatistics.getMethodCoveragePercents(method),
            coverageStatistics.getTransitiveCoveragePercents()
        )

        if (logInfoAboutAnalysis) {
            logger.info("Method {}", method)
            logger.info("Coverage: ${coverage.coverage}, transitive coverage: ${coverage.transitiveCoverage}")
        }
        val exceptionalStates = states.filter { state -> state.isExceptional }
        logger.debug("States: ${states.size}, exceptional: ${exceptionalStates.size}")
        exceptionalStates.forEach { state -> logger.debug(state.methodResult.toString()) }
        logger.debug("=====".repeat(20))

        states to coverage
    }.getOrElse {
        logger.error(it) {
            "Failed analyzing $method"
        }

        emptyList<TvmState>() to TvmMethodCoverage(coverage = 0f, transitiveCoverage = 0f)
    }

fun analyzeInterContract(
    contracts: List<TvmContractCode>,
    startContractId: ContractId,
    methodId: MethodId,
    inputInfo: TvmInputInfo = TvmInputInfo(),
    communicationScheme: Map<ContractId, TvmContractHandlers> = mapOf(),
    additionalStopStrategy: StopStrategy = StopStrategy { false },
    additionalObserver: UMachineObserver<TvmState>? = null,
    options: TvmOptions = TvmOptions(),
    manualStatePostProcess: (TvmState) -> List<TvmState> = { listOf(it) },
): TvmContractSymbolicTestResult {
    val machine = TvmMachine(tvmOptions = options)
    val startContractCode = contracts[startContractId]
    val method = startContractCode.methods[methodId]
        ?: error("Method $methodId not found in contract $startContractCode")
    val analysisResult = runAnalysisInCatchingBlock(
        contractIdForCoverageStats = startContractId,
        contractForCoverageStats = startContractCode,
        method = method,
        logInfoAboutAnalysis = false,
    ) { coverageStatistics ->
        machine.analyze(
            contracts,
            startContractId,
            Cell.Companion.of(DEFAULT_CONTRACT_DATA_HEX),
            coverageStatistics,
            methodId,
            inputInfo = inputInfo,
            additionalStopStrategy = additionalStopStrategy,
            additionalObserver = additionalObserver,
            manualStatePostProcess = manualStatePostProcess,
            communicationScheme = communicationScheme,
        )
    }

    machine.close()
    return TvmTestResolver.resolve(mapOf(method to analysisResult))
}

fun analyzeAllMethods(
    contract: TvmContractCode,
    methodsBlackList: Set<MethodId> = hashSetOf(),
    methodWhitelist: Set<MethodId>? = null,
    contractDataHex: String? = null,
    inputInfo: Map<BigInteger, TvmInputInfo> = emptyMap(),
    tvmOptions: TvmOptions = TvmOptions(),
): TvmContractSymbolicTestResult {
    val contractData = Cell.Companion.of(contractDataHex ?: DEFAULT_CONTRACT_DATA_HEX)
    val machineOptions = TvmMachine.defaultOptions.copy(timeout = tvmOptions.timeout)
    val machine = TvmMachine(tvmOptions = tvmOptions, options = machineOptions)
    val methodsExceptDictPushConst = contract.methods.filterKeys { it !in methodsBlackList }
    val methodStates = methodsExceptDictPushConst.values.associateWith { method ->
        if (methodWhitelist?.let { method.id in it } == false) {
            return@associateWith emptyList<TvmState>() to TvmMethodCoverage(0.0f, 0.0f)
        }
        runAnalysisInCatchingBlock(
            contractIdForCoverageStats = 0,
            contract,
            method
        ) { coverageStatistics ->
            machine.analyze(
                contract,
                contractData,
                coverageStatistics,
                method.id,
                inputInfo[method.id] ?: TvmInputInfo()
            )
        }
    }

    machine.close()

    return TvmTestResolver.resolve(methodStates)
}

private fun String.toExecutionCommand(): List<String> = listOf("/bin/sh", "-c", this)

data class FiftInterpreterResult(
    val exitCode: Int,
    val gasUsage: Int,
    val steps: Int,
    val stack: List<String>
)

const val DEFAULT_CONTRACT_DATA_HEX = "b5ee9c7241010101000a00001000000185d258f59ccfc59500"
private const val COMPILER_TIMEOUT = 5.toLong() // seconds

typealias MethodId = BigInteger

@Suppress("NOTHING_TO_INLINE")
inline fun Int.toMethodId(): MethodId = toBigInteger()

private val logger = object : KLogging() {}.logger
