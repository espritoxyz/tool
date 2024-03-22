package org.ton.bytecode

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TODO is it a real entity?
@Serializable
sealed class TvmCodeBlock {
    abstract val instList: List<TvmInst>

    protected fun initLocationsCodeBlock() {
        instList.forEach {
            it.location.codeBlock = this
        }
    }
}

@Serializable
@SerialName("TvmMethod")
data class TvmMethod(
    val id: Int,
    override val instList: List<TvmInst>
) : TvmCodeBlock() {
    init {
        initLocationsCodeBlock()
    }
}

// An artificial entity representing instructions in continuation
@Serializable
@SerialName("TvmLambda")
data class TvmLambda(
    override val instList: List<TvmInst>
) : TvmCodeBlock() {
    init {
        initLocationsCodeBlock()
    }
}
