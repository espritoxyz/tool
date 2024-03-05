package org.ton.bytecode

import kotlinx.serialization.SerialName
import org.example.org.ton.bytecode.TvmInstLocation

import kotlinx.serialization.Serializable

// TODO remove sealed everywhere?

@Serializable
sealed interface TvmInst {
    val mnemonic: String // use at least for debug
    val location: TvmInstLocation
    // TODO add gas consumption
    // TODO should we define opcodes?
}

@Serializable
sealed class TvmAbstractInst : TvmInst 

// TODO make it sealed?
// A.2
@Serializable
sealed class TvmStackInst : TvmAbstractInst()
@Serializable
sealed class TvmBasicStackInst : TvmStackInst()
@Serializable
sealed class TvmCompoundStackInst : TvmStackInst()
@Serializable
sealed class TvmExoticStackInst : TvmStackInst()
@Serializable
@SerialName(TvmStackNopInst.MNEMONIC)
data class TvmStackNopInst(override val location: TvmInstLocation) : TvmBasicStackInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "NOP"
    }
}
@Serializable
@SerialName(TvmStackXchg0IInst.MNEMONIC)
data class TvmStackXchg0IInst(val i: Int, override val location: TvmInstLocation) : TvmBasicStackInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "XCHG_0I"
    }
}
@Serializable
@SerialName(TvmStackXchgIJInst.MNEMONIC)
data class TvmStackXchgIJInst(val i: Int, val j: Int, override val location: TvmInstLocation) : TvmBasicStackInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "XCHG_IJ"
    }
}
@Serializable
@SerialName(TvmStackPushInst.MNEMONIC)
data class TvmStackPushInst(val i: Int, override val location: TvmInstLocation) : TvmBasicStackInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "PUSH"
    }
}
@Serializable
@SerialName(TvmStackPopInst.MNEMONIC)
data class TvmStackPopInst(val i: Int, override val location: TvmInstLocation) : TvmBasicStackInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "POP"
    }
}
@Serializable
@SerialName(TvmBlkDrop2Inst.MNEMONIC)
data class TvmBlkDrop2Inst(val i: Int, val j: Int, override val location: TvmInstLocation) : TvmExoticStackInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "BLKDROP2"
    }
}
// TODO add compound or replace them with equivalents?
// TODO add exotic

// A.3
@Serializable
sealed class TvmTupleInst : TvmAbstractInst()
@Serializable
@SerialName(TvmNullInst.MNEMONIC)
data class TvmNullInst(override val location: TvmInstLocation) : TvmTupleInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "NULL"
    }
}
@Serializable
@SerialName(TvmIsNullInst.MNEMONIC)
data class TvmIsNullInst(override val location: TvmInstLocation) : TvmTupleInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "ISNULL"
    }
}
@Serializable
@SerialName(TvmMkTupleInst.MNEMONIC)
data class TvmMkTupleInst(val n: Int, override val location: TvmInstLocation) : TvmTupleInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "TUPLE"
    }
}
@Serializable
@SerialName(TvmNilInst.MNEMONIC)
data class TvmNilInst(override val location: TvmInstLocation) : TvmTupleInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "NIL"
    }
}
// TODO add single, pair, and triple?
@Serializable
@SerialName(TvmTupleIndexInst.MNEMONIC)
data class TvmTupleIndexInst(val k: Int, override val location: TvmInstLocation) : TvmTupleInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "INDEX"
    }
}
// TODO add car, cdr, third?
@Serializable
@SerialName(TvmUntupleInst.MNEMONIC)
data class TvmUntupleInst(val n: Int, override val location: TvmInstLocation) : TvmTupleInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "UNTUPLE"
    }
}
// TODO add unsingle, unpair, untriple?
// TODO add unpackfirst or use untuple?
@Serializable
@SerialName(TvmCheckIsTupleInst.MNEMONIC)
data class TvmCheckIsTupleInst(override val location: TvmInstLocation) : TvmTupleInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "CHKTUPLE"
    }
}
@Serializable
@SerialName(TvmTupleExplodeInst.MNEMONIC)
data class TvmTupleExplodeInst(val n: Int, override val location: TvmInstLocation) : TvmTupleInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "EXPLODE"
    }
}
@Serializable
@SerialName(TvmTupleSetInst.MNEMONIC)
data class TvmTupleSetInst(val k: Int, override val location: TvmInstLocation) : TvmTupleInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "SETINDEX"
    }
}
// TODO add setfirst, setsecond, setthird?
@Serializable
@SerialName(TvmTupleIndexQInst.MNEMONIC)
data class TvmTupleIndexQInst(val k: Int, override val location: TvmInstLocation) : TvmTupleInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "INDEXQ"
    }
}
@Serializable
@SerialName(TvmTupleSetQInst.MNEMONIC)
data class TvmTupleSetQInst(val k: Int, override val location: TvmInstLocation) : TvmTupleInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "SETINDEXQ"
    }
}
//class TvmTupleVarInst : TvmTupleInst
//class TvmTupleIndexKInst : TvmTupleInst
// TODO add var and q versions
@Serializable
@SerialName(TvmTupleLenInst.MNEMONIC)
data class TvmTupleLenInst(override val location: TvmInstLocation) : TvmTupleInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "TLEN"
    }
}
@Serializable
@SerialName(TvmIsTupleInst.MNEMONIC)
data class TvmIsTupleInst(override val location: TvmInstLocation) : TvmTupleInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "ISTUPLE"
    }
}
// TODO add 6F8B-6FD5

