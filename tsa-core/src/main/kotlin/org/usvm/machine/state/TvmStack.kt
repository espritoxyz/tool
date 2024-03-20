package org.usvm.machine.state

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.ton.bytecode.TvmBuilderType
import org.ton.bytecode.TvmCellType
import org.ton.bytecode.TvmContinuationType
import org.ton.bytecode.TvmContinuationValue
import org.ton.bytecode.TvmIntegerType
import org.ton.bytecode.TvmNullType
import org.ton.bytecode.TvmSliceType
import org.ton.bytecode.TvmTupleType
import org.ton.bytecode.TvmType
import org.usvm.UBvSort
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.USort
import org.usvm.machine.TvmContext
import kotlin.math.max

class TvmStack(
    private val ctx: TvmContext,
    private var stack: PersistentList<TvmStackEntry> = persistentListOf(), // [n n-1 n-2 ... 2 1 0]
) {
    private var inputElements: PersistentList<TvmInputStackEntry> = persistentListOf()
    private inline val size: Int get() = stack.size

    val stackContents: List<TvmStackEntry> get() = stack

    fun takeLast(expectedType: TvmType, createEntry: (Int) -> UExpr<out USort>): TvmStackValue {
        extendStack(1)

        val lastStackEntry = stack.last()
        stack = stack.removeAt(size - 1)

        return getStackValue(lastStackEntry, expectedType, createEntry)
    }

    fun add(value: UExpr<out USort>, type: TvmType) {
        // TODO check size 256?
        stack = stack.add(value.toStackValue(type).toStackEntry())
    }

    operator fun plusAssign(value: TvmContinuationValue) {
        stack = stack.add(TvmStackContinuationValue(value).toStackEntry())
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
            TvmInputStackEntry(id = inputElements.size + it, cell = null)
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

    fun clone(): TvmStack = TvmStack(ctx, stack)

    // TODO continuations
    sealed interface TvmStackValue {
        val continuationValue: TvmContinuationValue get() = error("Cannot extract continuation from stack value $this")
        val intValue: UExpr<UBvSort> get() = error("Cannot extract int from stack value $this")
        val tupleValue: UHeapRef get() = error("Cannot extract tuple from stack value $this")
        val cellValue: UHeapRef get() = error("Cannot extract cell from stack value $this")
        val sliceValue: UHeapRef get() = error("Cannot extract slice from stack value $this")
        val builderValue: UHeapRef get() = error("Cannot extract builder from stack value $this")
        val isNull: Boolean get() = false
    }
    data class TvmStackContinuationValue(override val continuationValue: TvmContinuationValue) : TvmStackValue
    data class TvmStackIntValue(override val intValue: UExpr<UBvSort>): TvmStackValue
    data class TvmStackTupleValue(override val tupleValue: UHeapRef): TvmStackValue
    data class TvmStackCellValue(override val cellValue: UHeapRef): TvmStackValue
    data object TvmStackNullValue : TvmStackValue {
        override val isNull: Boolean get() = true
    }

    /*data class TvmStackSliceValue(
        val cell: UHeapRef,
        val dataPos: Uvalue<USizeSort>,
        val refPos: UExpr<USizeSort>,
        val dataLength: UExpr<USizeSort>,
        val refsLength: UExpr<USizeSort>,
    ): TvmStackValue
    data class TvmStackBuilderValue(
        val data: UExpr<UBvSort>,
        val refs: UHeapRef,
        val dataLength: UExpr<USizeSort>,
        val refsLength: UExpr<USizeSort>,
    ): TvmStackValue*/
    data class TvmStackSliceValue(override val sliceValue: UHeapRef): TvmStackValue
    data class TvmStackBuilderValue(override val builderValue: UHeapRef): TvmStackValue
///...

    sealed interface TvmStackEntry
    data class TvmConcreteStackEntry(val cell: TvmStackValue): TvmStackEntry
    data class TvmInputStackEntry(val id: Int, var cell: TvmStackValue?): TvmStackEntry

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
        is TvmIntegerType -> TvmStackIntValue(this as UExpr<UBvSort>)
        TvmBuilderType -> TvmStackBuilderValue(this as UHeapRef)
        TvmCellType -> TvmStackCellValue(this as UHeapRef)
        TvmContinuationType -> TODO()
        TvmNullType -> TvmStackNullValue
        TvmSliceType -> TvmStackSliceValue(this as UHeapRef)
        TvmTupleType -> TvmStackTupleValue(this as UHeapRef)
    }

    private fun TvmStackValue.toStackEntry(): TvmConcreteStackEntry = TvmConcreteStackEntry(this)
}
