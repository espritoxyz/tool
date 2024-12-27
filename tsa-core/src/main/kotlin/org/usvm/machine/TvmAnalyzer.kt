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
}

data object TactAnalyzer : TvmAnalyzer {
    @OptIn(ExperimentalPathApi::class)
    override fun analyzeAllMethods(
        sourcesPath: Path,
        contractDataHex: String?,
        methodsBlackList: Set<MethodId>,
        methodsWhiteList: Set<MethodId>?,
        inputInfo: Map<BigInteger, TvmInputInfo>,
        tvmOptions: TvmOptions,
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
                methodsWhiteList,
                inputInfo,
                tvmOptions,
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

class InterContractAnalyzer(
    private val funcStdlibPath: Path,
    private val fiftStdlibPath: Path,
) {
    private val funcExecutablePath: Path = Paths.get(FUNC_EXECUTABLE)
    private val fiftExecutablePath: Path = Paths.get(FIFT_EXECUTABLE)

    fun analyzeInternalMessagesWithInterContract(
        sourcesPaths: Collection<Path>,
        tvmOptions: TvmOptions,
    ): TvmContractSymbolicTestResult {
        val tmpFiles = mutableListOf<Path>()
        try {
            sourcesPaths.forEach { source ->
                val tmpBocFile = createTempFile(suffix = ".boc")
                compileFuncSourceToBoc(source, tmpBocFile)
                tmpFiles.add(tmpBocFile)
            }

            val tsaFunctionsPath = createTempFile(suffix = ".fc")
            tsaFunctionsPath.writeText(tsaFunctionsSources)

            val callerContractSourceCode = createInterContractCallerSources(tsaFunctionsPath.fileName, sourcesPaths.size + 1)
            val callerContractPath = createTempFile(directory = tsaFunctionsPath.parent, suffix = ".fc")
            callerContractPath.writeText(callerContractSourceCode)

            val callerContractBocFile = createTempFile(suffix = ".boc")
            compileFuncSourceToBoc(callerContractPath, callerContractBocFile)

            tmpFiles.add(0, callerContractBocFile)
            val contracts = tmpFiles.map { BocAnalyzer.loadContractFromBoc(it) }
            contracts.first().isContractWithTSACheckerFunctions = true

            tmpFiles.addAll(arrayOf(callerContractPath, tsaFunctionsPath))

            val recvInternalMethodId = BigInteger.ZERO
            val startContractCode = contracts.first()
            val method = startContractCode.methods[recvInternalMethodId]
                ?: error("Method $recvInternalMethodId not found in contract $startContractCode")
            val machine = TvmMachine(tvmOptions = tvmOptions)
            machine.use {
                val analysisResult = runAnalysisInCatchingBlock(
                    contractIdForCoverageStats = 0,
                    contractForCoverageStats = startContractCode,
                    method = method,
                    logInfoAboutAnalysis = false,
                ) { coverageStatistics ->
                    machine.analyze(
                        contractsCode = contracts,
                        startContractId = 0,
                        contractData = Cell.Companion.of(DEFAULT_CONTRACT_DATA_HEX),
                        coverageStatistics = coverageStatistics,
                        methodId = recvInternalMethodId,
                    )
                }

                val tests = TvmTestResolver.resolve(mapOf(method to analysisResult))

                return tests
            }
        } finally {
            tmpFiles.forEach { it.deleteIfExists() }
        }
    }

    private fun createInterContractCallerSources(tsaFunctionsPath: Path, numberOfContracts: Int): String = buildString {
        appendLine("#include \"$tsaFunctionsPath\";")
        appendLine()
        appendLine("() recv_internal(int my_balance, int msg_value, cell in_msg_full, slice msg_body) impure {")
        (1..<numberOfContracts - 1).map { contractId ->
            appendLine("\ttsa_call_0_4(my_balance, msg_value, in_msg_full, msg_body, $contractId, 0);")
            appendLine("\ttsa_process_output_actions($contractId);")
        }
        appendLine("tsa_call_0_4(my_balance, msg_value, in_msg_full, msg_body, ${numberOfContracts - 1}, 0);")
        appendLine("}")
    }

    fun compileFuncSourceToBoc(funcSourcesPath: Path, bocFilePath: Path) {
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

class FuncAnalyzer(
    private val funcStdlibPath: Path,
    private val fiftStdlibPath: Path,
) : TvmAnalyzer {
    private val funcExecutablePath: Path = Paths.get(FUNC_EXECUTABLE)
    private val fiftExecutablePath: Path = Paths.get(FIFT_EXECUTABLE)

    override fun analyzeAllMethods(
        sourcesPath: Path,
        contractDataHex: String?,
        methodsBlackList: Set<MethodId>,
        methodsWhiteList: Set<MethodId>?,
        inputInfo: Map<BigInteger, TvmInputInfo>,
        tvmOptions: TvmOptions,
    ): TvmContractSymbolicTestResult {
        val tmpBocFile = createTempFile(suffix = ".boc")
        try {
            compileFuncSourceToBoc(sourcesPath, tmpBocFile)
            return BocAnalyzer.analyzeAllMethods(
                tmpBocFile,
                contractDataHex,
                methodsBlackList,
                methodsWhiteList,
                inputInfo,
                tvmOptions,
            )
        } finally {
            tmpBocFile.deleteIfExists()
        }
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

        check(exitValue == 0) {
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

    override fun analyzeAllMethods(
        sourcesPath: Path,
        contractDataHex: String?,
        methodsBlackList: Set<MethodId>,
        methodsWhiteList: Set<MethodId>?,
        inputInfo: Map<BigInteger, TvmInputInfo>,
        tvmOptions: TvmOptions,
    ): TvmContractSymbolicTestResult {
        val tmpBocFile = createTempFile(suffix = ".boc")
        try {
            compileFiftToBoc(sourcesPath, tmpBocFile)
            return BocAnalyzer.analyzeAllMethods(
                tmpBocFile,
                contractDataHex,
                methodsBlackList,
                methodsWhiteList,
                inputInfo,
                tvmOptions,
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
    additionalStopStrategy: StopStrategy = StopStrategy { false },
    additionalObserver: UMachineObserver<TvmState>? = null,
    options: TvmOptions = TvmOptions(),
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


// TODO refactor this ASAP
// -----------------------------------------------
private val tsaFunctionsSources = """
    ;; generated

    ;; auxiliary functions

    forall A -> (A) return_1() asm "NOP";
    forall A, B -> (A, B) return_2() asm "NOP";
    forall A, B, C -> (A, B, C) return_3() asm "NOP";
    forall A, B, C, D -> (A, B, C, D) return_4() asm "NOP";
    forall A, B, C, D, E -> (A, B, C, D, E) return_5() asm "NOP";
    forall A, B, C, D, E, F -> (A, B, C, D, E, F) return_6() asm "NOP";
    forall A, B, C, D, E, F, G -> (A, B, C, D, E, F, G) return_7() asm "NOP";
    forall A, B, C, D, E, F, G, H -> (A, B, C, D, E, F, G, H) return_8() asm "NOP";
    forall A, B, C, D, E, F, G, H, I -> (A, B, C, D, E, F, G, H, I) return_9() asm "NOP";
    forall A, B, C, D, E, F, G, H, I, J -> (A, B, C, D, E, F, G, H, I, J) return_10() asm "NOP";

    ;; API functions
    
    () tsa_forbid_failures() impure method_id(1) {
        ;; do nothing
    }

    () tsa_allow_failures() impure method_id(2) {
        ;; do nothing
    }

    () tsa_assert(int condition) impure method_id(3) {
        ;; do nothing
    }

    () tsa_assert_not(int condition) impure method_id(4) {
        ;; do nothing
    }

    forall A -> () tsa_fetch_value(A value, int value_id) impure method_id(5) {
        ;; do nothing
    }
    
    ;; TODO add to the real tsa_functions
    () tsa_process_output_actions(int contract_id) impure method_id(6) {
        ;; do nothing
    }

    forall A -> () tsa_call_0_0(int id_contract, int id_method) impure method_id(10000) {
        ;; do nothing
    }

    forall A -> () tsa_call_0_1(A p0, int id_contract, int id_method) impure method_id(10001) {
        ;; do nothing
    }

    forall A, B -> () tsa_call_0_2(A p0, B p1, int id_contract, int id_method) impure method_id(10002) {
        ;; do nothing
    }

    forall A, B, C -> () tsa_call_0_3(A p0, B p1, C p2, int id_contract, int id_method) impure method_id(10003) {
        ;; do nothing
    }

    forall A, B, C, D -> () tsa_call_0_4(A p0, B p1, C p2, D p3, int id_contract, int id_method) impure method_id(10004) {
        ;; do nothing
    }

    forall A, B, C, D, E -> () tsa_call_0_5(A p0, B p1, C p2, D p3, E p4, int id_contract, int id_method) impure method_id(10005) {
        ;; do nothing
    }

    forall A, B, C, D, E, F -> () tsa_call_0_6(A p0, B p1, C p2, D p3, E p4, F p5, int id_contract, int id_method) impure method_id(10006) {
        ;; do nothing
    }

    forall A, B, C, D, E, F, G -> () tsa_call_0_7(A p0, B p1, C p2, D p3, E p4, F p5, G p6, int id_contract, int id_method) impure method_id(10007) {
        ;; do nothing
    }

    forall A, B, C, D, E, F, G, H -> () tsa_call_0_8(A p0, B p1, C p2, D p3, E p4, F p5, G p6, H p7, int id_contract, int id_method) impure method_id(10008) {
        ;; do nothing
    }

    forall A, B, C, D, E, F, G, H, I -> () tsa_call_0_9(A p0, B p1, C p2, D p3, E p4, F p5, G p6, H p7, I p8, int id_contract, int id_method) impure method_id(10009) {
        ;; do nothing
    }

    forall A, B, C, D, E, F, G, H, I, J -> () tsa_call_0_10(A p0, B p1, C p2, D p3, E p4, F p5, G p6, H p7, I p8, J p9, int id_contract, int id_method) impure method_id(10010) {
        ;; do nothing
    }

    forall A -> (A) tsa_call_1_0(int id_contract, int id_method) impure method_id(10100) {
        return return_1();
    }

    forall A, B -> (A) tsa_call_1_1(B p0, int id_contract, int id_method) impure method_id(10101) {
        return return_1();
    }

    forall A, B, C -> (A) tsa_call_1_2(B p0, C p1, int id_contract, int id_method) impure method_id(10102) {
        return return_1();
    }

    forall A, B, C, D -> (A) tsa_call_1_3(B p0, C p1, D p2, int id_contract, int id_method) impure method_id(10103) {
        return return_1();
    }

    forall A, B, C, D, E -> (A) tsa_call_1_4(B p0, C p1, D p2, E p3, int id_contract, int id_method) impure method_id(10104) {
        return return_1();
    }

    forall A, B, C, D, E, F -> (A) tsa_call_1_5(B p0, C p1, D p2, E p3, F p4, int id_contract, int id_method) impure method_id(10105) {
        return return_1();
    }

    forall A, B, C, D, E, F, G -> (A) tsa_call_1_6(B p0, C p1, D p2, E p3, F p4, G p5, int id_contract, int id_method) impure method_id(10106) {
        return return_1();
    }

    forall A, B, C, D, E, F, G, H -> (A) tsa_call_1_7(B p0, C p1, D p2, E p3, F p4, G p5, H p6, int id_contract, int id_method) impure method_id(10107) {
        return return_1();
    }

    forall A, B, C, D, E, F, G, H, I -> (A) tsa_call_1_8(B p0, C p1, D p2, E p3, F p4, G p5, H p6, I p7, int id_contract, int id_method) impure method_id(10108) {
        return return_1();
    }

    forall A, B, C, D, E, F, G, H, I, J -> (A) tsa_call_1_9(B p0, C p1, D p2, E p3, F p4, G p5, H p6, I p7, J p8, int id_contract, int id_method) impure method_id(10109) {
        return return_1();
    }

    forall A, B, C, D, E, F, G, H, I, J, K -> (A) tsa_call_1_10(B p0, C p1, D p2, E p3, F p4, G p5, H p6, I p7, J p8, K p9, int id_contract, int id_method) impure method_id(10110) {
        return return_1();
    }

    forall A, B -> (A, B) tsa_call_2_0(int id_contract, int id_method) impure method_id(10200) {
        return return_2();
    }

    forall A, B, C -> (A, B) tsa_call_2_1(C p0, int id_contract, int id_method) impure method_id(10201) {
        return return_2();
    }

    forall A, B, C, D -> (A, B) tsa_call_2_2(C p0, D p1, int id_contract, int id_method) impure method_id(10202) {
        return return_2();
    }

    forall A, B, C, D, E -> (A, B) tsa_call_2_3(C p0, D p1, E p2, int id_contract, int id_method) impure method_id(10203) {
        return return_2();
    }

    forall A, B, C, D, E, F -> (A, B) tsa_call_2_4(C p0, D p1, E p2, F p3, int id_contract, int id_method) impure method_id(10204) {
        return return_2();
    }

    forall A, B, C, D, E, F, G -> (A, B) tsa_call_2_5(C p0, D p1, E p2, F p3, G p4, int id_contract, int id_method) impure method_id(10205) {
        return return_2();
    }

    forall A, B, C, D, E, F, G, H -> (A, B) tsa_call_2_6(C p0, D p1, E p2, F p3, G p4, H p5, int id_contract, int id_method) impure method_id(10206) {
        return return_2();
    }

    forall A, B, C, D, E, F, G, H, I -> (A, B) tsa_call_2_7(C p0, D p1, E p2, F p3, G p4, H p5, I p6, int id_contract, int id_method) impure method_id(10207) {
        return return_2();
    }

    forall A, B, C, D, E, F, G, H, I, J -> (A, B) tsa_call_2_8(C p0, D p1, E p2, F p3, G p4, H p5, I p6, J p7, int id_contract, int id_method) impure method_id(10208) {
        return return_2();
    }

    forall A, B, C, D, E, F, G, H, I, J, K -> (A, B) tsa_call_2_9(C p0, D p1, E p2, F p3, G p4, H p5, I p6, J p7, K p8, int id_contract, int id_method) impure method_id(10209) {
        return return_2();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L -> (A, B) tsa_call_2_10(C p0, D p1, E p2, F p3, G p4, H p5, I p6, J p7, K p8, L p9, int id_contract, int id_method) impure method_id(10210) {
        return return_2();
    }

    forall A, B, C -> (A, B, C) tsa_call_3_0(int id_contract, int id_method) impure method_id(10300) {
        return return_3();
    }

    forall A, B, C, D -> (A, B, C) tsa_call_3_1(D p0, int id_contract, int id_method) impure method_id(10301) {
        return return_3();
    }

    forall A, B, C, D, E -> (A, B, C) tsa_call_3_2(D p0, E p1, int id_contract, int id_method) impure method_id(10302) {
        return return_3();
    }

    forall A, B, C, D, E, F -> (A, B, C) tsa_call_3_3(D p0, E p1, F p2, int id_contract, int id_method) impure method_id(10303) {
        return return_3();
    }

    forall A, B, C, D, E, F, G -> (A, B, C) tsa_call_3_4(D p0, E p1, F p2, G p3, int id_contract, int id_method) impure method_id(10304) {
        return return_3();
    }

    forall A, B, C, D, E, F, G, H -> (A, B, C) tsa_call_3_5(D p0, E p1, F p2, G p3, H p4, int id_contract, int id_method) impure method_id(10305) {
        return return_3();
    }

    forall A, B, C, D, E, F, G, H, I -> (A, B, C) tsa_call_3_6(D p0, E p1, F p2, G p3, H p4, I p5, int id_contract, int id_method) impure method_id(10306) {
        return return_3();
    }

    forall A, B, C, D, E, F, G, H, I, J -> (A, B, C) tsa_call_3_7(D p0, E p1, F p2, G p3, H p4, I p5, J p6, int id_contract, int id_method) impure method_id(10307) {
        return return_3();
    }

    forall A, B, C, D, E, F, G, H, I, J, K -> (A, B, C) tsa_call_3_8(D p0, E p1, F p2, G p3, H p4, I p5, J p6, K p7, int id_contract, int id_method) impure method_id(10308) {
        return return_3();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L -> (A, B, C) tsa_call_3_9(D p0, E p1, F p2, G p3, H p4, I p5, J p6, K p7, L p8, int id_contract, int id_method) impure method_id(10309) {
        return return_3();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M -> (A, B, C) tsa_call_3_10(D p0, E p1, F p2, G p3, H p4, I p5, J p6, K p7, L p8, M p9, int id_contract, int id_method) impure method_id(10310) {
        return return_3();
    }

    forall A, B, C, D -> (A, B, C, D) tsa_call_4_0(int id_contract, int id_method) impure method_id(10400) {
        return return_4();
    }

    forall A, B, C, D, E -> (A, B, C, D) tsa_call_4_1(E p0, int id_contract, int id_method) impure method_id(10401) {
        return return_4();
    }

    forall A, B, C, D, E, F -> (A, B, C, D) tsa_call_4_2(E p0, F p1, int id_contract, int id_method) impure method_id(10402) {
        return return_4();
    }

    forall A, B, C, D, E, F, G -> (A, B, C, D) tsa_call_4_3(E p0, F p1, G p2, int id_contract, int id_method) impure method_id(10403) {
        return return_4();
    }

    forall A, B, C, D, E, F, G, H -> (A, B, C, D) tsa_call_4_4(E p0, F p1, G p2, H p3, int id_contract, int id_method) impure method_id(10404) {
        return return_4();
    }

    forall A, B, C, D, E, F, G, H, I -> (A, B, C, D) tsa_call_4_5(E p0, F p1, G p2, H p3, I p4, int id_contract, int id_method) impure method_id(10405) {
        return return_4();
    }

    forall A, B, C, D, E, F, G, H, I, J -> (A, B, C, D) tsa_call_4_6(E p0, F p1, G p2, H p3, I p4, J p5, int id_contract, int id_method) impure method_id(10406) {
        return return_4();
    }

    forall A, B, C, D, E, F, G, H, I, J, K -> (A, B, C, D) tsa_call_4_7(E p0, F p1, G p2, H p3, I p4, J p5, K p6, int id_contract, int id_method) impure method_id(10407) {
        return return_4();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L -> (A, B, C, D) tsa_call_4_8(E p0, F p1, G p2, H p3, I p4, J p5, K p6, L p7, int id_contract, int id_method) impure method_id(10408) {
        return return_4();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M -> (A, B, C, D) tsa_call_4_9(E p0, F p1, G p2, H p3, I p4, J p5, K p6, L p7, M p8, int id_contract, int id_method) impure method_id(10409) {
        return return_4();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N -> (A, B, C, D) tsa_call_4_10(E p0, F p1, G p2, H p3, I p4, J p5, K p6, L p7, M p8, N p9, int id_contract, int id_method) impure method_id(10410) {
        return return_4();
    }

    forall A, B, C, D, E -> (A, B, C, D, E) tsa_call_5_0(int id_contract, int id_method) impure method_id(10500) {
        return return_5();
    }

    forall A, B, C, D, E, F -> (A, B, C, D, E) tsa_call_5_1(F p0, int id_contract, int id_method) impure method_id(10501) {
        return return_5();
    }

    forall A, B, C, D, E, F, G -> (A, B, C, D, E) tsa_call_5_2(F p0, G p1, int id_contract, int id_method) impure method_id(10502) {
        return return_5();
    }

    forall A, B, C, D, E, F, G, H -> (A, B, C, D, E) tsa_call_5_3(F p0, G p1, H p2, int id_contract, int id_method) impure method_id(10503) {
        return return_5();
    }

    forall A, B, C, D, E, F, G, H, I -> (A, B, C, D, E) tsa_call_5_4(F p0, G p1, H p2, I p3, int id_contract, int id_method) impure method_id(10504) {
        return return_5();
    }

    forall A, B, C, D, E, F, G, H, I, J -> (A, B, C, D, E) tsa_call_5_5(F p0, G p1, H p2, I p3, J p4, int id_contract, int id_method) impure method_id(10505) {
        return return_5();
    }

    forall A, B, C, D, E, F, G, H, I, J, K -> (A, B, C, D, E) tsa_call_5_6(F p0, G p1, H p2, I p3, J p4, K p5, int id_contract, int id_method) impure method_id(10506) {
        return return_5();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L -> (A, B, C, D, E) tsa_call_5_7(F p0, G p1, H p2, I p3, J p4, K p5, L p6, int id_contract, int id_method) impure method_id(10507) {
        return return_5();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M -> (A, B, C, D, E) tsa_call_5_8(F p0, G p1, H p2, I p3, J p4, K p5, L p6, M p7, int id_contract, int id_method) impure method_id(10508) {
        return return_5();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N -> (A, B, C, D, E) tsa_call_5_9(F p0, G p1, H p2, I p3, J p4, K p5, L p6, M p7, N p8, int id_contract, int id_method) impure method_id(10509) {
        return return_5();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O -> (A, B, C, D, E) tsa_call_5_10(F p0, G p1, H p2, I p3, J p4, K p5, L p6, M p7, N p8, O p9, int id_contract, int id_method) impure method_id(10510) {
        return return_5();
    }

    forall A, B, C, D, E, F -> (A, B, C, D, E, F) tsa_call_6_0(int id_contract, int id_method) impure method_id(10600) {
        return return_6();
    }

    forall A, B, C, D, E, F, G -> (A, B, C, D, E, F) tsa_call_6_1(G p0, int id_contract, int id_method) impure method_id(10601) {
        return return_6();
    }

    forall A, B, C, D, E, F, G, H -> (A, B, C, D, E, F) tsa_call_6_2(G p0, H p1, int id_contract, int id_method) impure method_id(10602) {
        return return_6();
    }

    forall A, B, C, D, E, F, G, H, I -> (A, B, C, D, E, F) tsa_call_6_3(G p0, H p1, I p2, int id_contract, int id_method) impure method_id(10603) {
        return return_6();
    }

    forall A, B, C, D, E, F, G, H, I, J -> (A, B, C, D, E, F) tsa_call_6_4(G p0, H p1, I p2, J p3, int id_contract, int id_method) impure method_id(10604) {
        return return_6();
    }

    forall A, B, C, D, E, F, G, H, I, J, K -> (A, B, C, D, E, F) tsa_call_6_5(G p0, H p1, I p2, J p3, K p4, int id_contract, int id_method) impure method_id(10605) {
        return return_6();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L -> (A, B, C, D, E, F) tsa_call_6_6(G p0, H p1, I p2, J p3, K p4, L p5, int id_contract, int id_method) impure method_id(10606) {
        return return_6();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M -> (A, B, C, D, E, F) tsa_call_6_7(G p0, H p1, I p2, J p3, K p4, L p5, M p6, int id_contract, int id_method) impure method_id(10607) {
        return return_6();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N -> (A, B, C, D, E, F) tsa_call_6_8(G p0, H p1, I p2, J p3, K p4, L p5, M p6, N p7, int id_contract, int id_method) impure method_id(10608) {
        return return_6();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O -> (A, B, C, D, E, F) tsa_call_6_9(G p0, H p1, I p2, J p3, K p4, L p5, M p6, N p7, O p8, int id_contract, int id_method) impure method_id(10609) {
        return return_6();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P -> (A, B, C, D, E, F) tsa_call_6_10(G p0, H p1, I p2, J p3, K p4, L p5, M p6, N p7, O p8, P p9, int id_contract, int id_method) impure method_id(10610) {
        return return_6();
    }

    forall A, B, C, D, E, F, G -> (A, B, C, D, E, F, G) tsa_call_7_0(int id_contract, int id_method) impure method_id(10700) {
        return return_7();
    }

    forall A, B, C, D, E, F, G, H -> (A, B, C, D, E, F, G) tsa_call_7_1(H p0, int id_contract, int id_method) impure method_id(10701) {
        return return_7();
    }

    forall A, B, C, D, E, F, G, H, I -> (A, B, C, D, E, F, G) tsa_call_7_2(H p0, I p1, int id_contract, int id_method) impure method_id(10702) {
        return return_7();
    }

    forall A, B, C, D, E, F, G, H, I, J -> (A, B, C, D, E, F, G) tsa_call_7_3(H p0, I p1, J p2, int id_contract, int id_method) impure method_id(10703) {
        return return_7();
    }

    forall A, B, C, D, E, F, G, H, I, J, K -> (A, B, C, D, E, F, G) tsa_call_7_4(H p0, I p1, J p2, K p3, int id_contract, int id_method) impure method_id(10704) {
        return return_7();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L -> (A, B, C, D, E, F, G) tsa_call_7_5(H p0, I p1, J p2, K p3, L p4, int id_contract, int id_method) impure method_id(10705) {
        return return_7();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M -> (A, B, C, D, E, F, G) tsa_call_7_6(H p0, I p1, J p2, K p3, L p4, M p5, int id_contract, int id_method) impure method_id(10706) {
        return return_7();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N -> (A, B, C, D, E, F, G) tsa_call_7_7(H p0, I p1, J p2, K p3, L p4, M p5, N p6, int id_contract, int id_method) impure method_id(10707) {
        return return_7();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O -> (A, B, C, D, E, F, G) tsa_call_7_8(H p0, I p1, J p2, K p3, L p4, M p5, N p6, O p7, int id_contract, int id_method) impure method_id(10708) {
        return return_7();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P -> (A, B, C, D, E, F, G) tsa_call_7_9(H p0, I p1, J p2, K p3, L p4, M p5, N p6, O p7, P p8, int id_contract, int id_method) impure method_id(10709) {
        return return_7();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q -> (A, B, C, D, E, F, G) tsa_call_7_10(H p0, I p1, J p2, K p3, L p4, M p5, N p6, O p7, P p8, Q p9, int id_contract, int id_method) impure method_id(10710) {
        return return_7();
    }

    forall A, B, C, D, E, F, G, H -> (A, B, C, D, E, F, G, H) tsa_call_8_0(int id_contract, int id_method) impure method_id(10800) {
        return return_8();
    }

    forall A, B, C, D, E, F, G, H, I -> (A, B, C, D, E, F, G, H) tsa_call_8_1(I p0, int id_contract, int id_method) impure method_id(10801) {
        return return_8();
    }

    forall A, B, C, D, E, F, G, H, I, J -> (A, B, C, D, E, F, G, H) tsa_call_8_2(I p0, J p1, int id_contract, int id_method) impure method_id(10802) {
        return return_8();
    }

    forall A, B, C, D, E, F, G, H, I, J, K -> (A, B, C, D, E, F, G, H) tsa_call_8_3(I p0, J p1, K p2, int id_contract, int id_method) impure method_id(10803) {
        return return_8();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L -> (A, B, C, D, E, F, G, H) tsa_call_8_4(I p0, J p1, K p2, L p3, int id_contract, int id_method) impure method_id(10804) {
        return return_8();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M -> (A, B, C, D, E, F, G, H) tsa_call_8_5(I p0, J p1, K p2, L p3, M p4, int id_contract, int id_method) impure method_id(10805) {
        return return_8();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N -> (A, B, C, D, E, F, G, H) tsa_call_8_6(I p0, J p1, K p2, L p3, M p4, N p5, int id_contract, int id_method) impure method_id(10806) {
        return return_8();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O -> (A, B, C, D, E, F, G, H) tsa_call_8_7(I p0, J p1, K p2, L p3, M p4, N p5, O p6, int id_contract, int id_method) impure method_id(10807) {
        return return_8();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P -> (A, B, C, D, E, F, G, H) tsa_call_8_8(I p0, J p1, K p2, L p3, M p4, N p5, O p6, P p7, int id_contract, int id_method) impure method_id(10808) {
        return return_8();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q -> (A, B, C, D, E, F, G, H) tsa_call_8_9(I p0, J p1, K p2, L p3, M p4, N p5, O p6, P p7, Q p8, int id_contract, int id_method) impure method_id(10809) {
        return return_8();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R -> (A, B, C, D, E, F, G, H) tsa_call_8_10(I p0, J p1, K p2, L p3, M p4, N p5, O p6, P p7, Q p8, R p9, int id_contract, int id_method) impure method_id(10810) {
        return return_8();
    }

    forall A, B, C, D, E, F, G, H, I -> (A, B, C, D, E, F, G, H, I) tsa_call_9_0(int id_contract, int id_method) impure method_id(10900) {
        return return_9();
    }

    forall A, B, C, D, E, F, G, H, I, J -> (A, B, C, D, E, F, G, H, I) tsa_call_9_1(J p0, int id_contract, int id_method) impure method_id(10901) {
        return return_9();
    }

    forall A, B, C, D, E, F, G, H, I, J, K -> (A, B, C, D, E, F, G, H, I) tsa_call_9_2(J p0, K p1, int id_contract, int id_method) impure method_id(10902) {
        return return_9();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L -> (A, B, C, D, E, F, G, H, I) tsa_call_9_3(J p0, K p1, L p2, int id_contract, int id_method) impure method_id(10903) {
        return return_9();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M -> (A, B, C, D, E, F, G, H, I) tsa_call_9_4(J p0, K p1, L p2, M p3, int id_contract, int id_method) impure method_id(10904) {
        return return_9();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N -> (A, B, C, D, E, F, G, H, I) tsa_call_9_5(J p0, K p1, L p2, M p3, N p4, int id_contract, int id_method) impure method_id(10905) {
        return return_9();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O -> (A, B, C, D, E, F, G, H, I) tsa_call_9_6(J p0, K p1, L p2, M p3, N p4, O p5, int id_contract, int id_method) impure method_id(10906) {
        return return_9();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P -> (A, B, C, D, E, F, G, H, I) tsa_call_9_7(J p0, K p1, L p2, M p3, N p4, O p5, P p6, int id_contract, int id_method) impure method_id(10907) {
        return return_9();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q -> (A, B, C, D, E, F, G, H, I) tsa_call_9_8(J p0, K p1, L p2, M p3, N p4, O p5, P p6, Q p7, int id_contract, int id_method) impure method_id(10908) {
        return return_9();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R -> (A, B, C, D, E, F, G, H, I) tsa_call_9_9(J p0, K p1, L p2, M p3, N p4, O p5, P p6, Q p7, R p8, int id_contract, int id_method) impure method_id(10909) {
        return return_9();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S -> (A, B, C, D, E, F, G, H, I) tsa_call_9_10(J p0, K p1, L p2, M p3, N p4, O p5, P p6, Q p7, R p8, S p9, int id_contract, int id_method) impure method_id(10910) {
        return return_9();
    }

    forall A, B, C, D, E, F, G, H, I, J -> (A, B, C, D, E, F, G, H, I, J) tsa_call_10_0(int id_contract, int id_method) impure method_id(11000) {
        return return_10();
    }

    forall A, B, C, D, E, F, G, H, I, J, K -> (A, B, C, D, E, F, G, H, I, J) tsa_call_10_1(K p0, int id_contract, int id_method) impure method_id(11001) {
        return return_10();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L -> (A, B, C, D, E, F, G, H, I, J) tsa_call_10_2(K p0, L p1, int id_contract, int id_method) impure method_id(11002) {
        return return_10();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M -> (A, B, C, D, E, F, G, H, I, J) tsa_call_10_3(K p0, L p1, M p2, int id_contract, int id_method) impure method_id(11003) {
        return return_10();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N -> (A, B, C, D, E, F, G, H, I, J) tsa_call_10_4(K p0, L p1, M p2, N p3, int id_contract, int id_method) impure method_id(11004) {
        return return_10();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O -> (A, B, C, D, E, F, G, H, I, J) tsa_call_10_5(K p0, L p1, M p2, N p3, O p4, int id_contract, int id_method) impure method_id(11005) {
        return return_10();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P -> (A, B, C, D, E, F, G, H, I, J) tsa_call_10_6(K p0, L p1, M p2, N p3, O p4, P p5, int id_contract, int id_method) impure method_id(11006) {
        return return_10();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q -> (A, B, C, D, E, F, G, H, I, J) tsa_call_10_7(K p0, L p1, M p2, N p3, O p4, P p5, Q p6, int id_contract, int id_method) impure method_id(11007) {
        return return_10();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R -> (A, B, C, D, E, F, G, H, I, J) tsa_call_10_8(K p0, L p1, M p2, N p3, O p4, P p5, Q p6, R p7, int id_contract, int id_method) impure method_id(11008) {
        return return_10();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S -> (A, B, C, D, E, F, G, H, I, J) tsa_call_10_9(K p0, L p1, M p2, N p3, O p4, P p5, Q p6, R p7, S p8, int id_contract, int id_method) impure method_id(11009) {
        return return_10();
    }

    forall A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T -> (A, B, C, D, E, F, G, H, I, J) tsa_call_10_10(K p0, L p1, M p2, N p3, O p4, P p5, Q p6, R p7, S p8, T p9, int id_contract, int id_method) impure method_id(11010) {
        return return_10();
    }
""".trimIndent()
