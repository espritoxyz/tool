package org.ton

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import org.ton.sarif.toSarifReport
import org.ton.tlb.readFromJson
import org.usvm.machine.BocAnalyzer
import org.usvm.machine.FiftAnalyzer
import org.usvm.machine.FuncAnalyzer
import org.usvm.machine.TactAnalyzer
import java.math.BigInteger

class ContractProperties : OptionGroup("Contract properties") {
    val contractData by option("-d", "--data").help("The serialized contract persistent data")
}

class FiftOptions : OptionGroup("Fift options") {
    val fiftStdlibPath by option("--fift-std")
        .path(mustExist = true, canBeDir = true, canBeFile = false)
        .required()
        .help("The path to the Fift standard library (dir containing Asm.fif, Fift.fif)")
}

class FuncOptions : OptionGroup("FunC options") {
    val funcStdlibPath by option("--func-std")
        .path(mustExist = true, canBeFile = true, canBeDir = false)
        .required()
        .help("The path to the dir containing FunC standard library file (stdlib.fc)")
}

class TactAnalysis : CliktCommand(name = "tact", help = "Options for analyzing Tact sources of smart contracts") {
    private val tactSourcesPath by option("-i", "--input")
        .path(mustExist = true, canBeFile = true, canBeDir = false)
        .required()
        .help("The path to the Tact source of the smart contract")

    private val contractProperties by ContractProperties()

    override fun run() {
        TactAnalyzer.analyzeAllMethods(tactSourcesPath, contractProperties.contractData).let {
            echo(it.toSarifReport(methodsMapping = emptyMap()))
        }
    }
}

class FuncAnalysis : CliktCommand(name = "func", help = "Options for analyzing FunC sources of smart contracts") {
    private val funcSourcesPath by option("-i", "--input")
        .path(mustExist = true, canBeFile = true, canBeDir = false)
        .required()
        .help("The path to the FunC source of the smart contract")

    private val contractProperties by ContractProperties()
    private val fiftOptions by FiftOptions()
    private val funcOptions by FuncOptions()

    private val tlbJsonPath by option("-t", "--tlb")
        .path(mustExist = true, canBeFile = true, canBeDir = false)
        .help("The path to the parsed TL-B scheme.")

    private fun extractInputInfo(): Map<BigInteger, TvmInputInfo> {
        val path = tlbJsonPath
        return if (path == null) {
            emptyMap()
        } else {
            val struct = readFromJson(path, "InternalMsgBody")
                ?: error("Couldn't parse `InternalMsgBody` structure from $path")
            val info = TvmParameterInfo.SliceInfo(
                TvmParameterInfo.DataCellInfo(
                    struct
                )
            )
            mapOf(BigInteger.ZERO to TvmInputInfo(mapOf(0 to info)))
        }
    }

    override fun run() {
        FuncAnalyzer(
            funcStdlibPath = funcOptions.funcStdlibPath,
            fiftStdlibPath = fiftOptions.fiftStdlibPath
        ).analyzeAllMethods(funcSourcesPath, contractProperties.contractData, inputInfo = extractInputInfo()).let {
            // TODO parse FunC sources in CLI without TON plugin usage
            echo(it.toSarifReport(methodsMapping = emptyMap()))
        }
    }
}

class FiftAnalysis : CliktCommand(name = "fift", help = "Options for analyzing smart contracts in Fift assembler") {
    private val fiftSourcesPath by option("-i", "--input")
        .path(mustExist = true, canBeFile = true, canBeDir = false)
        .required()
        .help("The path to the Fift assembly of the smart contract")

    private val contractProperties by ContractProperties()
    private val fiftOptions by FiftOptions()

    override fun run() {
        FiftAnalyzer(
            fiftStdlibPath = fiftOptions.fiftStdlibPath
        ).analyzeAllMethods(fiftSourcesPath, contractProperties.contractData,).let {
            echo(it.toSarifReport(methodsMapping = emptyMap()))
        }
    }
}

class BocAnalysis : CliktCommand(name = "boc", help = "Options for analyzing a smart contract in the BoC format") {
    private val bocPath by option("-i", "--input")
        .path(mustExist = true, canBeFile = true, canBeDir = false)
        .required()
        .help("The path to the smart contract in the BoC format")

    private val contractProperties by ContractProperties()

    override fun run() {
        BocAnalyzer.analyzeAllMethods(bocPath, contractProperties.contractData).let {
            echo(it.toSarifReport(methodsMapping = emptyMap()))
        }
    }
}

class TonAnalysis : NoOpCliktCommand()

fun main(args: Array<String>) = TonAnalysis()
    .subcommands(TactAnalysis(), FuncAnalysis(), FiftAnalysis(), BocAnalysis())
    .main(args)
