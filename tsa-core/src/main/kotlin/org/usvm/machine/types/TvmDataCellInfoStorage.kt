package org.usvm.machine.types

import org.ton.TvmInputInfo
import org.ton.bytecode.TvmCodeBlock
import org.usvm.UConcreteHeapRef
import org.usvm.machine.state.TvmStack
import org.usvm.machine.state.TvmState
import org.usvm.memory.UMemory

class TvmDataCellInfoStorage private constructor(
    private val trees: List<TvmDataCellInfoTree>,
    private var addressToTree: Map<UConcreteHeapRef, List<TvmDataCellInfoTree>>? = null,
) {
    fun initialize(state: TvmState) {
        if (addressToTree != null)
            return  // already initialized
        TODO()
    }

    fun treesOfAddress(address: UConcreteHeapRef): List<TvmDataCellInfoTree> {
        TODO()
    }

    // this behavior might be changed in the future
    fun clone(): TvmDataCellInfoStorage = this

    companion object {
        fun build(
            stack: TvmStack,
            info: TvmInputInfo,
        ): TvmDataCellInfoStorage {
            TODO()
        }
    }
}