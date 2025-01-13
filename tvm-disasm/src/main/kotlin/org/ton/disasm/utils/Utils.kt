package org.ton.disasm.utils

import org.ton.cell.CellSlice

fun CellSlice.copy(): CellSlice =
    CellSlice.of(bits, refs).also {
        it.bitsPosition = bitsPosition
        it.refsPosition = refsPosition
    }
