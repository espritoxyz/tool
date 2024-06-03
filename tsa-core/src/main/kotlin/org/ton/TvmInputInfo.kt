package org.ton

sealed interface TvmInputInfo {

    data object NoInfo : TvmInputInfo

    // all parameters must be listed ?
    data class TvmParametersInfo(
        val parameters: List<TvmParameterInfo>
    ): TvmInputInfo
}