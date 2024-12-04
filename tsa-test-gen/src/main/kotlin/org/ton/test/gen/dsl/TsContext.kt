package org.ton.test.gen.dsl

import java.math.BigInteger
import java.util.Collections.newSetFromMap
import java.util.IdentityHashMap
import org.ton.test.gen.dsl.models.TsBooleanValue
import org.ton.test.gen.dsl.models.TsReference
import org.ton.test.gen.dsl.models.toTsValue
import org.usvm.test.resolver.TvmTestDataCellValue

// TODO: create all elements only through context to collect properties
class TsContext {
    private val mutableVariables: MutableSet<TsReference<*>> = newSetFromMap(IdentityHashMap())

    val emptyCell = TvmTestDataCellValue().toTsValue()

    val falseValue = TsBooleanValue(value = false)
    val trueValue = TsBooleanValue(value = true)

    val zeroValue = 0.toTsValue()

    val zeroBigintValue = BigInteger.ZERO.toTsValue()

    internal fun TsReference<*>.isMutable(): Boolean = this in mutableVariables

    internal fun markAsMutable(ref: TsReference<*>) {
        mutableVariables.add(ref)
    }
}