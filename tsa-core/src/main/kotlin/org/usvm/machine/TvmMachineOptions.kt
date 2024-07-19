package org.usvm.machine

data class TvmMachineOptions(
    val checkDataCellContentTypes: Boolean = true,
    val excludeInputsThatDoNotMatchGivenScheme: Boolean = true,
) {
    init {
        if (excludeInputsThatDoNotMatchGivenScheme) {
            require(checkDataCellContentTypes) {
                "When option excludeInputsThatDoNotMatchGivenScheme in on, " +
                    "option checkDataCellContentTypes must be also on."
            }
        }
    }
}
