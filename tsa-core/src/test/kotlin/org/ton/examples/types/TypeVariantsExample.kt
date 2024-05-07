package org.ton.examples.types

import org.ton.examples.funcCompileAndAnalyzeAllMethods
import org.ton.examples.propertiesFound
import org.usvm.machine.types.Endian
import org.usvm.test.resolver.TvmCellDataBitArray
import org.usvm.test.resolver.TvmCellDataDictConstructorBit
import org.usvm.test.resolver.TvmCellDataInteger
import org.usvm.test.resolver.TvmCellDataTypeLoad
import org.usvm.test.resolver.TvmSymbolicTest
import org.usvm.test.resolver.TvmTestSliceValue
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class TypeVariantsExample {
    private val path = "/types/variants.fc"

    @Test
    fun testVariants() {
        val resourcePath = this::class.java.getResource(path)?.path?.let { Path(it) }
            ?: error("Cannot find resource $path")

        val result = funcCompileAndAnalyzeAllMethods(resourcePath)
        assertEquals(1, result.testSuites.size)
        val testSuite = result.testSuites.first()

        val expectedTypeSet1 = { bitArraySize: Int ->
            listOf(
                TvmCellDataTypeLoad(TvmCellDataInteger(8, true, Endian.BigEndian), 0),
                TvmCellDataTypeLoad(TvmCellDataBitArray(bitArraySize), 8)
            )
        }

        val expectedTypeSet2 = { intSize: Int ->
            listOf(
                TvmCellDataTypeLoad(TvmCellDataInteger(8, true, Endian.BigEndian), 0),
                TvmCellDataTypeLoad(TvmCellDataInteger(intSize, true, Endian.BigEndian), 8)
            )
        }

        propertiesFound(
            testSuite,
            listOf(
                generatePredicate1(listOf(TvmCellDataTypeLoad(TvmCellDataInteger(8, true, Endian.BigEndian), 0))),
                generatePredicate1(
                    listOf(
                        TvmCellDataTypeLoad(TvmCellDataInteger(8, true, Endian.BigEndian), 0),
                        TvmCellDataTypeLoad(TvmCellDataInteger(10, true, Endian.BigEndian), 8)
                    )
                ),
                generatePredicate1(
                    listOf(
                        TvmCellDataTypeLoad(TvmCellDataInteger(8, true, Endian.BigEndian), 0),
                        TvmCellDataTypeLoad(TvmCellDataDictConstructorBit, 8)
                    )
                ),
                generatePredicate1(
                    listOf(
                        TvmCellDataTypeLoad(TvmCellDataInteger(8, true, Endian.BigEndian), 0),
                        TvmCellDataTypeLoad(TvmCellDataBitArray(100), 8)
                    )
                ),
                generatePredicate(
                    listOf(
                        expectedTypeSet1(11),
                        expectedTypeSet1(12)
                    )
                ),
                generatePredicate(
                    listOf(
                        expectedTypeSet2(3),
                        expectedTypeSet2(4)
                    )
                )
            )
        )
    }

    private fun generatePredicate1(variant: List<TvmCellDataTypeLoad>): (TvmSymbolicTest) -> Boolean =
        generatePredicate(listOf(variant))

    private fun generatePredicate(variants: List<List<TvmCellDataTypeLoad>>): (TvmSymbolicTest) -> Boolean =
        result@{ test ->
            val casted = (test.usedParameters.last() as? TvmTestSliceValue)?.cell ?: return@result false
            variants.any { variant ->
                casted.knownTypes == variant
            }
        }
}