package org.ton.examples.types

import org.ton.examples.funcCompileAndAnalyzeAllMethods
import org.ton.examples.propertiesFound
import org.usvm.machine.types.Endian
import org.usvm.test.resolver.TvmCellDataMaybeConstructorBit
import org.usvm.test.resolver.TvmCellDataInteger
import org.usvm.test.resolver.TvmCellDataTypeLoad
import org.usvm.test.resolver.TvmTestDataCellValue
import org.usvm.test.resolver.TvmTestDictCellValue
import org.usvm.test.resolver.TvmTestSliceValue
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class IteExample {
    private val path = "/types/ite_example.fc"

    @Test
    fun testIteExample() {
        val resourcePath = this::class.java.getResource(path)?.path?.let { Path(it) }
            ?: error("Cannot find resource $path")

        val result = funcCompileAndAnalyzeAllMethods(resourcePath)
        assertEquals(1, result.testSuites.size)
        val testSuite = result.testSuites.first()
        propertiesFound(
            testSuite,
            listOf(
                { test ->
                    val casted = (test.usedParameters.lastOrNull() as? TvmTestSliceValue)?.cell
                        ?: return@listOf false
                    var predicateResult = casted.knownTypes == listOf(
                        TvmCellDataTypeLoad(TvmCellDataMaybeConstructorBit, 0),
                        TvmCellDataTypeLoad(TvmCellDataInteger(8, true, Endian.BigEndian), 1)
                    )
                    // the input dict is empty => there is no dict ref
                    predicateResult = predicateResult && casted.data.firstOrNull() == '0'
                    predicateResult = predicateResult && casted.refs.size >= 2
                    if (predicateResult) {
                        // the only two refs are data cells, that have dicts is them
                        val firstIsDict =
                            (casted.refs[0] as? TvmTestDataCellValue)?.knownTypes?.firstOrNull()?.type == TvmCellDataMaybeConstructorBit
                        val secondIsDict =
                            (casted.refs[1] as? TvmTestDataCellValue)?.knownTypes?.firstOrNull()?.type == TvmCellDataMaybeConstructorBit
                        predicateResult = firstIsDict || secondIsDict
                    }
                    predicateResult
                },
                { test ->
                    val casted = (test.usedParameters.lastOrNull() as? TvmTestSliceValue)?.cell
                        ?: return@listOf false
                    var predicateResult = casted.knownTypes == listOf(
                        TvmCellDataTypeLoad(TvmCellDataMaybeConstructorBit, 0),
                        TvmCellDataTypeLoad(TvmCellDataInteger(8, true, Endian.BigEndian), 1)
                    )
                    // the input dict is not empty => the first ref is dict cell
                    predicateResult = predicateResult && casted.data.firstOrNull() == '1'
                    predicateResult = predicateResult && casted.refs.size >= 3
                    if (predicateResult) {
                        // the dict cell
                        predicateResult = casted.refs.first() is TvmTestDictCellValue
                        // other two refs are data cells, that have dicts is them
                        val firstIsDict =
                            (casted.refs[1] as? TvmTestDataCellValue)?.knownTypes?.firstOrNull()?.type == TvmCellDataMaybeConstructorBit
                        val secondIsDict =
                            (casted.refs[2] as? TvmTestDataCellValue)?.knownTypes?.firstOrNull()?.type == TvmCellDataMaybeConstructorBit
                        predicateResult = predicateResult && (firstIsDict || secondIsDict)
                    }
                    predicateResult
                }
            )
        )
    }
}