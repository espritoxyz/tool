package org.ton.bytecode

import org.usvm.UHeapRef
import org.usvm.machine.state.TvmRegisters
import org.usvm.machine.state.TvmStack

data class TvmCellValue(val value: UHeapRef)

// TODO how to represent a continuation value?
data class TvmContinuationValue(
//    val slice: TvmCellClice,
    val codeBlock: TvmCodeBlock,
    val stack: TvmStack,
    val registers: TvmRegisters,
    // TODO codepage and nargs
    var currentInstIndex: Int = 0
) {
    fun takeCurrentStmt(): TvmInst = codeBlock.instList.getOrNull(currentInstIndex)
        ?: error("No instruction with index $currentInstIndex in code block $codeBlock")

    override fun toString(): String = "inst $currentInstIndex in $codeBlock"
}
