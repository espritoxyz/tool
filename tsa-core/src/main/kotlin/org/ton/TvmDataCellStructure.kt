package org.ton

sealed interface TvmDataCellStructure {
    data object Unknown : TvmDataCellStructure

    data object Empty : TvmDataCellStructure

    class KnownTypePrefix(
        val typeOfPrefix: TvmDataCellLabel,
        val rest: TvmDataCellStructure,
    ) : TvmDataCellStructure

    class LoadRef(
        val ref: TvmParameterInfo.CellInfo,
        val selfRest: TvmDataCellStructure,
    ) : TvmDataCellStructure

    class SwitchPrefix(
        val switchSize: Int,
        val variants: Map<String, TvmDataCellStructure>
    ) : TvmDataCellStructure {
        init {
            variants.keys.forEach {
                require(switchSize > 0) {
                    "switchSize in SwitchPrefix must be > 0"
                }
                require(it.length == switchSize) {
                    "Switch keys' lengths must be $switchSize, found key: $it"
                }
            }
        }
    }
}