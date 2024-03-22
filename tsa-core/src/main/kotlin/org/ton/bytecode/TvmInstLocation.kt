package org.ton.bytecode

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class TvmInstLocation {
//    val methodId: Int // TODO replace it with real TvmMethod
    abstract val index: Int
    abstract var codeBlock: TvmCodeBlock
}

@Serializable
@SerialName("TvmInstMethodLocation")
data class TvmInstMethodLocation(val methodId: Int, override val index: Int) : TvmInstLocation() {
    @kotlinx.serialization.Transient
    override lateinit var codeBlock: TvmCodeBlock

    override fun toString(): String {
        return "$methodId:#$index"
    }
}

@Serializable
@SerialName("TvmInstLambdaLocation")
data class TvmInstLambdaLocation(override val index: Int) : TvmInstLocation() {
    @kotlinx.serialization.Transient
    override lateinit var codeBlock: TvmCodeBlock

    override fun toString(): String {
        return "Lambda:#$index"
    }
}
