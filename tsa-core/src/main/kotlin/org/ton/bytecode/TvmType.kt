package org.ton.bytecode

sealed interface TvmType

data object TvmIntegerType : TvmType
// TODO there are functional types in FunC (such as int -> cell) that are represented as continuations in bytecode,
//  but there are no examples of such usage, so for now assume that continuations cannot be passed as method parameters
data object TvmContinuationType : TvmType

sealed interface TvmReferenceType : TvmType
data object TvmCellType : TvmReferenceType
data object TvmTupleType : TvmReferenceType
data object TvmNullType : TvmReferenceType
data object TvmSliceType : TvmReferenceType
data object TvmBuilderType : TvmReferenceType
