package org.ton.bytecode

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
sealed interface TvmArtificialInst : TvmInst {
    override val gasConsumption: TvmGas
        get() = TvmFixedGas(value = 0)
}

/**
 * Instruction that marks the beginning of a loop iteration
 */
data class TvmArtificialLoopEntranceInst(
    val id: UInt,
    override val location: TvmInstLocation,
) : TvmArtificialInst, TvmContLoopsInst {
    override val mnemonic: String get() = "artificial_loop_entrance"
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

sealed interface TvmArtificialContInst : TvmArtificialInst, TvmContBasicInst {
    val cont: TvmContinuation
}

@Serializable
data class TvmArtificialJmpToContInst(
    override val cont: TvmContinuation,
    override val location: TvmInstLocation
) : TvmArtificialContInst {
    override val mnemonic: String get() = "artificial_jmp_to_$cont"
}

@Serializable
data class TvmArtificialExecuteContInst(
    override val cont: TvmContinuation,
    override val location: TvmInstLocation,
) : TvmArtificialContInst {
    override val mnemonic: String get() = "artificial_execute_$cont"
}

fun SerializersModuleBuilder.registerTvmArtificialLoadAddrInstSerializer() {
    polymorphic(TvmArtificialLoadAddrInst::class) {
        subclass(TvmArtificialLoadAddrExternInst::class)
        subclass(TvmArtificialLoadAddrNoneInst::class)
        subclass(TvmArtificialLoadAddrStdInst::class)
        subclass(TvmArtificialLoadAddrVarInst::class)
    }
}
