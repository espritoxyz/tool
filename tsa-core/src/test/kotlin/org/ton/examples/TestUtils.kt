package org.ton.examples

import org.ton.bytecode.TvmContractCode
import org.ton.bytecode.TvmIntegerType
import org.usvm.machine.BocAnalyzer
import org.usvm.machine.FiftAnalyzer
import org.usvm.machine.FiftInterpreterResult
import org.usvm.machine.FuncAnalyzer
import org.usvm.machine.TactAnalyzer
import org.usvm.machine.intValue
import org.usvm.machine.state.TvmStack
import org.usvm.test.resolver.TvmContractSymbolicTestResult
import org.usvm.test.resolver.TvmMethodFailure
import org.usvm.test.resolver.TvmSuccessfulExecution
import org.usvm.test.resolver.TvmSymbolicTest
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
): TvmContractSymbolicTestResult = TactAnalyzer.analyzeAllMethods(
    tactSourcesPath,
    contractDataHex,
    methodsBlackList
)

fun funcCompileAndAnalyzeAllMethods(
    funcSourcesPath: Path,
    contractDataHex: String? = null,
    methodsBlackList: Set<Int> = hashSetOf(Int.MAX_VALUE),
): TvmContractSymbolicTestResult = FuncAnalyzer(funcStdlibPath = FUNC_STDLIB_RESOURCE, fiftStdlibPath = FIFT_STDLIB_RESOURCE).analyzeAllMethods(
    funcSourcesPath,
    contractDataHex,
    methodsBlackList
)

fun compileAndAnalyzeFift(
    fiftPath: Path,
    contractDataHex: String? = null,
    methodsBlackList: Set<Int> = hashSetOf(Int.MAX_VALUE),
): TvmContractSymbolicTestResult = FiftAnalyzer(fiftStdlibPath = FIFT_STDLIB_RESOURCE).analyzeAllMethods(
    fiftPath,
    contractDataHex,
    methodsBlackList,
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
): TvmContractSymbolicTestResult = BocAnalyzer.analyzeAllMethods(Path(bytecodePath), contractDataHex, methodsBlackList)

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

internal fun TvmSymbolicTest.executionCode(): Int = when (val it = result) {
    is TvmMethodFailure -> it.failure.exitCode.toInt()
    is TvmSuccessfulExecution -> 0
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