package org.usvm.machine

import org.usvm.machine.TvmMachine.Companion.DEFAULT_MAX_CELL_DEPTH_FOR_DEFAULT_CELLS_CONSISTENT_WITH_TLB
import org.usvm.machine.TvmMachine.Companion.DEFAULT_MAX_RECURSION_DEPTH
import org.usvm.machine.TvmMachine.Companion.DEFAULT_MAX_TLB_DEPTH
import kotlin.time.Duration

data class TvmOptions(
    val quietMode: Boolean = false,
    val enableVarAddress: Boolean = false,
    val enableExternalAddress: Boolean = false,
    val enableInternalArgsConstraints: Boolean = true,
    val turnOnTLBParsingChecks: Boolean = true,
    val excludeInputsThatDoNotMatchGivenScheme: Boolean = true,
    val tlbOptions: TlbOptions = TlbOptions(),
    val maxRecursionDepth: Int = DEFAULT_MAX_RECURSION_DEPTH,
    val timeout: Duration = Duration.INFINITE,
    val excludeExecutionsWithFailures: Boolean = false,
)

data class TlbOptions(
    val performTlbChecksOnAllocatedCells: Boolean = false,
    val maxTlbDepth: Int = DEFAULT_MAX_TLB_DEPTH,
    val maxCellDepthForDefaultCellsConsistentWithTlb: Int = DEFAULT_MAX_CELL_DEPTH_FOR_DEFAULT_CELLS_CONSISTENT_WITH_TLB,
) {
    init {
        check(maxTlbDepth >= 0) {
            "maxTlbDepth must be non-negative, but it is $maxTlbDepth"
        }
        check(maxCellDepthForDefaultCellsConsistentWithTlb >= 0) {
            "maxCellDepthForDefaultCellsConsistentWithTlb must be non-negative, " +
                    "but it is $maxCellDepthForDefaultCellsConsistentWithTlb"
        }
    }
}
