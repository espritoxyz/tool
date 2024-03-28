package org.usvm.test

import org.ton.bytecode.TvmMethod
import org.usvm.machine.state.TvmState

class TvmTestResolver {
    companion object {
        fun resolve(method: TvmMethod, state: TvmState): List<TvmTestValue> {
            val model = state.models.first()
            val ctx = state.ctx
            val stateResolver = TvmTestStateResolver(ctx, model, state)

            return stateResolver.resolveParameters()
        }
    }
}