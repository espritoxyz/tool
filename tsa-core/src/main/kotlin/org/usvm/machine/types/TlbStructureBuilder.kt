package org.usvm.machine.types

import org.ton.TlbLabel
import org.ton.TlbStructure
import org.ton.TlbStructureIdProvider

@JvmInline
value class TlbStructureBuilder(
    val build: (TlbStructure) -> TlbStructure
) {
    fun end(): TlbStructure =
        build(TlbStructure.Empty)

    fun addTlbLabel(label: TlbLabel): TlbStructureBuilder {
        // [label] must be deduced from store operations, and such labels have zero arity.
        // So, there is no need to support type arguments here.
        check(label.arity == 0) {
            "Only labels without arguments can be used in builder structures, but label $label has arity ${label.arity}"
        }
        return TlbStructureBuilder { suffix ->
            build(
                TlbStructure.KnownTypePrefix(
                    id = TlbStructureIdProvider.provideId(),
                    typeLabel = label,
                    typeArgIds = emptyList(),
                    rest = suffix,
                )
            )
        }
    }

    fun addConstant(bitString: String): TlbStructureBuilder =
        TlbStructureBuilder { suffix ->
            build(
                TlbStructure.SwitchPrefix(
                    id = TlbStructureIdProvider.provideId(),
                    switchSize = bitString.length,
                    variants = mapOf(
                        bitString to suffix
                    )
                )
            )
        }

    companion object {
        val empty = TlbStructureBuilder { it }
    }
}
