import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.path.Path
import kotlin.io.path.bufferedWriter
import kotlin.io.path.readText

private val instructionsListPath = Path("tvm-disasm/dist/tvm-spec/cp0.json")
private val generatedInstPath = Path("tsa-core/src/main/kotlin/org/ton/bytecode/TvmInstructions.kt")

private val instructionOperandType = mapOf(
    "PUSHCONT_SHORT" to mapOf("s" to "List<TvmInst>"),
    "PUSHCONT" to mapOf("s" to "List<TvmInst>"),
    "PUSHREFCONT" to mapOf("c" to "List<TvmInst>"),
)

private const val ADDITIONAL_CATEGORY_DICT = "dict"

private val additionalCategories = setOf(ADDITIONAL_CATEGORY_DICT)

@OptIn(ExperimentalSerializationApi::class)
private val json = Json {
    explicitNulls = false
    ignoreUnknownKeys = true
}

@Serializable
private data class InstructionsList(
    val instructions: List<InstructionDescription>,
    val aliases: List<InstructionAliasDescription>
)

@Serializable
private data class InstructionDescription(
    val mnemonic: String,
    val doc: InstructionDescriptionDoc,
    val bytecode: InstructionBytecodeDescription,
    @SerialName("value_flow")
    val valueFlow: InstructionValueFlowDescription
)

@Serializable
private data class InstructionDescriptionDoc(
    val category: String,
    val description: String,
    val gas: String,
    val fift: String
)

@Serializable
private data class InstructionBytecodeDescription(
    val operands: List<InstructionOperandDescription>?
)

@Serializable
private data class InstructionOperandDescription(
    val name: String,
    val loader: String,
    @SerialName("loader_args")
    val loaderArgs: InstructionOperandLoaderArgsDescription?,
)

@Serializable
private data class InstructionValueFlowDescription(
    @SerialName("doc_stack")
    val docStack: String,
    val inputs: InstructionValueFlowValueDescription?,
    val outputs: InstructionValueFlowValueDescription?
)

@Serializable
private data class InstructionValueFlowValueDescription(
    val stack: List<InstructionStackValueDescription>
)

@Serializable
private data class InstructionStackValueDescription(
    val type: String,
    // todo
)

@Suppress("PropertyName")
@Serializable
private data class InstructionOperandLoaderArgsDescription(
    val size: Int?,
    val bits_length_var: String?,
    val bits_padding: Int?,
    val refs_length_var: String?,
    val refs_add: Int?,
    val completion_tag: Boolean?
)

@Serializable
private data class InstructionAliasDescription(
    val mnemonic: String,
    @SerialName("alias_of")
    val aliasOf: String,
    val description: String,
    val operands: Map<String, Int>
)

private fun snakeToCamel(value: String): String =
    value.split('_').joinToString("") { it.replace("-", "Minus").lowercase().capitalize() }

private fun tvmInstCategoryClassName(categoryName: String): String =
    "Tvm${snakeToCamel(categoryName)}Inst"

private fun tvmInstClassName(inst: InstructionDescription): String =
    "Tvm${snakeToCamel(inst.doc.category)}${snakeToCamel(inst.mnemonic)}Inst"

private fun tvmAliasInstClassName(aliasMnemonic: String, original: InstructionDescription): String =
    "Tvm${snakeToCamel(original.doc.category)}${snakeToCamel(aliasMnemonic)}AliasInst"

private fun tvmInstOperandType(operand: InstructionOperandDescription): String? = when (operand.loader) {
    "uint", "int" -> "Int"
    "pushint_long" -> "String"
    "ref" -> null
    "subslice" -> "TvmSubSliceSerializedLoader"
    else -> error("Unexpected loader: ${operand.loader}")
}

private fun tvmInstSubSliceOperandGetter(
    indent: String,
    operand: InstructionOperandDescription
): String = """
    ${indent}val ${operand.name}Resolved: TvmSubSliceLoader
    $indent    get() = TvmSubSliceLoader(
    $indent        ${operand.name},
    $indent        bitsLength = ${operand.loaderArgs?.bits_length_var ?: 0},
    $indent        refsLength = ${operand.loaderArgs?.refs_length_var ?: 0},
    $indent        bitsPadding = ${operand.loaderArgs?.bits_padding ?: 0},
    $indent        refsAdd = ${operand.loaderArgs?.refs_add ?: 0},
    $indent        completion = ${operand.loaderArgs?.completion_tag ?: false}
    $indent    )
         """.trimIndent()

private fun tvmInstCategoryDeclaration(category: String): String {
    var additionalCategories = ""
    if (category !=  ADDITIONAL_CATEGORY_DICT && category.lowercase().startsWith(ADDITIONAL_CATEGORY_DICT)) {
        additionalCategories += ", ${tvmInstCategoryClassName(ADDITIONAL_CATEGORY_DICT)}"
    }

    return """
        @Serializable
        sealed interface ${tvmInstCategoryClassName(category)}: TvmInst$additionalCategories
    """.trimIndent()
}

private fun normalizeDocString(docStr: String): List<String> =
    docStr.split("\n").flatMap { str ->
        str.chunked(100).map { it.trim() }
    }