// A.4
@Serializable
sealed class TvmConstantInst : TvmAbstractInst()
@Serializable
@SerialName(TvmPushInt4Inst.MNEMONIC)
data class TvmPushInt4Inst(val i: Int, override val location: TvmInstLocation) : TvmConstantInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "PUSHINT_4"
    }
}
@Serializable
@SerialName(TvmPushInt8Inst.MNEMONIC)
data class TvmPushInt8Inst(val x: Int, override val location: TvmInstLocation) : TvmConstantInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "PUSHINT_8"
    }
}
@Serializable
@SerialName(TvmPushInt16Inst.MNEMONIC)
data class TvmPushInt16Inst(val x: Int, override val location: TvmInstLocation) : TvmConstantInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "PUSHINT_16"
    }
}
// TODO zero, one, two, ten, true, and different variations
// TODO 83xx - 85xx
@Serializable
@SerialName(TvmPushRefInst.MNEMONIC)
data class TvmPushRefInst(override val location: TvmInstLocation) : TvmConstantInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "PUSHREF"
    }
}
//class TvmPushRefSliceInst : TvmConstantInst
//class TvmPushRefContInst : TvmConstantInst
// TODO 8Bxsss - 9xccc

// A.5
@Serializable
sealed class TvmArithmeticInst : TvmAbstractInst()
@Serializable
@SerialName(TvmAddInst.MNEMONIC)
data class TvmAddInst(override val location: TvmInstLocation) : TvmArithmeticInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "ADD"
    }
}
@Serializable
@SerialName(TvmSubInst.MNEMONIC)
data class TvmSubInst(override val location: TvmInstLocation) : TvmArithmeticInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "SUB"
    }
}
@Serializable
@SerialName(TvmAddConstInst.MNEMONIC)
data class TvmAddConstInst(val c: Int, override val location: TvmInstLocation) : TvmArithmeticInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "ADDCONST"
    }
}
@Serializable
@SerialName(TvmMulConstInst.MNEMONIC)
data class TvmMulConstInst(val c: Int, override val location: TvmInstLocation) : TvmArithmeticInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "MULCONST"
    }
}
@Serializable
@SerialName(TvmMulInst.MNEMONIC)
data class TvmMulInst(override val location: TvmInstLocation) : TvmArithmeticInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "MUL"
    }
}
@Serializable
@SerialName(TvmDivInst.MNEMONIC)
data class TvmDivInst(override val location: TvmInstLocation) : TvmArithmeticInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "DIV"
    }
}
@Serializable
@SerialName(TvmModInst.MNEMONIC)
data class TvmModInst(override val location: TvmInstLocation) : TvmArithmeticInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "MOD"
    }
}
// TODO add all remaining

