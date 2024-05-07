package org.ton.bytecode

import org.usvm.machine.types.TvmType

interface TvmField {
    val enclosingType: TvmType
    val name: String
}

data class TvmFieldImpl(override val enclosingType: TvmType, override val name: String) : TvmField
