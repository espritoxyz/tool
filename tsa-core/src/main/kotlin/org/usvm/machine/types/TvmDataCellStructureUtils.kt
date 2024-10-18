package org.usvm.machine.types

import org.ton.TvmDataCellStructure

fun <Acc> TvmDataCellStructure.fold(
    init: Acc,
    f: (Acc, TvmDataCellStructure) -> Acc
): Acc {
    val cur = f(init, this)
    return when (this) {
        is TvmDataCellStructure.KnownTypePrefix -> rest.fold(cur, f)
        is TvmDataCellStructure.LoadRef -> selfRest.fold(cur, f)
        is TvmDataCellStructure.SwitchPrefix -> variants.values.fold(cur) { acc, struct -> struct.fold(acc, f) }
        is TvmDataCellStructure.Unknown, is TvmDataCellStructure.Empty -> cur
    }
}

fun TvmDataCellStructure.forEach(f: (TvmDataCellStructure) -> Unit) =
    fold(Unit) { _, struct -> f(struct) }