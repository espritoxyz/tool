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
import org.usvm.machine.state.TvmUnexpectedReading
import org.usvm.machine.state.generateSymbolicCell
import org.usvm.machine.state.generateSymbolicSlice
import org.usvm.machine.types.TvmDataCellInfoTree.Companion.construct
import org.usvm.mkSizeExpr
import org.usvm.mkSizeGtExpr

class TvmDataCellInfoStorage private constructor(
    private val ctx: TvmContext,
    private val addressToTree: Map<UConcreteHeapRef, List<TvmDataCellInfoTree>>,
) {
    private fun treesOfAddress(address: UConcreteHeapRef): List<TvmDataCellInfoTree> {
        return addressToTree[address] ?: emptyList()
    }

    fun getNoConflictConditionsForLoadData(
        loadData: TvmDataCellLoadedTypeInfo.LoadData
    ): Map<TvmStructuralError, UBoolExpr> = with(ctx) {
        val trees = treesOfAddress(loadData.address)
        val result = mutableMapOf<TvmStructuralError, UBoolExpr>()
        trees.forEach { tree ->
            tree.fold(Unit) { _, vertex ->
                val offsetGuard = loadData.offset eq vertex.prefixSize
                when (val struct = vertex.structure) {
                    is TvmDataCellStructure.Unknown,
                    is TvmDataCellStructure.SwitchPrefix,
                    is TvmDataCellStructure.LoadRef -> {
                        // no conflict here
                    }

                    is TvmDataCellStructure.Empty -> {
                        // TvmUnexpectedReading, if loaded more than 0 bits
                        val error = TvmUnexpectedReading(loadData.type)
                        val oldValue = result.getOrDefault(error, trueExpr)
                        val conflict = mkSizeGtExpr(loadData.type.sizeBits, zeroSizeExpr)
                        result[error] = oldValue and (loadData.guard and vertex.guard and offsetGuard and conflict).not()
                    }

                    is TvmDataCellStructure.KnownTypePrefix -> {
                        // conflict, if types are not consistent
                        val error = TvmReadingOfUnexpectedType(
                            labelType = struct.typeOfPrefix,
                            actualType = loadData.type
                        )
                        val oldValue = result.getOrDefault(error, trueExpr)
                        val conflict = struct.typeOfPrefix.accepts(loadData.type).not()
                        result[error] = oldValue and (loadData.guard and vertex.guard and offsetGuard and conflict).not()
                    }
                }
            }
        }
        return result
    }

    fun getNoUnexpectedEndOfReadingCondition(
        endOfCell: TvmDataCellLoadedTypeInfo.EndOfCell
    ): UBoolExpr = with(ctx) {
        val trees = treesOfAddress(endOfCell.address)
        return trees.fold(trueExpr as UBoolExpr) { outerResult, tree ->
            tree.fold(outerResult) { result, vertex ->
                // conflict, if ended cell before this vertex
                val offsetGuard = mkBvSignedLessExpr(endOfCell.offset, vertex.prefixSize)
                // conflict, if ended cell before loaded all refs
                val refNumberGuard = mkBvSignedLessExpr(endOfCell.refNumber, mkSizeExpr(vertex.refNumber))
                result and (endOfCell.guard and vertex.guard and (offsetGuard or refNumberGuard)).not()
            }
        }
    }

    fun getNoUnexpectedLoadRefCondition(
        loadRef: TvmDataCellLoadedTypeInfo.LoadRef,
    ): UBoolExpr = with(ctx) {
        val trees = treesOfAddress(loadRef.address)
        trees.fold(trueExpr as UBoolExpr) { outerResult, tree ->
            tree.fold(outerResult) { result, vertex ->
                when (vertex.structure) {
                    is TvmDataCellStructure.Unknown,
                    is TvmDataCellStructure.SwitchPrefix,
                    is TvmDataCellStructure.KnownTypePrefix,
                    is TvmDataCellStructure.LoadRef -> {
                        // no conflict here
                        result
                    }

                    is TvmDataCellStructure.Empty -> {
                        val conflict = mkBvSignedGreaterExpr(loadRef.refNumber, mkSizeExpr(vertex.refNumber))
                        result and (vertex.guard and conflict).not()
                    }
                }
            }
        }
    }

    // this behavior might be changed in the future
    fun clone(): TvmDataCellInfoStorage = this

    companion object {
        fun build(
            checkDataCellContentTypes: Boolean,
            state: TvmState,
            info: TvmInputInfo,
        ): TvmDataCellInfoStorage {
            if (!checkDataCellContentTypes) {
                return TvmDataCellInfoStorage(state.ctx, emptyMap())
            }

            val trees = mutableListOf<TvmDataCellInfoTree>()
            info.parameterInfos.entries.forEach { (param, paramInfo) ->
                val entry = state.stack.peekStackEntry(param)
                check(entry is TvmStack.TvmInputStackEntry) {
                    "During TvmDataCellInfoStorage building stack must consist only of input entries"
                }
                trees += buildTreesForParameter(state, paramInfo, entry)
            }

            val treeMap = trees.groupBy { it.address }

            return TvmDataCellInfoStorage(state.ctx, treeMap)
        }

        private fun buildTreesForParameter(
            state: TvmState,
            paramInfo: TvmParameterInfo,
            entry: TvmStack.TvmInputStackEntry,
        ): List<TvmDataCellInfoTree> =
            when (paramInfo) {
                is TvmParameterInfo.DataCellInfo -> {
                    val stackValue = state.stack.getStackValue(entry, TvmCellType) { state.generateSymbolicCell() }
                    val address = stackValue.cellValue as UConcreteHeapRef
                    construct(state, paramInfo.dataCellStructure, address)
                }

                is TvmParameterInfo.SliceInfo -> {
                    val stackValue = state.stack.getStackValue(entry, TvmSliceType) { state.generateSymbolicSlice() }
                    val sliceAddress = stackValue.sliceValue
                        ?: error("Could not extract slice address while building TvmDataCellInfoStorage")
                    val address =
                        state.memory.readField(sliceAddress, sliceCellField, state.ctx.addressSort) as UConcreteHeapRef
                    construct(state, paramInfo.cellInfo.dataCellStructure, address)
                }

                is TvmParameterInfo.NoInfo -> {
                    emptyList()
                }

                is TvmParameterInfo.DictCellInfo -> {
                    TODO()
                }
            }
    }
}