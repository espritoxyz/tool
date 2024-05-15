package org.usvm.machine

import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmInstLocation
import org.ton.bytecode.TvmLoopEntranceArtificialInst
import org.usvm.machine.state.TvmState
import org.usvm.ps.StateLoopTracker

class TvmLoopTracker : StateLoopTracker<TvmInstLocation, TvmInst, TvmState> {
    override fun findLoopEntrance(statement: TvmInst): TvmInstLocation? =
        (statement as? TvmLoopEntranceArtificialInst)?.location

    override fun isLoopIterationFork(loop: TvmInstLocation, forkPoint: TvmInst): Boolean = true
}
