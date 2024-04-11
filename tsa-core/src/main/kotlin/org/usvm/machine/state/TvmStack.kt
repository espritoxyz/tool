package org.usvm.machine.state

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentMap
import org.ton.bytecode.TvmBuilderType
import org.ton.bytecode.TvmCellType
import org.ton.bytecode.TvmContinuationType
import org.ton.bytecode.TvmContinuationValue
import org.ton.bytecode.TvmIntegerType
import org.ton.bytecode.TvmNullType
import org.ton.bytecode.TvmSliceType
import org.ton.bytecode.TvmTupleType
import org.ton.bytecode.TvmType
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.USort
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.TvmInt257Sort
import kotlin.math.max

class TvmStack(
    val ctx: TvmContext,
    private var stack: PersistentList<TvmStackEntry> = persistentListOf(), // [n n-1 n-2 ... 2 1 0]
    var inputElements: PersistentList<TvmInputStackEntry> = persistentListOf(),
) {
    private inline val size: Int get() = stack.size

    fun takeLast(expectedType: TvmType, createEntry: (Int) -> UExpr<out USort>): TvmStackValue {
        val lastEntry = takeLastEntry()

        return getStackValue(lastEntry, expectedType, createEntry)
    }

    fun takeLastEntry(): TvmStackEntry {
        extendStack(1)
        val lastStackEntry = stack.last()
        stack = stack.removeAt(size - 1)
        return lastStackEntry
    }

    fun addStackEntry(entry: TvmStackEntry) {
        stack = stack.add(entry)
    }

    fun lastIsNull(): Boolean = stack.lastOrNull().let {
        it is TvmConcreteStackEntry && it.cell is TvmStackNullValue
    }

    fun add(value: UExpr<out USort>, type: TvmType) {
        // TODO check size 256?
        stack = stack.add(value.toStackValue(type).toStackEntry())
    }

    fun addContinuation(value: TvmContinuationValue) {
        stack = stack.add(TvmStackContinuationValue(value).toStackEntry())
    }

    fun addTuple(value: TvmStackTupleValue) {
        stack = stack.add(value.toStackEntry())
    }

    fun swap(first: Int, second: Int) {
        val maxDepth = max(first, second)
        val newSize = maxDepth + 1

        extendStack(newSize)
        swapImpl(stackIndex(first), stackIndex(second))
    }

    /**
     * Drops [i] stack elements under the top [j] elements.
     */
    fun blkDrop2(i: Int, j: Int) {
        val newSize = i + j
        extendStack(newSize)

        val topElements = mutableListOf<TvmStackEntry>()
        for (k in 0 until newSize) {
            val topElement = stack.last()
            stack = stack.removeAt(size - 1)
            if (k < j) {
                topElements += topElement
            }
        }

        topElements.asReversed().forEach { stack = stack.add(it) }
    }

    /**
     * Reverses the order of s[j+i+1] ... s[j].
     * @param i -- number of stack entries to reverse
     * @param j -- offset before first reversed entry
     * */
    fun reverse(i: Int, j: Int) {
        extendStack(i + j)
        stack = stack.mutate { stack ->
            val blockStart = stack.size - j
            val reversedBlock = stack.subList(blockStart - i, blockStart).toList()
            reversedBlock.indices.forEach { stack[blockStart - 1 - it] = reversedBlock[it] }
        }
    }

    /**
     * Pushes a copy of the old s[i] into the stack.
     */
    fun push(i: Int) {
        val newSize = i + 1
        extendStack(newSize)

        // TODO should add copy, not the same ref
        val element = stack[stackIndex(i)]
        stack = stack.add(element)
    }

    /**
     * Pops the old s0 value into the old s[i].
     */
    fun pop(i: Int) {
        // Stack should contain at least (i + 1) elements to be able to remove i-th element
        val newSize = i + 1
        extendStack(newSize)

        val lastIdx = stackIndex(0)
        val value = stack[lastIdx]
        val removedLast = stack.removeAt(lastIdx)
        stack = if (i != 0) {
            removedLast.set(lastIdx - i, value)
        } else {
            removedLast
        }
    }

    private fun extendStack(newSize: Int) {
        if (size >= newSize) {
            return
        }

        val newValuesSize = newSize - size
        val newValues = List(newValuesSize) {
            TvmInputStackEntry(id = ctx.nextInputStackEntryId(), cell = null)
        }
        inputElements = inputElements.addAll(newValues)
        stack = stack.addAll(0, newValues.asReversed()) // reversed because the "newest" values are at the beginning
    }

    private fun stackIndex(i: Int): Int = size - i - 1

    private fun swapImpl(first: Int, second: Int) {
        val tmp = stack[first]
        stack = stack.set(first, stack[second])
        stack = stack.set(second, tmp)
    }

    fun clone(): TvmStack = TvmStack(ctx, stack, inputElements)

    // TODO continuations
    sealed interface TvmStackValue {
        val continuationValue: TvmContinuationValue get() = error("Cannot extract continuation from stack value $this")
        val intValue: UExpr<TvmInt257Sort> get() = error("Cannot extract int from stack value $this")
        val tupleValue: TvmStackTupleValue get() = error("Cannot extract tuple from stack value $this")
        val cellValue: UHeapRef get() = error("Cannot extract cell from stack value $this")
        val sliceValue: UHeapRef get() = error("Cannot extract slice from stack value $this")
        val builderValue: UHeapRef get() = error("Cannot extract builder from stack value $this")
        val isNull: Boolean get() = false
    }
    data class TvmStackContinuationValue(override val continuationValue: TvmContinuationValue) : TvmStackValue
    data class TvmStackIntValue(override val intValue: UExpr<TvmInt257Sort>): TvmStackValue

    sealed interface TvmStackTupleValue : TvmStackValue {
        override val tupleValue: TvmStackTupleValue get() = this
        val size: UExpr<TvmInt257Sort>

        operator fun get(idx: Int, stack: TvmStack): TvmStackEntry
        operator fun set(idx: Int, value: TvmStackEntry): TvmStackTupleValue
    }

    data class TvmStackTupleValueConcreteNew(val ctx: TvmContext, val entries: PersistentList<TvmStackEntry>) : TvmStackTupleValue {
        val concreteSize: Int = entries.size

        override val size: UExpr<TvmInt257Sort> get() = with(ctx) { concreteSize.toBv257() }
        override operator fun get(idx: Int, stack: TvmStack): TvmStackEntry = entries[idx]
        override operator fun set(idx: Int, value: TvmStackEntry): TvmStackTupleValueConcreteNew = copy(entries = entries.set(idx, value))
    }

    sealed interface TvmStackTupleValueInputValue : TvmStackTupleValue

    data class TvmStackTupleValueInputNew(
        var entries: MutableMap<Int, TvmInputStackEntry> = mutableMapOf(),
        override val size: UExpr<TvmInt257Sort> // id -> ctx.mkRegisterReading(id, ctx.int257sort)
    ): TvmStackTupleValueInputValue {
        override operator fun get(idx: Int, stack: TvmStack): TvmInputStackEntry = entries.getOrPut(idx) {
            TvmInputStackEntry(id = stack.ctx.nextInputStackEntryId(), cell = null).also {
                stack.inputElements = stack.inputElements.add(it)
            }
        }

        override operator fun set(idx: Int, value: TvmStackEntry): TvmStackTupleValueModifiedInputNew {
            // TODO should we increase size?
            return TvmStackTupleValueModifiedInputNew(
                entries.toPersistentMap<Int, TvmStackEntry>().put(idx, value),
                this
            )
        }
    }

    data class TvmStackTupleValueModifiedInputNew(
        var entries: PersistentMap<Int, TvmStackEntry>,
        val original: TvmStackTupleValueInputNew
    ) : TvmStackTupleValueInputValue {
        override val size: UExpr<TvmInt257Sort> = original.size

        override operator fun get(idx: Int, stack: TvmStack): TvmStackEntry {
            entries[idx]?.let { return it }

            val inputEntry = original[idx, stack]
            stack.inputElements = stack.inputElements.add(inputEntry)
            entries = entries.put(idx, inputEntry)

            return inputEntry
        }

        override operator fun set(idx: Int, value: TvmStackEntry): TvmStackTupleValueModifiedInputNew {
            // TODO should we increase size?
            return copy(entries = entries.put(idx, value))
        }
    }


    data class TvmStackCellValue(override val cellValue: UHeapRef): TvmStackValue
    data object TvmStackNullValue : TvmStackValue {
        override val isNull: Boolean get() = true
    }
    data class TvmStackSliceValue(override val sliceValue: UHeapRef): TvmStackValue
    data class TvmStackBuilderValue(override val builderValue: UHeapRef): TvmStackValue
///...

    sealed interface TvmStackEntry {
        val cell: TvmStackValue?
    }
    data class TvmConcreteStackEntry(override val cell: TvmStackValue): TvmStackEntry
    data class TvmInputStackEntry(val id: Int, override var cell: TvmStackValue?): TvmStackEntry

    private fun getStackValue(
        entry: TvmStackEntry,
        expectedType: TvmType,
        createEntry: (Int) -> UExpr<out USort>
    ): TvmStackValue {
        require(expectedType !is TvmNullType) {
            "Unexpected reading NULL from the stack"
        }

        val cell = when (entry) {
            is TvmConcreteStackEntry -> entry.cell
            is TvmInputStackEntry -> {
                entry.cell ?: run {
                    val stackValue = createEntry(entry.id)
                    stackValue.toStackValue(expectedType).also { entry.cell = it }
                }
            }
        }

        return cell
    }

    @Suppress("UNCHECKED_CAST")
    private fun UExpr<*>.toStackValue(expectedType: TvmType): TvmStackValue = when (expectedType) {
        is TvmIntegerType -> TvmStackIntValue(this as UExpr<TvmInt257Sort>)
        TvmBuilderType -> TvmStackBuilderValue(this as UHeapRef)
        TvmCellType -> TvmStackCellValue(this as UHeapRef)
        TvmContinuationType -> TODO("Unexpected $this for constructing stack value of $TvmContinuationType")
        TvmNullType -> TvmStackNullValue
        TvmSliceType -> TvmStackSliceValue(this as UHeapRef)
        TvmTupleType -> TODO("Unexpected $this for constructing stack value of $TvmTupleType")
    }

    private fun TvmStackValue.toStackEntry(): TvmConcreteStackEntry = TvmConcreteStackEntry(this)
}
