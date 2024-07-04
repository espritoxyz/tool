package org.usvm.machine

import org.ton.bytecode.TvmArtificialLoopEntranceInst
import org.ton.bytecode.TvmInst
import org.usvm.machine.state.TvmState
import org.usvm.ps.StateLoopTracker

class TvmLoopTracker : StateLoopTracker<UInt, TvmInst, TvmState> {
    override fun findLoopEntrance(statement: TvmInst): UInt? =
        (statement as? TvmArtificialLoopEntranceInst)?.id

    override fun isLoopIterationFork(loop: UInt, forkPoint: TvmInst): Boolean = true
}
