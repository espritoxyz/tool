package org.ton.bytecode

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.usvm.machine.MethodId

@Serializable
sealed class TvmInstLocation {
    abstract val index: Int
    abstract var codeBlock: TvmCodeBlock

    abstract fun increment(): TvmInstLocation
}

@Serializable
@SerialName("TvmInstMethodLocation")
data class TvmInstMethodLocation(
    val methodId: @Contextual MethodId,
    override val index: Int
) : TvmInstLocation() {
    @kotlinx.serialization.Transient
    override lateinit var codeBlock: TvmCodeBlock

    override fun increment() = TvmInstMethodLocation(methodId, index + 1).also {
        it.codeBlock = codeBlock
    }

    override fun toString(): String {
        return "$methodId:#$index"
    }
}

@Serializable
@SerialName("TvmInstLambdaLocation")
class TvmInstLambdaLocation(override val index: Int) : TvmInstLocation() {
    @kotlinx.serialization.Transient
    override lateinit var codeBlock: TvmCodeBlock

    @kotlinx.serialization.Transient
    lateinit var parent: TvmInstLocation

    override fun increment() = TvmInstLambdaLocation(index + 1).also {
        it.codeBlock = codeBlock
        it.parent = parent
    }

    override fun toString(): String {
        return "Lambda:#$index"
    }
}

@Serializable
@SerialName("TvmMainMethodLocation")
data class TvmMainMethodLocation(override val index: Int) : TvmInstLocation() {
    @kotlinx.serialization.Transient
    override lateinit var codeBlock: TvmCodeBlock

    override fun increment() = TvmMainMethodLocation(index + 1).also {
        it.codeBlock = codeBlock
    }

    override fun toString(): String {
        return "MainMethod:#$index"
    }
}
