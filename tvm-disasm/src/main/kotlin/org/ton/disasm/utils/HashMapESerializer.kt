package org.ton.disasm.utils

import org.ton.bitstring.BitString
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCodec

internal data object HashMapESerializer : TlbCodec<Cell> {
    override fun loadTlb(cellSlice: CellSlice): Cell {
        return Cell(
            BitString(cellSlice.bits.drop(cellSlice.bitsPosition)),
            *cellSlice.refs.drop(cellSlice.refsPosition).toTypedArray()
        )
    }

    override fun storeTlb(cellBuilder: CellBuilder, value: Cell) {
        error("Should not be called")
    }
}