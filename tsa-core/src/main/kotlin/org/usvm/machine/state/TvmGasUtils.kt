package org.usvm.machine.state

import org.ton.bytecode.TvmComplexGas
import org.ton.bytecode.TvmFixedGas
import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmSimpleGas
import org.usvm.UBv32Sort
import org.usvm.UExpr
import org.usvm.machine.interpreter.TvmStepScope

const val IMPLICIT_EXCEPTION_THROW_GAS = 50
const val SIMPLE_INSTRUCTION_BASE_GAS = 10
const val SIMPLE_INSTRUCTION_BIT_GAS = 1

const val SIMPLE_INSTRUCTION_BIT_SIZE = 16 // todo: check for instructions with simple gas and another bit size

fun TvmStepScope.setDefaultGasUsage(stmt: TvmInst) = doWithState {
    setDefaultGasUsage(stmt)
}

fun TvmState.setDefaultGasUsage(stmt: TvmInst) = when (val gas = stmt.gasConsumption) {
    is TvmFixedGas -> setGasUsage(gas.value)
    TvmSimpleGas -> setGasUsage(SIMPLE_INSTRUCTION_BASE_GAS + SIMPLE_INSTRUCTION_BIT_SIZE * SIMPLE_INSTRUCTION_BIT_GAS)
    is TvmComplexGas -> error("$stmt has complex Gas usage")
}

fun TvmState.setGasUsage(stmtGasUsage: Int) = setGasUsage(ctx.mkBv(stmtGasUsage))

fun TvmState.setGasUsage(stmtGasUsage: UExpr<UBv32Sort>) {
    gasUsage = gasUsage.add(stmtGasUsage)
}
