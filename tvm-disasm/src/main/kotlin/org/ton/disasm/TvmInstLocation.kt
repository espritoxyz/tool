package org.ton.disasm

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal sealed interface TvmInstLocation {
    val index: Int
    fun increment(steps: Int = 1): TvmInstLocation
    fun toJson(): JsonObject
}

internal data class TvmMainMethodLocation(
    override val index: Int
) : TvmInstLocation {
    override fun increment(steps: Int) = TvmMainMethodLocation(steps + 1)
    override fun toJson() = JsonObject(
        mapOf(
            "type" to JsonPrimitive("TvmMainMethodLocation"),
            "index" to JsonPrimitive(index),
        )
    )
}

internal data class TvmInstMethodLocation(
    val methodId: String,
    override val index: Int,
) : TvmInstLocation {
    override fun increment(steps: Int) = TvmInstMethodLocation(methodId, index + steps)

    override fun toJson() = JsonObject(
        mapOf(
            "type" to JsonPrimitive("TvmInstMethodLocation"),
            "methodId" to JsonPrimitive(methodId),
            "index" to JsonPrimitive(index),
        )
    )
}

internal data class TvmInstLambdaLocation(
    override val index: Int,
) : TvmInstLocation {
    override fun increment(steps: Int) = TvmInstLambdaLocation(index + steps)

    override fun toJson() = JsonObject(
        mapOf(
            "type" to JsonPrimitive("TvmInstLambdaLocation"),
            "index" to JsonPrimitive(index),
        )
    )
}
