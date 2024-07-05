package org.ton.examples.types

import org.ton.TvmCoinsLabel
import org.ton.TvmDataCellStructure
import org.ton.TvmInputInfo
import org.ton.TvmIntegerLabel
import org.ton.TvmMaybeLabel
import org.ton.TvmMsgAddrLabel
import org.ton.TvmParameterInfo.DataCellInfo
import org.ton.TvmParameterInfo.SliceInfo
import org.ton.examples.checkInvariants
import org.ton.examples.funcCompileAndAnalyzeAllMethods
import org.ton.examples.propertiesFound
import org.usvm.machine.TvmMachineOptions
import org.usvm.test.resolver.TvmCellDataCoins
import org.usvm.test.resolver.TvmCellDataMaybeConstructorBit
import org.usvm.test.resolver.TvmExecutionWithReadingOfUnexpectedType
import org.usvm.test.resolver.TvmExecutionWithStructuralError
import org.usvm.test.resolver.TvmExecutionWithUnexpectedEndOfReading
import org.usvm.test.resolver.TvmExecutionWithUnexpectedReading
import org.usvm.test.resolver.TvmExecutionWithUnexpectedRefReading
import org.usvm.test.resolver.TvmMethodFailure
import org.usvm.test.resolver.TvmSuccessfulExecution
import java.math.BigInteger
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InputParameterInfoTests {
    private val maybePath = "/types/maybe.fc"
    private val endOfCellPath = "/types/end_of_cell.fc"
    private val simpleLoadRefPath = "/types/simple_load_ref.fc"
    private val coinsPath = "/types/load_coins.fc"
    private val msgAddrPath = "/types/load_msg_addr.fc"
    private val dictPath = "/types/dict.fc"

    @Test
    fun testCorrectMaybe() {
        val resourcePath = this::class.java.getResource(maybePath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $maybePath")

        val inputInfo =
            TvmInputInfo(mapOf(0 to SliceInfo(DataCellInfo(maybeStructure))))
        val results = funcCompileAndAnalyzeAllMethods(resourcePath, inputInfo = mapOf(BigInteger.ZERO to inputInfo))
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()
        assertTrue(tests.any { it.result is TvmSuccessfulExecution })
        assertTrue(tests.any { it.result is TvmMethodFailure })

        checkInvariants(
            tests,
            listOf { test ->
                test.result !is TvmExecutionWithStructuralError
            }
        )
    }

    @Test
    fun testMaybeInsteadOfInt() {
        val resourcePath = this::class.java.getResource(maybePath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $maybePath")

        val inputInfo =
            TvmInputInfo(mapOf(0 to SliceInfo(DataCellInfo(int64Structure))))
        val results = funcCompileAndAnalyzeAllMethods(resourcePath, inputInfo = mapOf(BigInteger.ZERO to inputInfo))
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()
        assertTrue(tests.all { it.result !is TvmSuccessfulExecution })
        assertTrue(tests.any { it.result is TvmMethodFailure })

        propertiesFound(
            tests,
            listOf { test ->
                val exit = test.result as? TvmExecutionWithReadingOfUnexpectedType ?: return@listOf false
                exit.actualType is TvmCellDataMaybeConstructorBit && exit.labelType is TvmIntegerLabel
            }
        )
    }

    @Test
    fun testUnexpectedRead() {
        val resourcePath = this::class.java.getResource(maybePath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $maybePath")

        val inputInfo =
            TvmInputInfo(mapOf(0 to SliceInfo(DataCellInfo(TvmDataCellStructure.Empty))))
        val results = funcCompileAndAnalyzeAllMethods(resourcePath, inputInfo = mapOf(BigInteger.ZERO to inputInfo))
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()
        assertTrue(tests.all { it.result !is TvmSuccessfulExecution })
        assertTrue(tests.any { it.result is TvmMethodFailure })

        propertiesFound(
            tests,
            listOf { test ->
                val exit = test.result as? TvmExecutionWithUnexpectedReading ?: return@listOf false
                exit.readingType is TvmCellDataMaybeConstructorBit
            }
        )
    }

    @Test
    fun testTurnOff() {
        val resourcePath = this::class.java.getResource(maybePath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $maybePath")

        val inputInfo =
            TvmInputInfo(mapOf(0 to SliceInfo(DataCellInfo(TvmDataCellStructure.Empty))))
        val options = TvmMachineOptions(checkDataCellContentTypes = false)
        val results = funcCompileAndAnalyzeAllMethods(
            resourcePath,
            inputInfo = mapOf(BigInteger.ZERO to inputInfo),
            tvmOptions = options
        )
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()
        assertTrue(tests.any { it.result is TvmSuccessfulExecution })
        assertTrue(tests.any { it.result is TvmMethodFailure })
    }

    @Test
    fun testExpectedEndOfCell() {
        val resourcePath = this::class.java.getResource(endOfCellPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $endOfCellPath")

        val inputInfo =
            TvmInputInfo(mapOf(0 to SliceInfo(DataCellInfo(TvmDataCellStructure.Empty))))
        val results = funcCompileAndAnalyzeAllMethods(resourcePath, inputInfo = mapOf(BigInteger.ZERO to inputInfo))
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()
        assertTrue(tests.any { it.result is TvmSuccessfulExecution })
        assertTrue(tests.any { it.result is TvmMethodFailure })
    }

    @Test
    fun testUnexpectedEndOfCell() {
        val resourcePath = this::class.java.getResource(endOfCellPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $endOfCellPath")

        val inputInfo =
            TvmInputInfo(mapOf(0 to SliceInfo(DataCellInfo(int64Structure))))
        val results = funcCompileAndAnalyzeAllMethods(resourcePath, inputInfo = mapOf(BigInteger.ZERO to inputInfo))
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()
        assertTrue(tests.any { it.result !is TvmSuccessfulExecution })

        propertiesFound(
            tests,
            listOf { test ->
                test.result is TvmExecutionWithUnexpectedEndOfReading
            }
        )
    }

    @Test
    fun testUnexpectedEndOfCell2() {
        val resourcePath = this::class.java.getResource(endOfCellPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $endOfCellPath")

        val inputInfo =
            TvmInputInfo(mapOf(0 to SliceInfo(DataCellInfo(someRefStructure))))
        val results = funcCompileAndAnalyzeAllMethods(resourcePath, inputInfo = mapOf(BigInteger.ZERO to inputInfo))
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()
        assertTrue(tests.any { it.result !is TvmSuccessfulExecution })

        propertiesFound(
            tests,
            listOf { test ->
                test.result is TvmExecutionWithUnexpectedEndOfReading
            }
        )
    }

    @Test
    fun testExpectedLoadRef() {
        val resourcePath = this::class.java.getResource(simpleLoadRefPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $simpleLoadRefPath")

        val inputInfo =
            TvmInputInfo(mapOf(0 to SliceInfo(DataCellInfo(someRefStructure))))
        val results = funcCompileAndAnalyzeAllMethods(resourcePath, inputInfo = mapOf(BigInteger.ZERO to inputInfo))
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()
        assertTrue(tests.any { it.result is TvmSuccessfulExecution })
        assertTrue(tests.any { it.result is TvmMethodFailure })
    }

    @Test
    fun testPossibleLoadRef() {
        val resourcePath = this::class.java.getResource(simpleLoadRefPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $simpleLoadRefPath")

        val inputInfo =
            TvmInputInfo(mapOf(0 to SliceInfo(DataCellInfo(prefixInt64Structure))))
        val results = funcCompileAndAnalyzeAllMethods(resourcePath, inputInfo = mapOf(BigInteger.ZERO to inputInfo))
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()
        assertTrue(tests.any { it.result is TvmSuccessfulExecution })
        assertTrue(tests.any { it.result is TvmMethodFailure })
    }

    @Test
    fun testUnexpectedLoadRef() {
        val resourcePath = this::class.java.getResource(simpleLoadRefPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $simpleLoadRefPath")

        val inputInfo =
            TvmInputInfo(mapOf(0 to SliceInfo(DataCellInfo(int64Structure))))
        val results = funcCompileAndAnalyzeAllMethods(resourcePath, inputInfo = mapOf(BigInteger.ZERO to inputInfo))
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()

        propertiesFound(
            tests,
            listOf { test ->
                test.result is TvmExecutionWithUnexpectedRefReading
            }
        )
    }

    @Test
    fun testMaybeInsteadOfCoins() {
        val resourcePath = this::class.java.getResource(maybePath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $maybePath")

        val inputInfo =
            TvmInputInfo(mapOf(0 to SliceInfo(DataCellInfo(coinsStructure))))
        val results = funcCompileAndAnalyzeAllMethods(resourcePath, inputInfo = mapOf(BigInteger.ZERO to inputInfo))
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()
        assertTrue(tests.all { it.result !is TvmSuccessfulExecution })
        assertTrue(tests.any { it.result is TvmMethodFailure })

        propertiesFound(
            tests,
            listOf { test ->
                val exit = test.result as? TvmExecutionWithReadingOfUnexpectedType ?: return@listOf false
                exit.actualType is TvmCellDataMaybeConstructorBit && exit.labelType is TvmCoinsLabel
            }
        )
    }

    @Test
    fun testCorrectCoins() {
        val resourcePath = this::class.java.getResource(coinsPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $coinsPath")

        val inputInfo =
            TvmInputInfo(mapOf(0 to SliceInfo(DataCellInfo(coinsStructure))))
        val results = funcCompileAndAnalyzeAllMethods(resourcePath, inputInfo = mapOf(BigInteger.ZERO to inputInfo))
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()
        assertTrue(tests.any { it.result is TvmSuccessfulExecution })
        assertTrue(tests.any { it.result is TvmMethodFailure })

        checkInvariants(
            tests,
            listOf { test ->
                test.result !is TvmExecutionWithStructuralError
            }
        )
    }

    @Test
    fun testCoinsInsteadOfMsgAddr() {
        val resourcePath = this::class.java.getResource(coinsPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $coinsPath")

        val inputInfo =
            TvmInputInfo(mapOf(0 to SliceInfo(DataCellInfo(msgStructure))))
        val results = funcCompileAndAnalyzeAllMethods(resourcePath, inputInfo = mapOf(BigInteger.ZERO to inputInfo))
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()
        assertTrue(tests.any { it.result !is TvmSuccessfulExecution })
        assertTrue(tests.any { it.result is TvmMethodFailure })

        propertiesFound(
            tests,
            listOf { test ->
                val exit = test.result as? TvmExecutionWithReadingOfUnexpectedType ?: return@listOf false
                exit.actualType is TvmCellDataCoins && exit.labelType is TvmMsgAddrLabel
            }
        )
    }

    @Test
    fun testCorrectMsgAddr() {
        val resourcePath = this::class.java.getResource(msgAddrPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $msgAddrPath")

        val inputInfo =
            TvmInputInfo(mapOf(0 to SliceInfo(DataCellInfo(msgStructure))))
        val results = funcCompileAndAnalyzeAllMethods(resourcePath, inputInfo = mapOf(BigInteger.ZERO to inputInfo))
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()
        assertTrue(tests.any { it.result is TvmSuccessfulExecution })
        assertTrue(tests.any { it.result is TvmMethodFailure })

        checkInvariants(
            tests,
            listOf { test ->
                test.result !is TvmExecutionWithStructuralError
            }
        )
    }

    @Test
    fun testCorrectDict() {
        val resourcePath = this::class.java.getResource(dictPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $dictPath")

        val inputInfo =
            TvmInputInfo(mapOf(0 to SliceInfo(DataCellInfo(dict256Structure))))
        val results = funcCompileAndAnalyzeAllMethods(resourcePath, inputInfo = mapOf(BigInteger.ZERO to inputInfo))
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()
        assertTrue(tests.any { it.result is TvmSuccessfulExecution })
        assertTrue(tests.any { it.result is TvmMethodFailure })

        checkInvariants(
            tests,
            listOf { test ->
                test.result !is TvmExecutionWithStructuralError
            }
        )
    }

    @Test
    fun testCoinsInsteadOfDict() {
        val resourcePath = this::class.java.getResource(coinsPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $coinsPath")

        val inputInfo =
            TvmInputInfo(mapOf(0 to SliceInfo(DataCellInfo(dict256Structure))))
        val results = funcCompileAndAnalyzeAllMethods(resourcePath, inputInfo = mapOf(BigInteger.ZERO to inputInfo))
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()
        assertTrue(tests.any { it.result !is TvmSuccessfulExecution })
        assertTrue(tests.any { it.result is TvmMethodFailure })

        propertiesFound(
            tests,
            listOf { test ->
                val exit = test.result as? TvmExecutionWithReadingOfUnexpectedType ?: return@listOf false
                exit.actualType is TvmCellDataCoins && exit.labelType is TvmMaybeLabel
            }
        )
    }
}