// A.6
@Serializable
sealed class TvmComparisonInst : TvmAbstractInst()
@Serializable
@SerialName(TvmSignInst.MNEMONIC)
data class TvmSignInst(override val location: TvmInstLocation) : TvmComparisonInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "SGN"
    }
}
@Serializable
@SerialName(TvmLessInst.MNEMONIC)
data class TvmLessInst(override val location: TvmInstLocation) : TvmComparisonInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "LESS"
    }
}
@Serializable
@SerialName(TvmEqualInst.MNEMONIC)
data class TvmEqualInst(override val location: TvmInstLocation) : TvmComparisonInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "EQUAL"
    }
}
@Serializable
@SerialName(TvmLeqInst.MNEMONIC)
data class TvmLeqInst(override val location: TvmInstLocation) : TvmComparisonInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "LEQ"
    }
}
@Serializable
@SerialName(TvmGreaterInst.MNEMONIC)
data class TvmGreaterInst(override val location: TvmInstLocation) : TvmComparisonInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "GREATER"
    }
}
@Serializable
@SerialName(TvmNeqInst.MNEMONIC)
data class TvmNeqInst(override val location: TvmInstLocation) : TvmComparisonInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "NEQ"
    }
}
// TODO add remaining
@Serializable
@SerialName(TvmSemptyInst.MNEMONIC)
data class TvmSemptyInst(override val location: TvmInstLocation) : TvmComparisonInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "SEMPTY"
    }
}

// A.7
@Serializable
sealed class TvmCellInst : TvmAbstractInst()
// A.7.1
@Serializable
sealed class TvmCellSerializationInst : TvmCellInst()
@Serializable
sealed class TvmCellDeserializationInst : TvmCellInst()
@Serializable
@SerialName(TvmNewCellInst.MNEMONIC)
data class TvmNewCellInst(override val location: TvmInstLocation) : TvmCellSerializationInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "NEWC"
    }
}
@Serializable
@SerialName(TvmEndCellInst.MNEMONIC)
data class TvmEndCellInst(override val location: TvmInstLocation) : TvmCellSerializationInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "ENDC"
    }
}
@Serializable
@SerialName(TvmStoreUnsignedIntInst.MNEMONIC)
data class TvmStoreUnsignedIntInst(val c: Int, override val location: TvmInstLocation) : TvmCellSerializationInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "STU"
    }
}
// TODO add all other store insts
// A.7.2
@Serializable
@SerialName(TvmCellToSliceInst.MNEMONIC)
data class TvmCellToSliceInst(override val location: TvmInstLocation) : TvmCellDeserializationInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "CTOS"
    }
}
@Serializable
@SerialName(TvmEndSliceInst.MNEMONIC)
data class TvmEndSliceInst(override val location: TvmInstLocation) : TvmCellDeserializationInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "ENDS"
    }
}
@Serializable
@SerialName(TvmLoadUnsignedIntInst.MNEMONIC)
data class TvmLoadUnsignedIntInst(val c: Int, override val location: TvmInstLocation) : TvmCellDeserializationInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "LDU"
    }
}
@Serializable
@SerialName(TvmLoadRef.MNEMONIC)
data class TvmLoadRef(override val location: TvmInstLocation) : TvmCellDeserializationInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "LDREF"
    }
}
// TODO add all others

// A.8
@Serializable
sealed class TvmControlFlowInst : TvmAbstractInst()
@Serializable
sealed class TvmUnconditionalControlFlowInst : TvmControlFlowInst()
@Serializable
sealed class TvmConditionalControlFlowInst : TvmControlFlowInst()
@Serializable
sealed class TvmLoopInst : TvmControlFlowInst()
// TODO add A.8.4, A.8.5
@Serializable
sealed class TvmSaveControlFlowInst : TvmControlFlowInst()
@Serializable
sealed class TvmDictionaryJumpInst : TvmControlFlowInst()
@Serializable
@SerialName(TvmExecuteInst.MNEMONIC)
data class TvmExecuteInst(override val location: TvmInstLocation) : TvmUnconditionalControlFlowInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "EXECUTE"
    }
}
@Serializable
@SerialName(TvmJumpXInst.MNEMONIC)
data class TvmJumpXInst(override val location: TvmInstLocation) : TvmUnconditionalControlFlowInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "JMPX"
    }
}
@Serializable
@SerialName(TvmReturnInst.MNEMONIC)
data class TvmReturnInst(override val location: TvmInstLocation) : TvmUnconditionalControlFlowInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "RET"
    }
}
// TODO add remaining
@Serializable
@SerialName(TvmIfRetInst.MNEMONIC)
data class TvmIfRetInst(override val location: TvmInstLocation) : TvmConditionalControlFlowInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "IFRET"
    }
}
@Serializable
@SerialName(TvmIfInst.MNEMONIC)
data class TvmIfInst(override val location: TvmInstLocation) : TvmConditionalControlFlowInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "IF"
    }
}
// TODO add remaining
@Serializable
@SerialName(TvmRepeatInst.MNEMONIC)
data class TvmRepeatInst(override val location: TvmInstLocation) : TvmLoopInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "REPEAT"
    }
}
// TODO add remaining
@Serializable
@SerialName(TvmPushCtrInst.MNEMONIC)
data class TvmPushCtrInst(val i: Int, override val location: TvmInstLocation) : TvmSaveControlFlowInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "PUSHCTR"
    }
}
// TODO add remaining
@Serializable
@SerialName(TvmCallDictInst.MNEMONIC)
data class TvmCallDictInst(val n: Int, override val location: TvmInstLocation) : TvmDictionaryJumpInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "CALLDICT"
    }
}

