package org.usvm.machine.types

import org.ton.TvmInputInfo
import org.ton.TvmParameterInfo
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
import org.usvm.api.readField
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.Companion.sliceCellField
import org.usvm.machine.state.TvmStack
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.generateSymbolicCell
import org.usvm.machine.state.generateSymbolicSlice
import org.usvm.machine.types.TvmDataCellInfoTree.Companion.construct

class TvmDataCellInfoStorage private constructor(
    private val ctx: TvmContext,
    private val trees: List<TvmDataCellInfoTree>,
    private var addressToTree: Map<UConcreteHeapRef, List<TvmDataCellInfoTree>>? = null,
) {
    fun initialize(state: TvmState) {
        if (addressToTree != null)
            return  // already initialized
        val result = mutableMapOf<UConcreteHeapRef, MutableList<TvmDataCellInfoTree>>()
        trees.forEach { tree ->
            val address = tree.lazyAddress(state)
            val list = result[address] ?: run {
                val list = mutableListOf<TvmDataCellInfoTree>()
                result[address] = list
                list
            }
            list.add(tree)
        }
        addressToTree = result
    }

    private fun treesOfAddress(address: UConcreteHeapRef): List<TvmDataCellInfoTree> {
        val cache = addressToTree
        require(cache != null) {
            "TvmDataCellInfoStorage was not initialized"
        }
        return cache[address] ?: emptyList()
    }

    fun getNoConflictCondition(load: TvmDataCellLoadedTypeInfo.Load): UBoolExpr = with(ctx) {
        val trees = treesOfAddress(load.address)
        val conflictHappensGuard = trees.fold(falseExpr as UBoolExpr) { acc, tree ->
            acc
        }
        return (load.guard and conflictHappensGuard).not()
    }

    // this behavior might be changed in the future
    fun clone(): TvmDataCellInfoStorage = this

    companion object {
        fun build(
            ctx: TvmContext,
            stack: TvmStack,
            info: TvmInputInfo,
        ): TvmDataCellInfoStorage {
            val trees = mutableListOf<TvmDataCellInfoTree>()
            info.parameterInfos.entries.forEach { (param, paramInfo) ->
                val entry = stack.peekStackEntry(param)
                require(entry is TvmStack.TvmInputStackEntry)
                trees += buildTreesForParameter(ctx, paramInfo, entry)
            }
            return TvmDataCellInfoStorage(ctx, trees)
        }

        private fun buildTreesForParameter(
            ctx: TvmContext,
            paramInfo: TvmParameterInfo,
            entry: TvmStack.TvmInputStackEntry,
        ): List<TvmDataCellInfoTree> =
            when (paramInfo) {
                is TvmParameterInfo.DataCellInfo -> {
                    val lazyAddress = { state: TvmState ->
                        val value = state.stack.getStackValue(entry, TvmCellType) { state.generateSymbolicCell() }
                        value.cellValue as UConcreteHeapRef
                    }
                    construct(ctx, paramInfo.dataCellStructure, lazyAddress)
                }
                is TvmParameterInfo.SliceInfo -> {
                    val lazyAddress = { state: TvmState ->
                        val value = state.stack.getStackValue(entry, TvmSliceType) { state.generateSymbolicSlice() }
                        val sliceAddress = value.sliceValue
                            ?: error("Could not extract slice address while building TvmDataCellInfoStorage")
                        val res = state.memory.readField(sliceAddress, sliceCellField, ctx.addressSort)
                        res as UConcreteHeapRef
                    }
                    construct(ctx, paramInfo.cellInfo.dataCellStructure, lazyAddress)
                }
                is TvmParameterInfo.NoInfo -> {
                    emptyList()
                }
            }
    }
}