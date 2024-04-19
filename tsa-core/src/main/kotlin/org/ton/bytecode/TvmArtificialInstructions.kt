package org.ton.bytecode

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.usvm.UExpr
import org.usvm.machine.TvmContext.TvmInt257Sort

@Serializable
sealed interface TvmArtificialInst : TvmInst {
    override val gasConsumption: TvmGas
        get() = TvmFixedGas(value = 0)
}

sealed interface TvmLoopArtificialInst : TvmArtificialInst, TvmContLoopsInst {
    val originalInst: TvmContLoopsInst

    override val location: TvmInstLocation
        get() = originalInst.location
}

data class TvmArtificialRepeatInst(
    override val originalInst: TvmContLoopsInst,
    val continuationValue: TvmContinuationValue,
    val loopRepeats: UExpr<TvmInt257Sort>,
    val executeUntilEnd: Boolean,
) : TvmLoopArtificialInst {
    override val mnemonic: String
        get() = "artificial_repeat_${originalInst.mnemonic}"
}

data class TvmArtificialUntilInst(
    override val originalInst: TvmContLoopsInst,
    val continuationValue: TvmContinuationValue,
    val executeUntilEnd: Boolean,
) : TvmLoopArtificialInst {
    override val mnemonic: String
        get() = "artificial_until_${originalInst.mnemonic}"
}

data class TvmArtificialWhileStartInst(
    override val originalInst: TvmContLoopsInst,
    val continuationValue: TvmContinuationValue,
    val conditionContinuation: TvmContinuationValue,
    val executeUntilEnd: Boolean,
) : TvmLoopArtificialInst {
    override val mnemonic: String
        get() = "artificial_while_start_${originalInst.mnemonic}"
}

data class TvmArtificialWhileEndInst(
    override val originalInst: TvmContLoopsInst,
    val continuationValue: TvmContinuationValue,
    val conditionContinuation: TvmContinuationValue,
    val executeUntilEnd: Boolean,
) : TvmLoopArtificialInst {
    override val mnemonic: String
        get() = "artificial_while_end_${originalInst.mnemonic}"
}

data class TvmArtificialAgainInst(
    override val originalInst: TvmContLoopsInst,
    val continuationValue: TvmContinuationValue,
    val executeUntilEnd: Boolean,
) : TvmLoopArtificialInst {
    override val mnemonic: String
        get() = "artificial_again_${originalInst.mnemonic}"
}

sealed interface TvmArtificialLoadAddrInst : TvmArtificialInst, TvmAppAddrInst {
    val originalInst: TvmAppAddrLdmsgaddrInst

    override val location: TvmInstLocation
        get() = originalInst.location
}

@Serializable
data class TvmArtificialLoadAddrNoneInst(override val originalInst: TvmAppAddrLdmsgaddrInst) : TvmArtificialLoadAddrInst {
    override val mnemonic: String
        get() = "artificial_addr_none_${originalInst.mnemonic}"
}

@Serializable
data class TvmArtificialLoadAddrExternInst(override val originalInst: TvmAppAddrLdmsgaddrInst) : TvmArtificialLoadAddrInst {
    override val mnemonic: String
        get() = "artificial_addr_extern_${originalInst.mnemonic}"
}

@Serializable
data class TvmArtificialLoadAddrStdInst(override val originalInst: TvmAppAddrLdmsgaddrInst) : TvmArtificialLoadAddrInst {
    override val mnemonic: String
        get() = "artificial_addr_std_${originalInst.mnemonic}"
}

@Serializable
data class TvmArtificialLoadAddrVarInst(override val originalInst: TvmAppAddrLdmsgaddrInst) : TvmArtificialLoadAddrInst {
    override val mnemonic: String
        get() = "artificial_addr_var_${originalInst.mnemonic}"
}

@Serializable
data class TvmArtificialImplicitRetInst(
    override val location: TvmInstLocation
) : TvmInst, TvmArtificialInst, TvmContBasicInst {
    override val mnemonic: String get() = "implicit RET"
    override val gasConsumption get() = TvmFixedGas(value = 5)
}

fun SerializersModuleBuilder.registerTvmArtificialLoadAddrInstSerializer() {
    polymorphic(TvmArtificialLoadAddrInst::class) {
        subclass(TvmArtificialLoadAddrExternInst::class)
        subclass(TvmArtificialLoadAddrNoneInst::class)
        subclass(TvmArtificialLoadAddrStdInst::class)
        subclass(TvmArtificialLoadAddrVarInst::class)
    }
}
