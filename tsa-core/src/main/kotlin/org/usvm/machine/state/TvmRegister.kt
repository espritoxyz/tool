package org.usvm.machine.state

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.ton.bytecode.TvmCellValue
import org.ton.bytecode.TvmContinuationValue
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.TvmInt257Sort
import org.usvm.machine.state.TvmStack.TvmStackEntry
import org.usvm.machine.state.TvmStack.TvmStackNullValue
import org.usvm.machine.state.TvmStack.TvmStackTupleValueConcreteNew

interface TvmRegister

// TODO registers should contain symbolic values, not concrete?
data class C0Register(var value: TvmContinuationValue) : TvmRegister
data class C1Register(var value: TvmContinuationValue) : TvmRegister
data class C2Register(var value: TvmContinuationValue) : TvmRegister
data class C3Register(var value: TvmContinuationValue) : TvmRegister
data class C4Register(var value: TvmCellValue) : TvmRegister
data class C5Register(var value: TvmCellValue) : TvmRegister
data class C7Register(
    private val ctx: TvmContext,
    /**
     * The current Unix time as an Integer. 3 GETPARAM.
     */
    var now: UExpr<TvmInt257Sort>? = null,
    /**
     * The starting logical time of the current block. 4 GETPARAM.
     */
    var blockLogicTime: UExpr<TvmInt257Sort>? = null,
    /**
     * The logical time of the current transaction. 5 GETPARAM.
     */
    var transactionLogicTime: UExpr<TvmInt257Sort>? = null,
    /**
     * The current random seed as an unsigned 256-bit Integer. 6 GETPARAM.
     */
    var randomSeed: UExpr<TvmInt257Sort>? = null,
    /**
     * The remaining balance of the smart contract as a Tuple consisting of an Integer (the remaining Gram balance in nanograms)
     * and a Maybe Cell (a dictionary with 32-bit keys representing the balance of “extra currencies''). 7 GETPARAM.
     */
    var balance: TvmStackTupleValueConcreteNew? = null,
    /**
     * The internal address of the current smart contract as a Slice with a MsgAddressInt. 8 GETPARAM.
     */
    var addr: UHeapRef? = null,
    /**
     * The Maybe Cell D with the current global configuration dictionary. 9 GETPARAM.
     */
    var configRoot: UHeapRef? = null,
    /**
     * The code of the smart-contract. 10 GETPARAM.
     */
    // TODO is it just Int.MAX_VALUE method? Or all methods?
    var code: TvmContinuationValue? = null,
    /**
     * The value of incoming message. 11 GETPARAM.
     */
    // TODO what type to use?
    var incomingValue: UHeapRef? = null,
    /**
     * The value of storage phase fees. 12 GETPARAM.
     */
    // TODO what type to use?
    var storageFees: UHeapRef? = null,
    /**
     * The PrevBlocksInfo: [last_mc_blocks, prev_key_block]. 13 GETPARAM.
     */
    // TODO what type to use?
    var prevBlocksInfo: UHeapRef? = null,
    var globalVariables: TvmStackTupleValueConcreteNew? = null
) {
    operator fun get(idx: Int): TvmStack.TvmStackValue {
        if (idx == 0) {
            TODO("Support getting tuple with blockchain specific data")
        }

        require(idx in 1..< 255) {
            "Unexpected global variable with index $idx"
        }
        val globalEntries = requireNotNull(globalVariables) {
            "Global variables are not initialized yet"
        }.entries.extendToSize(idx + 1)

        return globalEntries.getOrNull(idx)?.cell
            ?: error("Cannot find global varaible with index $idx")
    }

    operator fun set(idx: Int, value: TvmStackEntry) {
        require(idx in 1..< 255) {
            "Unexpected setting global variable with index $idx"
        }

        val previousGlobalVariablesEntries = globalVariables?.entries ?: persistentListOf()
        val previousGlobalVariables = TvmStackTupleValueConcreteNew(
            ctx,
            previousGlobalVariablesEntries.extendToSize(idx + 1)
        )

        globalVariables = previousGlobalVariables.set(idx, value)
    }

    private fun PersistentList<TvmStackEntry>.extendToSize(newSize: Int): PersistentList<TvmStackEntry> {
        if (size >= newSize) {
            return this
        }

        val newValuesSize = newSize - size
        val newValues = List(newValuesSize) { TvmStackNullValue.toStackEntry() }

        return addAll(newValues)
    }
}

// TODO make them not-null?
data class TvmRegisters(
    private val ctx: TvmContext,
    var c0: C0Register? = null,
    var c1: C1Register? = null,
    var c2: C2Register? = null,
    var c3: C3Register? = null,
    var c4: C4Register? = null,
    var c5: C5Register? = null,
    var c7: C7Register = C7Register(ctx)
)
