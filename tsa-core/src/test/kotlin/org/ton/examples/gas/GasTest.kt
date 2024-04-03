package org.ton.examples.gas

import org.ton.examples.executionCode
import org.ton.examples.gasUsageValue
import org.usvm.machine.analyzeAllMethods
import org.usvm.machine.compileFiftCodeBlocksContract
import org.usvm.machine.runFiftCodeBlock
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

        val methodStates = analyzeAllMethods(contract)

        for ((method, states) in methodStates) {
            val concreteResult = concreteResults.getOrNull(method.id) ?: continue
            val state = states.single()

            assertEquals(concreteResult.exitCode, state.executionCode(), "Method: ${codeBlocks[method.id]}}")

            val stateGasUsage = state.gasUsageValue()
            assertEquals(concreteResult.gasUsage, stateGasUsage, "Method: ${codeBlocks[method.id]}}")
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
                    "fiftstdlib", "fift-examples" -> FileVisitResult.SKIP_SUBTREE
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
