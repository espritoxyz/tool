package org.ton.disasm

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.ton.cell.Cell

internal open class TvmInst(
    val type: String,
    val location: TvmInstLocation,
    val operands: Map<String, JsonElement>,
) {
    fun toJson(): JsonObject = JsonObject(
        mapOf(
            "type" to JsonPrimitive(type),
            "location" to location.toJson(),
        ) + operands
    )
}

internal class TvmConstDictInst(
    type: String,
    location: TvmInstLocation,
    operands: Map<String, JsonElement>,
    val dict: Map<String, Cell>,
) : TvmInst(type, location, operands)
