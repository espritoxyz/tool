package org.ton

import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.api.readField
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.state.TvmState
import org.usvm.machine.types.TlbVariableField
import org.usvm.sizeSort

sealed interface TlbStructure {
    data object Unknown : Leaf

    data object Empty : Leaf

    class KnownTypePrefix(
        override val id: Int,
        val typeLabel: TlbLabel,
        val typeArgIds: List<Int>,
        val rest: TlbStructure,
    ) : TlbStructure, CompositeNode {
        override fun equals(other: Any?): Boolean = performEquals(other)
        override fun hashCode(): Int = calculateHash()

        fun typeArgs(
            state: TvmState,
            address: UConcreteHeapRef,
            path: List<Int>,
        ): List<UExpr<TvmSizeSort>> = with(state.ctx) {
            typeArgIds.map {
                state.memory.readField(address, TlbVariableField(it, path), sizeSort)
            }
        }
    }

    class LoadRef(
        override val id: Int,
        val ref: TvmParameterInfo.CellInfo,
        val rest: TlbStructure,
    ) : TlbStructure, CompositeNode {
        override fun equals(other: Any?): Boolean = performEquals(other)
        override fun hashCode(): Int = calculateHash()
    }

    class SwitchPrefix(
        override val id: Int,
        val switchSize: Int,
        val variants: Map<String, TlbStructure>,
    ) : TlbStructure, CompositeNode {
        override fun equals(other: Any?): Boolean = performEquals(other)
        override fun hashCode(): Int = calculateHash()

        init {
            require(switchSize > 0) {
                "switchSize in SwitchPrefix must be > 0, but got $switchSize"
            }
            variants.keys.forEach {
                require(it.length == switchSize) {
                    "Switch keys' lengths must be $switchSize, found key: $it"
                }
            }
        }
    }

    sealed interface Leaf : TlbStructure

    sealed interface CompositeNode : TlbStructure {
        val id: Int

        fun performEquals(other: Any?): Boolean {
            if (other !is CompositeNode)
                return false
            return id == other.id
        }

        fun calculateHash() = id.hashCode()
    }
}

object TlbStructureIdProvider {
    private var nextId = 0

    fun provideId(): Int = nextId++
}