// A.9
@Serializable
sealed class TvmExceptionInst : TvmAbstractInst()
@Serializable
sealed class TvmThrowInst : TvmExceptionInst()
@Serializable
sealed class TvmHandlingExceptionInst : TvmExceptionInst()
@Serializable
@SerialName(TvmThrowShortZeroParameterInst.MNEMONIC)
data class TvmThrowShortZeroParameterInst(val n: Int, override val location: TvmInstLocation) : TvmThrowInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "THROW_SHORT"
    }
}
@Serializable
@SerialName(TvmThrowArgInst.MNEMONIC)
data class TvmThrowArgInst(val n: Int, override val location: TvmInstLocation) : TvmThrowInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "THROWARG"
    }
}
// TODO add remaining

// A.10
@Serializable
sealed class TvmDictionaryInst : TvmAbstractInst()
@Serializable
sealed class TvmDictCreationInst : TvmDictionaryInst()
@Serializable
sealed class TvmDictSerializationInst : TvmDictionaryInst()
@Serializable
sealed class TvmDictGetInst : TvmDictionaryInst()
@Serializable
sealed class TvmDictModificationInst : TvmDictionaryInst()
// TODO A.10.5
// TODO A.10.6
// TODO A.10.7
// TODO A.10.8
// TODO A.10.9
// TODO A.10.10
@Serializable
sealed class TvmDictControlFlowInst : TvmDictionaryInst()
// TODO A.10.12
@Serializable
@SerialName(TvmNewDictInst.MNEMONIC)
data class TvmNewDictInst(override val location: TvmInstLocation) : TvmDictCreationInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "NEWDICT"
    }
}
//class TvmIsDictEmpty : TvmDictCreationInst
// TODO add A.10.2
@Serializable
@SerialName(TvmSimpleDictGetInst.MNEMONIC)
data class TvmSimpleDictGetInst(override val location: TvmInstLocation) : TvmDictGetInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "DICTGET"
    }
}
// TODO add remaining A.10.3
// TODO add A.10.4
// TODO add A.10.5
// TODO add A.10.6
// TODO add A.10.7
// TODO add A.10.8
// TODO add A.10.9
// TODO add A.10.10
@Serializable
@SerialName(TvmDictPushConst.MNEMONIC)
data class TvmDictPushConst(val n: Int, override val location: TvmInstLocation) : TvmDictControlFlowInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "DICTPUSHCONST"
    }
}
@Serializable
@SerialName(TvmDictGetJumpZInst.MNEMONIC)
data class TvmDictGetJumpZInst(override val location: TvmInstLocation) : TvmDictControlFlowInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "DICTIGETJMPZ"
    }
}
// TODO add remaining A.10.11
// TODO add A.10.12

// A.11
@Serializable
sealed class TvmBlockchainInst : TvmAbstractInst()
// TODO add remaining

// A.12
@Serializable
sealed class TvmDebugInst : TvmAbstractInst()
@Serializable
@SerialName(TvmAnyDebugInst.MNEMONIC)
data class TvmAnyDebugInst(override val location: TvmInstLocation) : TvmDebugInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "TvmAnyDebugInst"
    }
} // all debug instructions are considered as NOP

// A.13
@Serializable
sealed class TvmCodepageInst : TvmAbstractInst()
@Serializable
@SerialName(TvmSetCodepageInst.MNEMONIC)
data class TvmSetCodepageInst(val n: Int, override val location: TvmInstLocation) : TvmCodepageInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "SETCP"
    }
}
@Serializable
@SerialName(TvmSetZeroCodepageInst.MNEMONIC)
data class TvmSetZeroCodepageInst(override val location: TvmInstLocation) : TvmCodepageInst() {
    override val mnemonic: String
        get() = MNEMONIC

    companion object {
        const val MNEMONIC = "SETCP0"
    }
}
// TODO add remaining
