package org.usvm.machine.state

import io.ksmt.expr.KExpr
import io.ksmt.sort.KBvSort
import io.ksmt.utils.asExpr
import io.ksmt.utils.uncheckedCast
import java.math.BigInteger
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.usvm.UBoolExpr
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.api.makeSymbolicPrimitive
import org.usvm.machine.TvmContext.Companion.ADDRESS_BITS
import org.usvm.machine.TvmContext.Companion.CONFIG_KEY_LENGTH
import org.usvm.machine.TvmContext.Companion.INT_BITS
import org.usvm.machine.TvmContext.TvmInt257Sort
import org.usvm.machine.state.TvmStack.TvmStackCellValue
import org.usvm.machine.state.TvmStack.TvmStackEntry
import org.usvm.machine.state.TvmStack.TvmStackIntValue
import org.usvm.machine.state.TvmStack.TvmStackNullValue
import org.usvm.machine.state.TvmStack.TvmStackTupleValue
import org.usvm.machine.state.TvmStack.TvmStackTupleValueConcreteNew
import org.usvm.machine.state.TvmStack.TvmStackValue
import org.usvm.machine.types.TvmDataCellType
import org.usvm.machine.types.TvmDictCellType


fun TvmState.getContractInfoParam(idx: Int): TvmStackValue {
    require(idx in 0..14) {
        "Unexpected param index $idx"
    }

    return getContractInfo()[idx, stack].cell(stack)
        ?: error("Unexpected param value")
}

fun TvmState.setContractInfoParam(idx: Int, value: TvmStackEntry) {
    require(idx in 0..14) {
        "Unexpected param index $idx"
    }

    val updatedContractInfo = getContractInfo().set(idx, value)
    val updatedC7 = registers.c7.value.set(0, updatedContractInfo.toStackEntry())

    registers.c7 = C7Register(updatedC7)
}

fun TvmState.getConfigParam(idx: UExpr<TvmInt257Sort>): UHeapRef = with(ctx) {
    val configDict = getConfig()

    dictGetValue(
        configDict,
        DictId(CONFIG_KEY_LENGTH),
        idx.extractToSort(mkBvSort(CONFIG_KEY_LENGTH.toUInt())),
        DictValueType.CELL
    ).asExpr(addressSort)
}

fun TvmState.configContainsParam(idx: UExpr<TvmInt257Sort>): UBoolExpr = with(ctx) {
    val configDict = getConfig()

    dictContainsKey(
        configDict,
        DictId(CONFIG_KEY_LENGTH),
        idx.extractToSort(mkBvSort(CONFIG_KEY_LENGTH.toUInt()))
    )
}

fun TvmState.getGlobalVariable(idx: Int, stack: TvmStack): TvmStackValue {
    require(idx in 0..< 255) {
        "Unexpected global variable with index $idx"
    }
    val globalEntries = registers.c7.value.entries.extendToSize(idx + 1)

    return globalEntries.getOrNull(idx)?.cell(stack)
        ?: error("Cannot find global variable with index $idx")
}

fun TvmState.setGlobalVariable(idx: Int, value: TvmStackEntry) {
    require(idx in 0..< 255) {
        "Unexpected setting global variable with index $idx"
    }

    val updatedC7 = TvmStackTupleValueConcreteNew(
        ctx,
        registers.c7.value.entries.extendToSize(idx + 1)
    ).set(idx, value)

    registers.c7 = C7Register(updatedC7)
}

fun TvmState.initC7(): TvmStackTupleValueConcreteNew =
    TvmStackTupleValueConcreteNew(
        ctx,
        persistentListOf(initContractInfo().toStackEntry())
    )

