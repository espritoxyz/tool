package org.usvm.machine.types

import org.ton.TvmInputInfo
import org.ton.bytecode.TvmCodeBlock
import org.usvm.UConcreteHeapRef
import org.usvm.machine.state.TvmStack
import org.usvm.memory.UMemory

class TvmDataCellInfoStorage {
    fun treesOfAddress(address: UConcreteHeapRef): List<TvmDataCellInfoTree> {
        TODO()
    }

    companion object {
        fun build(
            stack: TvmStack,
            memory: UMemory<TvmType, TvmCodeBlock>,
            info: TvmInputInfo,
        ): TvmDataCellInfoStorage {
            TODO()
        }
    }
}