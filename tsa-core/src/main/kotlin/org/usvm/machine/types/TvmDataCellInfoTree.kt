package org.usvm.machine.types

import org.ton.TvmAtomicDataCellLabel
import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmDataCellStructure
import org.ton.TvmParameterInfo
import org.usvm.UBoolExpr
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.api.readField
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.Companion.cellDataField
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.assertType
import org.usvm.machine.state.readCellRef
import org.usvm.mkSizeAddExpr
import org.usvm.mkSizeExpr
import org.usvm.mkSizeSubExpr

class TvmDataCellInfoTree private constructor(
    private val ctx: TvmContext,
    val address: UHeapRef,
    private val root: Vertex,
    private val initialOffset: UExpr<TvmSizeSort>,
    private val initialRefNumber: UExpr<TvmSizeSort>,
) {
    fun <Acc> fold(init: Acc, f: (Acc, Vertex) -> Acc): Acc =
        root.fold(init, f)

    fun onEachVertex(f: (Vertex) -> Unit) {
        fold(Unit) { _, vertex -> f(vertex) }
    }

    class Vertex(
        val guard: UBoolExpr,
        val structure: TvmDataCellStructure,
        val prefixSize: UExpr<TvmSizeSort>,
        val refNumber: UExpr<TvmSizeSort>,
        private val children: List<Vertex>,
        val internalTree: TvmDataCellInfoTree? = null,
    ) {
        internal fun <Acc> fold(init: Acc, f: (Acc, Vertex) -> Acc): Acc {
            return children.fold(f(init, this)) { acc, child -> child.fold(acc, f) }
        }

        init {
            val isCompositeNode = structure is TvmDataCellStructure.KnownTypePrefix &&
                    structure.typeOfPrefix is TvmCompositeDataCellLabel
            require((internalTree != null) == isCompositeNode) {
                "internalTree of vertex must be non-null if and only if this is a composite node."
            }
        }
    }

    fun hasUnknownLeaves(): Boolean =
        fold(false) { acc, vertex -> acc || vertex.structure is TvmDataCellStructure.Unknown }

    fun getTreeDataSize(): Map<UBoolExpr, UExpr<TvmSizeSort>> = with(ctx) {
        val result = mutableMapOf<UBoolExpr, UExpr<TvmSizeSort>>()
        onEachVertex { vertex ->
            when (vertex.structure) {
                is TvmDataCellStructure.Empty -> {
                    result[vertex.guard] = mkSizeSubExpr(vertex.prefixSize, initialOffset)
                }
                else -> {
                    // do nothing
                }
            }
        }
        return result
    }

    fun getTreeRefNumber(): Map<UBoolExpr, UExpr<TvmSizeSort>> = with(ctx) {
        val result = mutableMapOf<UBoolExpr, UExpr<TvmSizeSort>>()
        onEachVertex { vertex ->
            when (vertex.structure) {
                is TvmDataCellStructure.Empty -> {
                    result[vertex.guard] = mkSizeSubExpr(vertex.refNumber, initialRefNumber)
                }
                else -> {
                    // do nothing
                }
            }
        }
        return result
    }

    companion object {
        fun construct(
            state: TvmState,
            structure: TvmDataCellStructure,
            address: UHeapRef,
            guard: UBoolExpr = state.ctx.trueExpr,
            initialOffset: UExpr<TvmSizeSort> = state.ctx.zeroSizeExpr,
            initialRefNumber: UExpr<TvmSizeSort> = state.ctx.zeroSizeExpr,
        ): List<TvmDataCellInfoTree> {
            val (root, other) = constructVertex(
                state,
                structure,
                guard,
                address,
                prefixSize = initialOffset,
                refNumber = initialRefNumber,
            )
            val result = TvmDataCellInfoTree(state.ctx, address, root, initialOffset, initialRefNumber)
            return listOf(result) + other
        }

        private fun constructVertex(
            state: TvmState,
            structure: TvmDataCellStructure,
            guard: UBoolExpr,
            address: UHeapRef,
            prefixSize: UExpr<TvmSizeSort>,
            refNumber: UExpr<TvmSizeSort>,
        ): Pair<Vertex, List<TvmDataCellInfoTree>> =
            when (structure) {
                is TvmDataCellStructure.Empty, TvmDataCellStructure.Unknown -> {
                    Vertex(guard, structure, prefixSize, refNumber, emptyList()) to emptyList()
                }
                is TvmDataCellStructure.KnownTypePrefix -> {
                    constructVertexForKnownTypePrefix(structure, state, guard, address, prefixSize, refNumber)
                }
                is TvmDataCellStructure.SwitchPrefix -> {
                    constructVertexForSwitchPrefix(structure, state, guard, address, prefixSize, refNumber)
                }
                is TvmDataCellStructure.LoadRef -> {
                    constructVertexForLoadRef(structure, state, guard, address, prefixSize, refNumber)
                }
            }

        private fun constructOffset(
            offsetSet: Map<UBoolExpr, UExpr<TvmSizeSort>>,
        ) : UExpr<TvmSizeSort> {
            check(offsetSet.isNotEmpty()) {
                "internalTree must have at least one leaf"
            }
            var result = offsetSet.values.first()  // arbitrary value
            offsetSet.forEach { (condition, value) ->
                if (value == result) {
                    return@forEach
                }
                result = with(value.ctx) { mkIte(condition, value, result) }
            }
            return result
        }

        private fun constructVertexForKnownTypePrefix(
            structure: TvmDataCellStructure.KnownTypePrefix,
            state: TvmState,
            guard: UBoolExpr,
            address: UHeapRef,
            prefixSize: UExpr<TvmSizeSort>,
            refNumber: UExpr<TvmSizeSort>,
        ): Pair<Vertex, List<TvmDataCellInfoTree>> = with(state.ctx) {
            val newRootTrees = mutableListOf<TvmDataCellInfoTree>()
            val (offsets, internalTree) = when (structure.typeOfPrefix) {
                is TvmAtomicDataCellLabel -> {
                    (structure.typeOfPrefix.offset(state, address, prefixSize) to zeroSizeExpr) to null
                }
                is TvmCompositeDataCellLabel -> {
                    val treeList = construct(
                        state,
                        structure.typeOfPrefix.internalStructure,
                        address,
                        guard,
                        initialOffset = prefixSize,
                        initialRefNumber = refNumber,
                    )
                    check(treeList.isNotEmpty()) {
                        "At least one tree should have been constructed: the internal tree"
                    }
                    newRootTrees += treeList.drop(1)
                    val internalTree = treeList.first()
                    check(!internalTree.hasUnknownLeaves()) {
                        "Internal tree must not have unknown leaves"
                    }

                    val dataOffset = constructOffset(internalTree.getTreeDataSize())
                    val refOffset = constructOffset(internalTree.getTreeRefNumber())

                    (dataOffset to refOffset) to internalTree
                }
            }
            val (dataOffset, refOffset) = offsets
            val (child, other) = constructVertex(
                state,
                structure.rest,
                guard,
                address,
                mkSizeAddExpr(prefixSize, dataOffset),
                mkSizeAddExpr(refNumber, refOffset),
            )
            Vertex(guard, structure, prefixSize, refNumber, listOf(child), internalTree) to (other + newRootTrees)
        }

        private fun constructVertexForSwitchPrefix(
            structure: TvmDataCellStructure.SwitchPrefix,
            state: TvmState,
            guard: UBoolExpr,
            address: UHeapRef,
            prefixSize: UExpr<TvmSizeSort>,
            refNumber: UExpr<TvmSizeSort>,
        ): Pair<Vertex, List<TvmDataCellInfoTree>> = with(state.ctx) {
            val other = mutableListOf<TvmDataCellInfoTree>()
            val cellContent = state.memory.readField(address, cellDataField, cellDataSort)
            val newPrefixSize = mkSizeAddExpr(prefixSize, mkSizeExpr(structure.switchSize))
            val offsetFromEnd = mkSizeSubExpr(maxDataLengthSizeExpr, newPrefixSize)
            val shiftedCellContent = mkBvLogicalShiftRightExpr(cellContent, offsetFromEnd.zeroExtendToSort(cellDataSort))
            val prefix = mkBvExtractExpr(high = structure.switchSize - 1, low = 0, shiftedCellContent)
            val switchSize = structure.switchSize.toUInt()

            val children = structure.variants.entries.map { (key, selfRestVariant) ->
                val expectedPrefix = mkBv(key, switchSize)
                val prefixGuard = prefix eq expectedPrefix
                val newGuard = guard and prefixGuard
                val (child, childOther) = constructVertex(
                    state,
                    selfRestVariant,
                    newGuard,
                    address,
                    newPrefixSize,
                    refNumber,
                )
                other += childOther
                child
            }

            Vertex(guard, structure, prefixSize, refNumber, children) to other
        }

        private fun constructVertexForLoadRef(
            structure: TvmDataCellStructure.LoadRef,
            state: TvmState,
            guard: UBoolExpr,
            address: UHeapRef,
            prefixSize: UExpr<TvmSizeSort>,
            refNumber: UExpr<TvmSizeSort>,
        ): Pair<Vertex, List<TvmDataCellInfoTree>> = with(state.ctx) {
            val refAddress = state.readCellRef(address, refNumber)

            val other = when (structure.ref) {
                is TvmParameterInfo.DataCellInfo -> {
                    state.assertType(refAddress, TvmDataCellType)
                    construct(state, structure.ref.dataCellStructure, refAddress, guard)
                }

                is TvmParameterInfo.DictCellInfo -> {
                    state.assertType(refAddress, TvmDictCellType)
                    emptyList()
                }
            }

            val (child, childOther) = constructVertex(
                state,
                structure.selfRest,
                guard,
                address,
                prefixSize,
                mkSizeAddExpr(refNumber, oneSizeExpr),
            )

            Vertex(guard, structure, prefixSize, refNumber, listOf(child)) to (childOther + other)
        }
    }
}