private fun TvmState.initContractInfo(): TvmStackTupleValue = with(ctx) {
    val tag = TvmStackIntValue(mkBvHex("076ef1ea", sizeBits = INT_BITS).uncheckedCast())
    val actions = TvmStackIntValue(zeroValue)
    val msgsSent = TvmStackIntValue(zeroValue)
    val unixTime = TvmStackIntValue(makeSymbolicPrimitive(int257sort))
    val blockLogicTime = TvmStackIntValue(makeSymbolicPrimitive(int257sort))
    val transactionLogicTime = TvmStackIntValue(makeSymbolicPrimitive(int257sort))
    val randomSeed = TvmStackIntValue(makeSymbolicPrimitive(int257sort))
    val grams = makeSymbolicPrimitive(int257sort)
    val balance = TvmStackTupleValueConcreteNew(
        ctx,
        persistentListOf(
            TvmStackIntValue(grams).toStackEntry(),
            TvmStackNullValue.toStackEntry(),
        )
    )
    val addr = TvmStackCellValue(
        allocCellFromData(
            mkBvConcatExpr(
                // addr_std$10 anycast:(Maybe Anycast)
                mkBv("100", 3u),
                // workchain_id:int8 address:bits256
                makeSymbolicPrimitive(mkBvSort(8u + 256u))
            )
        )
    )
    val config = TvmStackCellValue(initConfigRoot())
    // TODO support `code` param
    val code = TvmStackNullValue
    // TODO support `incomingValue` param
    val incomingValue = TvmStackNullValue
    val storagePhaseFees = TvmStackIntValue(makeSymbolicPrimitive(int257sort))
    // TODO support `prevBlocksInfo` param
    val prevBlocksInfo = TvmStackNullValue

    // We can add constraints manually to path constraints because model list is empty
    pathConstraints += mkBvSignedLessOrEqualExpr(unitTimeMinValue, unixTime.intValue)
    pathConstraints += mkBvSignedGreaterOrEqualExpr(grams, zeroValue)
    pathConstraints += mkBvSignedGreaterOrEqualExpr(blockLogicTime.intValue, zeroValue)
    pathConstraints += mkBvSignedGreaterOrEqualExpr(transactionLogicTime.intValue, zeroValue)
    pathConstraints += mkBvSignedGreaterOrEqualExpr(storagePhaseFees.intValue, zeroValue)

    val paramList = listOf(
        tag, actions, msgsSent, unixTime, blockLogicTime, transactionLogicTime, randomSeed,
        balance, addr, config, code, incomingValue, storagePhaseFees, prevBlocksInfo
    )

    TvmStackTupleValueConcreteNew(
        ctx,
        paramList.map { it.toStackEntry() }.toPersistentList()
    )
}

