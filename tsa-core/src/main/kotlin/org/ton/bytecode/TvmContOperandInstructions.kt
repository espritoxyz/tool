package org.ton.bytecode

sealed interface TvmContOperandInst : TvmInst

interface TvmContOperand1Inst : TvmContOperandInst {
    val c: TvmInstList
}

interface TvmContOperand2Inst : TvmContOperandInst {
    val c1: TvmInstList
    val c2: TvmInstList
}