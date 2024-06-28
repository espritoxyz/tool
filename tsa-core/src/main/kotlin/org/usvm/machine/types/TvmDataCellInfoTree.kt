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
        val refNumber: Int,
        private val children: List<Vertex>,
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
            val (root, other) = constructVertex(
                ctx,
                structure,
                lazyGuard,
                lazyAddress,
                prefixSize = 0,
                refNumber = 0,
            )
            val result = TvmDataCellInfoTree(lazyAddress, root)
            return listOf(result) + other
        }

        private fun constructVertex(
            ctx: TvmContext,
            structure: TvmDataCellStructure,
            lazyGuard: (TvmState) -> UBoolExpr,
            lazyAddress: (TvmState) -> UConcreteHeapRef,
            prefixSize: Int,
            refNumber: Int,
        ): Pair<Vertex, List<TvmDataCellInfoTree>> = with(ctx) {
            when (structure) {
                is TvmDataCellStructure.Empty, TvmDataCellStructure.Unknown -> {
                    Vertex(lazyGuard, structure, prefixSize, refNumber, emptyList()) to emptyList()
                }
                is TvmDataCellStructure.KnownTypePrefix -> {
                    val (child, other) = constructVertex(
                        ctx,
                        structure.rest,
                        lazyGuard,
                        lazyAddress,
                        prefixSize + structure.typeOfPrefix.bitSize,
                        refNumber,
                    )
                    Vertex(lazyGuard, structure, prefixSize, refNumber, listOf(child)) to other
                }
                is TvmDataCellStructure.SwitchPrefix -> {
                    val other = mutableListOf<TvmDataCellInfoTree>()
                    val lazyPrefix = { state: TvmState ->
                        val address = lazyAddress(state)
                        val cellContent = state.memory.readField(address, cellDataField, cellDataSort)
                        require(structure.switchSize > 0)
                        mkBvExtractExpr(high = structure.switchSize - 1, low = 0, cellContent)
                    }
                    val children = structure.variants.entries.map { (key, selfRestVariant) ->
                        val expectedPrefix = mkBv(key, structure.switchSize.toUInt())
                        val newGuard = { state: TvmState ->
                            val prefixGuard = expectedPrefix.let {
                                val prefix = lazyPrefix(state)
                                prefix eq expectedPrefix
                            }
                            lazyGuard(state) and prefixGuard
                        }
                        val (child, childOther) = constructVertex(
                            ctx,
                            selfRestVariant,
                            newGuard,
                            lazyAddress,
                            prefixSize + structure.switchSize,
                            refNumber,
                        )
                        other += childOther
                        child
                    }
                    Vertex(lazyGuard, structure, prefixSize, refNumber, children) to other
                }
                is TvmDataCellStructure.LoadRef -> {
                    val refAddress = { state: TvmState ->
                        val address = lazyAddress(state)
                        state.readCellRef(address, mkSizeExpr(refNumber)) as UConcreteHeapRef
                    }
                    val other = construct(ctx, structure.ref, refAddress, lazyGuard)
                    val (child, childOther) = constructVertex(
                        ctx,
                        structure.selfRest,
                        lazyGuard,
                        lazyAddress,
                        prefixSize,
                        refNumber + 1,
                    )
                    Vertex(lazyGuard, structure, prefixSize, refNumber, listOf(child)) to (childOther + other)
                }
            }
        }
    }
}
