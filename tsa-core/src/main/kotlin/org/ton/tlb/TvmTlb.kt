package org.ton.tlb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TvmTlbTypeDefinition(
    val id: Int,
    val name: String,
    val arity: Int,
    val constructors: List<TvmTlbTypeConstructor>,
) {
    val isBuiltin
        get() = constructors.isEmpty()
}

@Serializable
data class TvmTlbTypeConstructor(
    val name: String,
    val tag: String,
    val fields: List<TvmTlbField>
)

@Serializable
data class TvmTlbField(
    val name: String,
    @SerialName("type-expr")
    val typeExpr: TvmTlbTypeExpr,
)

@Serializable
sealed interface TvmTlbTypeExpr

@Serializable
@SerialName("type")
data class TvmTlbType(
    val id: Int,
    val args: List<TvmTlbTypeExpr>
) : TvmTlbTypeExpr

@Serializable
@SerialName("ref")
data class TvmTlbReference(
    val ref: TvmTlbTypeExpr
) : TvmTlbTypeExpr

@Serializable
@SerialName("int-const")
data class TvmTlbIntConst(
    val value: Int,
) : TvmTlbTypeExpr

@Serializable
@SerialName("param")
data class TvmTlbTypeParam(
    val idx: Int,
) : TvmTlbTypeExpr
