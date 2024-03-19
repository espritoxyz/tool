package org.ton.bytecode

sealed interface TvmType

data object TvmIntegerType : TvmType
data object TvmCellType : TvmType
data object TvmTupleType : TvmType
data object TvmNullType : TvmType
data object TvmSliceType : TvmType
data object TvmBuilderType : TvmType
data object TvmContinuationType : TvmType
