package org.example.org.ton.bytecode

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface TvmInstLocation {
//    val methodId: Int // TODO replace it with real TvmMethod
    val index: Int
}

@Serializable
@SerialName("TvmInstMethodLocation")
data class TvmInstMethodLocation(val methodId: Int, override val index: Int) : TvmInstLocation {
    override fun toString(): String {
        return "$methodId:#$index"
    }
}

@Serializable
@SerialName("TvmInstLambdaLocation")
data class TvmInstLambdaLocation(override val index: Int) : TvmInstLocation {
    override fun toString(): String {
        return "Lambda:#$index"
    }
}
