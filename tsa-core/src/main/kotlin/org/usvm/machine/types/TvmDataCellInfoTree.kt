package org.usvm.machine.types

import org.ton.TvmAtomicDataCellLabel
import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmDataCellStructure
import org.ton.TvmParameterInfo
import org.usvm.UBoolExpr
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.api.readField
import org.usvm.machine.TvmContext.Companion.cellDataField
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.assertType
import org.usvm.machine.state.readCellRef
import org.usvm.mkSizeAddExpr
import org.usvm.mkSizeExpr

class TvmDataCellInfoTree private constructor(
    val address: UHeapRef,
    private val root: Vertex,
    val initialOffset: UExpr<TvmSizeSort>,
    val initialRefNumber: UExpr<TvmSizeSort>,
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

    fun getTreeDataSize(): Map<UBoolExpr, UExpr<TvmSizeSort>> {
        val result = mutableMapOf<UBoolExpr, UExpr<TvmSizeSort>>()
        onEachVertex { vertex ->
            when (vertex.structure) {
                is TvmDataCellStructure.Empty -> {
                    result[vertex.guard] = vertex.prefixSize
                }
                else -> {
                    // do nothing
                }
            }
        }
        return result
    }

    fun getTreeRefNumber(): Map<UBoolExpr, UExpr<TvmSizeSort>> {
        val result = mutableMapOf<UBoolExpr, UExpr<TvmSizeSort>>()
        onEachVertex { vertex ->
            when (vertex.structure) {
                is TvmDataCellStructure.Empty -> {
                    result[vertex.guard] = vertex.refNumber
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
            val result = TvmDataCellInfoTree(address, root, initialOffset, initialRefNumber)
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
                    require(treeList.isNotEmpty())
                    newRootTrees += treeList.drop(1)
                    val internalTree = treeList.first()
                    require(!internalTree.hasUnknownLeaves()) {
                        "Internal tree must not have unknown leaves"
                    }

                    val dataOffsets = internalTree.getTreeDataSize()
                    require(dataOffsets.isNotEmpty())
                    var dataOffset = dataOffsets.values.first()  // arbitrary value
                    dataOffsets.forEach { (condition, value) ->
                        if (value == dataOffset)
                            return@forEach
                        dataOffset = mkIte(condition, value, dataOffset)
                    }

                    val refOffsets = internalTree.getTreeRefNumber()
                    require(refOffsets.isNotEmpty())
                    var refOffset = refOffsets.values.first()  // arbitrary value
                    refOffsets.forEach { (condition, value) ->
                        if (value == dataOffset)
                            return@forEach
                        refOffset = mkIte(condition, value, refOffset)
                    }

                    (dataOffset to refOffset) to internalTree
                }
                else -> {
                    error("Not reachable. (Why doesn't compiler infer that?)")
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
            val prefix = mkBvExtractExpr(high = structure.switchSize - 1, low = 0, cellContent)
            val switchSize = structure.switchSize.toUInt()
            val newPrefixSize = mkSizeAddExpr(prefixSize, mkSizeExpr(structure.switchSize))

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
