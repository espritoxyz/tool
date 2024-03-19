package org.ton.bytecode

import kotlinx.serialization.Serializable

@Serializable
sealed interface TvmInst {
    val mnemonic: String
    val location: TvmInstLocation
    val gasConsumption: TvmGas
    // TODO should we define opcodes?
}

@Serializable
sealed interface TvmAliasInst : TvmInst {
    fun resolveAlias(): TvmInst

    override val gasConsumption: TvmGas // todo: check that alias consumption equal to original
        get() = resolveAlias().gasConsumption
}
