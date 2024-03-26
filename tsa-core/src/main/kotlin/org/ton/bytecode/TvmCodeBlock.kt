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
    @SerialName("instList")
    private val instListRaw: MutableList<TvmInst>
) : TvmCodeBlock() {
    override val instList: List<TvmInst>
        get() = instListRaw

    init {
        instListRaw += TvmContBasicRetInst(TvmInstMethodLocation(id, instListRaw.size))
        initLocationsCodeBlock()
    }
}

// An artificial entity representing instructions in continuation
@Serializable
@SerialName("TvmLambda")
data class TvmLambda(
    @SerialName("instList")
    private val instListRaw: MutableList<TvmInst>
) : TvmCodeBlock() {
    override val instList: List<TvmInst>
        get() = instListRaw

    init {
        instListRaw += TvmContBasicRetInst(TvmInstLambdaLocation(instListRaw.size))
        initLocationsCodeBlock()
    }
}
