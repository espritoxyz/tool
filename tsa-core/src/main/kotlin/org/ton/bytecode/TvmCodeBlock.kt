package org.ton.bytecode

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TODO is it a real entity?
@Serializable
sealed interface TvmCodeBlock {
    val instList: List<TvmInst>
}

@Serializable
@SerialName("TvmMethod")
data class TvmMethod(
    val id: Int,
    override val instList: List<TvmInst>
) : TvmCodeBlock

// An artificial entity representing instructions in continuation
@Serializable
@SerialName("TvmLambda")
data class TvmLambda(
    override val instList: List<TvmInst>
) : TvmCodeBlock
