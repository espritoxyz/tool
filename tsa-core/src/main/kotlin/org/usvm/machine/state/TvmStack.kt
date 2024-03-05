package org.usvm.machine.state

import io.ksmt.utils.uncheckedCast
import org.usvm.UExpr
import org.usvm.USort
import org.usvm.machine.TvmContext
import kotlin.math.max

class TvmStack(
    private val ctx: TvmContext,
    private val stack: MutableList<UExpr<out USort>?> = mutableListOf(), // [n n-1 n-2 ... 2 1 0]
) {
    private var stackElementIndex: Int = 0
    private inline val size: Int get() = stack.size

    fun <T : USort> removeLast(sort: T): UExpr<T> {
        extendStack(1)

        val lastStackValue = stack.removeLast() ?: ctx.mkRegisterReading(stackElementIndex++, sort)
        return lastStackValue.uncheckedCast()
    }

    operator fun plusAssign(value: UExpr<out USort>) {
        // TODO check size 256?
        stack += value
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

        val topElements = mutableListOf<UExpr<out USort>?>()
        for (k in 0 until newSize) {
            val topElement = stack.removeLast()
            if (k < j) {
                topElements += topElement
            }
        }

        topElements.asReversed().forEach { stack += it }
    }

    /**
     * Pushes a copy of the old s[i] into the stack.
     */
    fun push(i: Int) {
        val newSize = i + 1
        extendStack(newSize)

        val element = stack[stackIndex(i)]
        stack += element
    }

    /**
     * Pops the old s0 value into the old s[i].
     */
    fun pop(i: Int) {
        // Stack should contain at least (i + 1) elements to be able to remove i-th element
        val newSize = i + 1
        extendStack(newSize)

        stack.removeAt(stackIndex(i))
    }

    private fun extendStack(newSize: Int) {
        if (size >= newSize) {
            return
        }

        val newValuesSize = newSize - size
        stack.addAll(0, List<UExpr<out USort>?>(newValuesSize) { null })
    }

    private fun stackIndex(i: Int): Int = size - i - 1

    private fun swapImpl(first: Int, second: Int) {
        val tmp = stack[first]
        stack[first] = stack[second]
        stack[second] = tmp
    }

    fun clone(): TvmStack = TvmStack(ctx, stack.toMutableList())
}
