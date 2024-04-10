package org.ton.examples

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.BvUtils.toBigIntegerSigned
import org.ton.bytecode.TvmContractCode
import org.ton.bytecode.TvmIntegerType
import org.ton.bytecode.TvmMethod
import org.usvm.UBvSort
import org.usvm.UExpr
import org.usvm.machine.BocAnalyzer
import org.usvm.machine.FiftAnalyzer
import org.usvm.machine.FiftInterpreterResult
import org.usvm.machine.FuncAnalyzer
import org.usvm.machine.state.TvmMethodResult
import org.usvm.machine.state.TvmStack
import org.usvm.machine.state.TvmState
import org.usvm.test.TvmTestIntegerValue
import org.usvm.test.TvmTestNullValue
import org.usvm.test.TvmTestStateResolver
import org.usvm.test.TvmTestTupleValue
import org.usvm.test.TvmTestValue
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.assertEquals

private const val FUNC_STDLIB_PATH = "/imports"
private val FUNC_STDLIB_RESOURCE: Path = object {}.javaClass.getResource(FUNC_STDLIB_PATH)?.path?.let { Path(it) }
    ?: error("Cannot find func stdlib in $FUNC_STDLIB_PATH")

private const val FIFT_STDLIB_PATH = "/fiftstdlib"
private val FIFT_STDLIB_RESOURCE: Path = object {}.javaClass.getResource(FIFT_STDLIB_PATH)?.path?.let { Path(it) }
    ?: error("Cannot find fift stdlib in $FIFT_STDLIB_PATH")

fun compileAndAnalyzeAllMethods(
    funcSourcesPath: Path,
    contractDataHex: String? = null
): Map<TvmMethod, List<TvmState>> = FuncAnalyzer(funcStdlibPath = FUNC_STDLIB_RESOURCE, fiftStdlibPath = FIFT_STDLIB_RESOURCE).analyzeAllMethods(funcSourcesPath, contractDataHex)

fun compileAndAnalyzeFift(
    fiftPath: Path,
    contractDataHex: String? = null
): Map<TvmMethod, List<TvmState>> = FiftAnalyzer(fiftStdlibPath = FIFT_STDLIB_RESOURCE).analyzeAllMethods(fiftPath, contractDataHex)

/**
 * [codeBlocks] -- blocks of FIFT instructions, surrounded with <{ ... }>
 * */
fun compileFiftCodeBlocksContract(
    fiftWorkDir: Path,
    codeBlocks: List<String>,
): TvmContractCode = FiftAnalyzer(fiftStdlibPath = FIFT_STDLIB_RESOURCE).compileFiftCodeBlocksContract(fiftWorkDir, codeBlocks)

fun analyzeAllMethods(bytecodePath: String, contractDataHex: String? = null): Map<TvmMethod, List<TvmState>> =
    BocAnalyzer.analyzeAllMethods(Path(bytecodePath), contractDataHex)

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

internal fun TvmState.executionCode(): Int = when (val res = methodResult) {
    TvmMethodResult.NoCall -> error("Unexpected method result: $res")
    is TvmMethodResult.TvmFailure -> res.exitCode.toInt()
    is TvmMethodResult.TvmSuccess -> 0
}

internal fun TvmState.gasUsageValue(): Int {
    val model = models.first()
    return gasUsage.sumOf { model.eval(it).intValue() }
}

internal fun UExpr<out UBvSort>.intValue() = (this as KBitVecValue<*>).toBigIntegerSigned().toInt()

internal fun compareMethodStateStack(
    methodIds: Set<Int>,
    methodStates: Map<TvmMethod, List<TvmState>>,
    expectedState: (TvmMethod) -> FiftInterpreterResult,
) = compareMethodStateStack(methodIds, methodStates, expectedState,
    stateStack = { state, expectedStackSize ->
        val resolver = TvmTestStateResolver(state.ctx, state.models.first(), state)
        List(expectedStackSize) { resolver.resolveEntry(state.stack.takeLastEntry()) }.reversed()
    },
    concreteStack = { fiftResult ->
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

internal fun <T> compareMethodStateStack(
    methodIds: Set<Int>,
    methodStates: Map<TvmMethod, List<TvmState>>,
    expectedState: (TvmMethod) -> FiftInterpreterResult,
    stateStack: (TvmState, Int) -> List<T>,
    concreteStack: (FiftInterpreterResult) -> List<T>,
) = compareMethodStates(methodIds, methodStates, expectedState) { method, actualState, concreteState ->
    val actualStatus = actualState.executionCode()
    assertEquals(concreteState.exitCode, actualStatus, "Wrong exit code for method id: ${method.id}")

    val concreteStackValue = concreteStack(concreteState)
    val actualStack = stateStack(actualState, concreteStackValue.size)
    assertEquals(concreteStackValue, actualStack, "Wrong stack for method id: ${method.id}")
}

internal fun compareMethodStates(
    methodIds: Set<Int>,
    methodStates: Map<TvmMethod, List<TvmState>>,
    expectedState: (TvmMethod) -> FiftInterpreterResult,
    comparison: (TvmMethod, TvmState, FiftInterpreterResult) -> Unit
) {
    assertEquals(methodIds, methodStates.keys.mapTo(hashSetOf()) { it.id })

    for ((method, states) in methodStates) {
        val state = states.single()
        val concreteState = expectedState(method)
        comparison(method, state, concreteState)
    }
}
