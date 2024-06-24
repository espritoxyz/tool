package org.usvm.machine.types

import org.ton.TvmDataCellStructure
import org.ton.TvmInputInfo
import org.ton.TvmParameterInfo
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
import org.usvm.api.readField
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.Companion.sliceCellField
import org.usvm.machine.state.TvmMethodResult.TvmStructuralError
import org.usvm.machine.state.TvmReadingOfUnexpectedType
import org.usvm.machine.state.TvmStack
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.TvmUnexpectedEndOfReading
import org.usvm.machine.state.TvmUnexpectedReading
import org.usvm.machine.state.generateSymbolicCell
import org.usvm.machine.state.generateSymbolicSlice
import org.usvm.machine.types.TvmDataCellInfoTree.Companion.construct
import org.usvm.mkSizeExpr

class TvmDataCellInfoStorage private constructor(
    private val ctx: TvmContext,
    private val trees: List<TvmDataCellInfoTree>,
    private var addressToTree: Map<UConcreteHeapRef, List<TvmDataCellInfoTree>>? = null,
    private val checkDataCellContentTypes: Boolean = true,
) {
    fun initialize(state: TvmState) {
        if (addressToTree != null)
            return  // already initialized
        if (!checkDataCellContentTypes) {
            addressToTree = emptyMap()
            return
        }
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

    fun getNoConflictConditions(
        state: TvmState,
        load: TvmDataCellLoadedTypeInfo.Load
    ): Map<TvmStructuralError, UBoolExpr> = with(ctx) {
        val trees = treesOfAddress(load.address)
        val result = mutableMapOf<TvmStructuralError, UBoolExpr>()
        trees.forEach { tree ->
            tree.fold(Unit) { _, vertex ->
                val vertexGuard = vertex.lazyGuard(state)
                val offsetGuard = load.offset eq mkBv(vertex.prefixSize)
                when (val struct = vertex.structure) {
                    is TvmDataCellStructure.Unknown, is TvmDataCellStructure.SwitchPrefix -> {
                        // no conflict here
                    }
                    is TvmDataCellStructure.Empty -> {
                        // TvmUnexpectedReading, if loaded more than 0 bits
                        val error = TvmUnexpectedReading(load.type)
                        val oldValue = result.getOrDefault(error, trueExpr)
                        val conflict = mkBvSignedGreaterExpr(load.type.sizeBits, mkBv(0))
                        result[error] = oldValue and (load.guard and vertexGuard and offsetGuard and conflict).not()
                    }
                    is TvmDataCellStructure.KnownTypePrefix -> {
                        // conflict, if types are not consistent
                        val error = TvmReadingOfUnexpectedType(
                            expectedType = struct.typeOfPrefix,
                            actualType = load.type
                        )
                        val oldValue = result.getOrDefault(error, trueExpr)
                        val conflict = struct.typeOfPrefix.accepts(load.type).not()
                        result[error] = oldValue and (load.guard and vertexGuard and offsetGuard and conflict).not()
                    }
                }
            }
        }
        return result
    }

    fun getNoUnexpectedEndOfReadingCondition(
        state: TvmState,
        endOfCell: TvmDataCellLoadedTypeInfo.EndOfCell
    ): UBoolExpr = with(ctx) {
        val trees = treesOfAddress(endOfCell.address)
        var result: UBoolExpr = trueExpr
        trees.forEach { tree ->
            tree.fold(Unit) { _, vertex ->
                val vertexGuard = vertex.lazyGuard(state)
                when (val struct = vertex.structure) {
                    is TvmDataCellStructure.Empty, TvmDataCellStructure.Unknown -> {
                        // no conflict here
                    }
                    is TvmDataCellStructure.SwitchPrefix -> {
                        // TvmUnexpectedEndOfReading, if got here
                        val border = vertex.prefixSize + struct.switchSize
                        val offsetGuard = mkBvSignedLessExpr(endOfCell.offset, mkSizeExpr(border))
                        result = result and (endOfCell.guard and vertexGuard and offsetGuard).not()
                    }
                    is TvmDataCellStructure.KnownTypePrefix -> {
                        // TvmUnexpectedEndOfReading, if got here
                        val border = vertex.prefixSize + struct.typeOfPrefix.bitSize
                        val offsetGuard = mkBvSignedLessExpr(endOfCell.offset, mkSizeExpr(border))
                        result = result and (endOfCell.guard and vertexGuard and offsetGuard).not()
                    }
                }
            }
        }
        return result
    }

    // this behavior might be changed in the future
    fun clone(): TvmDataCellInfoStorage = this

    companion object {
        fun build(
            checkDataCellContentTypes: Boolean,
            ctx: TvmContext,
            stack: TvmStack,
            info: TvmInputInfo,
        ): TvmDataCellInfoStorage {
            if (!checkDataCellContentTypes) {
                return TvmDataCellInfoStorage(ctx, emptyList(), checkDataCellContentTypes = false)
            }
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