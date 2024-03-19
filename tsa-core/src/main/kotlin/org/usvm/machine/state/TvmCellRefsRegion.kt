package org.usvm.machine.state

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapAddress
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.isTrue
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.mapWithStaticAsConcrete
import org.usvm.memory.ULValue
import org.usvm.memory.UMemoryRegion
import org.usvm.memory.UMemoryRegionId
import org.usvm.memory.UReadOnlyMemory
import org.usvm.memory.foldHeapRef
import org.usvm.uctx

object TvmCellRefsRegionLValue : ULValue<TvmCellRefsRegionLValue, Nothing> {
    override val key: TvmCellRefsRegionLValue
        get() = this

    override val memoryRegionId: UMemoryRegionId<TvmCellRefsRegionLValue, Nothing>
        get() = TvmCellRefsRegionId

    override val sort: Nothing
        get() = error("TvmCellRefsRegion sort should not be used")
}

object TvmCellRefsRegionId : UMemoryRegionId<TvmCellRefsRegionLValue, Nothing> {
    override fun emptyRegion(): UMemoryRegion<TvmCellRefsRegionLValue, Nothing> = TvmCellRefsRegion()

    override val sort: Nothing
        get() = error("TvmCellRefsRegion sort should not be used")
}

class TvmCellRefsRegion(
    private val cellRefs: PersistentMap<UConcreteHeapAddress, TvmCellRefsRegionUpdateNode> = persistentHashMapOf()
) : UMemoryRegion<TvmCellRefsRegionLValue, Nothing> {
    override fun read(key: TvmCellRefsRegionLValue) = error("Use readCellRef")

    override fun write(
        key: TvmCellRefsRegionLValue,
        value: UExpr<Nothing>,
        guard: UBoolExpr
    ) = error("Use writeCellRef")

    fun readCellRef(
        cell: UHeapRef,
        idx: UExpr<TvmSizeSort>,
        generateSymbolicCell: () -> UHeapRef
    ): Pair<UHeapRef, TvmCellRefsRegion> {
        var region = this
        val result = cell.mapWithStaticAsConcrete(
            concreteMapper = { ref ->
                val (res, updatedRegion) = region.readCellRef(ref.address, idx, generateSymbolicCell)
                region = updatedRegion
                res
            },
            symbolicMapper = { ref -> error("Unexpected input cell $ref") }
        )
        return result to region
    }

    fun writeCellRef(cell: UHeapRef, idx: UExpr<TvmSizeSort>, ref: UHeapRef, guard: UBoolExpr): TvmCellRefsRegion =
        foldHeapRef(
            ref = cell,
            initial = this,
            initialGuard = guard,
            blockOnConcrete = { region, (cellRef, refGuard) ->
                region.writeCellRef(cellRef.address, idx, ref, refGuard)
            },
            blockOnStatic = { region, (cellRef, refGuard) ->
                region.writeCellRef(cellRef.address, idx, ref, refGuard)
            },
            blockOnSymbolic = { _, (ref, _) -> error("Unexpected input cell $ref") }
        )


    private fun readCellRef(
        cell: UConcreteHeapAddress,
        idx: UExpr<TvmSizeSort>,
        generateSymbolicCell: () -> UHeapRef
    ): Pair<UHeapRef, TvmCellRefsRegion> {
        val initialNode = cellRefs[cell]
        val (result, updatedNode) = readCellRef(initialNode, idx, generateSymbolicCell)

        if (updatedNode == null) {
            return result to this
        }

        return result to TvmCellRefsRegion(cellRefs.put(cell, updatedNode))
    }

    private fun writeCellRef(
        cell: UConcreteHeapAddress,
        idx: UExpr<TvmSizeSort>,
        ref: UHeapRef,
        guard: UBoolExpr
    ): TvmCellRefsRegion {
        val initialNode = cellRefs[cell]
        val updatedNode = writeCellRef(initialNode, idx, ref, guard)
        return TvmCellRefsRegion(cellRefs.put(cell, updatedNode))
    }

    private fun readCellRef(
        node: TvmCellRefsRegionUpdateNode?,
        idx: UExpr<TvmSizeSort>,
        generateSymbolicCell: () -> UHeapRef
    ): Pair<UHeapRef, TvmCellRefsRegionUpdateNode?> = with(idx.uctx) {
        if (node == null) {
            val newCell = generateSymbolicCell()
            val newNode = TvmCellRefsRegionUpdateNode(idx, newCell, guard = trueExpr, prevUpdate = null)
            return newCell to newNode
        }

        val nodeIncludesIndex = mkAnd(mkEq(idx, node.idx), node.guard)
        if (nodeIncludesIndex.isTrue) {
            return node.ref to null
        }

        val (prevResult, updatedPrevNode) = readCellRef(node.prevUpdate, idx, generateSymbolicCell)

        val resultWithNode = mkIte(nodeIncludesIndex, { node.ref }, { prevResult })
        val updatedNode = updatedPrevNode?.let { node.copy(prevUpdate = it) }

        return resultWithNode to updatedNode
    }

    private fun writeCellRef(
        node: TvmCellRefsRegionUpdateNode?,
        idx: UExpr<TvmSizeSort>,
        ref: UHeapRef,
        guard: UBoolExpr
    ): TvmCellRefsRegionUpdateNode = TvmCellRefsRegionUpdateNode(idx, ref, guard, node)

    data class TvmCellRefsRegionUpdateNode(
        val idx: UExpr<TvmSizeSort>,
        val ref: UHeapRef,
        val guard: UBoolExpr,
        val prevUpdate: TvmCellRefsRegionUpdateNode?
    )
}

fun UReadOnlyMemory<*>.tvmCellRefsRegion(): TvmCellRefsRegion =
    getRegion(TvmCellRefsRegionId) as TvmCellRefsRegion

fun TvmState.readCellRef(cell: UHeapRef, refIdx: UExpr<TvmSizeSort>): UHeapRef {
    val region = memory.tvmCellRefsRegion()
    val (result, updatedRegion) = region.readCellRef(cell, refIdx) {
        generateSymbolicCell()
    }
    memory.setRegion(TvmCellRefsRegionId, updatedRegion)
    return result
}

fun TvmState.writeCellRef(
    cell: UHeapRef,
    refIdx: UExpr<TvmSizeSort>,
    ref: UHeapRef,
    guard: UBoolExpr = ref.ctx.trueExpr
) {
    val region = memory.tvmCellRefsRegion()
    val updatedRegion = region.writeCellRef(cell, refIdx, ref, guard)
    memory.setRegion(TvmCellRefsRegionId, updatedRegion)
}
