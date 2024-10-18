package org.usvm.machine.state

import org.usvm.UHeapRef

data class TvmInitialStateData(
    val persistentData: UHeapRef,
    val c7: C7Register,
)
