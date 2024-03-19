package org.ton.bytecode

sealed interface TvmGas

data object TvmSimpleGas : TvmGas

data class TvmFixedGas(val value: Int) : TvmGas

data class TvmComplexGas(val instruction: TvmInst, val description: String) : TvmGas
