package org.usvm.machine

import org.ton.bytecode.TvmArtificialInst
import org.ton.bytecode.TvmContLoopsInst
import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmInstLocation
import org.ton.bytecode.TvmLoopEntranceArtificialInst
import org.usvm.machine.state.TvmState
import org.usvm.ps.StateLoopTracker

class TvmLoopTracker : StateLoopTracker<TvmInstLocation, TvmInst, TvmState> {
    override fun findLoopEntrance(statement: TvmInst): TvmInstLocation? =
        // The first loop iteration entrance is TvmArtificialInst instruction,
        // the following ones are TvmLoopEntranceArtificialInst instructions
        if (statement is TvmArtificialInst) {
            (statement as? TvmLoopEntranceArtificialInst)?.location
        } else {
            (statement as? TvmContLoopsInst)?.location
        }

    override fun isLoopIterationFork(loop: TvmInstLocation, forkPoint: TvmInst): Boolean = true
}