private fun tvmInstDeclaration(inst: InstructionDescription): String {
    val className = tvmInstClassName(inst)
    val arguments = mutableListOf<String>()
    val additionalProperties = mutableListOf<String>()

    arguments += "|    override val location: TvmInstLocation,"

    for (arg in inst.bytecode.operands.orEmpty()) {
        var type = instructionOperandType[inst.mnemonic]?.get(arg.name)
        if (type == null) {
            type = tvmInstOperandType(arg)
            if (arg.loader == "subslice") {
                additionalProperties += tvmInstSubSliceOperandGetter(indent = "|    ", arg)
            }
        }

        if (type == null) continue

        arguments += "|    val ${arg.name}: ${type}, // ${arg.loader}"
    }

    val additionalInterfaces = ", TvmRefOperandLoader".takeIf {
        inst.bytecode.operands.orEmpty().any { it.loader == "ref" }
    } ?: ""

    val docs = normalizeDocString(inst.doc.description).joinToString("\n") { "| * $it" }

    val gasUsage = inst.doc.gas.toIntOrNull()
    val tvmGasUsage = when{
        inst.doc.gas.isBlank() -> "TvmSimpleGas"
        gasUsage != null -> "TvmFixedGas(value = $gasUsage)"
        else -> "TvmComplexGas(this, description = \"${inst.doc.gas}\")"
    }

    return """
    |/**
    $docs
    | */
    |@Serializable
    |@SerialName($className.MNEMONIC)
    |data class $className(
    ${arguments.joinToString("\n")}
    |): TvmInst, ${tvmInstCategoryClassName(inst.doc.category)}$additionalInterfaces {
    |    override val mnemonic: String get() = MNEMONIC
    |    override val gasConsumption get() = $tvmGasUsage
         ${additionalProperties.joinToString("\n")}
    |    companion object {
    |        const val MNEMONIC = "${inst.mnemonic}"
    |    }
    |}
    """.trimMargin()
}

private val complexAliasInst = setOf(
    "STONE",
    "STZERO",
    "BLESSNUMARGS",
    "SETCONTARGS",
    "SETNUMARGS",
    "ROLL",
    "ROLLREV",
    "ROLLREV",
)

private fun tvmAliasInstDeclaration(inst: InstructionAliasDescription, original: InstructionDescription): String {
    val className = tvmAliasInstClassName(inst.mnemonic, original)
    val docs = normalizeDocString(inst.description).joinToString("\n") { "| * $it" }

    val originalClassName = tvmInstClassName(original)
    val allArgs = mapOf("location" to "location") + inst.operands
    val originalClassConsArgs = allArgs.entries.joinToString { "${it.key} = ${it.value}" }

    val originalClassInstance = if (inst.mnemonic in complexAliasInst) {
        "TODO(\"${inst.mnemonic} is a complex alias of $originalClassName\")"
    } else {
        "$originalClassName($originalClassConsArgs)"
    }

    return """
    |/**
    $docs
    | */
    |@Serializable
    |@SerialName($className.MNEMONIC)
    |data class $className(
    |    override val location: TvmInstLocation
    |): TvmAliasInst, ${tvmInstCategoryClassName(original.doc.category)} {
    |    override val mnemonic: String get() = MNEMONIC
    |    
    |    override fun resolveAlias() = $originalClassInstance
    |    
    |    companion object {
    |        const val MNEMONIC = "${inst.mnemonic}"
    |    }
    |}
    """.trimMargin()
}

private fun generateInstructionSerializer(instructions: Set<String>): String {
    val instructionSerializers = instructions.joinToString("\n") { "|        subclass($it::class)" }
    return """
    |fun SerializersModuleBuilder.registerTvmInstSerializer() {
    |    polymorphic(TvmInst::class) {
    $instructionSerializers
    |    }
    |}
    """.trimMargin()
}

fun main() {
    val instructions = json.decodeFromString<InstructionsList>(instructionsListPath.readText())
    val instructionByMnemonic = instructions.instructions.associateBy { it.mnemonic }

    val basicInstructions = instructions.instructions.associateBy { tvmInstClassName(it) }
    val aliasInstructions = instructions.aliases.associateBy {
        tvmAliasInstClassName(it.mnemonic, instructionByMnemonic.getValue(it.aliasOf))
    }

    val categories = instructions.instructions.mapTo(mutableSetOf()) { it.doc.category }
        .also { it += additionalCategories }
        .associateWith { tvmInstCategoryDeclaration(it) }

    val basicInstructionsDeclarations = basicInstructions.mapValues { tvmInstDeclaration(it.value) }
    val aliasInstructionsDeclarations = aliasInstructions.mapValues {
        tvmAliasInstDeclaration(it.value, instructionByMnemonic.getValue(it.value.aliasOf))
    }

    generatedInstPath.bufferedWriter().use { writer ->
        writer.appendLine(
            """
            // Generated
            package org.ton.bytecode

            import kotlinx.serialization.SerialName
            import kotlinx.serialization.Serializable
            import kotlinx.serialization.modules.SerializersModuleBuilder
            import kotlinx.serialization.modules.polymorphic
            import kotlinx.serialization.modules.subclass
            
        """.trimIndent()
        )

        categories.entries.sortedBy { it.key }.forEach {
            writer.appendLine(it.value)
            writer.appendLine()
        }

        basicInstructionsDeclarations.entries.sortedBy { it.key }.forEach {
            writer.appendLine(it.value)
            writer.appendLine()
        }

        aliasInstructionsDeclarations.entries.sortedBy { it.key }.forEach {
            writer.appendLine(it.value)
            writer.appendLine()
        }

        val allInstructions = basicInstructionsDeclarations.keys + aliasInstructionsDeclarations.keys
        writer.appendLine(generateInstructionSerializer(allInstructions))
    }
}
