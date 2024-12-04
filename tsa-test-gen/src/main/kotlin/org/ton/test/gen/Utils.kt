package org.ton.test.gen

import java.math.BigInteger
import java.nio.file.Files.createDirectories
import java.nio.file.Path
import org.ton.test.gen.dsl.render.TsRenderedTest
import org.ton.test.gen.dsl.render.TsRenderer

internal const val wrappersDirName: String = "wrappers"
internal const val testsDirName: String = "tests"

fun String.binaryToHex(): String = BigInteger(this, 2).toString(16)

fun String.binaryToSignedDecimal(): String {
    val binaryString = this

    val signBit = binaryString.first().digitToInt()
    val sign = BigInteger.valueOf(signBit.toLong()).shiftLeft(length - 1)
    val resultBigInteger = BigInteger("0" + binaryString.drop(1), 2) - sign

    return resultBigInteger.toString(10)
}

fun writeRenderedTest(projectPath: Path, test: TsRenderedTest) {
    val wrapperFolder = projectPath.resolve(TsRenderer.WRAPPERS_DIR_NAME)
    val testsFolder = projectPath.resolve(TsRenderer.TESTS_DIR_NAME)

    createDirectories(wrapperFolder)
    createDirectories(testsFolder)

    val testsFile = testsFolder.resolve(test.fileName).toFile()
    testsFile.writeText(test.code)

    test.wrappers.forEach { (fileName, code) ->
        val wrapperFile = wrapperFolder.resolve(fileName).toFile()
        wrapperFile.writeText(code)
    }
}
