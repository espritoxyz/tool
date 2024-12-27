package org.ton.examples

import org.ton.disasm.TvmDisassembler
import org.ton.TvmInputInfo
import org.ton.TvmParameterInfo
import org.ton.bytecode.TvmContractCode
import org.ton.examples.types.InputParameterInfoTests
import org.ton.tlb.readFromJson
import org.usvm.machine.BocAnalyzer
import org.usvm.machine.FiftAnalyzer
import org.usvm.machine.FiftInterpreterResult
import org.usvm.machine.FuncAnalyzer
import org.usvm.machine.MethodId
import org.usvm.machine.TactAnalyzer
import org.usvm.machine.TvmOptions
import org.usvm.machine.intValue
import org.usvm.machine.state.TvmStack
import org.usvm.machine.types.TvmIntegerType
import org.usvm.test.resolver.TvmContractSymbolicTestResult
import org.usvm.test.resolver.TvmExecutionWithStructuralError
import org.usvm.test.resolver.TvmSymbolicTest
import org.usvm.test.resolver.TvmSymbolicTestSuite
import org.usvm.test.resolver.TvmTerminalMethodSymbolicResult
import org.usvm.test.resolver.TvmTestIntegerValue
import org.usvm.test.resolver.TvmTestNullValue
import org.usvm.test.resolver.TvmTestTupleValue
import org.usvm.test.resolver.TvmTestValue
import java.math.BigInteger
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val FUNC_STDLIB_PATH = "/imports"
private val FUNC_STDLIB_RESOURCE: Path = object {}.javaClass.getResource(FUNC_STDLIB_PATH)?.path?.let { Path(it) }
    ?: error("Cannot find func stdlib in $FUNC_STDLIB_PATH")

private const val FIFT_STDLIB_PATH = "/fiftstdlib"
private val FIFT_STDLIB_RESOURCE: Path = object {}.javaClass.getResource(FIFT_STDLIB_PATH)?.path?.let { Path(it) }
    ?: error("Cannot find fift stdlib in $FIFT_STDLIB_PATH")

// Options for tests with fift concrete execution
val testFiftOptions = TvmOptions(turnOnTLBParsingChecks = false, enableInternalArgsConstraints = false)

fun tactCompileAndAnalyzeAllMethods(
    tactSourcesPath: Path,
    contractDataHex: String? = null,
    methodsBlackList: Set<MethodId> = hashSetOf(),
    methodWhiteList: Set<MethodId>? = null,
    inputInfo: Map<MethodId, TvmInputInfo> = emptyMap(),
    tvmOptions: TvmOptions = TvmOptions(),
): TvmContractSymbolicTestResult = TactAnalyzer.analyzeAllMethods(
    tactSourcesPath,
    contractDataHex,
    methodsBlackList,
    methodWhiteList,
    inputInfo,
    tvmOptions,
)

fun funcCompileAndAnalyzeAllMethods(
    funcSourcesPath: Path,
    contractDataHex: String? = null,
    methodsBlackList: Set<MethodId> = hashSetOf(),
    methodWhiteList: Set<MethodId>? = null,
    inputInfo: Map<MethodId, TvmInputInfo> = emptyMap(),
    tvmOptions: TvmOptions = TvmOptions(),
): TvmContractSymbolicTestResult = FuncAnalyzer(
    funcStdlibPath = FUNC_STDLIB_RESOURCE,
    fiftStdlibPath = FIFT_STDLIB_RESOURCE,
).analyzeAllMethods(
    funcSourcesPath,
    contractDataHex,
    methodsBlackList,
    methodWhiteList,
    inputInfo,
    tvmOptions,
)

fun compileAndAnalyzeFift(
    fiftPath: Path,
    contractDataHex: String? = null,
    methodsBlackList: Set<MethodId> = hashSetOf(),
    methodWhiteList: Set<MethodId>? = null,
    inputInfo: Map<MethodId, TvmInputInfo> = emptyMap(),
    tvmOptions: TvmOptions = TvmOptions(),
): TvmContractSymbolicTestResult = FiftAnalyzer(fiftStdlibPath = FIFT_STDLIB_RESOURCE).analyzeAllMethods(
    fiftPath,
    contractDataHex,
    methodsBlackList,
    methodWhiteList,
    inputInfo,
    tvmOptions,
)

/**
 * [codeBlocks] -- blocks of FIFT instructions, surrounded with <{ ... }>
 * */
fun compileFiftCodeBlocksContract(
    fiftWorkDir: Path,
    codeBlocks: List<String>,
): TvmContractCode = FiftAnalyzer(
    fiftStdlibPath = FIFT_STDLIB_RESOURCE,
).compileFiftCodeBlocksContract(fiftWorkDir, codeBlocks)

fun compileFuncToFift(funcSourcesPath: Path, fiftFilePath: Path) =
    FuncAnalyzer(
        funcStdlibPath = FUNC_STDLIB_RESOURCE,
        fiftStdlibPath = FIFT_STDLIB_RESOURCE,
    ).compileFuncSourceToFift(funcSourcesPath, fiftFilePath)

fun analyzeAllMethods(
    bytecodePath: String,
    contractDataHex: String? = null,
    methodsBlackList: Set<MethodId> = hashSetOf(),
    methodWhiteList: Set<MethodId>? = null,
    inputInfo: Map<MethodId, TvmInputInfo> = emptyMap(),
): TvmContractSymbolicTestResult =
    BocAnalyzer.analyzeAllMethods(Path(bytecodePath), contractDataHex, methodsBlackList, methodWhiteList, inputInfo)

/**
 * Run method with [methodId].
 *
 * Note: the result Gas usage includes additional runvmx cost.
 * */
