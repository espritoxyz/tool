package org.usvm.machine.types

import org.ton.TvmDataCellStructure
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
import org.usvm.api.readField
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.Companion.cellDataField
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.readCellRef
import org.usvm.mkSizeExpr

class TvmDataCellInfoTree private constructor(
    val lazyAddress: (TvmState) -> UConcreteHeapRef,
    private val root: Vertex,
) {
    fun <Acc> fold(init: Acc, f: (Acc, Vertex) -> Acc): Acc =
        root.fold(init, f)

    class Vertex(
        val lazyGuard: (TvmState) -> UBoolExpr,
        val structure: TvmDataCellStructure,
        val prefixSize: Int,  // TODO: symbolic values are probably possible here
        private val children: List<Vertex>
    ) {
        internal fun <Acc> fold(init: Acc, f: (Acc, Vertex) -> Acc): Acc {
            return children.fold(f(init, this)) { acc, child -> child.fold(acc, f) }
        }
    }

    companion object {
        fun construct(
            ctx: TvmContext,
            structure: TvmDataCellStructure,
            lazyAddress: (TvmState) -> UConcreteHeapRef,
            lazyGuard: (TvmState) -> UBoolExpr = { ctx.trueExpr }
        ): List<TvmDataCellInfoTree> {
            val (root, other) = constructVertex(ctx, structure, lazyGuard, lazyAddress, 0)
            val result = TvmDataCellInfoTree(lazyAddress, root)
            return listOf(result) + other
        }

        private fun constructVertex(
            ctx: TvmContext,
            structure: TvmDataCellStructure,
            lazyGuard: (TvmState) -> UBoolExpr,
            lazyAddress: (TvmState) -> UConcreteHeapRef,
            prefixSize: Int,
        ): Pair<Vertex, List<TvmDataCellInfoTree>> = with(ctx) {
            when (structure) {
                is TvmDataCellStructure.Empty, TvmDataCellStructure.Unknown -> {
                    Vertex(lazyGuard, structure, prefixSize, emptyList()) to emptyList()
                }
                is TvmDataCellStructure.KnownTypePrefix -> {
                    val (child, other) = constructVertex(
                        ctx,
                        structure.rest,
                        lazyGuard,
                        lazyAddress,
                        prefixSize + structure.typeOfPrefix.bitSize
                    )
                    Vertex(lazyGuard, structure, prefixSize, listOf(child)) to other
                }
                is TvmDataCellStructure.SwitchPrefix -> {
                    val other = mutableListOf<TvmDataCellInfoTree>()
                    val lazyPrefix = { state: TvmState ->
                        val address = lazyAddress(state)
                        val cellContent = state.memory.readField(address, cellDataField, cellDataSort)
                        mkBvExtractExpr(high = structure.switchSize - 1, low = 0, cellContent)
                    }
                    val children = structure.variants.entries.map { (key, variant) ->
                        val expectedPrefix = mkBv(key, structure.switchSize.toUInt())
                        val newGuard = { state: TvmState ->
                            val prefix = lazyPrefix(state)
                            lazyGuard(state) and (prefix eq expectedPrefix)
                        }
                        val newOther = variant.refs.flatMapIndexed { index, refStructure ->
                            val refAddress = { state: TvmState ->
                                val address = lazyAddress(state)
                                state.readCellRef(address, mkSizeExpr(index)) as UConcreteHeapRef
                            }
                            construct(ctx, refStructure, refAddress, lazyGuard = newGuard)
                        }
                        other += newOther
                        val (child, childOther) = constructVertex(
                            ctx,
                            variant.selfRest,
                            newGuard,
                            lazyAddress,
                            prefixSize + structure.switchSize
                        )
                        other += childOther
                        child
                    }
                    Vertex(lazyGuard, structure, prefixSize, children) to other
                }
            }
        }
    }
}