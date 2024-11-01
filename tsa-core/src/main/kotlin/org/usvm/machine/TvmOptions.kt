package org.usvm.machine

import org.usvm.machine.TvmMachine.Companion.DEFAULT_MAX_RECURSION_DEPTH
import kotlin.time.Duration

data class TvmOptions(
    val quietMode: Boolean = false,
    val enableVarAddress: Boolean = false,
    val enableInternalArgsConstraints: Boolean = true,
    val turnOnTLBParsingChecks: Boolean = true,
    val excludeInputsThatDoNotMatchGivenScheme: Boolean = true,
    val maxRecursionDepth: Int = DEFAULT_MAX_RECURSION_DEPTH,
    val timeout: Duration = Duration.INFINITE
)
