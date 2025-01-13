package org.ton.disasm.trie

import org.ton.disasm.bytecode.InstructionDescription

internal class TrieMapVertex(
    var inst: InstructionDescription?,
    private var zeroBitChild: TrieMapVertex? = null,
    private var oneBitChild: TrieMapVertex? = null,
) {
    fun step(nextBit: Boolean): TrieMapVertex? = if (nextBit) {
        oneBitChild
    } else {
        zeroBitChild
    }

    fun addChild(bit: Boolean): TrieMapVertex {
        return TrieMapVertex(inst = null).also {
            if (bit) {
                oneBitChild = it
            } else {
                zeroBitChild = it
            }
        }
    }
}
