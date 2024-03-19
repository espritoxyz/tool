package org.ton.bytecode

import org.ton.bigint.BigInt
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.usvm.UHeapRef
import org.usvm.machine.state.TvmRegisters
import org.usvm.machine.state.TvmStack

interface TvmValue {
    val type: TvmType
}

data class TvmIntegerValue(val value: BigInt) : TvmValue {
    override val type: TvmIntegerType = TvmIntegerType
}

data class TvmCellValue(val value: UHeapRef) : TvmValue {
    override val type: TvmCellType = TvmCellType
}

data class TvmTupleValue(val value: Array<out Any>) : TvmValue {
    override val type: TvmTupleType = TvmTupleType

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TvmTupleValue

        return value.contentEquals(other.value)
    }

    override fun hashCode(): Int {
        var result = value.contentHashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    companion object {
        private val EMPTY = TvmTupleValue(emptyArray())

        fun empty(): TvmTupleValue = EMPTY.copy()
    }
}

object TvmNullValue : TvmValue {
    override val type: TvmNullType = TvmNullType
}

data class TvmSliceValue(val value: /*TvmCellClice*/CellSlice) : TvmValue {
    override val type: TvmSliceType = TvmSliceType
}

data class TvmBuilderValue(val value: CellBuilder) : TvmValue {
    override val type: TvmBuilderType = TvmBuilderType
}

// TODO how to represent a continuation value?
data class TvmContinuationValue(
//    val slice: TvmCellClice,
    val codeBlock: TvmCodeBlock,
    val stack: TvmStack,
    val registers: TvmRegisters,
    // TODO codepage and nargs
    var currentInstIndex: Int = 0
) : TvmValue {
    override val type: TvmContinuationType = TvmContinuationType
}

/*data class TvmCell(
    val code: List<TvmInst>,
    val refs: List<TvmCell>
) {
    companion object {
        private val EMPTY = TvmCell(emptyList(), emptyList())

        fun empty(): TvmCell = EMPTY.copy()
    }
}

data class TvmCellClice(
    val code: List<TvmInst>,
    val refs: List<TvmCell>
) {
    private var currentInstIndex: Int = 0
    private var currentRefIndex: Int = 0

    fun loadInst(): TvmInst = code[currentInstIndex++]
    fun loadRef(): TvmCell = refs[currentRefIndex++]
}*/

//data class Dictionary(val keyLength: Int, private val methods: Map<BigInt, TvmCell>) : Map<BigInt, TvmCell> by methods
//
//fun TvmCell.toDictionary(keyLength: Int): Dictionary = Dictionary(keyLength, TODO())
