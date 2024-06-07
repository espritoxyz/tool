package org.usvm.machine.types

import org.ton.TvmDataCellStructure
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef

class TvmDataCellInfoTree(
    val address: UConcreteHeapRef,
    val treeGuard: UBoolExpr,
) {
    fun <Acc> fold(init: Acc, f: (Acc, Vertex) -> Acc): Acc {
        TODO()
    }

    class Vertex(
        val guard: UBoolExpr,
        val structure: TvmDataCellStructure
    )
}