fun runFiftMethod(fiftPath: Path, methodId: Int): FiftInterpreterResult =
    FiftAnalyzer(
        fiftStdlibPath = FIFT_STDLIB_RESOURCE,
    ).runFiftMethod(fiftPath, methodId)

/**
 * [codeBlock] -- block of FIFT instructions, surrounded with <{ ... }>
 * */
fun runFiftCodeBlock(fiftWorkDir: Path, codeBlock: String): FiftInterpreterResult =
    FiftAnalyzer(
        fiftStdlibPath = FIFT_STDLIB_RESOURCE,
    ).runFiftCodeBlock(fiftWorkDir, codeBlock)

internal fun TvmStack.loadIntegers(n: Int) = List(n) {
    takeLast(TvmIntegerType) { error("Impossible") }.intValue?.intValue()
        ?: error("Unexpected entry type")
}.reversed()

internal fun TvmSymbolicTest.executionCode(): Int? =
    when (val casted = result) {
        is TvmTerminalMethodSymbolicResult -> casted.exitCode.toInt()
        is TvmExecutionWithStructuralError -> null  // execution interrupted
    }

internal fun compareSymbolicAndConcreteResults(
    methodIds: Set<Int>,
    symbolicResult: TvmContractSymbolicTestResult,
    expectedState: (Int) -> FiftInterpreterResult,
) = compareSymbolicAndConcreteResults(methodIds, symbolicResult, expectedState,
    symbolicStack = { symbolicTest -> symbolicTest.result.stack },
    concreteStackBlock = { fiftResult ->
        val result = mutableListOf<TvmTestValue>()
        parseFiftStack(fiftResult.stack, result, initialIndex = 0)
        result
    }
)

private fun parseFiftStack(entries: List<String>, result: MutableList<TvmTestValue>, initialIndex: Int): Int {
    var index = initialIndex
    while (index < entries.size) {
        when (entries[index]) {
            "[" -> {
                // tuple start
                val tupleElements = mutableListOf<TvmTestValue>()
                index = parseFiftStack(entries, tupleElements, index + 1)
                result += TvmTestTupleValue(tupleElements)
            }

            "]" -> {
                // tuple end
                return index + 1
            }

            "(null)" -> {
                result += TvmTestNullValue
                index++
            }

            else -> {
                val number = entries[index].toBigInteger()
                result += TvmTestIntegerValue(number)
                index++
            }
        }
    }

    return index
}

internal fun <T> compareSymbolicAndConcreteResults(
    methodIds: Set<Int>,
    symbolicResult: TvmContractSymbolicTestResult,
    expectedResult: (Int) -> FiftInterpreterResult,
    symbolicStack: (TvmSymbolicTest) -> List<T>,
    concreteStackBlock: (FiftInterpreterResult) -> List<T>,
) = compareMethodStates(methodIds, symbolicResult, expectedResult) { methodId, symbolicTest, concreteResult ->
    val actualStatus = symbolicTest.executionCode()
    assertEquals(concreteResult.exitCode, actualStatus, "Wrong exit code for method id: $methodId")

    val concreteStackValue = concreteStackBlock(concreteResult)
    val actualStack = symbolicStack(symbolicTest)
    assertEquals(concreteStackValue, actualStack, "Wrong stack for method id: $methodId")
}

internal fun compareMethodStates(
    methodIds: Set<Int>,
    symbolicResult: TvmContractSymbolicTestResult,
    expectedResult: (Int) -> FiftInterpreterResult,
    comparison: (Int, TvmSymbolicTest, FiftInterpreterResult) -> Unit
) {
    assertEquals(methodIds, symbolicResult.testSuites.mapTo(hashSetOf()) { it.methodId.toInt() })

    for ((method, _, tests) in symbolicResult.testSuites) {
        val test = tests.single()
        val methodId = method.toInt()
        val concreteResult = expectedResult(methodId)
        comparison(methodId, test, concreteResult)
    }
}

internal fun checkAtLeastOneStateForAllMethods(methodsNumber: Int, symbolicResult: TvmContractSymbolicTestResult) {
    assertEquals(methodsNumber, symbolicResult.size)
    assertTrue(symbolicResult.all { it.tests.isNotEmpty() })
}

internal fun propertiesFound(
    testSuite: TvmSymbolicTestSuite,
    properties: List<(TvmSymbolicTest) -> Boolean>
) {
    val failedProperties = mutableListOf<Int>()
    properties.forEachIndexed outer@{ index, property ->
        testSuite.tests.forEach { test ->
            if (property(test)) {
                return@outer
            }
        }
        failedProperties.add(index)
    }
    assertTrue(failedProperties.isEmpty(), "Properties $failedProperties were not found")
}

internal fun checkInvariants(
    testSuite: TvmSymbolicTestSuite,
    properties: List<(TvmSymbolicTest) -> Boolean>
) {
    val failedInvariants = mutableListOf<Int>()
    properties.forEachIndexed outer@{ index, property ->
        testSuite.tests.forEach { test ->
            if (!property(test)) {
                failedInvariants.add(index)
                return@outer
            }
        }
    }
    assertTrue(failedInvariants.isEmpty(), "Invariants $failedInvariants were violated")
}

internal fun extractTlbInfo(typesPath: String): Map<MethodId, TvmInputInfo> {
    val path = InputParameterInfoTests::class.java.getResource(typesPath)?.path
        ?: error("Cannot find resource bytecode $typesPath")
    val struct = readFromJson(Path(path), "InternalMsgBody")
        ?: error("Couldn't parse TL-B structure")
    val info = TvmParameterInfo.SliceInfo(
        TvmParameterInfo.DataCellInfo(
            struct
        )
    )
    return mapOf(BigInteger.ZERO to TvmInputInfo(mapOf(0 to info)))
}
