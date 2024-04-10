package org.usvm.machine

import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmLoopArtificialInst
import org.usvm.machine.state.TvmState
import org.usvm.ps.StateLoopTracker

class TvmLoopTracker : StateLoopTracker<TvmLoopArtificialInst, TvmInst, TvmState> {
    override fun findLoopEntrance(statement: TvmInst): TvmLoopArtificialInst? = statement as? TvmLoopArtificialInst
    override fun isLoopIterationFork(loop: TvmLoopArtificialInst, forkPoint: TvmInst): Boolean = true
}
