package org.usvm.machine.types.dp

import kotlinx.collections.immutable.PersistentList
import org.ton.TlbAtomicLabel
import org.ton.TlbCompositeLabel
import org.ton.TlbStructure
import org.ton.TvmParameterInfo
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.preloadDataBitsFromCellWithoutChecks
import org.usvm.machine.types.dataLength
import org.usvm.mkSizeAddExpr
import org.usvm.mkSizeExpr

data class AbstractionForUExpr(
    val address: UConcreteHeapRef,
    val prefixSize: UExpr<TvmSizeSort>,
    val path: PersistentList<Int>,
    val state: TvmState,
)

@JvmInline
value class AbstractGuard(
    val apply: (AbstractionForUExpr) -> UBoolExpr
) {
    context(TvmContext)
    infix fun or(other: AbstractGuard) = AbstractGuard {
        apply(it) or other.apply(it)
    }

    context(TvmContext)
    infix fun and(other: AbstractGuard) = AbstractGuard {
        apply(it) and other.apply(it)
    }

    context(TvmContext)
    fun not() = AbstractGuard {
        apply(it).not()
    }

    context(TvmContext)
    fun shift(numOfBits: UExpr<TvmSizeSort>) = AbstractGuard { (address, prefixSize, path, state) ->
        apply(AbstractionForUExpr(address, mkSizeAddExpr(prefixSize, numOfBits), path, state))
    }

    context(TvmContext)
    fun shift(numOfBits: Int) = shift(mkSizeExpr(numOfBits))

    context(TvmContext)
    fun shift(numOfBits: AbstractSizeExpr) = AbstractGuard { param ->
        val offset = numOfBits.apply(param)
        val (address, prefixSize, path, state) = param
        apply(AbstractionForUExpr(address, mkSizeAddExpr(prefixSize, offset), path, state))
    }

    fun addTlbLevel(
        struct: TlbStructure.KnownTypePrefix
    ) = AbstractGuard { (address, prefixSize, path, state) ->
        apply(AbstractionForUExpr(address, prefixSize, path.add(0, struct.id), state))
    }
}

@JvmInline
value class AbstractSizeExpr(
    val apply: (AbstractionForUExpr) -> UExpr<TvmSizeSort>
) {
    context(TvmContext)
    fun shift(
        numOfBits: UExpr<TvmSizeSort>,
    ) = AbstractSizeExpr { (address, prefixSize, path, state) ->
        apply(AbstractionForUExpr(address, mkSizeAddExpr(prefixSize, numOfBits), path, state))
    }

    context(TvmContext)
    fun shift(numOfBits: Int) =
        shift(mkSizeExpr(numOfBits))

    context(TvmContext)
    fun shiftAndAdd(numOfBits: AbstractSizeExpr) = AbstractSizeExpr { param ->
        val (address, prefixSize, path, state) = param
        val offset = numOfBits.apply(param)
        val newParam = AbstractionForUExpr(address, mkSizeAddExpr(prefixSize, offset), path, state)
        mkSizeAddExpr(offset, apply(newParam))
    }

    fun addTlbLevel(
        struct: TlbStructure.KnownTypePrefix
    ) = AbstractSizeExpr { (address, prefixSize, path, state) ->
        apply(AbstractionForUExpr(address, prefixSize, path.add(0, struct.id), state))
    }
}

