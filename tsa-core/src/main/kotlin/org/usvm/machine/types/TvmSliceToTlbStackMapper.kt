package org.usvm.machine.types

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import org.ton.TvmDataCellLabel
import org.ton.TvmParameterInfo
import org.usvm.UConcreteHeapRef
import org.usvm.machine.TvmContext

class TvmSliceToTlbStackMapper(
    private var sliceToTlbStack: PersistentMap<UConcreteHeapRef, TlbStack> = persistentMapOf()
) {

    fun clone() = TvmSliceToTlbStackMapper(sliceToTlbStack)

    fun allocateInitialSlice(ctx: TvmContext, address: UConcreteHeapRef, label: TvmDataCellLabel) {
        sliceToTlbStack = sliceToTlbStack.put(address, TlbStack.new(ctx, label))
    }

    fun mapSliceToTlbStack(sliceAddress: UConcreteHeapRef, stack: TlbStack) {
        sliceToTlbStack = sliceToTlbStack.put(sliceAddress, stack)
    }

    fun getTlbStack(address: UConcreteHeapRef): TlbStack? = sliceToTlbStack[address]

    companion object {
        fun constructInitialSliceMapper(
            ctx: TvmContext,
            input: InputParametersStructure,
        ): TvmSliceToTlbStackMapper {
            val result = TvmSliceToTlbStackMapper()

            input.sliceToCell.forEach { (sliceAddress, cellAddress) ->
                val info = input.cellToInfo[cellAddress]
                    ?: error("Info for cell at ref $cellAddress must be known")
                if (info is TvmParameterInfo.DataCellInfo) {
                    result.allocateInitialSlice(ctx, sliceAddress, info.dataCellStructure)
                }
            }

            return result
        }
    }
}
