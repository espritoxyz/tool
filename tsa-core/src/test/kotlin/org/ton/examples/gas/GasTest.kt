package org.ton.examples.gas

import org.ton.examples.compileFiftCodeBlocksContract
import org.ton.examples.executionCode
import org.ton.examples.runFiftCodeBlock
import org.usvm.machine.analyzeAllMethods
import java.nio.file.FileVisitResult
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.visitFileTree
import kotlin.test.Test
import kotlin.test.assertEquals

class GasTest {

    @Test
    fun testGasUsage() {
        val (fiftFiles, fiftWorkDir) = findFiftTestFiles()
        val codeBlocks = fiftFiles.flatMap { fiftFunctions(it) }.distinct()

        val concreteResults = codeBlocks.map { runFiftCodeBlock(fiftWorkDir, it) }
        val contract = compileFiftCodeBlocksContract(fiftWorkDir, codeBlocks)

        val symbolicResult = analyzeAllMethods(contract)

        for ((methodId, tests) in symbolicResult) {
            val concreteResult = concreteResults.getOrNull(methodId) ?: continue
            val test = tests.single()

            assertEquals(concreteResult.exitCode, test.executionCode(), "Method: ${codeBlocks[methodId]}}")

            val symbolicGasUsage = test.gasUsage
            assertEquals(concreteResult.gasUsage, symbolicGasUsage, "Method: ${codeBlocks[methodId]}}")
        }
    }

    @OptIn(ExperimentalPathApi::class)
    private fun findFiftTestFiles(): Pair<List<Path>, Path> {
        val fiftStdLib = this::class.java.classLoader.getResource("fiftstdlib")?.path?.let { Path(it) }
        check(fiftStdLib != null && fiftStdLib.exists()) { "Resource root doesn't exists" }

        val resourceRoot = fiftStdLib.parent
        val fiftTestFiles = mutableListOf<Path>()
        resourceRoot.visitFileTree {
            onPreVisitDirectory { dir, _ ->
                when (dir.name) {
                    "fiftstdlib", "fift-examples", "demo" -> FileVisitResult.SKIP_SUBTREE
                    else -> FileVisitResult.CONTINUE
                }
            }
            onVisitFile { file, _ ->
                if (file.extension == "fif") {
                    fiftTestFiles.add(file)
                }
                FileVisitResult.CONTINUE
            }
        }

        return fiftTestFiles to fiftStdLib
    }

    private fun fiftFunctions(fiftFile: Path): List<String> {
        val fiftCode = fiftFile.readText()
        var blocks = fiftCode.split(fiftProcDeclPattern).drop(1) // remove header
        val lastBlock = blocks.lastOrNull()
        if (lastBlock != null) {
            blocks = blocks.dropLast(1) + lastBlock.trim().removeSuffix("}END>c")
        }

        return blocks
    }

    private val fiftProcDeclPattern = Regex("""\n.*?\s+PROC:""")
}
