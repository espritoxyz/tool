package org.usvm.machine.types

import org.ton.bytecode.TvmField

data class TlbVariableField(
    val structureId: Int,
    val pathToStructure: List<Int>,
) : TvmField {
    override val name: String
        get() = toString()
}