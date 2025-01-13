package org.ton.disasm.bytecode

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
internal val specJson = Json {
    explicitNulls = false
    ignoreUnknownKeys = true
}

@Serializable
internal data class InstructionsList(
    val instructions: List<InstructionDescription>
)

@Serializable
internal data class InstructionDescription(
    val mnemonic: String,
    @SerialName("since_version")
    val sinceVersion: Int,
    val doc: InstructionDescriptionDoc,
    val bytecode: InstructionBytecodeDescription,
    @SerialName("value_flow")
    val valueFlow: InstructionValueFlowDescription,
    @SerialName("control_flow")
    val controlFlow: ControlFlowValueDescription,
)

@Serializable
internal data class InstructionDescriptionDoc(
    val category: String,
    val description: String,
    val gas: String,
    val fift: String,
)

@Serializable
internal data class InstructionBytecodeDescription(
    val operands: List<InstructionOperandDescription>,
    val prefix: String,
    @SerialName("operands_range_check")
    val operandsRangeCheck: OperandsRangeCheck?,
)

@Serializable
internal data class OperandsRangeCheck(
    val length: Int,
    val from: Int,
    val to: Int,
)

@Serializable
internal data class InstructionOperandDescription(
    val name: String,
    val type: String,
    val size: Int?,
    val bits_length_var_size: Int?,
    val bits_padding: Int?,
    val refs_length_var_size: Int?,
    val refs_add: Int?,
    val completion_tag: Boolean?
)

@Serializable
internal data class InstructionValueFlowDescription(
    val inputs: InstructionValueFlowValueDescription?,
    val outputs: InstructionValueFlowValueDescription?
)

@Serializable
internal data class InstructionValueFlowValueDescription(
    val stack: List<InstructionStackValueDescription>?
)

@Serializable
internal data class InstructionStackValueDescription(
    val type: String,
    // TODO unused for now, could be useful in the future
)

@Serializable
internal data class ControlFlowValueDescription(
    val nobranch: Boolean,
    // TODO unused for now, could be useful in the future
)
