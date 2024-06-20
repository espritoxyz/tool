package org.ton.examples

import org.ton.TvmInputInfo
import org.ton.bytecode.TvmContractCode
import org.usvm.machine.types.TvmIntegerType
import org.usvm.machine.BocAnalyzer
import org.usvm.machine.FiftAnalyzer
import org.usvm.machine.FiftInterpreterResult
import org.usvm.machine.FuncAnalyzer
import org.usvm.machine.TactAnalyzer
import org.usvm.machine.intValue
import org.usvm.machine.state.TvmStack
import org.usvm.test.resolver.TvmContractSymbolicTestResult
import org.usvm.test.resolver.TvmExecutionWithStructuralError
import org.usvm.test.resolver.TvmMethodFailure
import org.usvm.test.resolver.TvmSuccessfulExecution
import org.usvm.test.resolver.TvmSymbolicTest
import org.usvm.test.resolver.TvmSymbolicTestSuite
import org.usvm.test.resolver.TvmTestIntegerValue
import org.usvm.test.resolver.TvmTestNullValue
import org.usvm.test.resolver.TvmTestTupleValue
import org.usvm.test.resolver.TvmTestValue
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

fun tactCompileAndAnalyzeAllMethods(
    tactSourcesPath: Path,
    contractDataHex: String? = null,
    methodsBlackList: Set<Int> = hashSetOf(Int.MAX_VALUE),
    inputInfo: TvmInputInfo = TvmInputInfo(),
): TvmContractSymbolicTestResult = TactAnalyzer.analyzeAllMethods(
    tactSourcesPath,
    contractDataHex,
    methodsBlackList,
    inputInfo,
)

fun funcCompileAndAnalyzeAllMethods(
    funcSourcesPath: Path,
    contractDataHex: String? = null,
    methodsBlackList: Set<Int> = hashSetOf(Int.MAX_VALUE),
    inputInfo: TvmInputInfo = TvmInputInfo(),
): TvmContractSymbolicTestResult = FuncAnalyzer(funcStdlibPath = FUNC_STDLIB_RESOURCE, fiftStdlibPath = FIFT_STDLIB_RESOURCE).analyzeAllMethods(
    funcSourcesPath,
    contractDataHex,
    methodsBlackList,
    inputInfo,
)

fun compileAndAnalyzeFift(
    fiftPath: Path,
    contractDataHex: String? = null,
    methodsBlackList: Set<Int> = hashSetOf(Int.MAX_VALUE),
    inputInfo: TvmInputInfo = TvmInputInfo(),
): TvmContractSymbolicTestResult = FiftAnalyzer(fiftStdlibPath = FIFT_STDLIB_RESOURCE).analyzeAllMethods(
    fiftPath,
    contractDataHex,
    methodsBlackList,
    inputInfo,
)

/**
 * [codeBlocks] -- blocks of FIFT instructions, surrounded with <{ ... }>
 * */
fun compileFiftCodeBlocksContract(
    fiftWorkDir: Path,
    codeBlocks: List<String>,
): TvmContractCode = FiftAnalyzer(fiftStdlibPath = FIFT_STDLIB_RESOURCE).compileFiftCodeBlocksContract(fiftWorkDir, codeBlocks)

fun analyzeAllMethods(
    bytecodePath: String,
    contractDataHex: String? = null,
    methodsBlackList: Set<Int> = hashSetOf(Int.MAX_VALUE),
    inputInfo: TvmInputInfo = TvmInputInfo(),
): TvmContractSymbolicTestResult =
    BocAnalyzer.analyzeAllMethods(Path(bytecodePath), contractDataHex, methodsBlackList, inputInfo)

/**
 * Run method with [methodId].
 *
 * Note: the result Gas usage includes additional runvmx cost.
 * */
fun runFiftMethod(fiftPath: Path, methodId: Int): FiftInterpreterResult =
    FiftAnalyzer(fiftStdlibPath = FIFT_STDLIB_RESOURCE).runFiftMethod(fiftPath, methodId)

/**
 * [codeBlock] -- block of FIFT instructions, surrounded with <{ ... }>
 * */
fun runFiftCodeBlock(fiftWorkDir: Path, codeBlock: String): FiftInterpreterResult =
    FiftAnalyzer(fiftStdlibPath = FIFT_STDLIB_RESOURCE).runFiftCodeBlock(fiftWorkDir, codeBlock)

internal fun TvmStack.loadIntegers(n: Int) = List(n) {
    takeLast(TvmIntegerType) { error("Impossible") }.intValue.intValue()
}.reversed()

internal fun TvmSymbolicTest.executionCode(): Int? = result.exitCode?.toInt()

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

    for ((method, tests) in symbolicResult.testSuites) {
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

internal const val runHardTestsVar = "TSA_RUN_HARD_TESTS"
internal const val runHardTestsRegex = ".+"

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
