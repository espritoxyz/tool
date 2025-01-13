package org.ton.disasm.trie

import kotlinx.serialization.decodeFromString
import org.ton.bitstring.BitString
import org.ton.cell.Cell
import org.ton.disasm.bytecode.InstructionDescription
import org.ton.disasm.bytecode.InstructionsList
import org.ton.disasm.bytecode.specJson
import org.ton.disasm.bytecode.validateCellOperandTypes
import java.io.InputStream

internal class TrieMap private constructor(
    val root: TrieMapVertex = TrieMapVertex(inst = null)
) {
    private fun addInstruction(bitString: BitString, inst: InstructionDescription) {
        var position = root
        bitString.forEach { bit ->
            position = position.step(bit)
                ?: position.addChild(bit)
        }
        check(position.inst == null) {
            "Cannot add instruction $inst with prefix $bitString: the prefix is already occupied with ${position.inst}"
        }
        position.inst = inst
    }

    companion object {
        fun construct(specStream: InputStream): TrieMap {
            val spec = specStream.bufferedReader().use {
                it.readText()
            }
            val parsed = specJson.decodeFromString<InstructionsList>(spec)

            validateCellOperandTypes(parsed)

            val result = TrieMap()

            val instructions = parsed.instructions
            instructions.forEach { instruction ->
                val prefix = instruction.bytecode.prefix
                val prefixAsCell = Cell.of(prefix)
                result.addInstruction(prefixAsCell.bits, instruction)
            }

            return result
        }
    }
}
