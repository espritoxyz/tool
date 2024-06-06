package org.usvm.machine.state

import io.ksmt.expr.KExpr
import io.ksmt.sort.KBvSort
import io.ksmt.utils.asExpr
import java.math.BigInteger
import org.usvm.UBoolExpr
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.api.makeSymbolicPrimitive
import org.usvm.machine.TvmContext.Companion.ADDRESS_BITS
import org.usvm.machine.TvmContext.Companion.CONFIG_KEY_LENGTH
import org.usvm.machine.TvmContext.TvmInt257Sort
import org.usvm.machine.types.TvmDataCellType
import org.usvm.machine.types.TvmDictCellType


fun TvmState.initConfigRoot(): UHeapRef = with(ctx) {
    val configDict = memory.allocConcrete(TvmDictCellType)

    val hexAddressBits = ADDRESS_BITS.toInt() / 4
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
    val configAddr = mkBvHex("5".repeat(hexAddressBits), ADDRESS_BITS)
    setConfigParam(configDict, 0, allocCellFromData(configAddr))

    /**
     * Index: 1
     */
    val electorAddr = mkBvHex("3".repeat(hexAddressBits), ADDRESS_BITS)
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
        mkBvHex("dd24c4a1f2b88f8b7053513b5cc6c5a31bc44b2a72dcb4d8c0338af0f0d37ec5", ADDRESS_BITS),  // bridge_addr
        mkBvHex("3b9bbfd0ad5338b9700f0833380ee17d463e51c1ae671ee6f08901bde899b202", ADDRESS_BITS),  // oracle_multisig_address
        // TODO real dict
        mkBv(0, sizeBits = 1u),                                                                     // oracles
        mkBvHex("000000000000000000000000582d872a1b094fc48f5de31d3b73f2d9be47def1", ADDRESS_BITS),  // external_chain_address
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

fun TvmState.getConfigParam(dict: UHeapRef, idx: UExpr<TvmInt257Sort>): UHeapRef = with(ctx) {
    dictGetValue(
        dict,
        DictId(CONFIG_KEY_LENGTH),
        idx.extractToSort(mkBvSort(CONFIG_KEY_LENGTH.toUInt())),
        DictValueType.CELL
    ).asExpr(addressSort)
}

fun TvmState.configContainsParam(dict: UHeapRef, idx: UExpr<TvmInt257Sort>): UBoolExpr = with(ctx) {
    dictContainsKey(
        dict,
        DictId(CONFIG_KEY_LENGTH),
        idx.extractToSort(mkBvSort(CONFIG_KEY_LENGTH.toUInt()))
    )
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
