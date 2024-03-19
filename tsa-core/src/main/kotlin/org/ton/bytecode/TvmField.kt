package org.ton.bytecode

import org.ton.bytecode.TvmType

interface TvmField {
    val enclosingType: TvmType
    val name: String
}

data class TvmFieldImpl(override val enclosingType: TvmType, override val name: String) : TvmField