private fun TvmState.initConfigRoot(): UHeapRef = with(ctx) {
    val configDict = memory.allocConcrete(TvmDictCellType)

    val hexAddressBits = ADDRESS_BITS / 4
    val addressBits = ADDRESS_BITS.toUInt()
    val tagBits = 8u
    val uint16Bits = 16u
    val uint32Bits = 32u
    val uint64Bits = 64u

    // https://explorer.toncoin.org/config
    // https://tonviewer.com/config
    // https://github.com/ton-blockchain/ton/blob/d2b418bb703ed6ccd89b7d40f9f1e44686012014/crypto/block/block.tlb

    /**
     * Index: 0
     */
    val configAddr = mkBvHex("5".repeat(hexAddressBits), addressBits)
    setConfigParam(configDict, 0, allocCellFromData(configAddr))

    /**
     * Index: 1
     */
    val electorAddr = mkBvHex("3".repeat(hexAddressBits), addressBits)
    setConfigParam(configDict, 1, allocCellFromData(electorAddr))

    /**
     * Index: 15
     */
    val elections = allocCellFromFields(
        mkBv(65536, uint32Bits),   // validators_elected_for
        mkBv(32768, uint32Bits),   // elections_start_before
        mkBv(8192, uint32Bits),    // elections_end_before
        mkBv(32768, uint32Bits),   // stake_held_for
    )
    setConfigParam(configDict, 15, elections)

    /**
     * Index: 20
     */
    val masterchainGasPrices = allocCellFromFields(
        mkBvHex("d1", tagBits),         // gas_flat_pfx tag
        mkBv(100, uint64Bits),          // flag_gas_limit
        mkBv(1000000, uint64Bits),      // flag_gas_price
        mkBvHex("de", tagBits),         // gas_prices_ext tag
        mkBv(655360000, uint64Bits),    // gas_price
        mkBv(1000000, uint64Bits),      // gas_limit
        mkBv(35000000, uint64Bits),     // special_gas_limit
        mkBv(10000, uint64Bits),        // gas_credit
        mkBv(2500000, uint64Bits),      // block_gas_limit
        mkBv(100000000, uint64Bits),    // freeze_due_limit
        mkBv(1000000000, uint64Bits),   // delete_due_limit
    )
    setConfigParam(configDict, 20, masterchainGasPrices)

    /**
     * Index: 21
     */
    val gasPrices = allocCellFromFields(
        mkBvHex("d1", tagBits),         // gas_flat_pfx tag
        mkBv(100, uint64Bits),          // flag_gas_limit
        mkBv(40000, uint64Bits),        // flag_gas_price
        mkBvHex("de", tagBits),         // gas_prices_ext tag
        mkBv(26214400, uint64Bits),     // gas_price
        mkBv(1000000, uint64Bits),      // gas_limit
        mkBv(1000000, uint64Bits),      // special_gas_limit
        mkBv(10000, uint64Bits),        // gas_credit
        mkBv(10000000, uint64Bits),     // block_gas_limit
        mkBv(100000000, uint64Bits),    // freeze_due_limit
        mkBv(1000000000, uint64Bits),   // delete_due_limit
    )
    setConfigParam(configDict, 21, gasPrices)

    /**
     * Index: 24
     */
    val masterchainMsgPrices = allocCellFromFields(
        mkBvHex("ea", tagBits),         // msg_forward_prices tag
        mkBv(10000000, uint64Bits),     // lump_price
        mkBv(655360000, uint64Bits),    // bit_price
        mkBv(65536000000, uint64Bits),  // cell_price
        mkBv(98304, uint32Bits),        // ihr_price_factor
        mkBv(21845, uint16Bits),        // first_frac
        mkBv(21845, uint16Bits),        // next_frac
    )
    setConfigParam(configDict, 24, masterchainMsgPrices)

    /**
     * Index: 25
     */
    val msgPrices = allocCellFromFields(
        mkBvHex("ea", tagBits),         // msg_forward_prices tag
        mkBv(400000, uint64Bits),       // lump_price
        mkBv(26214400, uint64Bits),     // bit_price
        mkBv(2621440000, uint64Bits),   // cell_price
        mkBv(98304, uint32Bits),        // ihr_price_factor
        mkBv(21845, uint16Bits),        // first_frac
        mkBv(21845, uint16Bits),        // next_frac
    )
    setConfigParam(configDict, 25, msgPrices)

    /**
     * Index: 34
     */
    val validatorSet = allocCellFromFields(
        mkBvHex("12", tagBits),                 // validators_ext tag
        mkBv(1717587720, uint32Bits),           // utime_since
        mkBv(1717653256, uint32Bits),           // utime_until
        mkBv(345, uint16Bits),                  // total
        mkBv(100, uint16Bits),                  // main
        mkBv(1152921504606846802, uint64Bits),  // total_weight
        // TODO real dict
        mkBv(0, sizeBits = 1u),                 // list
    )
    setConfigParam(configDict, 34, validatorSet)

    /**
     * Index: 40
     */
    val defaultFlatFineValue = BigInteger.valueOf(101) * BigInteger.valueOf(10).pow(9) // 101 TON
    val gramsLen = mkBv(5, sizeBits = 4u)
    val gramsValue = mkBv(defaultFlatFineValue, 5u * 8u)
    val defaultFlatFine = mkBvConcatExpr(gramsLen, gramsValue)

    // TODO get real values
    val punishmentSuffix = makeSymbolicPrimitive(mkBvSort(sizeBits = uint32Bits + 7u * uint16Bits))

    val misbehaviourPunishment = allocCellFromFields(
        mkBvHex(value = "01", tagBits), // misbehaviour_punishment_config_v1 tag
        defaultFlatFine,                // default_flat_fine
        punishmentSuffix                // default_proportional_fine, severity_flat_mult, ...
    )
    setConfigParam(configDict, 40, misbehaviourPunishment)

    /**
     * Index: 71
     */
    val ethereumBridge = allocCellFromFields(
        mkBvHex("dd24c4a1f2b88f8b7053513b5cc6c5a31bc44b2a72dcb4d8c0338af0f0d37ec5", addressBits), // bridge_addr
        mkBvHex("3b9bbfd0ad5338b9700f0833380ee17d463e51c1ae671ee6f08901bde899b202", addressBits), // oracle_multisig_address
        // TODO real dict
        mkBv(0, sizeBits = 1u),                                                                     // oracles
        mkBvHex("000000000000000000000000582d872a1b094fc48f5de31d3b73f2d9be47def1", addressBits), // external_chain_address
    )
    setConfigParam(configDict, 71, ethereumBridge)

    /**
     * Index: 80
     */
    // TODO: find documentation
    val dns = allocCellFromFields(
        // TODO real dict
        mkBv(0, sizeBits = 1u),     // ???
    )
    setConfigParam(configDict, 80, dns)

    configDict
}

private fun TvmState.allocCellFromFields(vararg fields: KExpr<KBvSort>): UHeapRef = with(ctx) {
    val data = fields.reduce { acc, field ->
        mkBvConcatExpr(acc, field)
    }

    allocCellFromData(data)
}

private fun TvmState.setConfigParam(dict: UHeapRef, idx: Int, cellValue: UHeapRef) = with(ctx) {
    assertType(cellValue, TvmDataCellType)

    dictAddKeyValue(
        dict,
        DictId(CONFIG_KEY_LENGTH),
        mkBv(idx, CONFIG_KEY_LENGTH.toUInt()),
        cellValue,
        DictValueType.CELL,
    )
}

private fun TvmState.getContractInfo() = registers.c7.value[0, stack].cell(stack)?.tupleValue
    ?: error("Unexpected contract info value")

private fun TvmState.getConfig() = getContractInfo()[9, stack].cell(stack)?.cellValue
    ?: error("Unexpected config value")

private fun PersistentList<TvmStackEntry>.extendToSize(newSize: Int): PersistentList<TvmStackEntry> {
    if (size >= newSize) {
        return this
    }

    val newValuesSize = newSize - size
    val newValues = List(newValuesSize) { TvmStackNullValue.toStackEntry() }

    return addAll(newValues)
}
