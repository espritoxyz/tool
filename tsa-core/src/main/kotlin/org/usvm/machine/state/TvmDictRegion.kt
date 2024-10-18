package org.usvm.machine.state

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.asExpr
import org.usvm.UAddressSort
import org.usvm.UBoolExpr
import org.usvm.UBvSort
import org.usvm.UConcreteHeapRef
import org.usvm.UContext
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.USort
import org.usvm.UTransformer
import org.usvm.api.readField
import org.usvm.api.writeField
import org.usvm.apply
import org.usvm.collection.set.primitive.USetEntryLValue
import org.usvm.machine.TvmContext.Companion.dictKeyLengthField
import org.usvm.machine.setUnion
import org.usvm.memory.ULValue
import org.usvm.memory.UMemoryRegion
import org.usvm.memory.UMemoryRegionId
import org.usvm.memory.UReadOnlyMemory
import org.usvm.memory.USymbolicCollectionKeyInfo
import org.usvm.regions.SetRegion
import org.usvm.uctx

data class DictId(val keyLength: Int)

data class TvmDictValueRegionLValue<KeySort : USort>(
    val dictId: DictId,
    val keySort: KeySort,
) : ULValue<TvmDictValueRegionLValue<KeySort>, Nothing> {
    override val key: TvmDictValueRegionLValue<KeySort>
        get() = this

    override val memoryRegionId: UMemoryRegionId<TvmDictValueRegionLValue<KeySort>, Nothing>
        get() = TvmDictValueRegionId(dictId, keySort)

    override val sort: Nothing
        get() = error("TvmDictValueRegion sort should not be used")
}

data class TvmDictValueRegionId<KeySort : USort>(
    val dictId: DictId,
    val keySort: KeySort,
) : UMemoryRegionId<TvmDictValueRegionLValue<KeySort>, Nothing> {
    override fun emptyRegion(): UMemoryRegion<TvmDictValueRegionLValue<KeySort>, Nothing> =
        TvmRefsMemoryRegion<TvmDictValueRegionLValue<KeySort>, KeySort, UAddressSort>()

    override val sort: Nothing
        get() = error("TvmDictValueRegion sort should not be used")
}

@Suppress("UNCHECKED_CAST")
fun <KeySort : USort> UReadOnlyMemory<*>.dictValueRegion(
    regionId: TvmDictValueRegionId<KeySort>
): TvmRefsMemoryRegion<TvmDictValueRegionLValue<KeySort>, KeySort, UAddressSort> =
    getRegion(regionId) as TvmRefsMemoryRegion<TvmDictValueRegionLValue<KeySort>, KeySort, UAddressSort>

class TvmDictValueRegionValueInfo<ValueSort : USort>(
    private val state: TvmState,
    private val valueSort: ValueSort,
) : TvmRefsMemoryRegion.TvmRefsRegionValueInfo<ValueSort> {

    override fun mkDefaultValue(): UExpr<ValueSort> = state.emptyRefValue.emptySlice.asExpr(valueSort)

    override fun mkSymbolicValue(): UExpr<ValueSort> = state.generateSymbolicSlice().asExpr(valueSort)

    override fun actualizeSymbolicValue(value: UExpr<ValueSort>): UExpr<ValueSort> = value.apply {
        state.ensureSymbolicSliceInitialized(value.asExpr(value.uctx.addressSort))
    }
}

object DictKeyInfo: USymbolicCollectionKeyInfo<UExpr<UBvSort>, SetRegion<UExpr<UBvSort>>> {
    override fun mapKey(key: UExpr<UBvSort>, transformer: UTransformer<*, *>?): UExpr<UBvSort> =
        transformer.apply(key)

    override fun eqSymbolic(ctx: UContext<*>, key1: UExpr<UBvSort>, key2: UExpr<UBvSort>): UBoolExpr =
        ctx.mkEq(key1, key2)