class ChildrenStructure(
    val children: List<ChildStructure>,
    val numberOfChildrenExceeded: AbstractGuard,
) {
    init {
        require(children.size == TvmContext.MAX_REFS_NUMBER)
    }

    fun exactNumberOfChildren(ctx: TvmContext, num: Int): AbstractGuard = with(ctx) {
        require(num in 0..TvmContext.MAX_REFS_NUMBER)
        when (num) {
            0 -> children[0].exists().not()
            TvmContext.MAX_REFS_NUMBER -> children[TvmContext.MAX_REFS_NUMBER - 1].exists() and numberOfChildrenExceeded.not()
            else -> children[num - 1].exists() and children[num].exists().not()
        }
    }

    fun numberOfChildren(ctx: TvmContext): AbstractSizeExpr = with(ctx) {
        AbstractSizeExpr { param ->
            children.foldIndexed(zeroSizeExpr) { childIndex, acc, struct ->
                mkIte(
                    struct.exists().apply(param),
                    trueBranch = mkSizeExpr(childIndex + 1),
                    falseBranch = acc
                )
            }
        }
    }

    context(TvmContext)
    fun shift(offset: AbstractSizeExpr) = ChildrenStructure(
        children.map { it.shift(offset) },
        numberOfChildrenExceeded.shift(offset)
    )

    fun addTlbLevel(struct: TlbStructure.KnownTypePrefix) = ChildrenStructure(
        children.map { it.addTlbLevel(struct) },
        numberOfChildrenExceeded.addTlbLevel(struct)
    )

    context(TvmContext)
    infix fun and(newGuard: AbstractGuard) = ChildrenStructure(
        children.map { it and newGuard },
        numberOfChildrenExceeded and newGuard
    )

    context(TvmContext)
    infix fun union(other: ChildrenStructure) = ChildrenStructure(
        (children zip other.children).map { (x, y) -> x union y },
        numberOfChildrenExceeded or other.numberOfChildrenExceeded
    )

    companion object {
        fun empty(ctx: TvmContext): ChildrenStructure = ChildrenStructure(
            List(TvmContext.MAX_REFS_NUMBER) { ChildStructure(emptyMap()) },
            ctx.abstractFalse,
        )
    }
}

class ChildStructure(val variants: Map<TvmParameterInfo.CellInfo, AbstractGuard>) {
    context(TvmContext)
    fun exists(): AbstractGuard =
        variants.values.fold(abstractFalse) { acc, guard ->
            acc or guard
        }

    context(TvmContext)
    fun shift(offset: AbstractSizeExpr) = ChildStructure(
        variants.entries.associate { (struct, guard) ->
            struct to guard.shift(offset)
        }
    )

    fun addTlbLevel(addedStruct: TlbStructure.KnownTypePrefix) = ChildStructure(
        variants.entries.associate { (struct, guard) ->
            struct to guard.addTlbLevel(addedStruct)
        }
    )

    context(TvmContext)
    infix fun union(other: ChildStructure): ChildStructure {
        val result = variants.toMutableMap()
        other.variants.entries.forEach { (struct, guard) ->
            val oldValue = result[struct] ?: abstractFalse
            result[struct] = oldValue or guard
        }
        return ChildStructure(result)
    }

    context(TvmContext)
    infix fun and(newGuard: AbstractGuard) = ChildStructure(
        variants.entries.associate { (struct, guard) ->
            struct to (guard and newGuard)
        }
    )
}

context(TvmContext)
fun getKnownTypePrefixDataLength(
    struct: TlbStructure.KnownTypePrefix,
    lengthsFromPreviousDepth: Map<TlbCompositeLabel, AbstractSizeExpr>,
): AbstractSizeExpr? = when (struct.typeLabel) {
    is TlbAtomicLabel -> {
        AbstractSizeExpr { (address, _, cnt, state) ->
            val typeArgs = struct.typeArgs(state, address, cnt)
            struct.typeLabel.dataLength(state, typeArgs)
        }
    }
    is TlbCompositeLabel -> {
        lengthsFromPreviousDepth[struct.typeLabel]?.addTlbLevel(struct)
    }
}

context(TvmContext)
fun generateSwitchGuard(switchSize: Int, key: String) = AbstractGuard { (address, prefixSize, _, state) ->
    val actualPrefix = state.preloadDataBitsFromCellWithoutChecks(address, prefixSize, switchSize)
    val expectedPrefix = mkBv(key, switchSize.toUInt())
    actualPrefix eq expectedPrefix
}

fun <T> calculateMapsByTlbDepth(
    maxTlbDepth: Int,
    labels: Iterable<TlbCompositeLabel>,
    makeCalculation: (TlbCompositeLabel, Int, Map<TlbCompositeLabel, T>) -> T?,
): List<Map<TlbCompositeLabel, T>> {
    var cur = mapOf<TlbCompositeLabel, T>()
    val result = mutableListOf<Map<TlbCompositeLabel, T>>()

    for (curDepth in 0..maxTlbDepth) {
        val newMap = hashMapOf<TlbCompositeLabel, T>()
        labels.forEach { label ->
            val newValue = makeCalculation(label, curDepth, cur)
            newValue?.let {
                newMap += label to it
            }
        }
        cur = newMap
        result.add(cur)
    }

    return result
}