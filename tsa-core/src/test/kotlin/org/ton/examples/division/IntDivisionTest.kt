package org.ton.examples.division

import org.junit.jupiter.api.Test
import org.ton.examples.compareActualAndExpectedMethodResults
import org.ton.examples.compareActualAndExpectedStack
import org.usvm.machine.compileAndAnalyzeFift
import org.usvm.machine.state.TvmIntegerOutOfRange
import org.usvm.machine.state.TvmIntegerOverflow
import java.math.BigInteger
import kotlin.io.path.Path

class IntDivisionTest {
    private val fiftPath = "/division/int_division_no_throw.fif"
    private val fiftPathOverflow = "/division/int_division_fail.fif"

    private val twoPow256 = BigInteger("115792089237316195423570985008687907853269984665640564039457584007913129639936")
    private val twoPow255 = BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819968")
    private val twoPow254 = BigInteger("28948022309329048855892746252171976963317496166410141009864396001978282409984")

    @Test
    fun testIntDivisionFiftNoThrow() {
        val fiftResourcePath = this::class.java.getResource(fiftPath)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $fiftPath")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val expectedMethodStackValues = mapOf(
            0 to listOf(-2),
            1 to listOf(-1),
            2 to listOf(-1),
            3 to listOf(-2),
            4 to listOf(0),
            5 to listOf(0),
            6 to listOf(0),
            7 to listOf(1),
            8 to listOf(2),
            9 to listOf(-2),
            10 to listOf(-1),
            11 to listOf(-1),
            12 to listOf(-1),
            13 to listOf(-1),
            14 to listOf(0),
            15 to listOf(-2),
            16 to listOf(1),
            17 to listOf(0),
            18 to listOf(1),
            19 to listOf(1),
            20 to listOf(-1),
            21 to listOf(-1),
            22 to listOf(-1),
            23 to listOf(0),
            24 to listOf(-2),
            25 to listOf(1),
            26 to listOf(0),
            27 to listOf(0),
            28 to listOf(1),
            29 to listOf(0),
            30 to listOf(1),
            31 to listOf(2, 3),
            32 to listOf(3, -2),
            33 to listOf(3, -2),
            34 to listOf(2, 2),
            35 to listOf(-2),
            36 to listOf(-2),
            37 to listOf(2),
            38 to listOf(2, 3),
            39 to listOf(3, -2),
            40 to listOf(3, -2),
            41 to listOf(2, 2),
            42 to listOf(2),
            43 to listOf(-6),
            44 to listOf(2),
            45 to listOf(2),
            46 to listOf(-6),
            47 to listOf(2),
            48 to listOf(twoPow255, 0),
            49 to listOf(-(twoPow254 + twoPow255), 0),
            50 to listOf(BigInteger("38597363079105398474523661669562635951089994888546854679819194669304376546645"), 1),
            51 to listOf(BigInteger("-46316835694926478169428394003475163141307993866256225615783033603165251855975"), 3),
            52 to listOf(twoPow255, 0),
            53 to listOf(-(twoPow254 + twoPow255), 0),
            54 to listOf(BigInteger("38597363079105398474523661669562635951089994888546854679819194669304376546646"), -2),
            55 to listOf(BigInteger("-46316835694926478169428394003475163141307993866256225615783033603165251855974"), -2),
            56 to listOf(twoPow255, 0),
            57 to listOf(-(twoPow254 + twoPow255), 0),
            58 to listOf(BigInteger("38597363079105398474523661669562635951089994888546854679819194669304376546645"), 1),
            59 to listOf(BigInteger("-46316835694926478169428394003475163141307993866256225615783033603165251855974"), -2),
            60 to listOf(BigInteger("10526553567028745038506453182607991623024544060512778549041598546173920876358"), -2),
            61 to listOf(BigInteger("-77194726158210796949047323339125271902179989777093709359638389338608753093291"), 1),
            62 to listOf(-twoPow256, 0),
            63 to listOf(0),
            64 to listOf(0),
            65 to listOf(0),
            66 to listOf(4),
            67 to listOf(-4),
            68 to listOf(5),
            69 to listOf(-1),
            70 to listOf(4),
            71 to listOf(-2),
            72 to listOf(2),
            73 to listOf(-1),
            74 to listOf(twoPow255),
            75 to listOf(-twoPow255),
            76 to listOf(-twoPow255),
            77 to listOf(twoPow255),
            78 to listOf(-twoPow255),
            79 to listOf(-twoPow255),
            80 to listOf(3),
            81 to listOf(2),
            82 to listOf(-4, 1),
            83 to listOf(-3, -1),
            84 to listOf(-3, -1),
            85 to listOf(1, 1),
            86 to listOf(-4, 1),
            87 to listOf(-3, -1),
            88 to listOf(-3, -1),
            89 to listOf(1, 1),
        )

        compareActualAndExpectedStack(expectedMethodStackValues, methodStates)
    }

    @Test
    fun testIntDivisionFiftFailure() {
        val fiftResourcePath = this::class.java.getResource(fiftPathOverflow)?.path?.let { Path(it) }
            ?: error("Cannot find resource fift $fiftPathOverflow")

        val methodStates = compileAndAnalyzeFift(fiftResourcePath)

        val expectedMethodResults = mapOf(
            0 to TvmIntegerOverflow,
            1 to TvmIntegerOverflow,
            2 to TvmIntegerOverflow,
            3 to TvmIntegerOverflow,
            4 to TvmIntegerOverflow,
            5 to TvmIntegerOverflow,
            6 to TvmIntegerOverflow,
            7 to TvmIntegerOverflow,
            8 to TvmIntegerOverflow,
            9 to TvmIntegerOutOfRange,
            10 to TvmIntegerOutOfRange,
            11 to TvmIntegerOutOfRange,
            12 to TvmIntegerOverflow,
            13 to TvmIntegerOverflow,
            14 to TvmIntegerOverflow,
            15 to TvmIntegerOverflow,
            16 to TvmIntegerOverflow,
            17 to TvmIntegerOverflow,
            18 to TvmIntegerOutOfRange,
            19 to TvmIntegerOutOfRange,
            20 to TvmIntegerOutOfRange,
            21 to TvmIntegerOutOfRange,
            22 to TvmIntegerOutOfRange,
            23 to TvmIntegerOutOfRange,
            24 to TvmIntegerOutOfRange,
            25 to TvmIntegerOutOfRange,
            26 to TvmIntegerOutOfRange,
            27 to TvmIntegerOutOfRange,
        )

        compareActualAndExpectedMethodResults(expectedMethodResults, methodStates)
    }
}