package org.ton.tlb

import org.ton.Endian.BigEndian
import org.ton.TvmDataCellLabel
import org.ton.TvmDataCellStructure
import org.ton.TvmDataCellStructure.Empty
import org.ton.TvmDataCellStructure.KnownTypePrefix
import org.ton.TvmDataCellStructure.LoadRef
import org.ton.TvmDataCellStructure.SwitchPrefix
import org.ton.TvmDataCellStructure.Unknown
import org.ton.TvmIntegerLabel
import org.ton.TvmParameterInfo.DataCellInfo

class TvmTlbTransformer(
    definitions: List<TvmTlbTypeDefinition>
) {
    private val typeDefinitions = definitions.associateBy { it.id }
    private val transformed = hashMapOf<Pair<TvmTlbTypeDefinition, List<TvmTlbTypeExpr>>, TvmDataCellStructure>()

    fun transform(
        def: TvmTlbTypeDefinition,
        args: List<TvmTlbTypeExpr> = emptyList(),
    ): TvmDataCellStructure = transformTypeDefinition(def, args, Empty)

    private fun getTypeDef(id: Int): TvmTlbTypeDefinition = typeDefinitions[id] ?: error("Unknown type id: $id")

    private fun transformTypeDefinition(
        def: TvmTlbTypeDefinition,
        args: List<TvmTlbTypeExpr>,
        next: TvmDataCellStructure,
    ): TvmDataCellStructure = transformed.getOrPut(def to args) {
        if (def.isBuiltin) return transformBuiltins(def, args, next)

        return transformComplexType(def, args, next)
    }

    private fun transformBuiltins(
        def: TvmTlbTypeDefinition,
        args: List<TvmTlbTypeExpr>,
        next: TvmDataCellStructure,
    ): TvmDataCellStructure {
        val name = def.name

        // TODO check `isSigned` and `endian` fields
        val label: TvmDataCellLabel = when {
            name == "#" -> TvmIntegerLabel(bitSize = 32, isSigned = false, endian = BigEndian)
            name == "##" -> TvmIntegerLabel(bitSize = args.toIntConst(), isSigned = true, endian = BigEndian)
            name == "bits" -> TODO()
            name == "uint" -> TvmIntegerLabel(bitSize = args.toIntConst(), isSigned = false, endian = BigEndian)
            name == "int" -> TvmIntegerLabel(bitSize = args.toIntConst(), isSigned = true, endian = BigEndian)
            name.startsWith("bits") -> TODO()
            name.startsWith("int") -> {
                val bits = name.removeSuffix("int").toInt()
                TvmIntegerLabel(bitSize = bits, isSigned = true, endian = BigEndian)
            }
            name.startsWith("uint") -> {
                val bits = name.removeSuffix("uint").toInt()
                TvmIntegerLabel(bitSize = bits, isSigned = false, endian = BigEndian)
            }
            name == "Cell" || name == "Any" -> return Unknown
            else -> TODO()
        }

        return KnownTypePrefix(label, next)
    }

    private fun transformComplexType(
        def: TvmTlbTypeDefinition,
        args: List<TvmTlbTypeExpr>,
        next: TvmDataCellStructure,
    ): TvmDataCellStructure {
        // special cases
        when (def.name) {
            "Maybe" -> TODO()
            "Either" -> TODO()
        }

        if (args.isNotEmpty()) {
            TODO()
        }

        // TODO use this
//        return TvmCompositeDataCellLabel(
//            name = def.name,
//            innerStructure = transformConstructors(
//                constructors = def.constructors.map { ConstructorTagSuffix(it, it.tag) },
//                next = next,
//            )
//        )
        return transformConstructors(
            constructors = def.constructors.map { ConstructorTagSuffix(it, it.tag) },
            next = next,
        )
    }

    private fun transformConstructors(
        constructors: List<ConstructorTagSuffix>,
        next: TvmDataCellStructure,
    ): TvmDataCellStructure {
        if (constructors.size == 1 && constructors.single().tagSuffix.isEmpty()) {
            return transformConstructor(constructors.single().constructor, next)
        }

        val minLen = constructors.minOf { it.tagSuffix.length }
        val groupedConstructors = hashMapOf<String, MutableList<ConstructorTagSuffix>>().withDefault { mutableListOf() }

        constructors.forEach {
            val prefix = it.tagSuffix.substring(0 until minLen)

            it.tagSuffix = it.tagSuffix.substring(minLen)
            groupedConstructors.getValue(prefix).add(it)
        }

        val variants = groupedConstructors.mapValues { transformConstructors(it.value, next) }

        return SwitchPrefix(
            minLen,
            variants,
        )
    }

    private fun transformConstructor(
        constructor: TvmTlbTypeConstructor,
        next: TvmDataCellStructure,
    ): TvmDataCellStructure {
        var last: TvmDataCellStructure = next

        constructor.fields.reversed().forEach { field ->
            last = when (val typeExpr = field.typeExpr) {
                is TvmTlbReference -> {
                    val ref = typeExpr.ref
                    require(ref is TvmTlbType) {
                        "Unexpected reference: $ref"
                    }

                    val refData = transformTypeDefinition(getTypeDef(ref.id), ref.args, Empty)

                    LoadRef(DataCellInfo(refData), last)
                }

                is TvmTlbType -> {
                    val typeDef = getTypeDef(typeExpr.id)

                    transformTypeDefinition(typeDef, typeExpr.args, last)
                }

                else -> TODO()
            }
        }

        return last
    }

    private data class ConstructorTagSuffix(
        val constructor: TvmTlbTypeConstructor,
        var tagSuffix: String,
    )

    private fun List<TvmTlbTypeExpr>.toIntConst(): Int =
        (singleOrNull() as? TvmTlbIntConst)?.value ?: error("Unexpected args: $this")
}