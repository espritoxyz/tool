package org.ton

sealed interface TvmDataCellStructure {
    sealed interface Leaf : TvmDataCellStructure

    data object Unknown : Leaf

    data object Empty : Leaf

    data class KnownTypePrefix(
        val typeOfPrefix: TvmDataCellLabel,
        val rest: TvmDataCellStructure,
    ) : TvmDataCellStructure

    data class LoadRef(
        val ref: TvmParameterInfo.CellInfo,
        val selfRest: TvmDataCellStructure,
    ) : TvmDataCellStructure

    data class SwitchPrefix(
        val switchSize: Int,
        val variants: Map<String, TvmDataCellStructure>
    ) : TvmDataCellStructure {
        init {
            require(switchSize > 0) {
                "switchSize in SwitchPrefix must be > 0, but got $switchSize"
            }
            variants.keys.forEach {
                require(it.length == switchSize) {
                    "Switch keys' lengths must be $switchSize, found key: $it"
                }
            }
        }
    }
}