    override fun eqConcrete(key1: UExpr<UBvSort>, key2: UExpr<UBvSort>): Boolean =
        key1 == key2

    override fun cmpSymbolicLe(ctx: UContext<*>, key1: UExpr<UBvSort>, key2: UExpr<UBvSort>): UBoolExpr =
        error("Dict keys should not be compared!")

    override fun cmpConcreteLe(key1: UExpr<UBvSort>, key2: UExpr<UBvSort>): Boolean =
        error("Dict keys should not be compared!")

    override fun keyToRegion(key: UExpr<UBvSort>) =
        if (key is KBitVecValue<UBvSort>) {
            SetRegion.singleton(key as UExpr<UBvSort>)
        } else {
            SetRegion.universe()
        }

    override fun keyRangeRegion(from: UExpr<UBvSort>, to: UExpr<UBvSort>) =
        error("This should not be called!")

    override fun topRegion() = SetRegion.universe<UExpr<UBvSort>>()

    override fun bottomRegion() = SetRegion.empty<UExpr<UBvSort>>()
}

fun TvmState.dictAddKeyValue(
    dictRef: UHeapRef,
    dictId: DictId,
    key: UExpr<UBvSort>,
    value: UExpr<*>,
) = with(ctx) {
    val keyContainsLValue = USetEntryLValue(key.sort, dictRef, key, dictId, DictKeyInfo)
    memory.write(keyContainsLValue, rvalue = trueExpr, guard = trueExpr)

    val valueSort = addressSort
    val dictValueRegionId = TvmDictValueRegionId(dictId, key.sort)
    val dictValueRegion = memory.dictValueRegion(dictValueRegionId)

    val updatedValues = dictValueRegion.writeRefValue(dictRef, key, value.asExpr(valueSort), guard = trueExpr)
    memory.setRegion(dictValueRegionId, updatedValues)
}

fun TvmState.dictGetValue(
    dictRef: UHeapRef,
    dictId: DictId,
    key: UExpr<UBvSort>,
): UHeapRef = with(ctx) {
    val valueSort = addressSort
    val dictValueRegionId = TvmDictValueRegionId(dictId, key.sort)
    val dictValueRegion = memory.dictValueRegion(dictValueRegionId)
    val dictValueInfo = TvmDictValueRegionValueInfo(this@dictGetValue, valueSort)

    return dictValueRegion.readRefValue(dictRef, key, dictValueInfo)
}

fun TvmState.dictContainsKey(
    dictRef: UHeapRef,
    dictId: DictId,
    key: UExpr<UBvSort>
): UBoolExpr {
    val keyContainsLValue = USetEntryLValue(key.sort, dictRef, key, dictId, DictKeyInfo)
    return memory.read(keyContainsLValue)
}

fun TvmState.dictRemoveKey(
    dictRef: UHeapRef,
    dictId: DictId,
    key: UExpr<UBvSort>,
) = with(ctx) {
    val resultKeyContainsLValue = USetEntryLValue(key.sort, dictRef, key, dictId, DictKeyInfo)
    memory.write(resultKeyContainsLValue, rvalue = falseExpr, guard = trueExpr)

    // todo: update values?
}

fun TvmState.copyDict(
    originalDict: UHeapRef,
    resultDict: UConcreteHeapRef,
    dictId: DictId,
    keySort: UBvSort,
) = with(ctx) {
    memory.setUnion(originalDict, resultDict, dictId, keySort, DictKeyInfo, guard = trueExpr)

    val dictValueRegionId = TvmDictValueRegionId(dictId, keySort)
    val dictValueRegion = memory.dictValueRegion(dictValueRegionId)

    val updatedValues = dictValueRegion.copyRefValues(originalDict, resultDict)
    memory.setRegion(dictValueRegionId, updatedValues)

    val dictKeyLength = memory.readField(originalDict, dictKeyLengthField, int257sort)
    memory.writeField(resultDict, dictKeyLengthField, int257sort, dictKeyLength, guard = trueExpr)
}