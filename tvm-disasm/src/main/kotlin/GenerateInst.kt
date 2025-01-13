import kotlinx.serialization.decodeFromString
import org.ton.disasm.bytecode.CellOperandType
import org.ton.disasm.bytecode.InstructionDescription
import org.ton.disasm.bytecode.InstructionOperandDescription
import org.ton.disasm.bytecode.InstructionsList
import org.ton.disasm.bytecode.opcodeToRefOperandType
import org.ton.disasm.bytecode.opcodeToSubSliceOperandType
import org.ton.disasm.bytecode.specJson
import kotlin.io.path.Path
import kotlin.io.path.bufferedWriter
import kotlin.io.path.readText

private val instructionsListPath = Path("tvm-spec/cp0.json")
private val generatedInstPath = Path("tsa-core/src/main/kotlin/org/ton/bytecode/TvmInstructions.kt")

private fun generateInstructionCellOperandTypes(
    instructions: Map<String, InstructionDescription>
): Map<String, Map<String, String>> =
    (opcodeToSubSliceOperandType + opcodeToRefOperandType).mapNotNull { (opname, type) ->
        val inst = instructions[opname]
            ?: error("Instruction $opname not found")
        val typeName = when (type) {
            CellOperandType.CodeCell -> {
                "TvmInstList"
            }
            CellOperandType.OrdinaryCell -> {
                "TvmCell"
            }
            CellOperandType.SpecialCell -> {
                return@mapNotNull null
            }
        }
        check(inst.bytecode.operands.isNotEmpty()) {
            "Expected operands for instruction $opname"
        }
        val map = if (inst.bytecode.operands.size == 1) {
            mapOf("c" to typeName)
        } else {
            List(inst.bytecode.operands.size) { index -> "c${index + 1}" to typeName }.toMap()
        }
        opname to map
    }.toMap()

private const val ADDITIONAL_CATEGORY_DICT = "dict"

private val additionalCategories = setOf(ADDITIONAL_CATEGORY_DICT)

private fun snakeToCamel(value: String): String =
    value.split('_').joinToString("") { it.replace("-", "Minus").lowercase().capitalize() }

private fun tvmInstCategoryClassName(categoryName: String): String =
    "Tvm${snakeToCamel(categoryName)}Inst"

private fun tvmInstClassName(inst: InstructionDescription): String =
    "Tvm${snakeToCamel(inst.doc.category)}${snakeToCamel(inst.mnemonic)}Inst"

private fun tvmInstOperandType(operand: InstructionOperandDescription): String? = when (operand.type) {
    "uint", "int" -> "Int"
    "pushint_long" -> "String"
    "ref" -> null
    "subslice" -> "TvmSubSliceSerializedLoader"
    else -> error("Unexpected operand type: $operand")
}

private fun tvmInstCategoryDeclaration(category: String): String {
    var additionalCategories = ""
    if (category != ADDITIONAL_CATEGORY_DICT && category.lowercase().startsWith(ADDITIONAL_CATEGORY_DICT)) {
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

private fun tvmInstDeclaration(
    inst: InstructionDescription,
    instructionOperandTypes: Map<String, Map<String, String>>,
): String {
    val className = tvmInstClassName(inst)
    val arguments = mutableListOf<String>()

    arguments += "|    override val location: TvmInstLocation,"

    var contArgsCount = 0
    for (arg in inst.bytecode.operands) {
        var type = instructionOperandTypes[inst.mnemonic]?.get(arg.name)
        if (type == null) {
            type = tvmInstOperandType(arg)
        }

        if (type == null) continue

        val modifier = if (type == "TvmInstList") {
            contArgsCount++
            "override "
        } else {
            ""
        }

        arguments += "|    ${modifier}val ${arg.name}: ${type}, // ${arg.type}"
    }

    var additionalInterfaces = ", TvmRefOperandLoader".takeIf {
        inst.bytecode.operands.any { it.type == "ref" }
    } ?: ""
    additionalInterfaces += ", TvmContOperand${contArgsCount}Inst".takeIf { contArgsCount > 0 } ?: ""

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
    val instructions = specJson.decodeFromString<InstructionsList>(instructionsListPath.readText())
    val instructionByMnemonic = instructions.instructions.associateBy { it.mnemonic }

    val basicInstructions = instructions.instructions.associateBy { tvmInstClassName(it) }

    val categories = instructions.instructions.mapTo(mutableSetOf()) { it.doc.category }
        .also { it += additionalCategories }
        .associateWith { tvmInstCategoryDeclaration(it) }

    val instructionOperandTypes = generateInstructionCellOperandTypes(instructionByMnemonic)

    val basicInstructionsDeclarations = basicInstructions.mapValues {
        tvmInstDeclaration(it.value, instructionOperandTypes)
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

        val allInstructions = basicInstructionsDeclarations.keys
        writer.appendLine(generateInstructionSerializer(allInstructions))
    }
}
