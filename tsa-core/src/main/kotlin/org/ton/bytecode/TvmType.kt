package org.ton.bytecode

sealed interface TvmType

object TvmIntegerType : TvmType
object TvmCellType : TvmType
object TvmCellArrayType : TvmType
object TvmTupleType : TvmType
object TvmNullType : TvmType
object TvmSliceType : TvmType
object TvmBuilderType : TvmType
object TvmContinuationType : TvmType

// Not real type in TVM but useful to represent bits
object TvmBoolType : TvmType
