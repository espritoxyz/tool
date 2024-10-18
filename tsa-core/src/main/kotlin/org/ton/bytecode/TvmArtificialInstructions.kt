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
