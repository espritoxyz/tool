package org.usvm.machine.types

import org.ton.TvmBuiltinDataCellLabel
import org.ton.TvmCompositeDataCellLabel
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
import org.usvm.memory.foldHeapRef
import org.usvm.mkSizeGtExpr
import org.usvm.mkSizeLtExpr


class TvmDataCellInfoStorage private constructor(
    private val ctx: TvmContext,
    private val addressToTree: Set<TvmDataCellInfoTree>,
) {
    private fun treesOfAddress(address: UConcreteHeapRef): List<Pair<TvmDataCellInfoTree, UBoolExpr>> =
        addressToTree.mapNotNull { tree ->
            val guard = foldHeapRef(
                tree.address,
                initial = ctx.falseExpr as UBoolExpr,
                initialGuard = ctx.trueExpr,
                staticIsConcrete = true,
                blockOnSymbolic = { _, (ref, _) -> error("Unexpected ref $ref") },
                blockOnConcrete = { acc, (expr, guard) ->
                    if (expr == address) {
                        with(ctx) { acc or guard }
                    } else {
                        acc
                    }
                }
            )
            if (guard == ctx.falseExpr) {
                null
            } else {
                tree to guard
            }
        }

    fun getConflictConditionsForLoadData(
        loadData: TvmDataCellLoadedTypeInfo.LoadData
    ): Map<TvmStructuralError, UBoolExpr> = with(ctx) {
        val trees = treesOfAddress(loadData.address)
        val result = mutableMapOf<TvmStructuralError, UBoolExpr>()
        trees.forEach { (tree, treeGuard) ->
            val cur = getConflictConditionsForLoadData(loadData, tree, treeGuard)
            cur.forEach { (error, guard) ->
                val oldValue = result.getOrDefault(error, falseExpr)
                result[error] = oldValue or guard
            }
        }
        return result
    }

    private fun getConflictConditionsForLoadData(
        loadData: TvmDataCellLoadedTypeInfo.LoadData,
        tree: TvmDataCellInfoTree,
        treeGuard: UBoolExpr,
        rootTree: Boolean = true,
    ): Map<TvmStructuralError, UBoolExpr> = with(ctx) {
        val result = mutableMapOf<TvmStructuralError, UBoolExpr>()
        tree.onEachVertex { vertex ->
            val exactOffsetGuard = loadData.offset eq vertex.prefixSize
            when (val struct = vertex.structure) {
                is TvmDataCellStructure.Unknown,
                is TvmDataCellStructure.SwitchPrefix,
                is TvmDataCellStructure.LoadRef -> {
                    // no conflict here
                }

                is TvmDataCellStructure.Empty -> {
                    if (rootTree) {
                        // TvmUnexpectedReading, if loaded more than 0 bits
                        val error = TvmUnexpectedReading(loadData.type)
                        val oldValue = result.getOrDefault(error, falseExpr)
                        val conflict = mkSizeGtExpr(loadData.type.sizeBits, zeroSizeExpr)
                        result[error] =
                            oldValue or (loadData.guard and vertex.guard and exactOffsetGuard and conflict and treeGuard)
                    }
                }

                is TvmDataCellStructure.KnownTypePrefix -> {
                    // TODO: Composite + builtin labels
                    if (struct.typeOfPrefix is TvmCompositeDataCellLabel && struct.typeOfPrefix !is TvmBuiltinDataCellLabel) {
                        val internalTree = vertex.internalTree
                        requireNotNull(internalTree) {
                            "InternalTree must not be null for TvmCompositeDataCellLabel"
                        }
                        val inner = getConflictConditionsForLoadData(loadData, internalTree, treeGuard, rootTree = false)
                        inner.forEach { (error, guard) ->
                            val oldValue = result.getOrDefault(error, falseExpr)
                            result[error] = oldValue or guard
                        }
                        return@onEachVertex
                    }

                    // skip artificial labels
                    if (struct.typeOfPrefix !is TvmBuiltinDataCellLabel) {
                        return@onEachVertex
                    }

                    // conflict, if types are not consistent
                    val error = TvmReadingOfUnexpectedType(
                        labelType = struct.typeOfPrefix,
                        actualType = loadData.type
                    )

                    val oldValue = result.getOrDefault(error, falseExpr)
                    val conflict = struct.typeOfPrefix.accepts(loadData.type).not()
                    result[error] = oldValue or (loadData.guard and vertex.guard and exactOffsetGuard and conflict and treeGuard)
                }
            }
        }
        result
    }

    fun getNoUnexpectedEndOfReadingCondition(
        endOfCell: TvmDataCellLoadedTypeInfo.EndOfCell
    ): UBoolExpr = with(ctx) {
        val trees = treesOfAddress(endOfCell.address)
        return trees.fold(trueExpr as UBoolExpr) { outerResult, (tree, treeGuard) ->
            tree.fold(outerResult) internalFold@{ result, vertex ->
                // we can check only leaves
                if (vertex.structure !is TvmDataCellStructure.Empty && vertex.structure !is TvmDataCellStructure.Unknown) {
                    return@internalFold result
                }

                // conflict, if ended cell before this vertex
                val offsetGuard = mkSizeLtExpr(endOfCell.offset, vertex.prefixSize)
                // conflict, if ended cell before loaded all refs
                val refNumberGuard = mkSizeLtExpr(endOfCell.refNumber, vertex.refNumber)
                result and (endOfCell.guard and vertex.guard and (offsetGuard or refNumberGuard) and treeGuard).not()
            }
        }
    }

    fun getNoUnexpectedLoadRefCondition(
        loadRef: TvmDataCellLoadedTypeInfo.LoadRef,
    ): UBoolExpr = with(ctx) {
        val trees = treesOfAddress(loadRef.address)
        trees.fold(trueExpr as UBoolExpr) { outerResult, (tree, treeGuard) ->
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
                        val conflict = mkSizeGtExpr(loadRef.refNumber, vertex.refNumber)
                        result and (vertex.guard and conflict and treeGuard).not()
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
                return TvmDataCellInfoStorage(state.ctx, emptySet())
            }

            val trees = mutableSetOf<TvmDataCellInfoTree>()
            info.parameterInfos.entries.forEach { (param, paramInfo) ->
                val entry = state.stack.peekStackEntry(param)
                check(entry is TvmStack.TvmInputStackEntry) {
                    "During TvmDataCellInfoStorage building stack must consist only of input entries, but $entry found"
                }
                trees += buildTreesForParameter(state, paramInfo, entry)
            }

            return TvmDataCellInfoStorage(state.ctx, trees)
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