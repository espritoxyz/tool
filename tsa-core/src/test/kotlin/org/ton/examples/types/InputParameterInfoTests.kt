package org.ton.examples.types

import org.ton.TvmInputInfo
import org.ton.TvmParameterInfo
import org.ton.examples.checkInvariants
import org.ton.examples.funcCompileAndAnalyzeAllMethods
import org.ton.examples.propertiesFound
import org.usvm.test.resolver.TvmExecutionWithDataCellTypesError
import org.usvm.test.resolver.TvmMethodFailure
import org.usvm.test.resolver.TvmSuccessfulExecution
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InputParameterInfoTests {
    private val maybePath = "/types/maybe.fc"

    @Test
    fun testCorrectMaybe() {
        val resourcePath = this::class.java.getResource(maybePath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $maybePath")

        val inputInfo = TvmInputInfo(mapOf(0 to TvmParameterInfo.SliceInfo(TvmParameterInfo.DataCellInfo(maybeStructure))))
        val results = funcCompileAndAnalyzeAllMethods(resourcePath, inputInfo = inputInfo)
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()
        assertTrue(tests.any { it.result is TvmSuccessfulExecution })
        assertTrue(tests.any { it.result is TvmMethodFailure })

        checkInvariants(
            tests,
            listOf { test ->
                test.result !is TvmExecutionWithDataCellTypesError
            }
        )
    }

    @Test
    fun testMaybeInsteadOfInt() {
        val resourcePath = this::class.java.getResource(maybePath)?.path?.let { Path(it) }
            ?: error("Cannot find resource $maybePath")

        val inputInfo = TvmInputInfo(mapOf(0 to TvmParameterInfo.SliceInfo(TvmParameterInfo.DataCellInfo(int64Structure))))
        val results = funcCompileAndAnalyzeAllMethods(resourcePath, inputInfo = inputInfo)
        assertEquals(1, results.testSuites.size)
        val tests = results.testSuites.first()
        assertTrue(tests.any { it.result is TvmSuccessfulExecution })
        assertTrue(tests.any { it.result is TvmMethodFailure })

        propertiesFound(
            tests,
            listOf { test ->
                test.result is TvmExecutionWithDataCellTypesError
            }
        )
    }
}