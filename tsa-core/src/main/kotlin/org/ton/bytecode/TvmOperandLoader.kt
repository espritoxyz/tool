package org.ton.bytecode

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TvmSubSliceSerializedLoader(
    @SerialName("_bits")
    val bits: List<Int>,
    @SerialName("_refs")
    val refs: List<Int> // todo: refs format
)

data class TvmSubSliceLoader(
    val serializedLoader: TvmSubSliceSerializedLoader,
    val bitsLength: Int,
    val bitsPadding: Int,
    val refsLength: Int,
    val refsAdd: Int,
    val completion: Boolean
)

interface TvmRefOperandLoader
