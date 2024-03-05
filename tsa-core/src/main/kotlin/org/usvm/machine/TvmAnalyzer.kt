package org.example.org.usvm.machine

import org.example.org.ton.bytecode.TvmContractCode
import org.ton.bytecode.TvmDictPushConst
import org.ton.bytecode.TvmMethod
import org.ton.cell.Cell
import org.usvm.machine.TvmMachine
import org.usvm.machine.state.TvmState
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

fun analyzeAllMethods(bytecodePath: String, contractDataHex: String? = null): Map<TvmMethod, List<TvmState>> {
    val contractData = Cell.Companion.of(contractDataHex ?: DEFAULT_CONTRAACT_DATA_HEX)

    val disasmProcess = ProcessBuilder(*(DISASSEMBLER_RUN_COMMAND.split(" ") + bytecodePath).toTypedArray())
        .directory(Paths.get(DISASSEMBLER_PATH).toFile())
        .start()

    disasmProcess.waitFor(DISASSEMBLER_TIMEOUT, TimeUnit.SECONDS)

    val bytecodeJson = disasmProcess.inputStream.bufferedReader().readText()
    val contract = TvmContractCode.fromJson(bytecodeJson)
    val machine = TvmMachine()
    val methodsExceptDictPushConst = contract.methods.filterValues {
        it.instList.none { inst -> inst is TvmDictPushConst }
    }
    val methodStates = methodsExceptDictPushConst.values.associateWith { machine.analyze(contract, contractData, it.id) }
    methodStates.forEach {
        println("Method ${it.key}")
        val exceptionalStates = it.value.filter { state -> state.isExceptional }
        println("States: ${it.value.size}, exceptional: ${exceptionalStates.size}")
        exceptionalStates.forEach { state -> println(state.methodResult) }
        println("=====".repeat(20))
    }

    return methodStates
}

private const val DISASSEMBLER_PATH = "../tvm-disasm"
private const val DISASSEMBLER_RUN_COMMAND = "node dist/index.js"
private const val DISASSEMBLER_TIMEOUT = 5.toLong() // seconds
private const val DEFAULT_CONTRAACT_DATA_HEX = "b5ee9c7241010101000a00001000000185d258f59ccfc59500"
