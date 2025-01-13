// Generated
package org.ton.bytecode

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
sealed interface TvmAppActionsInst: TvmInst

@Serializable
sealed interface TvmAppAddrInst: TvmInst

@Serializable
sealed interface TvmAppConfigInst: TvmInst

@Serializable
sealed interface TvmAppCryptoInst: TvmInst

@Serializable
sealed interface TvmAppCurrencyInst: TvmInst

@Serializable
sealed interface TvmAppGasInst: TvmInst

@Serializable
sealed interface TvmAppGlobalInst: TvmInst

@Serializable
sealed interface TvmAppMiscInst: TvmInst

@Serializable
sealed interface TvmAppRndInst: TvmInst

@Serializable
sealed interface TvmArithmBasicInst: TvmInst

@Serializable
sealed interface TvmArithmDivInst: TvmInst

@Serializable
sealed interface TvmArithmLogicalInst: TvmInst

@Serializable
sealed interface TvmArithmQuietInst: TvmInst

@Serializable
sealed interface TvmCellBuildInst: TvmInst

@Serializable
sealed interface TvmCellParseInst: TvmInst

@Serializable
sealed interface TvmCodepageInst: TvmInst

@Serializable
sealed interface TvmCompareIntInst: TvmInst

@Serializable
sealed interface TvmCompareOtherInst: TvmInst

@Serializable
sealed interface TvmConstDataInst: TvmInst

@Serializable
sealed interface TvmConstIntInst: TvmInst

@Serializable
sealed interface TvmContBasicInst: TvmInst

@Serializable
sealed interface TvmContConditionalInst: TvmInst

@Serializable
sealed interface TvmContCreateInst: TvmInst

@Serializable
sealed interface TvmContDictInst: TvmInst

@Serializable
sealed interface TvmContLoopsInst: TvmInst

@Serializable
sealed interface TvmContRegistersInst: TvmInst

@Serializable
sealed interface TvmContStackInst: TvmInst

@Serializable
sealed interface TvmDebugInst: TvmInst

@Serializable
sealed interface TvmDictInst: TvmInst

@Serializable
sealed interface TvmDictDeleteInst: TvmInst, TvmDictInst

@Serializable
sealed interface TvmDictGetInst: TvmInst, TvmDictInst

@Serializable
sealed interface TvmDictMayberefInst: TvmInst, TvmDictInst

@Serializable
sealed interface TvmDictMinInst: TvmInst, TvmDictInst

@Serializable
sealed interface TvmDictNextInst: TvmInst, TvmDictInst

@Serializable
sealed interface TvmDictPrefixInst: TvmInst, TvmDictInst

@Serializable
sealed interface TvmDictSerialInst: TvmInst, TvmDictInst

@Serializable
sealed interface TvmDictSetInst: TvmInst, TvmDictInst

@Serializable
sealed interface TvmDictSetBuilderInst: TvmInst, TvmDictInst

@Serializable
sealed interface TvmDictSpecialInst: TvmInst, TvmDictInst

@Serializable
sealed interface TvmDictSubInst: TvmInst, TvmDictInst

@Serializable
sealed interface TvmExceptionsInst: TvmInst

@Serializable
sealed interface TvmStackBasicInst: TvmInst

@Serializable
sealed interface TvmStackComplexInst: TvmInst

@Serializable
sealed interface TvmTupleInst: TvmInst

/**
 * Creates an output action similarly to `SETLIBCODE`, but instead of the library code accepts its hash
 * as an unsigned 256-bit integer `h`. If `x!=0` and the library with hash `h` is absent from the libr
 * ary collection of this smart contract, this output action will fail.
 */
@Serializable
@SerialName(TvmAppActionsChangelibInst.MNEMONIC)
data class TvmAppActionsChangelibInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppActionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 526)

    companion object {
        const val MNEMONIC = "CHANGELIB"
    }
}

/**
 * Creates an output action which would reserve exactly `x` nanograms (if `y=0`), at most `x` nanograms
 * (if `y=2`), or all but `x` nanograms (if `y=1` or `y=3`), from the remaining balance of the account
 * . It is roughly equivalent to creating an outbound message carrying `x` nanograms (or `b-x` nanogram
 * s, where `b` is the remaining balance) to oneself, so that the subsequent output actions would not b
 * e able to spend more money than the remainder. Bit `+2` in `y` means that the external action does n
 * ot fail if the specified amount cannot be reserved; instead, all remaining balance is reserved. Bit
 * `+8` in `y` means `x:=-x` before performing any further actions. Bit `+4` in `y` means that `x` is i
 * ncreased by the original balance of the current account (before the compute phase), including all ex
 * tra currencies, before performing any other checks and actions. Currently `x` must be a non-negative
 * integer, and `y` must be in the range `0...15`.
 */
@Serializable
@SerialName(TvmAppActionsRawreserveInst.MNEMONIC)
data class TvmAppActionsRawreserveInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppActionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 526)

    companion object {
        const val MNEMONIC = "RAWRESERVE"
    }
}

/**
 * Similar to `RAWRESERVE`, but also accepts a dictionary `D` (represented by a _Cell_ or _Null_) with
 * extra currencies. In this way currencies other than Grams can be reserved.
 */
@Serializable
@SerialName(TvmAppActionsRawreservexInst.MNEMONIC)
data class TvmAppActionsRawreservexInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppActionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 526)

    companion object {
        const val MNEMONIC = "RAWRESERVEX"
    }
}

/**
 * Creates an output action and returns a fee for creating a message. Mode has the same effect as in th
 * e case of `SENDRAWMSG`. Additionally `+1024` means - do not create an action, only estimate fee. Oth
 * er modes affect the fee calculation as follows: `+64` substitutes the entire balance of the incoming
 * message as an outcoming value (slightly inaccurate, gas expenses that cannot be estimated before th
 * e computation is completed are not taken into account), `+128` substitutes the value of the entire b
 * alance of the contract before the start of the computation phase (slightly inaccurate, since gas exp
 * enses that cannot be estimated before the completion of the computation phase are not taken into acc
 * ount).
 */
@Serializable
@SerialName(TvmAppActionsSendmsgInst.MNEMONIC)
data class TvmAppActionsSendmsgInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppActionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 526)

    companion object {
        const val MNEMONIC = "SENDMSG"
    }
}

/**
 * Sends a raw message contained in _Cell `c`_, which should contain a correctly serialized object `Mes
 * sage X`, with the only exception that the source address is allowed to have dummy value `addr_none`
 * (to be automatically replaced with the current smart-contract address), and `ihr_fee`, `fwd_fee`, `c
 * reated_lt` and `created_at` fields can have arbitrary values (to be rewritten with correct values du
 * ring the action phase of the current transaction). Integer parameter `x` contains the flags. Current
 * ly `x=0` is used for ordinary messages; `x=128` is used for messages that are to carry all the remai
 * ning balance of the current smart contract (instead of the value originally indicated in the message
 * ); `x=64` is used for messages that carry all the remaining value of the inbound message in addition
 * to the value initially indicated in the new message (if bit 0 is not set, the gas fees are deducted
 * from this amount); `x'=x+1` means that the sender wants to pay transfer fees separately; `x'=x+2` m
 * eans that any errors arising while processing this message during the action phase should be ignored
 * . Finally, `x'=x+32` means that the current account must be destroyed if its resulting balance is ze
 * ro. This flag is usually employed together with `+128`.
 */
@Serializable
@SerialName(TvmAppActionsSendrawmsgInst.MNEMONIC)
data class TvmAppActionsSendrawmsgInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppActionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 526)

    companion object {
        const val MNEMONIC = "SENDRAWMSG"
    }
}

/**
 * Creates an output action that would change this smart contract code to that given by _Cell_ `c`. Not
 * ice that this change will take effect only after the successful termination of the current run of th
 * e smart contract.
 */
@Serializable
@SerialName(TvmAppActionsSetcodeInst.MNEMONIC)
data class TvmAppActionsSetcodeInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppActionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 526)

    companion object {
        const val MNEMONIC = "SETCODE"
    }
}

/**
 * Creates an output action that would modify the collection of this smart contract libraries by adding
 * or removing library with code given in _Cell_ `c`. If `x=0`, the library is actually removed if it
 * was previously present in the collection (if not, this action does nothing). If `x=1`, the library i
 * s added as a private library, and if `x=2`, the library is added as a public library (and becomes av
 * ailable to all smart contracts if the current smart contract resides in the masterchain); if the lib
 * rary was present in the collection before, its public/private status is changed according to `x`. Va
 * lues of `x` other than `0...2` are invalid.
 */
@Serializable
@SerialName(TvmAppActionsSetlibcodeInst.MNEMONIC)
data class TvmAppActionsSetlibcodeInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppActionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 526)

    companion object {
        const val MNEMONIC = "SETLIBCODE"
    }
}

/**
 * Loads from _Slice_ `s` the only prefix that is a valid `MsgAddress`, and returns both this prefix `s
 * '` and the remainder `s''` of `s` as slices.
 */
@Serializable
@SerialName(TvmAppAddrLdmsgaddrInst.MNEMONIC)
data class TvmAppAddrLdmsgaddrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppAddrInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDMSGADDR"
    }
}

/**
 * A quiet version of `LDMSGADDR`: on success, pushes an extra `-1`; on failure, pushes the original `s
 * ` and a zero.
 */
@Serializable
@SerialName(TvmAppAddrLdmsgaddrqInst.MNEMONIC)
data class TvmAppAddrLdmsgaddrqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppAddrInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDMSGADDRQ"
    }
}

/**
 * Decomposes _Slice_ `s` containing a valid `MsgAddress` into a _Tuple_ `t` with separate fields of th
 * is `MsgAddress`. If `s` is not a valid `MsgAddress`, a cell deserialization exception is thrown.
 */
@Serializable
@SerialName(TvmAppAddrParsemsgaddrInst.MNEMONIC)
data class TvmAppAddrParsemsgaddrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppAddrInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PARSEMSGADDR"
    }
}

/**
 * A quiet version of `PARSEMSGADDR`: returns a zero on error instead of throwing an exception.
 */
@Serializable
@SerialName(TvmAppAddrParsemsgaddrqInst.MNEMONIC)
data class TvmAppAddrParsemsgaddrqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppAddrInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PARSEMSGADDRQ"
    }
}

/**
 * Parses _Slice_ `s` containing a valid `MsgAddressInt` (usually a `msg_addr_std`), applies rewriting
 * from the `anycast` (if present) to the same-length prefix of the address, and returns both the workc
 * hain `x` and the 256-bit address `y` as integers. If the address is not 256-bit, or if `s` is not a
 * valid serialization of `MsgAddressInt`, throws a cell deserialization exception.
 */
@Serializable
@SerialName(TvmAppAddrRewritestdaddrInst.MNEMONIC)
data class TvmAppAddrRewritestdaddrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppAddrInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "REWRITESTDADDR"
    }
}

/**
 * A quiet version of primitive `REWRITESTDADDR`.
 */
@Serializable
@SerialName(TvmAppAddrRewritestdaddrqInst.MNEMONIC)
data class TvmAppAddrRewritestdaddrqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppAddrInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "REWRITESTDADDRQ"
    }
}

/**
 * A variant of `REWRITESTDADDR` that returns the (rewritten) address as a _Slice_ `s`, even if it is n
 * ot exactly 256 bit long (represented by a `msg_addr_var`).
 */
@Serializable
@SerialName(TvmAppAddrRewritevaraddrInst.MNEMONIC)
data class TvmAppAddrRewritevaraddrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppAddrInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "REWRITEVARADDR"
    }
}

/**
 * A quiet version of primitive `REWRITEVARADDR`.
 */
@Serializable
@SerialName(TvmAppAddrRewritevaraddrqInst.MNEMONIC)
data class TvmAppAddrRewritevaraddrqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppAddrInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "REWRITEVARADDRQ"
    }
}

/**
 * Returns the global configuration dictionary along with its key length (32).
 * Equivalent to `CONFIGROOT` `32 PUSHINT`.
 */
@Serializable
@SerialName(TvmAppConfigConfigdictInst.MNEMONIC)
data class TvmAppConfigConfigdictInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppConfigInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "CONFIGDICT"
    }
}

/**
 * Returns the value of the global configuration parameter with integer index `i` as a _Maybe Cell_ `c^
 * ?`.
 * Equivalent to `CONFIGDICT` `DICTIGETOPTREF`.
 */
@Serializable
@SerialName(TvmAppConfigConfigoptparamInst.MNEMONIC)
data class TvmAppConfigConfigoptparamInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppConfigInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "CONFIGOPTPARAM"
    }
}

/**
 * Returns the value of the global configuration parameter with integer index `i` as a _Cell_ `c`, and
 * a flag to indicate success.
 * Equivalent to `CONFIGDICT` `DICTIGETREF`.
 */
@Serializable
@SerialName(TvmAppConfigConfigparamInst.MNEMONIC)
data class TvmAppConfigConfigparamInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppConfigInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "CONFIGPARAM"
    }
}

/**
 * Calculates forward fee.
 */
@Serializable
@SerialName(TvmAppConfigGetforwardfeeInst.MNEMONIC)
data class TvmAppConfigGetforwardfeeInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppConfigInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "GETFORWARDFEE"
    }
}

/**
 * Same as `GETFORWARDFEE`, but without lump price (just (`bits*bit_price + cells*cell_price) / 2^16`).
 */
@Serializable
@SerialName(TvmAppConfigGetforwardfeesimpleInst.MNEMONIC)
data class TvmAppConfigGetforwardfeesimpleInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppConfigInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "GETFORWARDFEESIMPLE"
    }
}

/**
 * Calculates gas fee
 */
@Serializable
@SerialName(TvmAppConfigGetgasfeeInst.MNEMONIC)
data class TvmAppConfigGetgasfeeInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppConfigInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "GETGASFEE"
    }
}

/**
 * Same as `GETGASFEE`, but without flat price (just `(gas_used * price) / 2^16)`.
 */
@Serializable
@SerialName(TvmAppConfigGetgasfeesimpleInst.MNEMONIC)
data class TvmAppConfigGetgasfeesimpleInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppConfigInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "GETGASFEESIMPLE"
    }
}

/**
 * Calculate `fwd_fee * 2^16 / first_frac`. Can be used to get the original `fwd_fee` of the message.
 */
@Serializable
@SerialName(TvmAppConfigGetoriginalfwdfeeInst.MNEMONIC)
data class TvmAppConfigGetoriginalfwdfeeInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppConfigInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "GETORIGINALFWDFEE"
    }
}

/**
 * Returns the `i`-th parameter from the _Tuple_ provided at `c7` for `0 <= i <= 15`. Equivalent to `c7
 * PUSHCTR` `FIRST` `[i] INDEX`.
 * If one of these internal operations fails, throws an appropriate type checking or range checking exc
 * eption.
 */
@Serializable
@SerialName(TvmAppConfigGetparamInst.MNEMONIC)
data class TvmAppConfigGetparamInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmAppConfigInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "GETPARAM"
    }
}

/**
 * Returns gas usage for the current contract if it is precompiled, `null` otherwise.
 */
@Serializable
@SerialName(TvmAppConfigGetprecompiledgasInst.MNEMONIC)
data class TvmAppConfigGetprecompiledgasInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppConfigInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "GETPRECOMPILEDGAS"
    }
}

/**
 * Calculates storage fees (only current StoragePrices entry is used).
 */
@Serializable
@SerialName(TvmAppConfigGetstoragefeeInst.MNEMONIC)
data class TvmAppConfigGetstoragefeeInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppConfigInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "GETSTORAGEFEE"
    }
}

/**
 * Retrieves `global_id` from 19 network config.
 */
@Serializable
@SerialName(TvmAppConfigGlobalidInst.MNEMONIC)
data class TvmAppConfigGlobalidInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppConfigInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "GLOBALID"
    }
}

/**
 * Retrives `prev_key_block` part of PrevBlocksInfo from c7 (parameter 13).
 */
@Serializable
@SerialName(TvmAppConfigPrevkeyblockInst.MNEMONIC)
data class TvmAppConfigPrevkeyblockInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppConfigInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "PREVKEYBLOCK"
    }
}

/**
 * Retrives `last_mc_blocks` part of PrevBlocksInfo from c7 (parameter 13).
 */
@Serializable
@SerialName(TvmAppConfigPrevmcblocksInst.MNEMONIC)
data class TvmAppConfigPrevmcblocksInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppConfigInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "PREVMCBLOCKS"
    }
}

/**
 * Aggregates signatures. `n>0`. Throw exception if `n=0` or if some `sig_i` is not a valid signature.
 */
@Serializable
@SerialName(TvmAppCryptoBlsAggregateInst.MNEMONIC)
data class TvmAppCryptoBlsAggregateInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "n*4350-2616")

    companion object {
        const val MNEMONIC = "BLS_AGGREGATE"
    }
}

/**
 * Checks aggregated BLS signature for key-message pairs `pk_1 msg_1...pk_n msg_n`. Return true on succ
 * ess, false otherwise. Return false if `n=0`.
 */
@Serializable
@SerialName(TvmAppCryptoBlsAggregateverifyInst.MNEMONIC)
data class TvmAppCryptoBlsAggregateverifyInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "38534+n*22500")

    companion object {
        const val MNEMONIC = "BLS_AGGREGATEVERIFY"
    }
}

/**
 * Checks aggregated BLS signature for keys `pk_1...pk_n` and message `msg`. Return true on success, fa
 * lse otherwise. Return false if `n=0`.
 */
@Serializable
@SerialName(TvmAppCryptoBlsFastaggregateverifyInst.MNEMONIC)
data class TvmAppCryptoBlsFastaggregateverifyInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "58034+n*3000")

    companion object {
        const val MNEMONIC = "BLS_FASTAGGREGATEVERIFY"
    }
}

/**
 * Addition on G1.
 */
@Serializable
@SerialName(TvmAppCryptoBlsG1AddInst.MNEMONIC)
data class TvmAppCryptoBlsG1AddInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 3934)

    companion object {
        const val MNEMONIC = "BLS_G1_ADD"
    }
}

/**
 * Checks that slice `x` represents a valid element of G1.
 */
@Serializable
@SerialName(TvmAppCryptoBlsG1IngroupInst.MNEMONIC)
data class TvmAppCryptoBlsG1IngroupInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 2984)

    companion object {
        const val MNEMONIC = "BLS_G1_INGROUP"
    }
}

/**
 * Checks that G1 point `x` is equal to zero.
 */
@Serializable
@SerialName(TvmAppCryptoBlsG1IszeroInst.MNEMONIC)
data class TvmAppCryptoBlsG1IszeroInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "BLS_G1_ISZERO"
    }
}

/**
 * Multiplies G1 point `x` by scalar `s`. Any `s` is valid, including negative.
 */
@Serializable
@SerialName(TvmAppCryptoBlsG1MulInst.MNEMONIC)
data class TvmAppCryptoBlsG1MulInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 5234)

    companion object {
        const val MNEMONIC = "BLS_G1_MUL"
    }
}

/**
 * Calculates `x_1*s_1+...+x_n*s_n` for G1 points `x_i` and scalars `s_i`. Returns zero point if `n=0`.
 * Any `s_i` is valid, including negative.
 */
@Serializable
@SerialName(TvmAppCryptoBlsG1MultiexpInst.MNEMONIC)
data class TvmAppCryptoBlsG1MultiexpInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "11409+n*630+n/floor(max(log2(n),4))*8820")

    companion object {
        const val MNEMONIC = "BLS_G1_MULTIEXP"
    }
}

/**
 * Negation on G1.
 */
@Serializable
@SerialName(TvmAppCryptoBlsG1NegInst.MNEMONIC)
data class TvmAppCryptoBlsG1NegInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 784)

    companion object {
        const val MNEMONIC = "BLS_G1_NEG"
    }
}

/**
 * Subtraction on G1.
 */
@Serializable
@SerialName(TvmAppCryptoBlsG1SubInst.MNEMONIC)
data class TvmAppCryptoBlsG1SubInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 3934)

    companion object {
        const val MNEMONIC = "BLS_G1_SUB"
    }
}

/**
 * Pushes zero point in G1.
 */
@Serializable
@SerialName(TvmAppCryptoBlsG1ZeroInst.MNEMONIC)
data class TvmAppCryptoBlsG1ZeroInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "BLS_G1_ZERO"
    }
}

/**
 * Addition on G2.
 */
@Serializable
@SerialName(TvmAppCryptoBlsG2AddInst.MNEMONIC)
data class TvmAppCryptoBlsG2AddInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 6134)

    companion object {
        const val MNEMONIC = "BLS_G2_ADD"
    }
}

/**
 * Checks that slice `x` represents a valid element of G2.
 */
@Serializable
@SerialName(TvmAppCryptoBlsG2IngroupInst.MNEMONIC)
data class TvmAppCryptoBlsG2IngroupInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 4284)

    companion object {
        const val MNEMONIC = "BLS_G2_INGROUP"
    }
}

/**
 * Checks that G2 point `x` is equal to zero.
 */
@Serializable
@SerialName(TvmAppCryptoBlsG2IszeroInst.MNEMONIC)
data class TvmAppCryptoBlsG2IszeroInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "BLS_G2_ISZERO"
    }
}

/**
 * Multiplies G2 point `x` by scalar `s`. Any `s` is valid, including negative.
 */
@Serializable
@SerialName(TvmAppCryptoBlsG2MulInst.MNEMONIC)
data class TvmAppCryptoBlsG2MulInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 10584)

    companion object {
        const val MNEMONIC = "BLS_G2_MUL"
    }
}

/**
 * Calculates `x_1*s_1+...+x_n*s_n` for G2 points `x_i` and scalars `s_i`. Returns zero point if `n=0`.
 * Any `s_i` is valid, including negative.
 */
@Serializable
@SerialName(TvmAppCryptoBlsG2MultiexpInst.MNEMONIC)
data class TvmAppCryptoBlsG2MultiexpInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "30422+n*1280+n/floor(max(log2(n),4))*22840")

    companion object {
        const val MNEMONIC = "BLS_G2_MULTIEXP"
    }
}

/**
 * Negation on G2.
 */
@Serializable
@SerialName(TvmAppCryptoBlsG2NegInst.MNEMONIC)
data class TvmAppCryptoBlsG2NegInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 1584)

    companion object {
        const val MNEMONIC = "BLS_G2_NEG"
    }
}

/**
 * Subtraction on G2.
 */
@Serializable
@SerialName(TvmAppCryptoBlsG2SubInst.MNEMONIC)
data class TvmAppCryptoBlsG2SubInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 6134)

    companion object {
        const val MNEMONIC = "BLS_G2_SUB"
    }
}

/**
 * Pushes zero point in G2.
 */
@Serializable
@SerialName(TvmAppCryptoBlsG2ZeroInst.MNEMONIC)
data class TvmAppCryptoBlsG2ZeroInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "BLS_G2_ZERO"
    }
}

/**
 * Converts FP element `f` to a G1 point.
 */
@Serializable
@SerialName(TvmAppCryptoBlsMapToG1Inst.MNEMONIC)
data class TvmAppCryptoBlsMapToG1Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 2384)

    companion object {
        const val MNEMONIC = "BLS_MAP_TO_G1"
    }
}

/**
 * Converts FP2 element `f` to a G2 point.
 */
@Serializable
@SerialName(TvmAppCryptoBlsMapToG2Inst.MNEMONIC)
data class TvmAppCryptoBlsMapToG2Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 7984)

    companion object {
        const val MNEMONIC = "BLS_MAP_TO_G2"
    }
}

/**
 * Given G1 points `x_i` and G2 points `y_i`, calculates and multiply pairings of `x_i,y_i`. Returns tr
 * ue if the result is the multiplicative identity in FP12, false otherwise. Returns false if `n=0`.
 */
@Serializable
@SerialName(TvmAppCryptoBlsPairingInst.MNEMONIC)
data class TvmAppCryptoBlsPairingInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "20034+n*11800")

    companion object {
        const val MNEMONIC = "BLS_PAIRING"
    }
}

/**
 * Pushes the order of G1 and G2 (approx. `2^255`).
 */
@Serializable
@SerialName(TvmAppCryptoBlsPushrInst.MNEMONIC)
data class TvmAppCryptoBlsPushrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "BLS_PUSHR"
    }
}

/**
 * Checks BLS signature, return true on success, false otherwise.
 */
@Serializable
@SerialName(TvmAppCryptoBlsVerifyInst.MNEMONIC)
data class TvmAppCryptoBlsVerifyInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 61034)

    companion object {
        const val MNEMONIC = "BLS_VERIFY"
    }
}

/**
 * Checks whether `s` is a valid Ed25519-signature of the data portion of _Slice_ `d` using public key
 * `k`, similarly to `CHKSIGNU`. If the bit length of _Slice_ `d` is not divisible by eight, throws a c
 * ell underflow exception. The verification of Ed25519 signatures is the standard one, with `Sha` used
 * to reduce `d` to the 256-bit number that is actually signed.
 */
@Serializable
@SerialName(TvmAppCryptoChksignsInst.MNEMONIC)
data class TvmAppCryptoChksignsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "CHKSIGNS"
    }
}

/**
 * Checks the Ed25519-signature `s` of a hash `h` (a 256-bit unsigned integer, usually computed as the
 * hash of some data) using public key `k` (also represented by a 256-bit unsigned integer).
 * The signature `s` must be a _Slice_ containing at least 512 data bits; only the first 512 bits are u
 * sed. The result is `-1` if the signature is valid, `0` otherwise.
 * Notice that `CHKSIGNU` is equivalent to `ROT` `NEWC` `256 STU` `ENDC` `ROTREV` `CHKSIGNS`, i.e., to
 * `CHKSIGNS` with the first argument `d` set to 256-bit _Slice_ containing `h`. Therefore, if `h` is c
 * omputed as the hash of some data, these data are hashed _twice_, the second hashing occurring inside
 * `CHKSIGNS`.
 */
@Serializable
@SerialName(TvmAppCryptoChksignuInst.MNEMONIC)
data class TvmAppCryptoChksignuInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "CHKSIGNU"
    }
}

/**
 * Recovers public key from signature, identical to Bitcoin/Ethereum operations. Takes 32-byte hash as
 * uint256 `hash`; 65-byte signature as uint8 `v` and uint256 `r`, `s`. Returns `0` on failure, public
 * key and `-1` on success. 65-byte public key is returned as uint8 `h`, uint256 `x1`, `x2`.
 */
@Serializable
@SerialName(TvmAppCryptoEcrecoverInst.MNEMONIC)
data class TvmAppCryptoEcrecoverInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 1526)

    companion object {
        const val MNEMONIC = "ECRECOVER"
    }
}

/**
 * Computes the representation hash of a _Cell_ `c` and returns it as a 256-bit unsigned integer `x`. U
 * seful for signing and checking signatures of arbitrary entities represented by a tree of cells.
 */
@Serializable
@SerialName(TvmAppCryptoHashcuInst.MNEMONIC)
data class TvmAppCryptoHashcuInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "HASHCU"
    }
}

/**
 * Calculates and returns hash of the concatenation of slices (or builders) `s_1...s_n`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextBlake2bInst.MNEMONIC)
data class TvmAppCryptoHashextBlake2bInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/19 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXT_BLAKE2B"
    }
}

/**
 * Calculates and returns hash of the concatenation of slices (or builders) `s_1...s_n`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextKeccak256Inst.MNEMONIC)
data class TvmAppCryptoHashextKeccak256Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/11 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXT_KECCAK256"
    }
}

/**
 * Calculates and returns hash of the concatenation of slices (or builders) `s_1...s_n`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextKeccak512Inst.MNEMONIC)
data class TvmAppCryptoHashextKeccak512Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/19 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXT_KECCAK512"
    }
}

/**
 * Calculates and returns hash of the concatenation of slices (or builders) `s_1...s_n`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextSha256Inst.MNEMONIC)
data class TvmAppCryptoHashextSha256Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/33 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXT_SHA256"
    }
}

/**
 * Calculates and returns hash of the concatenation of slices (or builders) `s_1...s_n`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextSha512Inst.MNEMONIC)
data class TvmAppCryptoHashextSha512Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/16 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXT_SHA512"
    }
}

/**
 * Calculates hash of the concatenation of slices (or builders) `s_1...s_n`. Appends the resulting hash
 * to a builder `b`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextaBlake2bInst.MNEMONIC)
data class TvmAppCryptoHashextaBlake2bInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/19 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXTA_BLAKE2B"
    }
}

/**
 * Calculates hash of the concatenation of slices (or builders) `s_1...s_n`. Appends the resulting hash
 * to a builder `b`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextaKeccak256Inst.MNEMONIC)
data class TvmAppCryptoHashextaKeccak256Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/11 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXTA_KECCAK256"
    }
}

/**
 * Calculates hash of the concatenation of slices (or builders) `s_1...s_n`. Appends the resulting hash
 * to a builder `b`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextaKeccak512Inst.MNEMONIC)
data class TvmAppCryptoHashextaKeccak512Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/6 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXTA_KECCAK512"
    }
}

/**
 * Calculates hash of the concatenation of slices (or builders) `s_1...s_n`. Appends the resulting hash
 * to a builder `b`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextaSha256Inst.MNEMONIC)
data class TvmAppCryptoHashextaSha256Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/33 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXTA_SHA256"
    }
}

/**
 * Calculates hash of the concatenation of slices (or builders) `s_1...s_n`. Appends the resulting hash
 * to a builder `b`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextaSha512Inst.MNEMONIC)
data class TvmAppCryptoHashextaSha512Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/16 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXTA_SHA512"
    }
}

/**
 * Calculates hash of the concatenation of slices (or builders) `s_1...s_n`. Appends the resulting hash
 * to a builder `b`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextarBlake2bInst.MNEMONIC)
data class TvmAppCryptoHashextarBlake2bInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/19 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXTAR_BLAKE2B"
    }
}

/**
 * Calculates hash of the concatenation of slices (or builders) `s_1...s_n`. Appends the resulting hash
 * to a builder `b`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextarKeccak256Inst.MNEMONIC)
data class TvmAppCryptoHashextarKeccak256Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/11 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXTAR_KECCAK256"
    }
}

/**
 * Calculates hash of the concatenation of slices (or builders) `s_1...s_n`. Appends the resulting hash
 * to a builder `b`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextarKeccak512Inst.MNEMONIC)
data class TvmAppCryptoHashextarKeccak512Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/6 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXTAR_KECCAK512"
    }
}

/**
 * Calculates hash of the concatenation of slices (or builders) `s_1...s_n`. Appends the resulting hash
 * to a builder `b`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextarSha256Inst.MNEMONIC)
data class TvmAppCryptoHashextarSha256Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/33 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXTAR_SHA256"
    }
}

/**
 * Calculates hash of the concatenation of slices (or builders) `s_1...s_n`. Appends the resulting hash
 * to a builder `b`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextarSha512Inst.MNEMONIC)
data class TvmAppCryptoHashextarSha512Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/16 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXTAR_SHA512"
    }
}

/**
 * Calculates and returns hash of the concatenation of slices (or builders) `s_1...s_n`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextrBlake2bInst.MNEMONIC)
data class TvmAppCryptoHashextrBlake2bInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/19 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXTR_BLAKE2B"
    }
}

/**
 * Calculates and returns hash of the concatenation of slices (or builders) `s_1...s_n`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextrKeccak256Inst.MNEMONIC)
data class TvmAppCryptoHashextrKeccak256Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/11 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXTR_KECCAK256"
    }
}

/**
 * Calculates and returns hash of the concatenation of slices (or builders) `s_1...s_n`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextrKeccak512Inst.MNEMONIC)
data class TvmAppCryptoHashextrKeccak512Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/19 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXTR_KECCAK512"
    }
}

/**
 * Calculates and returns hash of the concatenation of slices (or builders) `s_1...s_n`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextrSha256Inst.MNEMONIC)
data class TvmAppCryptoHashextrSha256Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/33 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXTR_SHA256"
    }
}

/**
 * Calculates and returns hash of the concatenation of slices (or builders) `s_1...s_n`.
 */
@Serializable
@SerialName(TvmAppCryptoHashextrSha512Inst.MNEMONIC)
data class TvmAppCryptoHashextrSha512Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "1/16 gas per byte")

    companion object {
        const val MNEMONIC = "HASHEXTR_SHA512"
    }
}

/**
 * Computes the hash of a _Slice_ `s` and returns it as a 256-bit unsigned integer `x`. The result is t
 * he same as if an ordinary cell containing only data and references from `s` had been created and its
 * hash computed by `HASHCU`.
 */
@Serializable
@SerialName(TvmAppCryptoHashsuInst.MNEMONIC)
data class TvmAppCryptoHashsuInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 526)

    companion object {
        const val MNEMONIC = "HASHSU"
    }
}

/**
 * Checks seck256r1-signature `sig` of data portion of slice `d` and public key `k`. Returns -1 on succ
 * ess, 0 on failure. Public key is a 33-byte slice (encoded according to Sec. 2.3.4 point 2 of [SECG S
 * EC 1](https://www.secg.org/sec1-v2.pdf)). Signature `sig` is a 64-byte slice (two 256-bit unsigned i
 * ntegers `r` and `s`).
 */
@Serializable
@SerialName(TvmAppCryptoP256ChksignsInst.MNEMONIC)
data class TvmAppCryptoP256ChksignsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 3526)

    companion object {
        const val MNEMONIC = "P256_CHKSIGNS"
    }
}

/**
 * Checks seck256r1-signature `sig` of a number `h` (a 256-bit unsigned integer, usually computed as th
 * e hash of some data) and public key `k`. Returns -1 on success, 0 on failure. Public key is a 33-byt
 * e slice (encoded according to Sec. 2.3.4 point 2 of [SECG SEC 1](https://www.secg.org/sec1-v2.pdf)).
 * Signature `sig` is a 64-byte slice (two 256-bit unsigned integers `r` and `s`).
 */
@Serializable
@SerialName(TvmAppCryptoP256ChksignuInst.MNEMONIC)
data class TvmAppCryptoP256ChksignuInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 3526)

    companion object {
        const val MNEMONIC = "P256_CHKSIGNU"
    }
}

/**
 * Addition of two points on a curve.
 */
@Serializable
@SerialName(TvmAppCryptoRist255AddInst.MNEMONIC)
data class TvmAppCryptoRist255AddInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 626)

    companion object {
        const val MNEMONIC = "RIST255_ADD"
    }
}

/**
 * Deterministically generates a valid point `x` from a 512-bit hash (given as two 256-bit integers).
 */
@Serializable
@SerialName(TvmAppCryptoRist255FromhashInst.MNEMONIC)
data class TvmAppCryptoRist255FromhashInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 626)

    companion object {
        const val MNEMONIC = "RIST255_FROMHASH"
    }
}

/**
 * Multiplies point `x` by a scalar `n`. Any `n` is valid, including negative.
 */
@Serializable
@SerialName(TvmAppCryptoRist255MulInst.MNEMONIC)
data class TvmAppCryptoRist255MulInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 2026)

    companion object {
        const val MNEMONIC = "RIST255_MUL"
    }
}

/**
 * Multiplies the generator point `g` by a scalar `n`. Any `n` is valid, including negative.
 */
@Serializable
@SerialName(TvmAppCryptoRist255MulbaseInst.MNEMONIC)
data class TvmAppCryptoRist255MulbaseInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 776)

    companion object {
        const val MNEMONIC = "RIST255_MULBASE"
    }
}

/**
 * Pushes integer l=2^252+27742317777372353535851937790883648493, which is the order of the group.
 */
@Serializable
@SerialName(TvmAppCryptoRist255PushlInst.MNEMONIC)
data class TvmAppCryptoRist255PushlInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "RIST255_PUSHL"
    }
}

/**
 * Addition of two points on a curve. Returns -1 on success and 0 on failure.
 */
@Serializable
@SerialName(TvmAppCryptoRist255QaddInst.MNEMONIC)
data class TvmAppCryptoRist255QaddInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 634)

    companion object {
        const val MNEMONIC = "RIST255_QADD"
    }
}

/**
 * Multiplies point `x` by a scalar `n`. Any `n` is valid, including negative. Returns -1 on success an
 * d 0 on failure.
 */
@Serializable
@SerialName(TvmAppCryptoRist255QmulInst.MNEMONIC)
data class TvmAppCryptoRist255QmulInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 2034)

    companion object {
        const val MNEMONIC = "RIST255_QMUL"
    }
}

/**
 * Multiplies the generator point `g` by a scalar `n`. Any `n` is valid, including negative.
 */
@Serializable
@SerialName(TvmAppCryptoRist255QmulbaseInst.MNEMONIC)
data class TvmAppCryptoRist255QmulbaseInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 784)

    companion object {
        const val MNEMONIC = "RIST255_QMULBASE"
    }
}

/**
 * Subtraction of two points on curve. Returns -1 on success and 0 on failure.
 */
@Serializable
@SerialName(TvmAppCryptoRist255QsubInst.MNEMONIC)
data class TvmAppCryptoRist255QsubInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 634)

    companion object {
        const val MNEMONIC = "RIST255_QSUB"
    }
}

/**
 * Checks that integer `x` is a valid representation of some curve point. Returns -1 on success and 0 o
 * n failure.
 */
@Serializable
@SerialName(TvmAppCryptoRist255QvalidateInst.MNEMONIC)
data class TvmAppCryptoRist255QvalidateInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 234)

    companion object {
        const val MNEMONIC = "RIST255_QVALIDATE"
    }
}

/**
 * Subtraction of two points on curve.
 */
@Serializable
@SerialName(TvmAppCryptoRist255SubInst.MNEMONIC)
data class TvmAppCryptoRist255SubInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 626)

    companion object {
        const val MNEMONIC = "RIST255_SUB"
    }
}

/**
 * Checks that integer `x` is a valid representation of some curve point. Throws range_chk on error.
 */
@Serializable
@SerialName(TvmAppCryptoRist255ValidateInst.MNEMONIC)
data class TvmAppCryptoRist255ValidateInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 226)

    companion object {
        const val MNEMONIC = "RIST255_VALIDATE"
    }
}

/**
 * Computes `Sha` of the data bits of _Slice_ `s`. If the bit length of `s` is not divisible by eight,
 * throws a cell underflow exception. The hash value is returned as a 256-bit unsigned integer `x`.
 */
@Serializable
@SerialName(TvmAppCryptoSha256uInst.MNEMONIC)
data class TvmAppCryptoSha256uInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCryptoInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SHA256U"
    }
}

/**
 * Loads (deserializes) a `Gram` or `VarUInteger 16` amount from _Slice_ `s`, and returns the amount as
 * _Integer_ `x` along with the remainder `s'` of `s`. The expected serialization of `x` consists of a
 * 4-bit unsigned big-endian integer `l`, followed by an `8l`-bit unsigned big-endian representation o
 * f `x`.
 * The net effect is approximately equivalent to `4 LDU` `SWAP` `3 LSHIFT#` `LDUX`.
 */
@Serializable
@SerialName(TvmAppCurrencyLdgramsInst.MNEMONIC)
data class TvmAppCurrencyLdgramsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCurrencyInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDGRAMS"
    }
}

/**
 * Similar to `LDVARUINT16`, but loads a _signed_ _Integer_ `x`.
 * Approximately equivalent to `4 LDU` `SWAP` `3 LSHIFT#` `LDIX`.
 */
@Serializable
@SerialName(TvmAppCurrencyLdvarint16Inst.MNEMONIC)
data class TvmAppCurrencyLdvarint16Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCurrencyInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDVARINT16"
    }
}

/**
 * Similar to `LDVARUINT32`, but loads a _signed_ _Integer_ `x`.
 * Approximately equivalent to `5 LDU` `SWAP` `3 LSHIFT#` `LDIX`.
 */
@Serializable
@SerialName(TvmAppCurrencyLdvarint32Inst.MNEMONIC)
data class TvmAppCurrencyLdvarint32Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCurrencyInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDVARINT32"
    }
}

/**
 * Loads (deserializes) a `VarUInteger 32` amount from _Slice_ `s`, and returns the amount as _Integer_
 * `x` along with the remainder `s'` of `s`. The expected serialization of `x` consists of a 5-bit uns
 * igned big-endian integer `l`, followed by an `8l`-bit unsigned big-endian representation of `x`.
 * The net effect is approximately equivalent to `4 LDU` `SWAP` `3 LSHIFT#` `LDUX`.
 */
@Serializable
@SerialName(TvmAppCurrencyLdvaruint32Inst.MNEMONIC)
data class TvmAppCurrencyLdvaruint32Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCurrencyInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDVARUINT32"
    }
}

/**
 * Stores (serializes) an _Integer_ `x` in the range `0...2^120-1` into _Builder_ `b`, and returns the
 * resulting _Builder_ `b'`. The serialization of `x` consists of a 4-bit unsigned big-endian integer `
 * l`, which is the smallest integer `l>=0`, such that `x<2^(8l)`, followed by an `8l`-bit unsigned big
 * -endian representation of `x`. If `x` does not belong to the supported range, a range check exceptio
 * n is thrown.
 */
@Serializable
@SerialName(TvmAppCurrencyStgramsInst.MNEMONIC)
data class TvmAppCurrencyStgramsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCurrencyInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STGRAMS"
    }
}

/**
 * Similar to `STVARUINT16`, but serializes a _signed_ _Integer_ `x` in the range `-2^119...2^119-1`.
 */
@Serializable
@SerialName(TvmAppCurrencyStvarint16Inst.MNEMONIC)
data class TvmAppCurrencyStvarint16Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCurrencyInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STVARINT16"
    }
}

/**
 * Similar to `STVARUINT32`, but serializes a _signed_ _Integer_ `x` in the range `-2^247...2^247-1`.
 */
@Serializable
@SerialName(TvmAppCurrencyStvarint32Inst.MNEMONIC)
data class TvmAppCurrencyStvarint32Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCurrencyInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STVARINT32"
    }
}

/**
 * Stores (serializes) an _Integer_ `x` in the range `0...2^248-1` into _Builder_ `b`, and returns the
 * resulting _Builder_ `b'`. The serialization of `x` consists of a 5-bit unsigned big-endian integer `
 * l`, which is the smallest integer `l>=0`, such that `x<2^(8l)`, followed by an `8l`-bit unsigned big
 * -endian representation of `x`. If `x` does not belong to the supported range, a range check exceptio
 * n is thrown.
 */
@Serializable
@SerialName(TvmAppCurrencyStvaruint32Inst.MNEMONIC)
data class TvmAppCurrencyStvaruint32Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppCurrencyInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STVARUINT32"
    }
}

/**
 * Sets current gas limit `g_l` to its maximal allowed value `g_m`, and resets the gas credit `g_c` to
 * zero, decreasing the value of `g_r` by `g_c` in the process.
 * In other words, the current smart contract agrees to buy some gas to finish the current transaction.
 * This action is required to process external messages, which bring no value (hence no gas) with them
 * selves.
 */
@Serializable
@SerialName(TvmAppGasAcceptInst.MNEMONIC)
data class TvmAppGasAcceptInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppGasInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "ACCEPT"
    }
}

/**
 * Commits the current state of registers `c4` (''persistent data'') and `c5` (''actions'') so that the
 * current execution is considered ''successful'' with the saved values even if an exception is thrown
 * later.
 */
@Serializable
@SerialName(TvmAppGasCommitInst.MNEMONIC)
data class TvmAppGasCommitInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppGasInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "COMMIT"
    }
}

/**
 * Returns gas consumed by VM so far (including this instruction).
 */
@Serializable
@SerialName(TvmAppGasGasconsumedInst.MNEMONIC)
data class TvmAppGasGasconsumedInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppGasInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "GASCONSUMED"
    }
}

/**
 * Sets current gas limit `g_l` to the minimum of `g` and `g_m`, and resets the gas credit `g_c` to zer
 * o. If the gas consumed so far (including the present instruction) exceeds the resulting value of `g_
 * l`, an (unhandled) out of gas exception is thrown before setting new gas limits. Notice that `SETGAS
 * LIMIT` with an argument `g >= 2^63-1` is equivalent to `ACCEPT`.
 */
@Serializable
@SerialName(TvmAppGasSetgaslimitInst.MNEMONIC)
data class TvmAppGasSetgaslimitInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppGasInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SETGASLIMIT"
    }
}

/**
 * Returns the `k`-th global variable for `1 <= k <= 31`.
 * Equivalent to `c7 PUSHCTR` `[k] INDEXQ`.
 */
@Serializable
@SerialName(TvmAppGlobalGetglobInst.MNEMONIC)
data class TvmAppGlobalGetglobInst(
    override val location: TvmInstLocation,
    val k: Int, // uint
): TvmInst, TvmAppGlobalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "GETGLOB"
    }
}

/**
 * Returns the `k`-th global variable for `0 <= k < 255`.
 * Equivalent to `c7 PUSHCTR` `SWAP` `INDEXVARQ`.
 */
@Serializable
@SerialName(TvmAppGlobalGetglobvarInst.MNEMONIC)
data class TvmAppGlobalGetglobvarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppGlobalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "GETGLOBVAR"
    }
}

/**
 * Assigns `x` to the `k`-th global variable for `1 <= k <= 31`.
 * Equivalent to `c7 PUSHCTR` `SWAP` `k SETINDEXQ` `c7 POPCTR`.
 */
@Serializable
@SerialName(TvmAppGlobalSetglobInst.MNEMONIC)
data class TvmAppGlobalSetglobInst(
    override val location: TvmInstLocation,
    val k: Int, // uint
): TvmInst, TvmAppGlobalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+|c7'|")

    companion object {
        const val MNEMONIC = "SETGLOB"
    }
}

/**
 * Assigns `x` to the `k`-th global variable for `0 <= k < 255`.
 * Equivalent to `c7 PUSHCTR` `ROTREV` `SETINDEXVARQ` `c7 POPCTR`.
 */
@Serializable
@SerialName(TvmAppGlobalSetglobvarInst.MNEMONIC)
data class TvmAppGlobalSetglobvarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppGlobalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+|c7'|")

    companion object {
        const val MNEMONIC = "SETGLOBVAR"
    }
}

/**
 * A non-quiet version of `CDATASIZEQ` that throws a cell overflow exception (8) on failure.
 */
@Serializable
@SerialName(TvmAppMiscCdatasizeInst.MNEMONIC)
data class TvmAppMiscCdatasizeInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppMiscInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "CDATASIZE"
    }
}

/**
 * Recursively computes the count of distinct cells `x`, data bits `y`, and cell references `z` in the
 * dag rooted at _Cell_ `c`, effectively returning the total storage used by this dag taking into accou
 * nt the identification of equal cells. The values of `x`, `y`, and `z` are computed by a depth-first
 * traversal of this dag, with a hash table of visited cell hashes used to prevent visits of already-vi
 * sited cells. The total count of visited cells `x` cannot exceed non-negative _Integer_ `n`; otherwis
 * e the computation is aborted before visiting the `(n+1)`-st cell and a zero is returned to indicate
 * failure. If `c` is _Null_, returns `x=y=z=0`.
 */
@Serializable
@SerialName(TvmAppMiscCdatasizeqInst.MNEMONIC)
data class TvmAppMiscCdatasizeqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppMiscInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "CDATASIZEQ"
    }
}

/**
 * A non-quiet version of `SDATASIZEQ` that throws a cell overflow exception (8) on failure.
 */
@Serializable
@SerialName(TvmAppMiscSdatasizeInst.MNEMONIC)
data class TvmAppMiscSdatasizeInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppMiscInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "SDATASIZE"
    }
}

/**
 * Similar to `CDATASIZEQ`, but accepting a _Slice_ `s` instead of a _Cell_. The returned value of `x`
 * does not take into account the cell that contains the slice `s` itself; however, the data bits and t
 * he cell references of `s` are accounted for in `y` and `z`.
 */
@Serializable
@SerialName(TvmAppMiscSdatasizeqInst.MNEMONIC)
data class TvmAppMiscSdatasizeqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppMiscInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "SDATASIZEQ"
    }
}

/**
 * Mixes unsigned 256-bit _Integer_ `x` into the random seed `r` by setting the random seed to `Sha` of
 * the concatenation of two 32-byte strings: the first with the big-endian representation of the old s
 * eed `r`, and the second with the big-endian representation of `x`.
 */
@Serializable
@SerialName(TvmAppRndAddrandInst.MNEMONIC)
data class TvmAppRndAddrandInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppRndInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "ADDRAND"
    }
}

/**
 * Generates a new pseudo-random integer `z` in the range `0...y-1` (or `y...-1`, if `y<0`). More preci
 * sely, an unsigned random value `x` is generated as in `RAND256U`; then `z:=floor(x*y/2^256)` is comp
 * uted.
 * Equivalent to `RANDU256` `256 MULRSHIFT`.
 */
@Serializable
@SerialName(TvmAppRndRandInst.MNEMONIC)
data class TvmAppRndRandInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppRndInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+|c7|+|c1_1|")

    companion object {
        const val MNEMONIC = "RAND"
    }
}

/**
 * Generates a new pseudo-random unsigned 256-bit _Integer_ `x`. The algorithm is as follows: if `r` is
 * the old value of the random seed, considered as a 32-byte array (by constructing the big-endian rep
 * resentation of an unsigned 256-bit integer), then its `sha512(r)` is computed; the first 32 bytes of
 * this hash are stored as the new value `r'` of the random seed, and the remaining 32 bytes are retur
 * ned as the next random value `x`.
 */
@Serializable
@SerialName(TvmAppRndRandu256Inst.MNEMONIC)
data class TvmAppRndRandu256Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppRndInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+|c7|+|c1_1|")

    companion object {
        const val MNEMONIC = "RANDU256"
    }
}

/**
 * Sets the random seed to unsigned 256-bit _Integer_ `x`.
 */
@Serializable
@SerialName(TvmAppRndSetrandInst.MNEMONIC)
data class TvmAppRndSetrandInst(
    override val location: TvmInstLocation,
): TvmInst, TvmAppRndInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+|c7|+|c1_1|")

    companion object {
        const val MNEMONIC = "SETRAND"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmBasicAddInst.MNEMONIC)
data class TvmArithmBasicAddInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "ADD"
    }
}

/**
 * `-128 <= cc <= 127`.
 */
@Serializable
@SerialName(TvmArithmBasicAddconstInst.MNEMONIC)
data class TvmArithmBasicAddconstInst(
    override val location: TvmInstLocation,
    val c: Int, // int
): TvmInst, TvmArithmBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "ADDCONST"
    }
}

/**
 * Equivalent to `-1 ADDCONST`.
 */
@Serializable
@SerialName(TvmArithmBasicDecInst.MNEMONIC)
data class TvmArithmBasicDecInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "DEC"
    }
}

/**
 * Equivalent to `1 ADDCONST`.
 */
@Serializable
@SerialName(TvmArithmBasicIncInst.MNEMONIC)
data class TvmArithmBasicIncInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "INC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmBasicMulInst.MNEMONIC)
data class TvmArithmBasicMulInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "MUL"
    }
}

/**
 * `-128 <= cc <= 127`.
 */
@Serializable
@SerialName(TvmArithmBasicMulconstInst.MNEMONIC)
data class TvmArithmBasicMulconstInst(
    override val location: TvmInstLocation,
    val c: Int, // int
): TvmInst, TvmArithmBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULCONST"
    }
}

/**
 * Equivalent to `-1 MULCONST` or to `ZERO SUBR`.
 * Notice that it triggers an integer overflow exception if `x=-2^256`.
 */
@Serializable
@SerialName(TvmArithmBasicNegateInst.MNEMONIC)
data class TvmArithmBasicNegateInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "NEGATE"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmBasicSubInst.MNEMONIC)
data class TvmArithmBasicSubInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "SUB"
    }
}

/**
 * Equivalent to `SWAP` `SUB`.
 */
@Serializable
@SerialName(TvmArithmBasicSubrInst.MNEMONIC)
data class TvmArithmBasicSubrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "SUBR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivAdddivmodInst.MNEMONIC)
data class TvmArithmDivAdddivmodInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "ADDDIVMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivAdddivmodcInst.MNEMONIC)
data class TvmArithmDivAdddivmodcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "ADDDIVMODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivAdddivmodrInst.MNEMONIC)
data class TvmArithmDivAdddivmodrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "ADDDIVMODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivAddrshiftcmodInst.MNEMONIC)
data class TvmArithmDivAddrshiftcmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "ADDRSHIFTCMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivAddrshiftmodInst.MNEMONIC)
data class TvmArithmDivAddrshiftmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "ADDRSHIFTMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivAddrshiftmodVarInst.MNEMONIC)
data class TvmArithmDivAddrshiftmodVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "ADDRSHIFTMOD_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivAddrshiftmodcInst.MNEMONIC)
data class TvmArithmDivAddrshiftmodcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "ADDRSHIFTMODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivAddrshiftmodrInst.MNEMONIC)
data class TvmArithmDivAddrshiftmodrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "ADDRSHIFTMODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivAddrshiftrmodInst.MNEMONIC)
data class TvmArithmDivAddrshiftrmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "ADDRSHIFTRMOD"
    }
}

/**
 * `q=floor(x/y)`, `r=x-y*q`
 */
@Serializable
@SerialName(TvmArithmDivDivInst.MNEMONIC)
data class TvmArithmDivDivInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "DIV"
    }
}

/**
 * `q''=ceil(x/y)`, `r''=x-y*q''`
 */
@Serializable
@SerialName(TvmArithmDivDivcInst.MNEMONIC)
data class TvmArithmDivDivcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "DIVC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivDivmodInst.MNEMONIC)
data class TvmArithmDivDivmodInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "DIVMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivDivmodcInst.MNEMONIC)
data class TvmArithmDivDivmodcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "DIVMODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivDivmodrInst.MNEMONIC)
data class TvmArithmDivDivmodrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "DIVMODR"
    }
}

/**
 * `q'=round(x/y)`, `r'=x-y*q'`
 */
@Serializable
@SerialName(TvmArithmDivDivrInst.MNEMONIC)
data class TvmArithmDivDivrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "DIVR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftadddivmodInst.MNEMONIC)
data class TvmArithmDivLshiftadddivmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LSHIFTADDDIVMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftadddivmodVarInst.MNEMONIC)
data class TvmArithmDivLshiftadddivmodVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LSHIFTADDDIVMOD_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftadddivmodcInst.MNEMONIC)
data class TvmArithmDivLshiftadddivmodcInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LSHIFTADDDIVMODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftadddivmodcVarInst.MNEMONIC)
data class TvmArithmDivLshiftadddivmodcVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LSHIFTADDDIVMODC_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftadddivmodrInst.MNEMONIC)
data class TvmArithmDivLshiftadddivmodrInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LSHIFTADDDIVMODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftadddivmodrVarInst.MNEMONIC)
data class TvmArithmDivLshiftadddivmodrVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LSHIFTADDDIVMODR_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftdivInst.MNEMONIC)
data class TvmArithmDivLshiftdivInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LSHIFTDIV"
    }
}

/**
 * `0 <= z <= 256`
 */
@Serializable
@SerialName(TvmArithmDivLshiftdivVarInst.MNEMONIC)
data class TvmArithmDivLshiftdivVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LSHIFTDIV_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftdivcInst.MNEMONIC)
data class TvmArithmDivLshiftdivcInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LSHIFTDIVC"
    }
}

/**
 * `0 <= z <= 256`
 */
@Serializable
@SerialName(TvmArithmDivLshiftdivcVarInst.MNEMONIC)
data class TvmArithmDivLshiftdivcVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LSHIFTDIVC_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftdivmodInst.MNEMONIC)
data class TvmArithmDivLshiftdivmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LSHIFTDIVMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftdivmodVarInst.MNEMONIC)
data class TvmArithmDivLshiftdivmodVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LSHIFTDIVMOD_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftdivmodcInst.MNEMONIC)
data class TvmArithmDivLshiftdivmodcInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LSHIFTDIVMODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftdivmodcVarInst.MNEMONIC)
data class TvmArithmDivLshiftdivmodcVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LSHIFTDIVMODC_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftdivmodrInst.MNEMONIC)
data class TvmArithmDivLshiftdivmodrInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LSHIFTDIVMODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftdivmodrVarInst.MNEMONIC)
data class TvmArithmDivLshiftdivmodrVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LSHIFTDIVMODR_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftdivrInst.MNEMONIC)
data class TvmArithmDivLshiftdivrInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LSHIFTDIVR"
    }
}

/**
 * `0 <= z <= 256`
 */
@Serializable
@SerialName(TvmArithmDivLshiftdivrVarInst.MNEMONIC)
data class TvmArithmDivLshiftdivrVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LSHIFTDIVR_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftmodInst.MNEMONIC)
data class TvmArithmDivLshiftmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LSHIFTMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftmodVarInst.MNEMONIC)
data class TvmArithmDivLshiftmodVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LSHIFTMOD_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftmodcInst.MNEMONIC)
data class TvmArithmDivLshiftmodcInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LSHIFTMODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftmodcVarInst.MNEMONIC)
data class TvmArithmDivLshiftmodcVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LSHIFTMODC_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftmodrInst.MNEMONIC)
data class TvmArithmDivLshiftmodrInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LSHIFTMODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivLshiftmodrVarInst.MNEMONIC)
data class TvmArithmDivLshiftmodrVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LSHIFTMODR_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivModInst.MNEMONIC)
data class TvmArithmDivModInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivModcInst.MNEMONIC)
data class TvmArithmDivModcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivModpow2Inst.MNEMONIC)
data class TvmArithmDivModpow2Inst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "MODPOW2"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivModpow2VarInst.MNEMONIC)
data class TvmArithmDivModpow2VarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MODPOW2_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivModpow2cInst.MNEMONIC)
data class TvmArithmDivModpow2cInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "MODPOW2C"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivModpow2cVarInst.MNEMONIC)
data class TvmArithmDivModpow2cVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MODPOW2C_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivModpow2rInst.MNEMONIC)
data class TvmArithmDivModpow2rInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "MODPOW2R"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivModpow2rVarInst.MNEMONIC)
data class TvmArithmDivModpow2rVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MODPOW2R_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivModrInst.MNEMONIC)
data class TvmArithmDivModrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMuladddivmodInst.MNEMONIC)
data class TvmArithmDivMuladddivmodInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULADDDIVMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMuladddivmodcInst.MNEMONIC)
data class TvmArithmDivMuladddivmodcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULADDDIVMODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMuladddivmodrInst.MNEMONIC)
data class TvmArithmDivMuladddivmodrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULADDDIVMODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMuladdrshiftcmodInst.MNEMONIC)
data class TvmArithmDivMuladdrshiftcmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "MULADDRSHIFTCMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMuladdrshiftmodInst.MNEMONIC)
data class TvmArithmDivMuladdrshiftmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "MULADDRSHIFTMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMuladdrshiftrmodInst.MNEMONIC)
data class TvmArithmDivMuladdrshiftrmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "MULADDRSHIFTRMOD"
    }
}

/**
 * `q=floor(x*y/z)`
 */
@Serializable
@SerialName(TvmArithmDivMuldivInst.MNEMONIC)
data class TvmArithmDivMuldivInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULDIV"
    }
}

/**
 * `q'=ceil(x*y/z)`
 */
@Serializable
@SerialName(TvmArithmDivMuldivcInst.MNEMONIC)
data class TvmArithmDivMuldivcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULDIVC"
    }
}

/**
 * `q=floor(x*y/z)`, `r=x*y-z*q`
 */
@Serializable
@SerialName(TvmArithmDivMuldivmodInst.MNEMONIC)
data class TvmArithmDivMuldivmodInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULDIVMOD"
    }
}

/**
 * `q=ceil(x*y/z)`, `r=x*y-z*q`
 */
@Serializable
@SerialName(TvmArithmDivMuldivmodcInst.MNEMONIC)
data class TvmArithmDivMuldivmodcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULDIVMODC"
    }
}

/**
 * `q=round(x*y/z)`, `r=x*y-z*q`
 */
@Serializable
@SerialName(TvmArithmDivMuldivmodrInst.MNEMONIC)
data class TvmArithmDivMuldivmodrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULDIVMODR"
    }
}

/**
 * `q'=round(x*y/z)`
 */
@Serializable
@SerialName(TvmArithmDivMuldivrInst.MNEMONIC)
data class TvmArithmDivMuldivrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULDIVR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulmodInst.MNEMONIC)
data class TvmArithmDivMulmodInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulmodcInst.MNEMONIC)
data class TvmArithmDivMulmodcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULMODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulmodpow2Inst.MNEMONIC)
data class TvmArithmDivMulmodpow2Inst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "MULMODPOW2"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulmodpow2VarInst.MNEMONIC)
data class TvmArithmDivMulmodpow2VarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULMODPOW2_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulmodpow2cInst.MNEMONIC)
data class TvmArithmDivMulmodpow2cInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "MULMODPOW2C"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulmodpow2cVarInst.MNEMONIC)
data class TvmArithmDivMulmodpow2cVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULMODPOW2C_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulmodpow2rInst.MNEMONIC)
data class TvmArithmDivMulmodpow2rInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "MULMODPOW2R"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulmodpow2rVarInst.MNEMONIC)
data class TvmArithmDivMulmodpow2rVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULMODPOW2R_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulmodrInst.MNEMONIC)
data class TvmArithmDivMulmodrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULMODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulrshiftInst.MNEMONIC)
data class TvmArithmDivMulrshiftInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "MULRSHIFT"
    }
}

/**
 * `0 <= z <= 256`
 */
@Serializable
@SerialName(TvmArithmDivMulrshiftVarInst.MNEMONIC)
data class TvmArithmDivMulrshiftVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULRSHIFT_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulrshiftcInst.MNEMONIC)
data class TvmArithmDivMulrshiftcInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "MULRSHIFTC"
    }
}

/**
 * `0 <= z <= 256`
 */
@Serializable
@SerialName(TvmArithmDivMulrshiftcVarInst.MNEMONIC)
data class TvmArithmDivMulrshiftcVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULRSHIFTC_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulrshiftcmodInst.MNEMONIC)
data class TvmArithmDivMulrshiftcmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "MULRSHIFTCMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulrshiftcmodVarInst.MNEMONIC)
data class TvmArithmDivMulrshiftcmodVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULRSHIFTCMOD_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulrshiftmodInst.MNEMONIC)
data class TvmArithmDivMulrshiftmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "MULRSHIFTMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulrshiftmodVarInst.MNEMONIC)
data class TvmArithmDivMulrshiftmodVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULRSHIFTMOD_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulrshiftrInst.MNEMONIC)
data class TvmArithmDivMulrshiftrInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "MULRSHIFTR"
    }
}

/**
 * `0 <= z <= 256`
 */
@Serializable
@SerialName(TvmArithmDivMulrshiftrVarInst.MNEMONIC)
data class TvmArithmDivMulrshiftrVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULRSHIFTR_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulrshiftrmodInst.MNEMONIC)
data class TvmArithmDivMulrshiftrmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "MULRSHIFTRMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivMulrshiftrmodVarInst.MNEMONIC)
data class TvmArithmDivMulrshiftrmodVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MULRSHIFTRMOD_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivRshiftcInst.MNEMONIC)
data class TvmArithmDivRshiftcInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "RSHIFTC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivRshiftcVarInst.MNEMONIC)
data class TvmArithmDivRshiftcVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "RSHIFTC_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivRshiftcmodInst.MNEMONIC)
data class TvmArithmDivRshiftcmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "RSHIFTCMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivRshiftmodInst.MNEMONIC)
data class TvmArithmDivRshiftmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "RSHIFTMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivRshiftmodVarInst.MNEMONIC)
data class TvmArithmDivRshiftmodVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "RSHIFTMOD_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivRshiftmodcVarInst.MNEMONIC)
data class TvmArithmDivRshiftmodcVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "RSHIFTMODC_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivRshiftmodrVarInst.MNEMONIC)
data class TvmArithmDivRshiftmodrVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "RSHIFTMODR_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivRshiftrInst.MNEMONIC)
data class TvmArithmDivRshiftrInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "RSHIFTR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivRshiftrVarInst.MNEMONIC)
data class TvmArithmDivRshiftrVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "RSHIFTR_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmDivRshiftrmodInst.MNEMONIC)
data class TvmArithmDivRshiftrmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmDivInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "RSHIFTRMOD"
    }
}

/**
 * Computes the absolute value of an integer `x`.
 */
@Serializable
@SerialName(TvmArithmLogicalAbsInst.MNEMONIC)
data class TvmArithmLogicalAbsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "ABS"
    }
}

/**
 * Bitwise and of two signed integers `x` and `y`, sign-extended to infinity.
 */
@Serializable
@SerialName(TvmArithmLogicalAndInst.MNEMONIC)
data class TvmArithmLogicalAndInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "AND"
    }
}

/**
 * Computes smallest `c >= 0` such that `x` fits into a `c`-bit signed integer (`-2^(c-1) <= c < 2^(c-1
 * )`).
 */
@Serializable
@SerialName(TvmArithmLogicalBitsizeInst.MNEMONIC)
data class TvmArithmLogicalBitsizeInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BITSIZE"
    }
}

/**
 * Checks whether `x` is a `cc+1`-bit signed integer for `0 <= cc <= 255` (i.e., whether `-2^cc <= x <
 * 2^cc`).
 * If not, either triggers an integer overflow exception, or replaces `x` with a `NaN` (quiet version).
 */
@Serializable
@SerialName(TvmArithmLogicalFitsInst.MNEMONIC)
data class TvmArithmLogicalFitsInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/76")

    companion object {
        const val MNEMONIC = "FITS"
    }
}

/**
 * Checks whether `x` is a `c`-bit signed integer for `0 <= c <= 1023`.
 */
@Serializable
@SerialName(TvmArithmLogicalFitsxInst.MNEMONIC)
data class TvmArithmLogicalFitsxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/76")

    companion object {
        const val MNEMONIC = "FITSX"
    }
}

/**
 * `0 <= cc <= 255`
 */
@Serializable
@SerialName(TvmArithmLogicalLshiftInst.MNEMONIC)
data class TvmArithmLogicalLshiftInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LSHIFT"
    }
}

/**
 * `0 <= y <= 1023`
 */
@Serializable
@SerialName(TvmArithmLogicalLshiftVarInst.MNEMONIC)
data class TvmArithmLogicalLshiftVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "LSHIFT_VAR"
    }
}

/**
 * Computes the maximum of two integers `x` and `y`.
 */
@Serializable
@SerialName(TvmArithmLogicalMaxInst.MNEMONIC)
data class TvmArithmLogicalMaxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MAX"
    }
}

/**
 * Computes the minimum of two integers `x` and `y`.
 */
@Serializable
@SerialName(TvmArithmLogicalMinInst.MNEMONIC)
data class TvmArithmLogicalMinInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MIN"
    }
}

/**
 * Sorts two integers. Quiet version of this operation returns two `NaN`s if any of the arguments are `
 * NaN`s.
 */
@Serializable
@SerialName(TvmArithmLogicalMinmaxInst.MNEMONIC)
data class TvmArithmLogicalMinmaxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "MINMAX"
    }
}

/**
 * Bitwise not of an integer.
 */
@Serializable
@SerialName(TvmArithmLogicalNotInst.MNEMONIC)
data class TvmArithmLogicalNotInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "NOT"
    }
}

/**
 * Bitwise or of two integers.
 */
@Serializable
@SerialName(TvmArithmLogicalOrInst.MNEMONIC)
data class TvmArithmLogicalOrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "OR"
    }
}

/**
 * `0 <= y <= 1023`
 * Equivalent to `ONE` `SWAP` `LSHIFT`.
 */
@Serializable
@SerialName(TvmArithmLogicalPow2Inst.MNEMONIC)
data class TvmArithmLogicalPow2Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "POW2"
    }
}

/**
 * `0 <= cc <= 255`
 */
@Serializable
@SerialName(TvmArithmLogicalRshiftInst.MNEMONIC)
data class TvmArithmLogicalRshiftInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "RSHIFT"
    }
}

/**
 * `0 <= y <= 1023`
 */
@Serializable
@SerialName(TvmArithmLogicalRshiftVarInst.MNEMONIC)
data class TvmArithmLogicalRshiftVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "RSHIFT_VAR"
    }
}

/**
 * Computes smallest `c >= 0` such that `x` fits into a `c`-bit unsigned integer (`0 <= x < 2^c`), or t
 * hrows a range check exception.
 */
@Serializable
@SerialName(TvmArithmLogicalUbitsizeInst.MNEMONIC)
data class TvmArithmLogicalUbitsizeInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "UBITSIZE"
    }
}

/**
 * Checks whether `x` is a `cc+1`-bit unsigned integer for `0 <= cc <= 255` (i.e., whether `0 <= x < 2^
 * (cc+1)`).
 */
@Serializable
@SerialName(TvmArithmLogicalUfitsInst.MNEMONIC)
data class TvmArithmLogicalUfitsInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/76")

    companion object {
        const val MNEMONIC = "UFITS"
    }
}

/**
 * Checks whether `x` is a `c`-bit unsigned integer for `0 <= c <= 1023`.
 */
@Serializable
@SerialName(TvmArithmLogicalUfitsxInst.MNEMONIC)
data class TvmArithmLogicalUfitsxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/76")

    companion object {
        const val MNEMONIC = "UFITSX"
    }
}

/**
 * Bitwise xor of two integers.
 */
@Serializable
@SerialName(TvmArithmLogicalXorInst.MNEMONIC)
data class TvmArithmLogicalXorInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmLogicalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "XOR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQaddInst.MNEMONIC)
data class TvmArithmQuietQaddInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "QADD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQadddivmodInst.MNEMONIC)
data class TvmArithmQuietQadddivmodInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QADDDIVMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQadddivmodcInst.MNEMONIC)
data class TvmArithmQuietQadddivmodcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QADDDIVMODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQadddivmodrInst.MNEMONIC)
data class TvmArithmQuietQadddivmodrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QADDDIVMODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQaddrshiftcmodInst.MNEMONIC)
data class TvmArithmQuietQaddrshiftcmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QADDRSHIFTCMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQaddrshiftmodInst.MNEMONIC)
data class TvmArithmQuietQaddrshiftmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QADDRSHIFTMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQaddrshiftmodcInst.MNEMONIC)
data class TvmArithmQuietQaddrshiftmodcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QADDRSHIFTMODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQaddrshiftmodrInst.MNEMONIC)
data class TvmArithmQuietQaddrshiftmodrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QADDRSHIFTMODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQaddrshiftrmodInst.MNEMONIC)
data class TvmArithmQuietQaddrshiftrmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QADDRSHIFTRMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQandInst.MNEMONIC)
data class TvmArithmQuietQandInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "QAND"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQdecInst.MNEMONIC)
data class TvmArithmQuietQdecInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "QDEC"
    }
}

/**
 * Division returns `NaN` if `y=0`.
 */
@Serializable
@SerialName(TvmArithmQuietQdivInst.MNEMONIC)
data class TvmArithmQuietQdivInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QDIV"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQdivcInst.MNEMONIC)
data class TvmArithmQuietQdivcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QDIVC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQdivmodInst.MNEMONIC)
data class TvmArithmQuietQdivmodInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QDIVMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQdivmodcInst.MNEMONIC)
data class TvmArithmQuietQdivmodcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QDIVMODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQdivmodrInst.MNEMONIC)
data class TvmArithmQuietQdivmodrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QDIVMODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQdivrInst.MNEMONIC)
data class TvmArithmQuietQdivrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QDIVR"
    }
}

/**
 * Replaces `x` with a `NaN` if x is not a `cc+1`-bit signed integer, leaves it intact otherwise.
 */
@Serializable
@SerialName(TvmArithmQuietQfitsInst.MNEMONIC)
data class TvmArithmQuietQfitsInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QFITS"
    }
}

/**
 * Replaces `x` with a `NaN` if x is not a c-bit signed integer, leaves it intact otherwise.
 */
@Serializable
@SerialName(TvmArithmQuietQfitsxInst.MNEMONIC)
data class TvmArithmQuietQfitsxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QFITSX"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQincInst.MNEMONIC)
data class TvmArithmQuietQincInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "QINC"
    }
}

/**
 * `0 <= cc <= 255`
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftInst.MNEMONIC)
data class TvmArithmQuietQlshiftInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QLSHIFT"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftVarInst.MNEMONIC)
data class TvmArithmQuietQlshiftVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "QLSHIFT_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftadddivmodInst.MNEMONIC)
data class TvmArithmQuietQlshiftadddivmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QLSHIFTADDDIVMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftadddivmodVarInst.MNEMONIC)
data class TvmArithmQuietQlshiftadddivmodVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QLSHIFTADDDIVMOD_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftadddivmodcInst.MNEMONIC)
data class TvmArithmQuietQlshiftadddivmodcInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QLSHIFTADDDIVMODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftadddivmodcVarInst.MNEMONIC)
data class TvmArithmQuietQlshiftadddivmodcVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QLSHIFTADDDIVMODC_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftadddivmodrInst.MNEMONIC)
data class TvmArithmQuietQlshiftadddivmodrInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QLSHIFTADDDIVMODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftadddivmodrVarInst.MNEMONIC)
data class TvmArithmQuietQlshiftadddivmodrVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QLSHIFTADDDIVMODR_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftdivInst.MNEMONIC)
data class TvmArithmQuietQlshiftdivInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QLSHIFTDIV"
    }
}

/**
 * `0 <= z <= 256`
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftdivVarInst.MNEMONIC)
data class TvmArithmQuietQlshiftdivVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QLSHIFTDIV_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftdivcInst.MNEMONIC)
data class TvmArithmQuietQlshiftdivcInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QLSHIFTDIVC"
    }
}

/**
 * `0 <= z <= 256`
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftdivcVarInst.MNEMONIC)
data class TvmArithmQuietQlshiftdivcVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QLSHIFTDIVC_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftdivmodInst.MNEMONIC)
data class TvmArithmQuietQlshiftdivmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QLSHIFTDIVMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftdivmodVarInst.MNEMONIC)
data class TvmArithmQuietQlshiftdivmodVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QLSHIFTDIVMOD_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftdivmodcInst.MNEMONIC)
data class TvmArithmQuietQlshiftdivmodcInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QLSHIFTDIVMODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftdivmodcVarInst.MNEMONIC)
data class TvmArithmQuietQlshiftdivmodcVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QLSHIFTDIVMODC_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftdivmodrInst.MNEMONIC)
data class TvmArithmQuietQlshiftdivmodrInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QLSHIFTDIVMODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftdivmodrVarInst.MNEMONIC)
data class TvmArithmQuietQlshiftdivmodrVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QLSHIFTDIVMODR_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftdivrInst.MNEMONIC)
data class TvmArithmQuietQlshiftdivrInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QLSHIFTDIVR"
    }
}

/**
 * `0 <= z <= 256`
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftdivrVarInst.MNEMONIC)
data class TvmArithmQuietQlshiftdivrVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QLSHIFTDIVR_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftmodInst.MNEMONIC)
data class TvmArithmQuietQlshiftmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QLSHIFTMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftmodVarInst.MNEMONIC)
data class TvmArithmQuietQlshiftmodVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QLSHIFTMOD_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftmodcInst.MNEMONIC)
data class TvmArithmQuietQlshiftmodcInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QLSHIFTMODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftmodcVarInst.MNEMONIC)
data class TvmArithmQuietQlshiftmodcVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QLSHIFTMODC_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftmodrInst.MNEMONIC)
data class TvmArithmQuietQlshiftmodrInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QLSHIFTMODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQlshiftmodrVarInst.MNEMONIC)
data class TvmArithmQuietQlshiftmodrVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QLSHIFTMODR_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmodInst.MNEMONIC)
data class TvmArithmQuietQmodInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmodcInst.MNEMONIC)
data class TvmArithmQuietQmodcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmodpow2Inst.MNEMONIC)
data class TvmArithmQuietQmodpow2Inst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QMODPOW2"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmodpow2VarInst.MNEMONIC)
data class TvmArithmQuietQmodpow2VarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMODPOW2_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmodpow2cInst.MNEMONIC)
data class TvmArithmQuietQmodpow2cInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QMODPOW2C"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmodpow2cVarInst.MNEMONIC)
data class TvmArithmQuietQmodpow2cVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMODPOW2C_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmodpow2rInst.MNEMONIC)
data class TvmArithmQuietQmodpow2rInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QMODPOW2R"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmodpow2rVarInst.MNEMONIC)
data class TvmArithmQuietQmodpow2rVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMODPOW2R_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmodrInst.MNEMONIC)
data class TvmArithmQuietQmodrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulInst.MNEMONIC)
data class TvmArithmQuietQmulInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "QMUL"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmuladddivmodInst.MNEMONIC)
data class TvmArithmQuietQmuladddivmodInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULADDDIVMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmuladddivmodcInst.MNEMONIC)
data class TvmArithmQuietQmuladddivmodcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULADDDIVMODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmuladddivmodrInst.MNEMONIC)
data class TvmArithmQuietQmuladddivmodrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULADDDIVMODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmuladdrshiftcmodInst.MNEMONIC)
data class TvmArithmQuietQmuladdrshiftcmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QMULADDRSHIFTCMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmuladdrshiftmodInst.MNEMONIC)
data class TvmArithmQuietQmuladdrshiftmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QMULADDRSHIFTMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmuladdrshiftrmodInst.MNEMONIC)
data class TvmArithmQuietQmuladdrshiftrmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QMULADDRSHIFTRMOD"
    }
}

/**
 * `q=floor(x*y/z)`
 */
@Serializable
@SerialName(TvmArithmQuietQmuldivInst.MNEMONIC)
data class TvmArithmQuietQmuldivInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULDIV"
    }
}

/**
 * `q'=ceil(x*y/z)`
 */
@Serializable
@SerialName(TvmArithmQuietQmuldivcInst.MNEMONIC)
data class TvmArithmQuietQmuldivcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULDIVC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmuldivmodInst.MNEMONIC)
data class TvmArithmQuietQmuldivmodInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULDIVMOD"
    }
}

/**
 * `q=ceil(x*y/z)`, `r=x*y-z*q`
 */
@Serializable
@SerialName(TvmArithmQuietQmuldivmodcInst.MNEMONIC)
data class TvmArithmQuietQmuldivmodcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULDIVMODC"
    }
}

/**
 * `q=round(x*y/z)`, `r=x*y-z*q`
 */
@Serializable
@SerialName(TvmArithmQuietQmuldivmodrInst.MNEMONIC)
data class TvmArithmQuietQmuldivmodrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULDIVMODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmuldivrInst.MNEMONIC)
data class TvmArithmQuietQmuldivrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULDIVR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulmodInst.MNEMONIC)
data class TvmArithmQuietQmulmodInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulmodcInst.MNEMONIC)
data class TvmArithmQuietQmulmodcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULMODC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulmodpow2Inst.MNEMONIC)
data class TvmArithmQuietQmulmodpow2Inst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QMULMODPOW2"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulmodpow2VarInst.MNEMONIC)
data class TvmArithmQuietQmulmodpow2VarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULMODPOW2_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulmodpow2cInst.MNEMONIC)
data class TvmArithmQuietQmulmodpow2cInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QMULMODPOW2C"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulmodpow2cVarInst.MNEMONIC)
data class TvmArithmQuietQmulmodpow2cVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULMODPOW2C_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulmodpow2rInst.MNEMONIC)
data class TvmArithmQuietQmulmodpow2rInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QMULMODPOW2R"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulmodpow2rVarInst.MNEMONIC)
data class TvmArithmQuietQmulmodpow2rVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULMODPOW2R_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulmodrInst.MNEMONIC)
data class TvmArithmQuietQmulmodrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULMODR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulrshiftInst.MNEMONIC)
data class TvmArithmQuietQmulrshiftInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QMULRSHIFT"
    }
}

/**
 * `0 <= z <= 256`
 */
@Serializable
@SerialName(TvmArithmQuietQmulrshiftVarInst.MNEMONIC)
data class TvmArithmQuietQmulrshiftVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULRSHIFT_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulrshiftcInst.MNEMONIC)
data class TvmArithmQuietQmulrshiftcInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QMULRSHIFTC"
    }
}

/**
 * `0 <= z <= 256`
 */
@Serializable
@SerialName(TvmArithmQuietQmulrshiftcVarInst.MNEMONIC)
data class TvmArithmQuietQmulrshiftcVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULRSHIFTC_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulrshiftcmodInst.MNEMONIC)
data class TvmArithmQuietQmulrshiftcmodInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QMULRSHIFTCMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulrshiftcmodVarInst.MNEMONIC)
data class TvmArithmQuietQmulrshiftcmodVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULRSHIFTCMOD_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulrshiftmodInst.MNEMONIC)
data class TvmArithmQuietQmulrshiftmodInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QMULRSHIFTMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulrshiftmodVarInst.MNEMONIC)
data class TvmArithmQuietQmulrshiftmodVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULRSHIFTMOD_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulrshiftrInst.MNEMONIC)
data class TvmArithmQuietQmulrshiftrInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QMULRSHIFTR"
    }
}

/**
 * `0 <= z <= 256`
 */
@Serializable
@SerialName(TvmArithmQuietQmulrshiftrVarInst.MNEMONIC)
data class TvmArithmQuietQmulrshiftrVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULRSHIFTR_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulrshiftrmodInst.MNEMONIC)
data class TvmArithmQuietQmulrshiftrmodInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QMULRSHIFTRMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQmulrshiftrmodVarInst.MNEMONIC)
data class TvmArithmQuietQmulrshiftrmodVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QMULRSHIFTRMOD_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQnegateInst.MNEMONIC)
data class TvmArithmQuietQnegateInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "QNEGATE"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQnotInst.MNEMONIC)
data class TvmArithmQuietQnotInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "QNOT"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQorInst.MNEMONIC)
data class TvmArithmQuietQorInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "QOR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQpow2Inst.MNEMONIC)
data class TvmArithmQuietQpow2Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "QPOW2"
    }
}

/**
 * `0 <= cc <= 255`
 */
@Serializable
@SerialName(TvmArithmQuietQrshiftInst.MNEMONIC)
data class TvmArithmQuietQrshiftInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "QRSHIFT"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQrshiftVarInst.MNEMONIC)
data class TvmArithmQuietQrshiftVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "QRSHIFT_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQrshiftcInst.MNEMONIC)
data class TvmArithmQuietQrshiftcInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QRSHIFTC"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQrshiftcVarInst.MNEMONIC)
data class TvmArithmQuietQrshiftcVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QRSHIFTC_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQrshiftcmodInst.MNEMONIC)
data class TvmArithmQuietQrshiftcmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QRSHIFTCMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQrshiftmodInst.MNEMONIC)
data class TvmArithmQuietQrshiftmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QRSHIFTMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQrshiftmodVarInst.MNEMONIC)
data class TvmArithmQuietQrshiftmodVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QRSHIFTMOD_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQrshiftmodcVarInst.MNEMONIC)
data class TvmArithmQuietQrshiftmodcVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QRSHIFTMODC_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQrshiftmodrVarInst.MNEMONIC)
data class TvmArithmQuietQrshiftmodrVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QRSHIFTMODR_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQrshiftrInst.MNEMONIC)
data class TvmArithmQuietQrshiftrInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QRSHIFTR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQrshiftrVarInst.MNEMONIC)
data class TvmArithmQuietQrshiftrVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QRSHIFTR_VAR"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQrshiftrmodInst.MNEMONIC)
data class TvmArithmQuietQrshiftrmodInst(
    override val location: TvmInstLocation,
    val t: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 42)

    companion object {
        const val MNEMONIC = "QRSHIFTRMOD"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQsubInst.MNEMONIC)
data class TvmArithmQuietQsubInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "QSUB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQsubrInst.MNEMONIC)
data class TvmArithmQuietQsubrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "QSUBR"
    }
}

/**
 * Replaces `x` with a `NaN` if x is not a `cc+1`-bit unsigned integer, leaves it intact otherwise.
 */
@Serializable
@SerialName(TvmArithmQuietQufitsInst.MNEMONIC)
data class TvmArithmQuietQufitsInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QUFITS"
    }
}

/**
 * Replaces `x` with a `NaN` if x is not a c-bit unsigned integer, leaves it intact otherwise.
 */
@Serializable
@SerialName(TvmArithmQuietQufitsxInst.MNEMONIC)
data class TvmArithmQuietQufitsxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "QUFITSX"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmArithmQuietQxorInst.MNEMONIC)
data class TvmArithmQuietQxorInst(
    override val location: TvmInstLocation,
): TvmInst, TvmArithmQuietInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "QXOR"
    }
}

/**
 * Returns the numbers of both data bits and cell references in `b`.
 */
@Serializable
@SerialName(TvmCellBuildBbitrefsInst.MNEMONIC)
data class TvmCellBuildBbitrefsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BBITREFS"
    }
}

/**
 * Returns the number of data bits already stored in _Builder_ `b`.
 */
@Serializable
@SerialName(TvmCellBuildBbitsInst.MNEMONIC)
data class TvmCellBuildBbitsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BBITS"
    }
}

/**
 * Checks whether `x` bits and `y` references can be stored into `b`, `0 <= x <= 1023`, `0 <= y <= 7`.
 */
@Serializable
@SerialName(TvmCellBuildBchkbitrefsInst.MNEMONIC)
data class TvmCellBuildBchkbitrefsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/76")

    companion object {
        const val MNEMONIC = "BCHKBITREFS"
    }
}

/**
 * Checks whether `x` bits and `y` references can be stored into `b`, `0 <= x <= 1023`, `0 <= y <= 7`.
 */
@Serializable
@SerialName(TvmCellBuildBchkbitrefsqInst.MNEMONIC)
data class TvmCellBuildBchkbitrefsqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BCHKBITREFSQ"
    }
}

/**
 * Checks whether `cc+1` bits can be stored into `b`, where `0 <= cc <= 255`.
 */
@Serializable
@SerialName(TvmCellBuildBchkbitsInst.MNEMONIC)
data class TvmCellBuildBchkbitsInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "34/84")

    companion object {
        const val MNEMONIC = "BCHKBITS"
    }
}

/**
 * Checks whether `x` bits can be stored into `b`, `0 <= x <= 1023`. If there is no space for `x` more
 * bits in `b`, or if `x` is not within the range `0...1023`, throws an exception.
 */
@Serializable
@SerialName(TvmCellBuildBchkbitsVarInst.MNEMONIC)
data class TvmCellBuildBchkbitsVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/76")

    companion object {
        const val MNEMONIC = "BCHKBITS_VAR"
    }
}

/**
 * Checks whether `cc+1` bits can be stored into `b`, where `0 <= cc <= 255`.
 */
@Serializable
@SerialName(TvmCellBuildBchkbitsqInst.MNEMONIC)
data class TvmCellBuildBchkbitsqInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "BCHKBITSQ"
    }
}

/**
 * Checks whether `x` bits can be stored into `b`, `0 <= x <= 1023`.
 */
@Serializable
@SerialName(TvmCellBuildBchkbitsqVarInst.MNEMONIC)
data class TvmCellBuildBchkbitsqVarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BCHKBITSQ_VAR"
    }
}

/**
 * Checks whether `y` references can be stored into `b`, `0 <= y <= 7`.
 */
@Serializable
@SerialName(TvmCellBuildBchkrefsInst.MNEMONIC)
data class TvmCellBuildBchkrefsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/76")

    companion object {
        const val MNEMONIC = "BCHKREFS"
    }
}

/**
 * Checks whether `y` references can be stored into `b`, `0 <= y <= 7`.
 */
@Serializable
@SerialName(TvmCellBuildBchkrefsqInst.MNEMONIC)
data class TvmCellBuildBchkrefsqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BCHKREFSQ"
    }
}

/**
 * Returns the depth of _Builder_ `b`. If no cell references are stored in `b`, then `x=0`; otherwise `
 * x` is one plus the maximum of depths of cells referred to from `b`.
 */
@Serializable
@SerialName(TvmCellBuildBdepthInst.MNEMONIC)
data class TvmCellBuildBdepthInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BDEPTH"
    }
}

/**
 * Returns the number of cell references already stored in `b`.
 */
@Serializable
@SerialName(TvmCellBuildBrefsInst.MNEMONIC)
data class TvmCellBuildBrefsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BREFS"
    }
}

/**
 * Returns the numbers of both data bits and references that can still be stored in `b`.
 */
@Serializable
@SerialName(TvmCellBuildBrembitrefsInst.MNEMONIC)
data class TvmCellBuildBrembitrefsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BREMBITREFS"
    }
}

/**
 * Returns the number of data bits that can still be stored in `b`.
 */
@Serializable
@SerialName(TvmCellBuildBrembitsInst.MNEMONIC)
data class TvmCellBuildBrembitsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BREMBITS"
    }
}

/**
 * Returns the number of references that can still be stored in `b`.
 */
@Serializable
@SerialName(TvmCellBuildBremrefsInst.MNEMONIC)
data class TvmCellBuildBremrefsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BREMREFS"
    }
}

/**
 * Converts a _Builder_ into an ordinary _Cell_.
 */
@Serializable
@SerialName(TvmCellBuildEndcInst.MNEMONIC)
data class TvmCellBuildEndcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 518)

    companion object {
        const val MNEMONIC = "ENDC"
    }
}

/**
 * If `x!=0`, creates a _special_ or _exotic_ cell from _Builder_ `b`.
 * The type of the exotic cell must be stored in the first 8 bits of `b`.
 * If `x=0`, it is equivalent to `ENDC`. Otherwise some validity checks on the data and references of `
 * b` are performed before creating the exotic cell.
 */
@Serializable
@SerialName(TvmCellBuildEndxcInst.MNEMONIC)
data class TvmCellBuildEndxcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 526)

    companion object {
        const val MNEMONIC = "ENDXC"
    }
}

/**
 * Creates a new empty _Builder_.
 */
@Serializable
@SerialName(TvmCellBuildNewcInst.MNEMONIC)
data class TvmCellBuildNewcInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "NEWC"
    }
}

/**
 * Appends all data from _Builder_ `b'` to _Builder_ `b`.
 */
@Serializable
@SerialName(TvmCellBuildStbInst.MNEMONIC)
data class TvmCellBuildStbInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STB"
    }
}

/**
 * Quiet version of `STB`.
 */
@Serializable
@SerialName(TvmCellBuildStbqInst.MNEMONIC)
data class TvmCellBuildStbqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STBQ"
    }
}

/**
 * Concatenates two builders.
 * Equivalent to `SWAP` `STB`.
 */
@Serializable
@SerialName(TvmCellBuildStbrInst.MNEMONIC)
data class TvmCellBuildStbrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STBR"
    }
}

/**
 * Equivalent to `SWAP` `STBREFR`.
 */
@Serializable
@SerialName(TvmCellBuildStbrefInst.MNEMONIC)
data class TvmCellBuildStbrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 526)

    companion object {
        const val MNEMONIC = "STBREF"
    }
}

/**
 * Quiet version of `STBREF`.
 */
@Serializable
@SerialName(TvmCellBuildStbrefqInst.MNEMONIC)
data class TvmCellBuildStbrefqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 526)

    companion object {
        const val MNEMONIC = "STBREFQ"
    }
}

/**
 * A longer encoding of `STBREFR`.
 */
@Serializable
@SerialName(TvmCellBuildStbrefrAltInst.MNEMONIC)
data class TvmCellBuildStbrefrAltInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 526)

    companion object {
        const val MNEMONIC = "STBREFR_ALT"
    }
}

/**
 * Equivalent to `ENDC` `SWAP` `STREF`.
 */
@Serializable
@SerialName(TvmCellBuildStbrefrInst.MNEMONIC)
data class TvmCellBuildStbrefrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 518)

    companion object {
        const val MNEMONIC = "STBREFR"
    }
}

/**
 * Quiet version of `STBREFR`.
 */
@Serializable
@SerialName(TvmCellBuildStbrefrqInst.MNEMONIC)
data class TvmCellBuildStbrefrqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 526)

    companion object {
        const val MNEMONIC = "STBREFRQ"
    }
}

/**
 * Quiet version of `STBR`.
 */
@Serializable
@SerialName(TvmCellBuildStbrqInst.MNEMONIC)
data class TvmCellBuildStbrqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STBRQ"
    }
}

/**
 * A longer version of `[cc+1] STI`.
 */
@Serializable
@SerialName(TvmCellBuildStiAltInst.MNEMONIC)
data class TvmCellBuildStiAltInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "STI_ALT"
    }
}

/**
 * Stores a signed `cc+1`-bit integer `x` into _Builder_ `b` for `0 <= cc <= 255`, throws a range check
 * exception if `x` does not fit into `cc+1` bits.
 */
@Serializable
@SerialName(TvmCellBuildStiInst.MNEMONIC)
data class TvmCellBuildStiInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STI"
    }
}

/**
 * Stores a little-endian signed 32-bit integer.
 */
@Serializable
@SerialName(TvmCellBuildStile4Inst.MNEMONIC)
data class TvmCellBuildStile4Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STILE4"
    }
}

/**
 * Stores a little-endian signed 64-bit integer.
 */
@Serializable
@SerialName(TvmCellBuildStile8Inst.MNEMONIC)
data class TvmCellBuildStile8Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STILE8"
    }
}

/**
 * A quiet version of `STI`.
 */
@Serializable
@SerialName(TvmCellBuildStiqInst.MNEMONIC)
data class TvmCellBuildStiqInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "STIQ"
    }
}

/**
 * Equivalent to `SWAP` `[cc+1] STI`.
 */
@Serializable
@SerialName(TvmCellBuildStirInst.MNEMONIC)
data class TvmCellBuildStirInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "STIR"
    }
}

/**
 * A quiet version of `STIR`.
 */
@Serializable
@SerialName(TvmCellBuildStirqInst.MNEMONIC)
data class TvmCellBuildStirqInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "STIRQ"
    }
}

/**
 * Stores a signed `l`-bit integer `x` into `b` for `0 <= l <= 257`.
 */
@Serializable
@SerialName(TvmCellBuildStixInst.MNEMONIC)
data class TvmCellBuildStixInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STIX"
    }
}

/**
 * A quiet version of `STIX`. If there is no space in `b`, sets `b'=b` and `f=-1`.
 * If `x` does not fit into `l` bits, sets `b'=b` and `f=1`.
 * If the operation succeeds, `b'` is the new _Builder_ and `f=0`.
 * However, `0 <= l <= 257`, with a range check exception if this is not so.
 */
@Serializable
@SerialName(TvmCellBuildStixqInst.MNEMONIC)
data class TvmCellBuildStixqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STIXQ"
    }
}

/**
 * Similar to `STIX`, but with arguments in a different order.
 */
@Serializable
@SerialName(TvmCellBuildStixrInst.MNEMONIC)
data class TvmCellBuildStixrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STIXR"
    }
}

/**
 * A quiet version of `STIXR`.
 */
@Serializable
@SerialName(TvmCellBuildStixrqInst.MNEMONIC)
data class TvmCellBuildStixrqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STIXRQ"
    }
}

/**
 * Stores `n` binary ones into _Builder_ `b`.
 */
@Serializable
@SerialName(TvmCellBuildStonesInst.MNEMONIC)
data class TvmCellBuildStonesInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STONES"
    }
}

/**
 * Equivalent to `STREFCONST` `STREFCONST`.
 */
@Serializable
@SerialName(TvmCellBuildStref2constInst.MNEMONIC)
data class TvmCellBuildStref2constInst(
    override val location: TvmInstLocation,
    val c1: TvmCell, // ref
    val c2: TvmCell, // ref
): TvmInst, TvmCellBuildInst, TvmRefOperandLoader {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STREF2CONST"
    }
}

/**
 * A longer version of `STREF`.
 */
@Serializable
@SerialName(TvmCellBuildStrefAltInst.MNEMONIC)
data class TvmCellBuildStrefAltInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STREF_ALT"
    }
}

/**
 * Stores a reference to _Cell_ `c` into _Builder_ `b`.
 */
@Serializable
@SerialName(TvmCellBuildStrefInst.MNEMONIC)
data class TvmCellBuildStrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "STREF"
    }
}

/**
 * Equivalent to `PUSHREF` `STREFR`.
 */
@Serializable
@SerialName(TvmCellBuildStrefconstInst.MNEMONIC)
data class TvmCellBuildStrefconstInst(
    override val location: TvmInstLocation,
    val c: TvmCell, // ref
): TvmInst, TvmCellBuildInst, TvmRefOperandLoader {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STREFCONST"
    }
}

/**
 * Quiet version of `STREF`.
 */
@Serializable
@SerialName(TvmCellBuildStrefqInst.MNEMONIC)
data class TvmCellBuildStrefqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STREFQ"
    }
}

/**
 * Equivalent to `SWAP` `STREF`.
 */
@Serializable
@SerialName(TvmCellBuildStrefrInst.MNEMONIC)
data class TvmCellBuildStrefrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STREFR"
    }
}

/**
 * Quiet version of `STREFR`.
 */
@Serializable
@SerialName(TvmCellBuildStrefrqInst.MNEMONIC)
data class TvmCellBuildStrefrqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STREFRQ"
    }
}

/**
 * Stores `n` binary `x`es (`0 <= x <= 1`) into _Builder_ `b`.
 */
@Serializable
@SerialName(TvmCellBuildStsameInst.MNEMONIC)
data class TvmCellBuildStsameInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STSAME"
    }
}

/**
 * A longer version of `STSLICE`.
 */
@Serializable
@SerialName(TvmCellBuildStsliceAltInst.MNEMONIC)
data class TvmCellBuildStsliceAltInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STSLICE_ALT"
    }
}

/**
 * Stores _Slice_ `s` into _Builder_ `b`.
 */
@Serializable
@SerialName(TvmCellBuildStsliceInst.MNEMONIC)
data class TvmCellBuildStsliceInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "STSLICE"
    }
}

/**
 * Stores a constant subslice `sss`.
 * _Details:_ `sss` consists of `0 <= x <= 3` references and up to `8y+2` data bits, with `0 <= y <= 7`
 * . Completion bit is assumed.
 * Note that the assembler can replace `STSLICECONST` with `PUSHSLICE` `STSLICER` if the slice is too b
 * ig.
 */
@Serializable
@SerialName(TvmCellBuildStsliceconstInst.MNEMONIC)
data class TvmCellBuildStsliceconstInst(
    override val location: TvmInstLocation,
    val s: TvmSubSliceSerializedLoader, // subslice
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 24)

    companion object {
        const val MNEMONIC = "STSLICECONST"
    }
}

/**
 * Quiet version of `STSLICE`.
 */
@Serializable
@SerialName(TvmCellBuildStsliceqInst.MNEMONIC)
data class TvmCellBuildStsliceqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STSLICEQ"
    }
}

/**
 * Equivalent to `SWAP` `STSLICE`.
 */
@Serializable
@SerialName(TvmCellBuildStslicerInst.MNEMONIC)
data class TvmCellBuildStslicerInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STSLICER"
    }
}

/**
 * Quiet version of `STSLICER`.
 */
@Serializable
@SerialName(TvmCellBuildStslicerqInst.MNEMONIC)
data class TvmCellBuildStslicerqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STSLICERQ"
    }
}

/**
 * A longer version of `[cc+1] STU`.
 */
@Serializable
@SerialName(TvmCellBuildStuAltInst.MNEMONIC)
data class TvmCellBuildStuAltInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "STU_ALT"
    }
}

/**
 * Stores an unsigned `cc+1`-bit integer `x` into _Builder_ `b`. In all other respects it is similar to
 * `STI`.
 */
@Serializable
@SerialName(TvmCellBuildStuInst.MNEMONIC)
data class TvmCellBuildStuInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STU"
    }
}

/**
 * Stores a little-endian unsigned 32-bit integer.
 */
@Serializable
@SerialName(TvmCellBuildStule4Inst.MNEMONIC)
data class TvmCellBuildStule4Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STULE4"
    }
}

/**
 * Stores a little-endian unsigned 64-bit integer.
 */
@Serializable
@SerialName(TvmCellBuildStule8Inst.MNEMONIC)
data class TvmCellBuildStule8Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STULE8"
    }
}

/**
 * A quiet version of `STU`.
 */
@Serializable
@SerialName(TvmCellBuildStuqInst.MNEMONIC)
data class TvmCellBuildStuqInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "STUQ"
    }
}

/**
 * Equivalent to `SWAP` `[cc+1] STU`.
 */
@Serializable
@SerialName(TvmCellBuildSturInst.MNEMONIC)
data class TvmCellBuildSturInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "STUR"
    }
}

/**
 * A quiet version of `STUR`.
 */
@Serializable
@SerialName(TvmCellBuildSturqInst.MNEMONIC)
data class TvmCellBuildSturqInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "STURQ"
    }
}

/**
 * Stores an unsigned `l`-bit integer `x` into `b` for `0 <= l <= 256`.
 */
@Serializable
@SerialName(TvmCellBuildStuxInst.MNEMONIC)
data class TvmCellBuildStuxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STUX"
    }
}

/**
 * A quiet version of `STUX`.
 */
@Serializable
@SerialName(TvmCellBuildStuxqInst.MNEMONIC)
data class TvmCellBuildStuxqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STUXQ"
    }
}

/**
 * Similar to `STUX`, but with arguments in a different order.
 */
@Serializable
@SerialName(TvmCellBuildStuxrInst.MNEMONIC)
data class TvmCellBuildStuxrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STUXR"
    }
}

/**
 * A quiet version of `STUXR`.
 */
@Serializable
@SerialName(TvmCellBuildStuxrqInst.MNEMONIC)
data class TvmCellBuildStuxrqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STUXRQ"
    }
}

/**
 * Stores `n` binary zeroes into _Builder_ `b`.
 */
@Serializable
@SerialName(TvmCellBuildStzeroesInst.MNEMONIC)
data class TvmCellBuildStzeroesInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellBuildInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STZEROES"
    }
}

/**
 * Returns the depth of _Cell_ `c`. If `c` has no references, then `x=0`; otherwise `x` is one plus the
 * maximum of depths of cells referred to from `c`. If `c` is a _Null_ instead of a _Cell_, returns ze
 * ro.
 */
@Serializable
@SerialName(TvmCellParseCdepthInst.MNEMONIC)
data class TvmCellParseCdepthInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "CDEPTH"
    }
}

/**
 * Returns `i`th depth of the cell.
 */
@Serializable
@SerialName(TvmCellParseCdepthiInst.MNEMONIC)
data class TvmCellParseCdepthiInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "CDEPTHI"
    }
}

/**
 * Returns `i`th depth of the cell.
 */
@Serializable
@SerialName(TvmCellParseCdepthixInst.MNEMONIC)
data class TvmCellParseCdepthixInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "CDEPTHIX"
    }
}

/**
 * Returns `i`th hash of the cell.
 */
@Serializable
@SerialName(TvmCellParseChashiInst.MNEMONIC)
data class TvmCellParseChashiInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "CHASHI"
    }
}

/**
 * Returns `i`th hash of the cell.
 */
@Serializable
@SerialName(TvmCellParseChashixInst.MNEMONIC)
data class TvmCellParseChashixInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "CHASHIX"
    }
}

/**
 * Returns level of the cell.
 */
@Serializable
@SerialName(TvmCellParseClevelInst.MNEMONIC)
data class TvmCellParseClevelInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "CLEVEL"
    }
}

/**
 * Returns level mask of the cell.
 */
@Serializable
@SerialName(TvmCellParseClevelmaskInst.MNEMONIC)
data class TvmCellParseClevelmaskInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "CLEVELMASK"
    }
}

/**
 * Converts a _Cell_ into a _Slice_. Notice that `c` must be either an ordinary cell, or an exotic cell
 * which is automatically _loaded_ to yield an ordinary cell `c'`, converted into a _Slice_ afterwards
 * .
 */
@Serializable
@SerialName(TvmCellParseCtosInst.MNEMONIC)
data class TvmCellParseCtosInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "118/43")

    companion object {
        const val MNEMONIC = "CTOS"
    }
}

/**
 * Removes a _Slice_ `s` from the stack, and throws an exception if it is not empty.
 */
@Serializable
@SerialName(TvmCellParseEndsInst.MNEMONIC)
data class TvmCellParseEndsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "18/68")

    companion object {
        const val MNEMONIC = "ENDS"
    }
}

/**
 * A longer encoding for `LDI`.
 */
@Serializable
@SerialName(TvmCellParseLdiAltInst.MNEMONIC)
data class TvmCellParseLdiAltInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LDI_ALT"
    }
}

/**
 * Loads (i.e., parses) a signed `cc+1`-bit integer `x` from _Slice_ `s`, and returns the remainder of
 * `s` as `s'`.
 */
@Serializable
@SerialName(TvmCellParseLdiInst.MNEMONIC)
data class TvmCellParseLdiInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDI"
    }
}

/**
 * Loads a little-endian signed 32-bit integer.
 */
@Serializable
@SerialName(TvmCellParseLdile4Inst.MNEMONIC)
data class TvmCellParseLdile4Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDILE4"
    }
}

/**
 * Quietly loads a little-endian signed 32-bit integer.
 */
@Serializable
@SerialName(TvmCellParseLdile4qInst.MNEMONIC)
data class TvmCellParseLdile4qInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDILE4Q"
    }
}

/**
 * Loads a little-endian signed 64-bit integer.
 */
@Serializable
@SerialName(TvmCellParseLdile8Inst.MNEMONIC)
data class TvmCellParseLdile8Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDILE8"
    }
}

/**
 * Quietly loads a little-endian signed 64-bit integer.
 */
@Serializable
@SerialName(TvmCellParseLdile8qInst.MNEMONIC)
data class TvmCellParseLdile8qInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDILE8Q"
    }
}

/**
 * A quiet version of `LDI`.
 */
@Serializable
@SerialName(TvmCellParseLdiqInst.MNEMONIC)
data class TvmCellParseLdiqInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LDIQ"
    }
}

/**
 * Loads a signed `l`-bit (`0 <= l <= 257`) integer `x` from _Slice_ `s`, and returns the remainder of
 * `s` as `s'`.
 */
@Serializable
@SerialName(TvmCellParseLdixInst.MNEMONIC)
data class TvmCellParseLdixInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDIX"
    }
}

/**
 * Quiet version of `LDIX`: loads a signed `l`-bit integer from `s` similarly to `LDIX`, but returns a
 * success flag, equal to `-1` on success or to `0` on failure (if `s` does not have `l` bits), instead
 * of throwing a cell underflow exception.
 */
@Serializable
@SerialName(TvmCellParseLdixqInst.MNEMONIC)
data class TvmCellParseLdixqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDIXQ"
    }
}

/**
 * Returns the count `n` of leading one bits in `s`, and removes these bits from `s`.
 */
@Serializable
@SerialName(TvmCellParseLdonesInst.MNEMONIC)
data class TvmCellParseLdonesInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDONES"
    }
}

/**
 * Loads a cell reference `c` from `s`.
 */
@Serializable
@SerialName(TvmCellParseLdrefInst.MNEMONIC)
data class TvmCellParseLdrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "LDREF"
    }
}

/**
 * Equivalent to `LDREF` `SWAP` `CTOS`.
 */
@Serializable
@SerialName(TvmCellParseLdrefrtosInst.MNEMONIC)
data class TvmCellParseLdrefrtosInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "118/43")

    companion object {
        const val MNEMONIC = "LDREFRTOS"
    }
}

/**
 * Returns the count `n` of leading bits equal to `0 <= x <= 1` in `s`, and removes these bits from `s`
 * .
 */
@Serializable
@SerialName(TvmCellParseLdsameInst.MNEMONIC)
data class TvmCellParseLdsameInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDSAME"
    }
}

/**
 * A longer encoding for `LDSLICE`.
 */
@Serializable
@SerialName(TvmCellParseLdsliceAltInst.MNEMONIC)
data class TvmCellParseLdsliceAltInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LDSLICE_ALT"
    }
}

/**
 * Cuts the next `cc+1` bits of `s` into a separate _Slice_ `s''`.
 */
@Serializable
@SerialName(TvmCellParseLdsliceInst.MNEMONIC)
data class TvmCellParseLdsliceInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDSLICE"
    }
}

/**
 * A quiet version of `LDSLICE`.
 */
@Serializable
@SerialName(TvmCellParseLdsliceqInst.MNEMONIC)
data class TvmCellParseLdsliceqInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LDSLICEQ"
    }
}

/**
 * Loads the first `0 <= l <= 1023` bits from _Slice_ `s` into a separate _Slice_ `s''`, returning the
 * remainder of `s` as `s'`.
 */
@Serializable
@SerialName(TvmCellParseLdslicexInst.MNEMONIC)
data class TvmCellParseLdslicexInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDSLICEX"
    }
}

/**
 * A quiet version of `LDSLICEX`.
 */
@Serializable
@SerialName(TvmCellParseLdslicexqInst.MNEMONIC)
data class TvmCellParseLdslicexqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDSLICEXQ"
    }
}

/**
 * A longer encoding for `LDU`.
 */
@Serializable
@SerialName(TvmCellParseLduAltInst.MNEMONIC)
data class TvmCellParseLduAltInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LDU_ALT"
    }
}

/**
 * Loads an unsigned `cc+1`-bit integer `x` from _Slice_ `s`.
 */
@Serializable
@SerialName(TvmCellParseLduInst.MNEMONIC)
data class TvmCellParseLduInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDU"
    }
}

/**
 * Loads a little-endian unsigned 32-bit integer.
 */
@Serializable
@SerialName(TvmCellParseLdule4Inst.MNEMONIC)
data class TvmCellParseLdule4Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDULE4"
    }
}

/**
 * Quietly loads a little-endian unsigned 32-bit integer.
 */
@Serializable
@SerialName(TvmCellParseLdule4qInst.MNEMONIC)
data class TvmCellParseLdule4qInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDULE4Q"
    }
}

/**
 * Loads a little-endian unsigned 64-bit integer.
 */
@Serializable
@SerialName(TvmCellParseLdule8Inst.MNEMONIC)
data class TvmCellParseLdule8Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDULE8"
    }
}

/**
 * Quietly loads a little-endian unsigned 64-bit integer.
 */
@Serializable
@SerialName(TvmCellParseLdule8qInst.MNEMONIC)
data class TvmCellParseLdule8qInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDULE8Q"
    }
}

/**
 * A quiet version of `LDU`.
 */
@Serializable
@SerialName(TvmCellParseLduqInst.MNEMONIC)
data class TvmCellParseLduqInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "LDUQ"
    }
}

/**
 * Loads an unsigned `l`-bit integer `x` from (the first `l` bits of) `s`, with `0 <= l <= 256`.
 */
@Serializable
@SerialName(TvmCellParseLduxInst.MNEMONIC)
data class TvmCellParseLduxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDUX"
    }
}

/**
 * Quiet version of `LDUX`.
 */
@Serializable
@SerialName(TvmCellParseLduxqInst.MNEMONIC)
data class TvmCellParseLduxqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDUXQ"
    }
}

/**
 * Returns the count `n` of leading zero bits in `s`, and removes these bits from `s`.
 */
@Serializable
@SerialName(TvmCellParseLdzeroesInst.MNEMONIC)
data class TvmCellParseLdzeroesInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDZEROES"
    }
}

/**
 * Preloads a signed `cc+1`-bit integer from _Slice_ `s`.
 */
@Serializable
@SerialName(TvmCellParsePldiInst.MNEMONIC)
data class TvmCellParsePldiInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "PLDI"
    }
}

/**
 * Preloads a little-endian signed 32-bit integer.
 */
@Serializable
@SerialName(TvmCellParsePldile4Inst.MNEMONIC)
data class TvmCellParsePldile4Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDILE4"
    }
}

/**
 * Quietly preloads a little-endian signed 32-bit integer.
 */
@Serializable
@SerialName(TvmCellParsePldile4qInst.MNEMONIC)
data class TvmCellParsePldile4qInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDILE4Q"
    }
}

/**
 * Preloads a little-endian signed 64-bit integer.
 */
@Serializable
@SerialName(TvmCellParsePldile8Inst.MNEMONIC)
data class TvmCellParsePldile8Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDILE8"
    }
}

/**
 * Quietly preloads a little-endian signed 64-bit integer.
 */
@Serializable
@SerialName(TvmCellParsePldile8qInst.MNEMONIC)
data class TvmCellParsePldile8qInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDILE8Q"
    }
}

/**
 * A quiet version of `PLDI`.
 */
@Serializable
@SerialName(TvmCellParsePldiqInst.MNEMONIC)
data class TvmCellParsePldiqInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "PLDIQ"
    }
}

/**
 * Preloads a signed `l`-bit integer from _Slice_ `s`, for `0 <= l <= 257`.
 */
@Serializable
@SerialName(TvmCellParsePldixInst.MNEMONIC)
data class TvmCellParsePldixInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDIX"
    }
}

/**
 * Quiet version of `PLDIX`.
 */
@Serializable
@SerialName(TvmCellParsePldixqInst.MNEMONIC)
data class TvmCellParsePldixqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDIXQ"
    }
}

/**
 * Returns the `n`-th cell reference of _Slice_ `s`, where `0 <= n <= 3`.
 */
@Serializable
@SerialName(TvmCellParsePldrefidxInst.MNEMONIC)
data class TvmCellParsePldrefidxInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDREFIDX"
    }
}

/**
 * Returns the `n`-th cell reference of _Slice_ `s` for `0 <= n <= 3`.
 */
@Serializable
@SerialName(TvmCellParsePldrefvarInst.MNEMONIC)
data class TvmCellParsePldrefvarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDREFVAR"
    }
}

/**
 * Returns the first `0 < cc+1 <= 256` bits of `s` as `s''`.
 */
@Serializable
@SerialName(TvmCellParsePldsliceInst.MNEMONIC)
data class TvmCellParsePldsliceInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "PLDSLICE"
    }
}

/**
 * A quiet version of `PLDSLICE`.
 */
@Serializable
@SerialName(TvmCellParsePldsliceqInst.MNEMONIC)
data class TvmCellParsePldsliceqInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "PLDSLICEQ"
    }
}

/**
 * Returns the first `0 <= l <= 1023` bits of `s` as `s''`.
 */
@Serializable
@SerialName(TvmCellParsePldslicexInst.MNEMONIC)
data class TvmCellParsePldslicexInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDSLICEX"
    }
}

/**
 * A quiet version of `LDSLICEXQ`.
 */
@Serializable
@SerialName(TvmCellParsePldslicexqInst.MNEMONIC)
data class TvmCellParsePldslicexqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDSLICEXQ"
    }
}

/**
 * Preloads an unsigned `cc+1`-bit integer from `s`.
 */
@Serializable
@SerialName(TvmCellParsePlduInst.MNEMONIC)
data class TvmCellParsePlduInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "PLDU"
    }
}

/**
 * Preloads a little-endian unsigned 32-bit integer.
 */
@Serializable
@SerialName(TvmCellParsePldule4Inst.MNEMONIC)
data class TvmCellParsePldule4Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDULE4"
    }
}

/**
 * Quietly preloads a little-endian unsigned 32-bit integer.
 */
@Serializable
@SerialName(TvmCellParsePldule4qInst.MNEMONIC)
data class TvmCellParsePldule4qInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDULE4Q"
    }
}

/**
 * Preloads a little-endian unsigned 64-bit integer.
 */
@Serializable
@SerialName(TvmCellParsePldule8Inst.MNEMONIC)
data class TvmCellParsePldule8Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDULE8"
    }
}

/**
 * Quietly preloads a little-endian unsigned 64-bit integer.
 */
@Serializable
@SerialName(TvmCellParsePldule8qInst.MNEMONIC)
data class TvmCellParsePldule8qInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDULE8Q"
    }
}

/**
 * A quiet version of `PLDU`.
 */
@Serializable
@SerialName(TvmCellParsePlduqInst.MNEMONIC)
data class TvmCellParsePlduqInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "PLDUQ"
    }
}

/**
 * Preloads an unsigned `l`-bit integer from `s`, for `0 <= l <= 256`.
 */
@Serializable
@SerialName(TvmCellParsePlduxInst.MNEMONIC)
data class TvmCellParsePlduxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDUX"
    }
}

/**
 * Quiet version of `PLDUX`.
 */
@Serializable
@SerialName(TvmCellParsePlduxqInst.MNEMONIC)
data class TvmCellParsePlduxqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDUXQ"
    }
}

/**
 * Preloads the first `32(c+1)` bits of _Slice_ `s` into an unsigned integer `x`, for `0 <= c <= 7`. If
 * `s` is shorter than necessary, missing bits are assumed to be zero. This operation is intended to b
 * e used along with `IFBITJMP` and similar instructions.
 */
@Serializable
@SerialName(TvmCellParsePlduzInst.MNEMONIC)
data class TvmCellParsePlduzInst(
    override val location: TvmInstLocation,
    val c: Int, // uint
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDUZ"
    }
}

/**
 * Returns both the number of data bits and the number of references in `s`.
 */
@Serializable
@SerialName(TvmCellParseSbitrefsInst.MNEMONIC)
data class TvmCellParseSbitrefsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SBITREFS"
    }
}

/**
 * Returns the number of data bits in _Slice_ `s`.
 */
@Serializable
@SerialName(TvmCellParseSbitsInst.MNEMONIC)
data class TvmCellParseSbitsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SBITS"
    }
}

/**
 * Checks whether there are at least `l` data bits and `r` references in _Slice_ `s`.
 */
@Serializable
@SerialName(TvmCellParseSchkbitrefsInst.MNEMONIC)
data class TvmCellParseSchkbitrefsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/76")

    companion object {
        const val MNEMONIC = "SCHKBITREFS"
    }
}

/**
 * Checks whether there are at least `l` data bits and `r` references in _Slice_ `s`.
 */
@Serializable
@SerialName(TvmCellParseSchkbitrefsqInst.MNEMONIC)
data class TvmCellParseSchkbitrefsqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SCHKBITREFSQ"
    }
}

/**
 * Checks whether there are at least `l` data bits in _Slice_ `s`. If this is not the case, throws a ce
 * ll deserialisation (i.e., cell underflow) exception.
 */
@Serializable
@SerialName(TvmCellParseSchkbitsInst.MNEMONIC)
data class TvmCellParseSchkbitsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/76")

    companion object {
        const val MNEMONIC = "SCHKBITS"
    }
}

/**
 * Checks whether there are at least `l` data bits in _Slice_ `s`.
 */
@Serializable
@SerialName(TvmCellParseSchkbitsqInst.MNEMONIC)
data class TvmCellParseSchkbitsqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SCHKBITSQ"
    }
}

/**
 * Checks whether there are at least `r` references in _Slice_ `s`.
 */
@Serializable
@SerialName(TvmCellParseSchkrefsInst.MNEMONIC)
data class TvmCellParseSchkrefsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/76")

    companion object {
        const val MNEMONIC = "SCHKREFS"
    }
}

/**
 * Checks whether there are at least `r` references in _Slice_ `s`.
 */
@Serializable
@SerialName(TvmCellParseSchkrefsqInst.MNEMONIC)
data class TvmCellParseSchkrefsqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SCHKREFSQ"
    }
}

/**
 * Returns the first `0 <= l <= 1023` bits and first `0 <= r <= 4` references of `s`.
 */
@Serializable
@SerialName(TvmCellParseScutfirstInst.MNEMONIC)
data class TvmCellParseScutfirstInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SCUTFIRST"
    }
}

/**
 * Returns the last `0 <= l <= 1023` data bits and last `0 <= r <= 4` references of `s`.
 */
@Serializable
@SerialName(TvmCellParseScutlastInst.MNEMONIC)
data class TvmCellParseScutlastInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SCUTLAST"
    }
}

/**
 * Checks whether `s` begins with constant bitstring `sss` of length `8x+3` (with continuation bit assu
 * med), where `0 <= x <= 127`, and removes `sss` from `s` on success.
 */
@Serializable
@SerialName(TvmCellParseSdbeginsInst.MNEMONIC)
data class TvmCellParseSdbeginsInst(
    override val location: TvmInstLocation,
    val s: TvmSubSliceSerializedLoader, // subslice
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 31)

    companion object {
        const val MNEMONIC = "SDBEGINS"
    }
}

/**
 * A quiet version of `SDBEGINS`.
 */
@Serializable
@SerialName(TvmCellParseSdbeginsqInst.MNEMONIC)
data class TvmCellParseSdbeginsqInst(
    override val location: TvmInstLocation,
    val s: TvmSubSliceSerializedLoader, // subslice
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 31)

    companion object {
        const val MNEMONIC = "SDBEGINSQ"
    }
}

/**
 * Checks whether `s` begins with (the data bits of) `s'`, and removes `s'` from `s` on success. On fai
 * lure throws a cell deserialization exception. Primitive `SDPFXREV` can be considered a quiet version
 * of `SDBEGINSX`.
 */
@Serializable
@SerialName(TvmCellParseSdbeginsxInst.MNEMONIC)
data class TvmCellParseSdbeginsxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDBEGINSX"
    }
}

/**
 * A quiet version of `SDBEGINSX`.
 */
@Serializable
@SerialName(TvmCellParseSdbeginsxqInst.MNEMONIC)
data class TvmCellParseSdbeginsxqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDBEGINSXQ"
    }
}

/**
 * Returns the first `0 <= l <= 1023` bits of `s`. It is equivalent to `PLDSLICEX`.
 */
@Serializable
@SerialName(TvmCellParseSdcutfirstInst.MNEMONIC)
data class TvmCellParseSdcutfirstInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDCUTFIRST"
    }
}

/**
 * Returns the last `0 <= l <= 1023` bits of `s`.
 */
@Serializable
@SerialName(TvmCellParseSdcutlastInst.MNEMONIC)
data class TvmCellParseSdcutlastInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDCUTLAST"
    }
}

/**
 * Returns the depth of _Slice_ `s`. If `s` has no references, then `x=0`; otherwise `x` is one plus th
 * e maximum of depths of cells referred to from `s`.
 */
@Serializable
@SerialName(TvmCellParseSdepthInst.MNEMONIC)
data class TvmCellParseSdepthInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDEPTH"
    }
}

/**
 * Returns all but the first `0 <= l <= 1023` bits of `s`. It is equivalent to `LDSLICEX` `NIP`.
 */
@Serializable
@SerialName(TvmCellParseSdskipfirstInst.MNEMONIC)
data class TvmCellParseSdskipfirstInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDSKIPFIRST"
    }
}

/**
 * Returns all but the last `0 <= l <= 1023` bits of `s`.
 */
@Serializable
@SerialName(TvmCellParseSdskiplastInst.MNEMONIC)
data class TvmCellParseSdskiplastInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDSKIPLAST"
    }
}

/**
 * Returns `0 <= l' <= 1023` bits of `s` starting from offset `0 <= l <= 1023`, thus extracting a bit s
 * ubstring out of the data of `s`.
 */
@Serializable
@SerialName(TvmCellParseSdsubstrInst.MNEMONIC)
data class TvmCellParseSdsubstrInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDSUBSTR"
    }
}

/**
 * Splits the first `0 <= l <= 1023` data bits and first `0 <= r <= 4` references from `s` into `s'`, r
 * eturning the remainder of `s` as `s''`.
 */
@Serializable
@SerialName(TvmCellParseSplitInst.MNEMONIC)
data class TvmCellParseSplitInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SPLIT"
    }
}

/**
 * A quiet version of `SPLIT`.
 */
@Serializable
@SerialName(TvmCellParseSplitqInst.MNEMONIC)
data class TvmCellParseSplitqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SPLITQ"
    }
}

/**
 * Returns the number of references in _Slice_ `s`.
 */
@Serializable
@SerialName(TvmCellParseSrefsInst.MNEMONIC)
data class TvmCellParseSrefsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SREFS"
    }
}

/**
 * Returns all but the first `l` bits of `s` and `r` references of `s`.
 */
@Serializable
@SerialName(TvmCellParseSskipfirstInst.MNEMONIC)
data class TvmCellParseSskipfirstInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SSKIPFIRST"
    }
}

/**
 * Returns all but the last `l` bits of `s` and `r` references of `s`.
 */
@Serializable
@SerialName(TvmCellParseSskiplastInst.MNEMONIC)
data class TvmCellParseSskiplastInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SSKIPLAST"
    }
}

/**
 * Returns `0 <= l' <= 1023` bits and `0 <= r' <= 4` references from _Slice_ `s`, after skipping the fi
 * rst `0 <= l <= 1023` bits and first `0 <= r <= 4` references.
 */
@Serializable
@SerialName(TvmCellParseSubsliceInst.MNEMONIC)
data class TvmCellParseSubsliceInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SUBSLICE"
    }
}

/**
 * Transforms an ordinary or exotic cell into a _Slice_, as if it were an ordinary cell. A flag is retu
 * rned indicating whether `c` is exotic. If that be the case, its type can later be deserialized from
 * the first eight bits of `s`.
 */
@Serializable
@SerialName(TvmCellParseXctosInst.MNEMONIC)
data class TvmCellParseXctosInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "XCTOS"
    }
}

/**
 * Loads an exotic cell `c` and returns an ordinary cell `c'`. If `c` is already ordinary, does nothing
 * . If `c` cannot be loaded, throws an exception.
 */
@Serializable
@SerialName(TvmCellParseXloadInst.MNEMONIC)
data class TvmCellParseXloadInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "XLOAD"
    }
}

/**
 * Loads an exotic cell `c` and returns an ordinary cell `c'`. If `c` is already ordinary, does nothing
 * . If `c` cannot be loaded, returns 0.
 */
@Serializable
@SerialName(TvmCellParseXloadqInst.MNEMONIC)
data class TvmCellParseXloadqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCellParseInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "XLOADQ"
    }
}

/**
 * Selects TVM codepage `0 <= nn < 240`. If the codepage is not supported, throws an invalid opcode exc
 * eption.
 */
@Serializable
@SerialName(TvmCodepageSetcpInst.MNEMONIC)
data class TvmCodepageSetcpInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmCodepageInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SETCP"
    }
}

/**
 * Selects TVM codepage `z-16` for `1 <= z <= 15`. Negative codepages `-13...-1` are reserved for restr
 * icted versions of TVM needed to validate runs of TVM in other codepages. Negative codepage `-14` is
 * reserved for experimental codepages, not necessarily compatible between different TVM implementation
 * s, and should be disabled in the production versions of TVM.
 */
@Serializable
@SerialName(TvmCodepageSetcpSpecialInst.MNEMONIC)
data class TvmCodepageSetcpSpecialInst(
    override val location: TvmInstLocation,
    val z: Int, // uint
): TvmInst, TvmCodepageInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SETCP_SPECIAL"
    }
}

/**
 * Selects codepage `c` with `-2^15 <= c < 2^15` passed in the top of the stack.
 */
@Serializable
@SerialName(TvmCodepageSetcpxInst.MNEMONIC)
data class TvmCodepageSetcpxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCodepageInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SETCPX"
    }
}

/**
 * Throws an arithmetic overflow exception if `x` is a `NaN`.
 */
@Serializable
@SerialName(TvmCompareIntChknanInst.MNEMONIC)
data class TvmCompareIntChknanInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "18/68")

    companion object {
        const val MNEMONIC = "CHKNAN"
    }
}

/**
 * Computes the sign of `x-y`:
 * `-1` if `x<y`, `0` if `x=y`, `1` if `x>y`.
 * No integer overflow can occur here unless `x` or `y` is a `NaN`.
 */
@Serializable
@SerialName(TvmCompareIntCmpInst.MNEMONIC)
data class TvmCompareIntCmpInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "CMP"
    }
}

/**
 * Returns `-1` if `x=yy`, `0` otherwise.
 * `-2^7 <= yy < 2^7`.
 */
@Serializable
@SerialName(TvmCompareIntEqintInst.MNEMONIC)
data class TvmCompareIntEqintInst(
    override val location: TvmInstLocation,
    val y: Int, // int
): TvmInst, TvmCompareIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "EQINT"
    }
}

/**
 * Returns `-1` if `x=y`, `0` otherwise.
 */
@Serializable
@SerialName(TvmCompareIntEqualInst.MNEMONIC)
data class TvmCompareIntEqualInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "EQUAL"
    }
}

/**
 * Equivalent to `LESS` `NOT`.
 */
@Serializable
@SerialName(TvmCompareIntGeqInst.MNEMONIC)
data class TvmCompareIntGeqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "GEQ"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmCompareIntGreaterInst.MNEMONIC)
data class TvmCompareIntGreaterInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "GREATER"
    }
}

/**
 * Returns `-1` if `x>yy`, `0` otherwise.
 * `-2^7 <= yy < 2^7`.
 */
@Serializable
@SerialName(TvmCompareIntGtintInst.MNEMONIC)
data class TvmCompareIntGtintInst(
    override val location: TvmInstLocation,
    val y: Int, // int
): TvmInst, TvmCompareIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "GTINT"
    }
}

/**
 * Checks whether `x` is a `NaN`.
 */
@Serializable
@SerialName(TvmCompareIntIsnanInst.MNEMONIC)
data class TvmCompareIntIsnanInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "ISNAN"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmCompareIntLeqInst.MNEMONIC)
data class TvmCompareIntLeqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "LEQ"
    }
}

/**
 * Returns `-1` if `x<y`, `0` otherwise.
 */
@Serializable
@SerialName(TvmCompareIntLessInst.MNEMONIC)
data class TvmCompareIntLessInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "LESS"
    }
}

/**
 * Returns `-1` if `x<yy`, `0` otherwise.
 * `-2^7 <= yy < 2^7`.
 */
@Serializable
@SerialName(TvmCompareIntLessintInst.MNEMONIC)
data class TvmCompareIntLessintInst(
    override val location: TvmInstLocation,
    val y: Int, // int
): TvmInst, TvmCompareIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LESSINT"
    }
}

/**
 * Equivalent to `EQUAL` `NOT`.
 */
@Serializable
@SerialName(TvmCompareIntNeqInst.MNEMONIC)
data class TvmCompareIntNeqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "NEQ"
    }
}

/**
 * Returns `-1` if `x!=yy`, `0` otherwise.
 * `-2^7 <= yy < 2^7`.
 */
@Serializable
@SerialName(TvmCompareIntNeqintInst.MNEMONIC)
data class TvmCompareIntNeqintInst(
    override val location: TvmInstLocation,
    val y: Int, // int
): TvmInst, TvmCompareIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "NEQINT"
    }
}

/**
 * Computes the sign of an integer `x`:
 * `-1` if `x<0`, `0` if `x=0`, `1` if `x>0`.
 */
@Serializable
@SerialName(TvmCompareIntSgnInst.MNEMONIC)
data class TvmCompareIntSgnInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "SGN"
    }
}

/**
 * Returns the number of leading zeroes in `s`.
 */
@Serializable
@SerialName(TvmCompareOtherSdcntlead0Inst.MNEMONIC)
data class TvmCompareOtherSdcntlead0Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDCNTLEAD0"
    }
}

/**
 * Returns the number of leading ones in `s`.
 */
@Serializable
@SerialName(TvmCompareOtherSdcntlead1Inst.MNEMONIC)
data class TvmCompareOtherSdcntlead1Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDCNTLEAD1"
    }
}

/**
 * Returns the number of trailing zeroes in `s`.
 */
@Serializable
@SerialName(TvmCompareOtherSdcnttrail0Inst.MNEMONIC)
data class TvmCompareOtherSdcnttrail0Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDCNTTRAIL0"
    }
}

/**
 * Returns the number of trailing ones in `s`.
 */
@Serializable
@SerialName(TvmCompareOtherSdcnttrail1Inst.MNEMONIC)
data class TvmCompareOtherSdcnttrail1Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDCNTTRAIL1"
    }
}

/**
 * Checks whether _Slice_ `s` has no bits of data.
 */
@Serializable
@SerialName(TvmCompareOtherSdemptyInst.MNEMONIC)
data class TvmCompareOtherSdemptyInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDEMPTY"
    }
}

/**
 * Checks whether the data parts of `s` and `s'` coincide, equivalent to `SDLEXCMP` `ISZERO`.
 */
@Serializable
@SerialName(TvmCompareOtherSdeqInst.MNEMONIC)
data class TvmCompareOtherSdeqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDEQ"
    }
}

/**
 * Checks whether the first bit of _Slice_ `s` is a one.
 */
@Serializable
@SerialName(TvmCompareOtherSdfirstInst.MNEMONIC)
data class TvmCompareOtherSdfirstInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDFIRST"
    }
}

/**
 * Compares the data of `s` lexicographically with the data of `s'`, returning `-1`, 0, or 1 depending
 * on the result.
 */
@Serializable
@SerialName(TvmCompareOtherSdlexcmpInst.MNEMONIC)
data class TvmCompareOtherSdlexcmpInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDLEXCMP"
    }
}

/**
 * Checks whether `s` is a prefix of `s'`.
 */
@Serializable
@SerialName(TvmCompareOtherSdpfxInst.MNEMONIC)
data class TvmCompareOtherSdpfxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDPFX"
    }
}

/**
 * Checks whether `s'` is a prefix of `s`, equivalent to `SWAP` `SDPFX`.
 */
@Serializable
@SerialName(TvmCompareOtherSdpfxrevInst.MNEMONIC)
data class TvmCompareOtherSdpfxrevInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDPFXREV"
    }
}

/**
 * Checks whether `s` is a proper prefix of `s'` (i.e., a prefix distinct from `s'`).
 */
@Serializable
@SerialName(TvmCompareOtherSdppfxInst.MNEMONIC)
data class TvmCompareOtherSdppfxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDPPFX"
    }
}

/**
 * Checks whether `s'` is a proper prefix of `s`.
 */
@Serializable
@SerialName(TvmCompareOtherSdppfxrevInst.MNEMONIC)
data class TvmCompareOtherSdppfxrevInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDPPFXREV"
    }
}

/**
 * Checks whether `s` is a proper suffix of `s'`.
 */
@Serializable
@SerialName(TvmCompareOtherSdpsfxInst.MNEMONIC)
data class TvmCompareOtherSdpsfxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDPSFX"
    }
}

/**
 * Checks whether `s'` is a proper suffix of `s`.
 */
@Serializable
@SerialName(TvmCompareOtherSdpsfxrevInst.MNEMONIC)
data class TvmCompareOtherSdpsfxrevInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDPSFXREV"
    }
}

/**
 * Checks whether `s` is a suffix of `s'`.
 */
@Serializable
@SerialName(TvmCompareOtherSdsfxInst.MNEMONIC)
data class TvmCompareOtherSdsfxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDSFX"
    }
}

/**
 * Checks whether `s'` is a suffix of `s`.
 */
@Serializable
@SerialName(TvmCompareOtherSdsfxrevInst.MNEMONIC)
data class TvmCompareOtherSdsfxrevInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SDSFXREV"
    }
}

/**
 * Checks whether a _Slice_ `s` is empty (i.e., contains no bits of data and no cell references).
 */
@Serializable
@SerialName(TvmCompareOtherSemptyInst.MNEMONIC)
data class TvmCompareOtherSemptyInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SEMPTY"
    }
}

/**
 * Checks whether _Slice_ `s` has no references.
 */
@Serializable
@SerialName(TvmCompareOtherSremptyInst.MNEMONIC)
data class TvmCompareOtherSremptyInst(
    override val location: TvmInstLocation,
): TvmInst, TvmCompareOtherInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SREMPTY"
    }
}

/**
 * Pushes a continuation made from `builder`.
 * _Details:_ Pushes the simple ordinary continuation `cccc` made from the first `0 <= r <= 3` referenc
 * es and the first `0 <= xx <= 127` bytes of `cc.code`.
 */
@Serializable
@SerialName(TvmConstDataPushcontInst.MNEMONIC)
data class TvmConstDataPushcontInst(
    override val location: TvmInstLocation,
    override val c: TvmInstList, // subslice
): TvmInst, TvmConstDataInst, TvmContOperand1Inst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PUSHCONT"
    }
}

/**
 * Pushes a continuation made from `builder`.
 * _Details:_ Pushes an `x`-byte continuation for `0 <= x <= 15`.
 */
@Serializable
@SerialName(TvmConstDataPushcontShortInst.MNEMONIC)
data class TvmConstDataPushcontShortInst(
    override val location: TvmInstLocation,
    override val c: TvmInstList, // subslice
): TvmInst, TvmConstDataInst, TvmContOperand1Inst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "PUSHCONT_SHORT"
    }
}

/**
 * Pushes the reference `ref` into the stack.
 * _Details:_ Pushes the first reference of `cc.code` into the stack as a _Cell_ (and removes this refe
 * rence from the current continuation).
 */
@Serializable
@SerialName(TvmConstDataPushrefInst.MNEMONIC)
data class TvmConstDataPushrefInst(
    override val location: TvmInstLocation,
    val c: TvmCell, // ref
): TvmInst, TvmConstDataInst, TvmRefOperandLoader {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "PUSHREF"
    }
}

/**
 * Similar to `PUSHREFSLICE`, but makes a simple ordinary _Continuation_ out of the cell.
 */
@Serializable
@SerialName(TvmConstDataPushrefcontInst.MNEMONIC)
data class TvmConstDataPushrefcontInst(
    override val location: TvmInstLocation,
    override val c: TvmInstList, // ref
): TvmInst, TvmConstDataInst, TvmRefOperandLoader, TvmContOperand1Inst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "118/43")

    companion object {
        const val MNEMONIC = "PUSHREFCONT"
    }
}

/**
 * Similar to `PUSHREF`, but converts the cell into a _Slice_.
 */
@Serializable
@SerialName(TvmConstDataPushrefsliceInst.MNEMONIC)
data class TvmConstDataPushrefsliceInst(
    override val location: TvmInstLocation,
    val c: TvmCell, // ref
): TvmInst, TvmConstDataInst, TvmRefOperandLoader {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "118/43")

    companion object {
        const val MNEMONIC = "PUSHREFSLICE"
    }
}

/**
 * Pushes the slice `slice` into the stack.
 * _Details:_ Pushes the (prefix) subslice of `cc.code` consisting of its first `8x+4` bits and no refe
 * rences (i.e., essentially a bitstring), where `0 <= x <= 15`.
 * A completion tag is assumed, meaning that all trailing zeroes and the last binary one (if present) a
 * re removed from this bitstring.
 * If the original bitstring consists only of zeroes, an empty slice will be pushed.
 */
@Serializable
@SerialName(TvmConstDataPushsliceInst.MNEMONIC)
data class TvmConstDataPushsliceInst(
    override val location: TvmInstLocation,
    val s: TvmSubSliceSerializedLoader, // subslice
): TvmInst, TvmConstDataInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 22)

    companion object {
        const val MNEMONIC = "PUSHSLICE"
    }
}

/**
 * Pushes the slice `slice` into the stack.
 * _Details:_ Pushes the subslice of `cc.code` consisting of `0 <= r <= 4` references and up to `8xx+6`
 * bits of data, with `0 <= xx <= 127`.
 * A completion tag is assumed.
 */
@Serializable
@SerialName(TvmConstDataPushsliceLongInst.MNEMONIC)
data class TvmConstDataPushsliceLongInst(
    override val location: TvmInstLocation,
    val slice: TvmSubSliceSerializedLoader, // subslice
): TvmInst, TvmConstDataInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 28)

    companion object {
        const val MNEMONIC = "PUSHSLICE_LONG"
    }
}

/**
 * Pushes the slice `slice` into the stack.
 * _Details:_ Pushes the (prefix) subslice of `cc.code` consisting of its first `1 <= r+1 <= 4` referen
 * ces and up to first `8xx+1` bits of data, with `0 <= xx <= 31`.
 * A completion tag is also assumed.
 */
@Serializable
@SerialName(TvmConstDataPushsliceRefsInst.MNEMONIC)
data class TvmConstDataPushsliceRefsInst(
    override val location: TvmInstLocation,
    val slice: TvmSubSliceSerializedLoader, // subslice
): TvmInst, TvmConstDataInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 25)

    companion object {
        const val MNEMONIC = "PUSHSLICE_REFS"
    }
}

/**
 * Pushes integer `xxxx`. `-2^15 <= xx < 2^15`.
 */
@Serializable
@SerialName(TvmConstIntPushint16Inst.MNEMONIC)
data class TvmConstIntPushint16Inst(
    override val location: TvmInstLocation,
    val x: Int, // int
): TvmInst, TvmConstIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "PUSHINT_16"
    }
}

/**
 * Pushes integer `x` into the stack. `-5 <= x <= 10`.
 * Here `i` equals four lower-order bits of `x` (`i=x mod 16`).
 */
@Serializable
@SerialName(TvmConstIntPushint4Inst.MNEMONIC)
data class TvmConstIntPushint4Inst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmConstIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "PUSHINT_4"
    }
}

/**
 * Pushes integer `xx`. `-128 <= xx <= 127`.
 */
@Serializable
@SerialName(TvmConstIntPushint8Inst.MNEMONIC)
data class TvmConstIntPushint8Inst(
    override val location: TvmInstLocation,
    val x: Int, // int
): TvmInst, TvmConstIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PUSHINT_8"
    }
}

/**
 * Pushes integer `xxx`.
 * _Details:_ 5-bit `0 <= l <= 30` determines the length `n=8l+19` of signed big-endian integer `xxx`.
 * The total length of this instruction is `l+4` bytes or `n+13=8l+32` bits.
 */
@Serializable
@SerialName(TvmConstIntPushintLongInst.MNEMONIC)
data class TvmConstIntPushintLongInst(
    override val location: TvmInstLocation,
    val x: String, // pushint_long
): TvmInst, TvmConstIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 23)

    companion object {
        const val MNEMONIC = "PUSHINT_LONG"
    }
}

/**
 * Pushes a `NaN`.
 */
@Serializable
@SerialName(TvmConstIntPushnanInst.MNEMONIC)
data class TvmConstIntPushnanInst(
    override val location: TvmInstLocation,
): TvmInst, TvmConstIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PUSHNAN"
    }
}

/**
 * Pushes `-2^(xx+1)` for `0 <= xx <= 255`.
 */
@Serializable
@SerialName(TvmConstIntPushnegpow2Inst.MNEMONIC)
data class TvmConstIntPushnegpow2Inst(
    override val location: TvmInstLocation,
    val x: Int, // uint
): TvmInst, TvmConstIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PUSHNEGPOW2"
    }
}

/**
 * (Quietly) pushes `2^(xx+1)` for `0 <= xx <= 255`.
 * `2^256` is a `NaN`.
 */
@Serializable
@SerialName(TvmConstIntPushpow2Inst.MNEMONIC)
data class TvmConstIntPushpow2Inst(
    override val location: TvmInstLocation,
    val x: Int, // uint
): TvmInst, TvmConstIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PUSHPOW2"
    }
}

/**
 * Pushes `2^(xx+1)-1` for `0 <= xx <= 255`.
 */
@Serializable
@SerialName(TvmConstIntPushpow2decInst.MNEMONIC)
data class TvmConstIntPushpow2decInst(
    override val location: TvmInstLocation,
    val x: Int, // uint
): TvmInst, TvmConstIntInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PUSHPOW2DEC"
    }
}

/**
 * Performs `RETTRUE` if integer `f!=0`, or `RETFALSE` if `f=0`.
 */
@Serializable
@SerialName(TvmContBasicBranchInst.MNEMONIC)
data class TvmContBasicBranchInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BRANCH"
    }
}

/**
 * _Call with current continuation_, transfers control to `c`, pushing the old value of `cc` into `c`'s
 * stack (instead of discarding it or writing it into new `c0`).
 */
@Serializable
@SerialName(TvmContBasicCallccInst.MNEMONIC)
data class TvmContBasicCallccInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "CALLCC"
    }
}

/**
 * Similar to `CALLXARGS`, but pushes the old value of `cc` (along with the top `0 <= p <= 15` values f
 * rom the original stack) into the stack of newly-invoked continuation `c`, setting `cc.nargs` to `-1
 * <= r <= 14`.
 */
@Serializable
@SerialName(TvmContBasicCallccargsInst.MNEMONIC)
data class TvmContBasicCallccargsInst(
    override val location: TvmInstLocation,
    val p: Int, // uint
    val r: Int, // uint
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "CALLCCARGS"
    }
}

/**
 * Similar to `CALLCCARGS`.
 */
@Serializable
@SerialName(TvmContBasicCallccvarargsInst.MNEMONIC)
data class TvmContBasicCallccvarargsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "CALLCCVARARGS"
    }
}

/**
 * Equivalent to `PUSHREFCONT` `CALLX`.
 */
@Serializable
@SerialName(TvmContBasicCallrefInst.MNEMONIC)
data class TvmContBasicCallrefInst(
    override val location: TvmInstLocation,
    override val c: TvmInstList, // ref
): TvmInst, TvmContBasicInst, TvmRefOperandLoader, TvmContOperand1Inst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "126/51")

    companion object {
        const val MNEMONIC = "CALLREF"
    }
}

/**
 * _Calls_ continuation `c` with `p` parameters and expecting `r` return values
 * `0 <= p <= 15`, `0 <= r <= 15`
 */
@Serializable
@SerialName(TvmContBasicCallxargsInst.MNEMONIC)
data class TvmContBasicCallxargsInst(
    override val location: TvmInstLocation,
    val p: Int, // uint
    val r: Int, // uint
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "CALLXARGS"
    }
}

/**
 * _Calls_ continuation `c` with `0 <= p <= 15` parameters, expecting an arbitrary number of return val
 * ues.
 */
@Serializable
@SerialName(TvmContBasicCallxargsVarInst.MNEMONIC)
data class TvmContBasicCallxargsVarInst(
    override val location: TvmInstLocation,
    val p: Int, // uint
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "CALLXARGS_VAR"
    }
}

/**
 * Similar to `CALLXARGS`, but takes `-1 <= p,r <= 254` from the stack. The next three operations also
 * take `p` and `r` from the stack, both in the range `-1...254`.
 */
@Serializable
@SerialName(TvmContBasicCallxvarargsInst.MNEMONIC)
data class TvmContBasicCallxvarargsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "CALLXVARARGS"
    }
}

/**
 * _Calls_, or _executes_, continuation `c`.
 */
@Serializable
@SerialName(TvmContBasicExecuteInst.MNEMONIC)
data class TvmContBasicExecuteInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "EXECUTE"
    }
}

/**
 * Equivalent to `PUSHREFCONT` `JMPX`.
 */
@Serializable
@SerialName(TvmContBasicJmprefInst.MNEMONIC)
data class TvmContBasicJmprefInst(
    override val location: TvmInstLocation,
    override val c: TvmInstList, // ref
): TvmInst, TvmContBasicInst, TvmRefOperandLoader, TvmContOperand1Inst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "126/51")

    companion object {
        const val MNEMONIC = "JMPREF"
    }
}

/**
 * Equivalent to `PUSHREFCONT` `JMPXDATA`.
 */
@Serializable
@SerialName(TvmContBasicJmprefdataInst.MNEMONIC)
data class TvmContBasicJmprefdataInst(
    override val location: TvmInstLocation,
    override val c: TvmInstList, // ref
): TvmInst, TvmContBasicInst, TvmRefOperandLoader, TvmContOperand1Inst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "126/51")

    companion object {
        const val MNEMONIC = "JMPREFDATA"
    }
}

/**
 * _Jumps_, or transfers control, to continuation `c`.
 * The remainder of the previous current continuation `cc` is discarded.
 */
@Serializable
@SerialName(TvmContBasicJmpxInst.MNEMONIC)
data class TvmContBasicJmpxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "JMPX"
    }
}

/**
 * _Jumps_ to continuation `c`, passing only the top `0 <= p <= 15` values from the current stack to it
 * (the remainder of the current stack is discarded).
 */
@Serializable
@SerialName(TvmContBasicJmpxargsInst.MNEMONIC)
data class TvmContBasicJmpxargsInst(
    override val location: TvmInstLocation,
    val p: Int, // uint
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "JMPXARGS"
    }
}

/**
 * Similar to `CALLCC`, but the remainder of the current continuation (the old value of `cc`) is conver
 * ted into a _Slice_ before pushing it into the stack of `c`.
 */
@Serializable
@SerialName(TvmContBasicJmpxdataInst.MNEMONIC)
data class TvmContBasicJmpxdataInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "JMPXDATA"
    }
}

/**
 * Similar to `JMPXARGS`.
 */
@Serializable
@SerialName(TvmContBasicJmpxvarargsInst.MNEMONIC)
data class TvmContBasicJmpxvarargsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "JMPXVARARGS"
    }
}

/**
 * _Returns_ to the continuation at `c0`. The remainder of the current continuation `cc` is discarded.
 * Approximately equivalent to `c0 PUSHCTR` `JMPX`.
 */
@Serializable
@SerialName(TvmContBasicRetInst.MNEMONIC)
data class TvmContBasicRetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "RET"
    }
}

/**
 * _Returns_ to the continuation at `c1`.
 * Approximately equivalent to `c1 PUSHCTR` `JMPX`.
 */
@Serializable
@SerialName(TvmContBasicRetaltInst.MNEMONIC)
data class TvmContBasicRetaltInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "RETALT"
    }
}

/**
 * _Returns_ to `c0`, with `0 <= r <= 15` return values taken from the current stack.
 */
@Serializable
@SerialName(TvmContBasicRetargsInst.MNEMONIC)
data class TvmContBasicRetargsInst(
    override val location: TvmInstLocation,
    val r: Int, // uint
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "RETARGS"
    }
}

/**
 * Equivalent to `c0 PUSHCTR` `JMPXDATA`. In this way, the remainder of the current continuation is con
 * verted into a _Slice_ and returned to the caller.
 */
@Serializable
@SerialName(TvmContBasicRetdataInst.MNEMONIC)
data class TvmContBasicRetdataInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "RETDATA"
    }
}

/**
 * Similar to `RETARGS`.
 */
@Serializable
@SerialName(TvmContBasicRetvarargsInst.MNEMONIC)
data class TvmContBasicRetvarargsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "RETVARARGS"
    }
}

/**
 * Runs child VM with code `code` and stack `x_1...x_n`. Returns the resulting stack `x'_1...x'_m` and
 * exitcode. Other arguments and return values are enabled by flags.
 */
@Serializable
@SerialName(TvmContBasicRunvmInst.MNEMONIC)
data class TvmContBasicRunvmInst(
    override val location: TvmInstLocation,
    val flags: Int, // uint
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "RUNVM"
    }
}

/**
 * Runs child VM with code `code` and stack `x_1...x_n`. Returns the resulting stack `x'_1...x'_m` and
 * exitcode. Other arguments and return values are enabled by flags.
 */
@Serializable
@SerialName(TvmContBasicRunvmxInst.MNEMONIC)
data class TvmContBasicRunvmxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "RUNVMX"
    }
}

/**
 * If integer `f` is non-zero, returns `x`, otherwise returns `y`. Notice that no type checks are perfo
 * rmed on `x` and `y`; as such, it is more like a conditional stack operation. Roughly equivalent to `
 * ROT` `ISZERO` `INC` `ROLLX` `NIP`.
 */
@Serializable
@SerialName(TvmContConditionalCondselInst.MNEMONIC)
data class TvmContConditionalCondselInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContConditionalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "CONDSEL"
    }
}

/**
 * Same as `CONDSEL`, but first checks whether `x` and `y` have the same type.
 */
@Serializable
@SerialName(TvmContConditionalCondselchkInst.MNEMONIC)
data class TvmContConditionalCondselchkInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContConditionalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "CONDSELCHK"
    }
}

/**
 * Performs `EXECUTE` for `c` (i.e., _executes_ `c`), but only if integer `f` is non-zero. Otherwise si
 * mply discards both values.
 */
@Serializable
@SerialName(TvmContConditionalIfInst.MNEMONIC)
data class TvmContConditionalIfInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContConditionalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "IF"
    }
}

/**
 * Checks whether bit `0 <= n <= 31` is set in integer `x`, and if so, performs `JMPX` to continuation
 * `c`. Value `x` is left in the stack.
 */
@Serializable
@SerialName(TvmContConditionalIfbitjmpInst.MNEMONIC)
data class TvmContConditionalIfbitjmpInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmContConditionalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "IFBITJMP"
    }
}

/**
 * Performs a `JMPREF` if bit `0 <= n <= 31` is set in integer `x`.
 */
@Serializable
@SerialName(TvmContConditionalIfbitjmprefInst.MNEMONIC)
data class TvmContConditionalIfbitjmprefInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmContConditionalInst, TvmRefOperandLoader {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "126/51")

    companion object {
        const val MNEMONIC = "IFBITJMPREF"
    }
}

/**
 * If integer `f` is non-zero, executes `c`, otherwise executes `c'`. Equivalent to `CONDSELCHK` `EXECU
 * TE`.
 */
@Serializable
@SerialName(TvmContConditionalIfelseInst.MNEMONIC)
data class TvmContConditionalIfelseInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContConditionalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "IFELSE"
    }
}

/**
 * Equivalent to `PUSHREFCONT` `IFELSE`.
 */
@Serializable
@SerialName(TvmContConditionalIfelserefInst.MNEMONIC)
data class TvmContConditionalIfelserefInst(
    override val location: TvmInstLocation,
    override val c: TvmInstList, // ref
): TvmInst, TvmContConditionalInst, TvmRefOperandLoader, TvmContOperand1Inst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/126/51")

    companion object {
        const val MNEMONIC = "IFELSEREF"
    }
}

/**
 * Jumps to `c` (similarly to `JMPX`), but only if `f` is non-zero.
 */
@Serializable
@SerialName(TvmContConditionalIfjmpInst.MNEMONIC)
data class TvmContConditionalIfjmpInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContConditionalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "IFJMP"
    }
}

/**
 * Equivalent to `PUSHREFCONT` `IFJMP`.
 */
@Serializable
@SerialName(TvmContConditionalIfjmprefInst.MNEMONIC)
data class TvmContConditionalIfjmprefInst(
    override val location: TvmInstLocation,
    override val c: TvmInstList, // ref
): TvmInst, TvmContConditionalInst, TvmRefOperandLoader, TvmContOperand1Inst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/126/51")

    companion object {
        const val MNEMONIC = "IFJMPREF"
    }
}

/**
 * Jumps to `c` if bit `0 <= n <= 31` is not set in integer `x`.
 */
@Serializable
@SerialName(TvmContConditionalIfnbitjmpInst.MNEMONIC)
data class TvmContConditionalIfnbitjmpInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmContConditionalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "IFNBITJMP"
    }
}

/**
 * Performs a `JMPREF` if bit `0 <= n <= 31` is not set in integer `x`.
 */
@Serializable
@SerialName(TvmContConditionalIfnbitjmprefInst.MNEMONIC)
data class TvmContConditionalIfnbitjmprefInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmContConditionalInst, TvmRefOperandLoader {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "126/51")

    companion object {
        const val MNEMONIC = "IFNBITJMPREF"
    }
}

/**
 * Executes continuation `c`, but only if integer `f` is zero. Otherwise simply discards both values.
 */
@Serializable
@SerialName(TvmContConditionalIfnotInst.MNEMONIC)
data class TvmContConditionalIfnotInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContConditionalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "IFNOT"
    }
}

/**
 * Jumps to `c` (similarly to `JMPX`), but only if `f` is zero.
 */
@Serializable
@SerialName(TvmContConditionalIfnotjmpInst.MNEMONIC)
data class TvmContConditionalIfnotjmpInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContConditionalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "IFNOTJMP"
    }
}

/**
 * Equivalent to `PUSHREFCONT` `IFNOTJMP`.
 */
@Serializable
@SerialName(TvmContConditionalIfnotjmprefInst.MNEMONIC)
data class TvmContConditionalIfnotjmprefInst(
    override val location: TvmInstLocation,
    override val c: TvmInstList, // ref
): TvmInst, TvmContConditionalInst, TvmRefOperandLoader, TvmContOperand1Inst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/126/51")

    companion object {
        const val MNEMONIC = "IFNOTJMPREF"
    }
}

/**
 * Equivalent to `PUSHREFCONT` `IFNOT`.
 */
@Serializable
@SerialName(TvmContConditionalIfnotrefInst.MNEMONIC)
data class TvmContConditionalIfnotrefInst(
    override val location: TvmInstLocation,
    override val c: TvmInstList, // ref
): TvmInst, TvmContConditionalInst, TvmRefOperandLoader, TvmContOperand1Inst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/126/51")

    companion object {
        const val MNEMONIC = "IFNOTREF"
    }
}

/**
 * Performs a `RET`, but only if integer `f` is zero.
 */
@Serializable
@SerialName(TvmContConditionalIfnotretInst.MNEMONIC)
data class TvmContConditionalIfnotretInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContConditionalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "IFNOTRET"
    }
}

/**
 * Performs `RETALT` if integer `f=0`.
 */
@Serializable
@SerialName(TvmContConditionalIfnotretaltInst.MNEMONIC)
data class TvmContConditionalIfnotretaltInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContConditionalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "IFNOTRETALT"
    }
}

/**
 * Equivalent to `PUSHREFCONT` `IF`, with the optimization that the cell reference is not actually load
 * ed into a _Slice_ and then converted into an ordinary _Continuation_ if `f=0`.
 * Gas consumption of this primitive depends on whether `f=0` and whether the reference was loaded befo
 * re.
 * Similar remarks apply other primitives that accept a continuation as a reference.
 */
@Serializable
@SerialName(TvmContConditionalIfrefInst.MNEMONIC)
data class TvmContConditionalIfrefInst(
    override val location: TvmInstLocation,
    override val c: TvmInstList, // ref
): TvmInst, TvmContConditionalInst, TvmRefOperandLoader, TvmContOperand1Inst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/126/51")

    companion object {
        const val MNEMONIC = "IFREF"
    }
}

/**
 * Equivalent to `PUSHREFCONT` `SWAP` `IFELSE`, with the optimization that the cell reference is not ac
 * tually loaded into a _Slice_ and then converted into an ordinary _Continuation_ if `f=0`. Similar re
 * marks apply to the next two primitives: cells are converted into continuations only when necessary.
 */
@Serializable
@SerialName(TvmContConditionalIfrefelseInst.MNEMONIC)
data class TvmContConditionalIfrefelseInst(
    override val location: TvmInstLocation,
    override val c: TvmInstList, // ref
): TvmInst, TvmContConditionalInst, TvmRefOperandLoader, TvmContOperand1Inst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/126/51")

    companion object {
        const val MNEMONIC = "IFREFELSE"
    }
}

/**
 * Equivalent to `PUSHREFCONT` `PUSHREFCONT` `IFELSE`.
 */
@Serializable
@SerialName(TvmContConditionalIfrefelserefInst.MNEMONIC)
data class TvmContConditionalIfrefelserefInst(
    override val location: TvmInstLocation,
    override val c1: TvmInstList, // ref
    override val c2: TvmInstList, // ref
): TvmInst, TvmContConditionalInst, TvmRefOperandLoader, TvmContOperand2Inst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "126/51")

    companion object {
        const val MNEMONIC = "IFREFELSEREF"
    }
}

/**
 * Performs a `RET`, but only if integer `f` is non-zero. If `f` is a `NaN`, throws an integer overflow
 * exception.
 */
@Serializable
@SerialName(TvmContConditionalIfretInst.MNEMONIC)
data class TvmContConditionalIfretInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContConditionalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "IFRET"
    }
}

/**
 * Performs `RETALT` if integer `f!=0`.
 */
@Serializable
@SerialName(TvmContConditionalIfretaltInst.MNEMONIC)
data class TvmContConditionalIfretaltInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContConditionalInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "IFRETALT"
    }
}

/**
 * Transforms a _Slice_ `s` into a simple ordinary continuation `c`, with `c.code=s` and an empty stack
 * and savelist.
 */
@Serializable
@SerialName(TvmContCreateBlessInst.MNEMONIC)
data class TvmContCreateBlessInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContCreateInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BLESS"
    }
}

/**
 * `0 <= r <= 15`, `-1 <= n <= 14`
 * Equivalent to `BLESS` `[r] [n] SETCONTARGS`.
 * The value of `n` is represented inside the instruction by the 4-bit integer `n mod 16`.
 */
@Serializable
@SerialName(TvmContCreateBlessargsInst.MNEMONIC)
data class TvmContCreateBlessargsInst(
    override val location: TvmInstLocation,
    val r: Int, // uint
    val n: Int, // uint
): TvmInst, TvmContCreateInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BLESSARGS"
    }
}

/**
 * Equivalent to `ROT` `BLESS` `ROTREV` `SETCONTVARARGS`.
 */
@Serializable
@SerialName(TvmContCreateBlessvarargsInst.MNEMONIC)
data class TvmContCreateBlessvarargsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContCreateInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+s''")

    companion object {
        const val MNEMONIC = "BLESSVARARGS"
    }
}

/**
 * Calls the continuation in `c3`, pushing integer `0 <= nn <= 255` into its stack as an argument.
 * Approximately equivalent to `[nn] PUSHINT` `c3 PUSHCTR` `EXECUTE`.
 */
@Serializable
@SerialName(TvmContDictCalldictInst.MNEMONIC)
data class TvmContDictCalldictInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmContDictInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "CALLDICT"
    }
}

/**
 * For `0 <= n < 2^14`, an encoding of `[n] CALL` for larger values of `n`.
 */
@Serializable
@SerialName(TvmContDictCalldictLongInst.MNEMONIC)
data class TvmContDictCalldictLongInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmContDictInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "CALLDICT_LONG"
    }
}

/**
 * Jumps to the continuation in `c3`, pushing integer `0 <= n < 2^14` as its argument.
 * Approximately equivalent to `n PUSHINT` `c3 PUSHCTR` `JMPX`.
 */
@Serializable
@SerialName(TvmContDictJmpdictInst.MNEMONIC)
data class TvmContDictJmpdictInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmContDictInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "JMPDICT"
    }
}

/**
 * Equivalent to `n PUSHINT` `c3 PUSHCTR`, for `0 <= n < 2^14`.
 * In this way, `[n] CALL` is approximately equivalent to `[n] PREPARE` `EXECUTE`, and `[n] JMP` is app
 * roximately equivalent to `[n] PREPARE` `JMPX`.
 * One might use, for instance, `CALLXARGS` or `CALLCC` instead of `EXECUTE` here.
 */
@Serializable
@SerialName(TvmContDictPreparedictInst.MNEMONIC)
data class TvmContDictPreparedictInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmContDictInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "PREPAREDICT"
    }
}

/**
 * Similar to `REPEAT`, but executes `c` infinitely many times. A `RET` only begins a new iteration of
 * the infinite loop, which can be exited only by an exception, or a `RETALT` (or an explicit `JMPX`).
 */
@Serializable
@SerialName(TvmContLoopsAgainInst.MNEMONIC)
data class TvmContLoopsAgainInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContLoopsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "AGAIN"
    }
}

/**
 * Similar to `AGAIN`, but also modifies `c1` in the same way as `REPEATBRK`.
 */
@Serializable
@SerialName(TvmContLoopsAgainbrkInst.MNEMONIC)
data class TvmContLoopsAgainbrkInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContLoopsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "AGAINBRK"
    }
}

/**
 * Similar to `AGAIN`, but performed with respect to the current continuation `cc`.
 */
@Serializable
@SerialName(TvmContLoopsAgainendInst.MNEMONIC)
data class TvmContLoopsAgainendInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContLoopsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "AGAINEND"
    }
}

/**
 * Equivalent to `SAMEALTSAVE` `AGAINEND`.
 */
@Serializable
@SerialName(TvmContLoopsAgainendbrkInst.MNEMONIC)
data class TvmContLoopsAgainendbrkInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContLoopsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "AGAINENDBRK"
    }
}

/**
 * Executes continuation `c` `n` times, if integer `n` is non-negative. If `n>=2^31` or `n<-2^31`, gene
 * rates a range check exception.
 * Notice that a `RET` inside the code of `c` works as a `continue`, not as a `break`. One should use e
 * ither alternative (experimental) loops or alternative `RETALT` (along with a `SETEXITALT` before the
 * loop) to `break` out of a loop.
 */
@Serializable
@SerialName(TvmContLoopsRepeatInst.MNEMONIC)
data class TvmContLoopsRepeatInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContLoopsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "REPEAT"
    }
}

/**
 * Similar to `REPEAT`, but also sets `c1` to the original `cc` after saving the old value of `c1` into
 * the savelist of the original `cc`. In this way `RETALT` could be used to break out of the loop body
 * .
 */
@Serializable
@SerialName(TvmContLoopsRepeatbrkInst.MNEMONIC)
data class TvmContLoopsRepeatbrkInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContLoopsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "REPEATBRK"
    }
}

/**
 * Similar to `REPEAT`, but it is applied to the current continuation `cc`.
 */
@Serializable
@SerialName(TvmContLoopsRepeatendInst.MNEMONIC)
data class TvmContLoopsRepeatendInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContLoopsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "REPEATEND"
    }
}

/**
 * Similar to `REPEATEND`, but also sets `c1` to the original `c0` after saving the old value of `c1` i
 * nto the savelist of the original `c0`. Equivalent to `SAMEALTSAVE` `REPEATEND`.
 */
@Serializable
@SerialName(TvmContLoopsRepeatendbrkInst.MNEMONIC)
data class TvmContLoopsRepeatendbrkInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContLoopsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "REPEATENDBRK"
    }
}

/**
 * Executes continuation `c`, then pops an integer `x` from the resulting stack. If `x` is zero, perfor
 * ms another iteration of this loop. The actual implementation of this primitive involves an extraordi
 * nary continuation `ec_until` with its arguments set to the body of the loop (continuation `c`) and t
 * he original current continuation `cc`. This extraordinary continuation is then saved into the saveli
 * st of `c` as `c.c0` and the modified `c` is then executed. The other loop primitives are implemented
 * similarly with the aid of suitable extraordinary continuations.
 */
@Serializable
@SerialName(TvmContLoopsUntilInst.MNEMONIC)
data class TvmContLoopsUntilInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContLoopsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "UNTIL"
    }
}

/**
 * Similar to `UNTIL`, but also modifies `c1` in the same way as `REPEATBRK`.
 */
@Serializable
@SerialName(TvmContLoopsUntilbrkInst.MNEMONIC)
data class TvmContLoopsUntilbrkInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContLoopsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "UNTILBRK"
    }
}

/**
 * Similar to `UNTIL`, but executes the current continuation `cc` in a loop. When the loop exit conditi
 * on is satisfied, performs a `RET`.
 */
@Serializable
@SerialName(TvmContLoopsUntilendInst.MNEMONIC)
data class TvmContLoopsUntilendInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContLoopsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "UNTILEND"
    }
}

/**
 * Equivalent to `SAMEALTSAVE` `UNTILEND`.
 */
@Serializable
@SerialName(TvmContLoopsUntilendbrkInst.MNEMONIC)
data class TvmContLoopsUntilendbrkInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContLoopsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "UNTILENDBRK"
    }
}

/**
 * Executes `c'` and pops an integer `x` from the resulting stack. If `x` is zero, exists the loop and
 * transfers control to the original `cc`. If `x` is non-zero, executes `c`, and then begins a new iter
 * ation.
 */
@Serializable
@SerialName(TvmContLoopsWhileInst.MNEMONIC)
data class TvmContLoopsWhileInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContLoopsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "WHILE"
    }
}

/**
 * Similar to `WHILE`, but also modifies `c1` in the same way as `REPEATBRK`.
 */
@Serializable
@SerialName(TvmContLoopsWhilebrkInst.MNEMONIC)
data class TvmContLoopsWhilebrkInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContLoopsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "WHILEBRK"
    }
}

/**
 * Similar to `WHILE`, but uses the current continuation `cc` as the loop body.
 */
@Serializable
@SerialName(TvmContLoopsWhileendInst.MNEMONIC)
data class TvmContLoopsWhileendInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContLoopsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "WHILEEND"
    }
}

/**
 * Equivalent to `SAMEALTSAVE` `WHILEEND`.
 */
@Serializable
@SerialName(TvmContLoopsWhileendbrkInst.MNEMONIC)
data class TvmContLoopsWhileendbrkInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContLoopsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "WHILEENDBRK"
    }
}

/**
 * Sets `c0` to `compose0(c, c0)`. In other words, `c` will be executed before exiting current subrouti
 * ne.
 */
@Serializable
@SerialName(TvmContRegistersAtexitInst.MNEMONIC)
data class TvmContRegistersAtexitInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "ATEXIT"
    }
}

/**
 * Sets `c1` to `compose1(c, c1)`. In other words, `c` will be executed before exiting current subrouti
 * ne by its alternative return path.
 */
@Serializable
@SerialName(TvmContRegistersAtexitaltInst.MNEMONIC)
data class TvmContRegistersAtexitaltInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "ATEXITALT"
    }
}

/**
 * Performs `cc:=compose1(compose0(c, compose0(-1 PUSHINT, cc)), compose0(0 PUSHINT, cc))`. If `c` repr
 * esents a boolean circuit, the net effect is to evaluate it and push either `-1` or `0` into the stac
 * k before continuing.
 */
@Serializable
@SerialName(TvmContRegistersBoolevalInst.MNEMONIC)
data class TvmContRegistersBoolevalInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BOOLEVAL"
    }
}

/**
 * Computes the composition `compose0(c, c')`, which has the meaning of ''perform `c`, and, if successf
 * ul, perform `c'`'' (if `c` is a boolean circuit) or simply ''perform `c`, then `c'`''. Equivalent to
 * `SWAP` `c0 SETCONT`.
 */
@Serializable
@SerialName(TvmContRegistersComposInst.MNEMONIC)
data class TvmContRegistersComposInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "COMPOS"
    }
}

/**
 * Computes the alternative composition `compose1(c, c')`, which has the meaning of ''perform `c`, and,
 * if not successful, perform `c'`'' (if `c` is a boolean circuit). Equivalent to `SWAP` `c1 SETCONT`.
 */
@Serializable
@SerialName(TvmContRegistersComposaltInst.MNEMONIC)
data class TvmContRegistersComposaltInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "COMPOSALT"
    }
}

/**
 * Computes composition `compose1(compose0(c, c'), c')`, which has the meaning of ''compute boolean cir
 * cuit `c`, then compute `c'`, regardless of the result of `c`''.
 */
@Serializable
@SerialName(TvmContRegistersComposbothInst.MNEMONIC)
data class TvmContRegistersComposbothInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "COMPOSBOTH"
    }
}

/**
 * Interchanges `c0` and `c1`.
 */
@Serializable
@SerialName(TvmContRegistersInvertInst.MNEMONIC)
data class TvmContRegistersInvertInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "INVERT"
    }
}

/**
 * Pops a value `x` from the stack and stores it into control register `c(i)`, if supported in the curr
 * ent codepage. Notice that if a control register accepts only values of a specific type, a type-check
 * ing exception may occur.
 */
@Serializable
@SerialName(TvmContRegistersPopctrInst.MNEMONIC)
data class TvmContRegistersPopctrInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "POPCTR"
    }
}

/**
 * Similar to `c[i] POPCTR`, but with `0 <= i <= 255` from the stack.
 */
@Serializable
@SerialName(TvmContRegistersPopctrxInst.MNEMONIC)
data class TvmContRegistersPopctrxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "POPCTRX"
    }
}

/**
 * Similar to `c[i] POPCTR`, but also saves the old value of `c[i]` into continuation `c0`.
 * Equivalent (up to exceptions) to `c[i] SAVECTR` `c[i] POPCTR`.
 */
@Serializable
@SerialName(TvmContRegistersPopsaveInst.MNEMONIC)
data class TvmContRegistersPopsaveInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "POPSAVE"
    }
}

/**
 * Pushes the current value of control register `c(i)`. If the control register is not supported in the
 * current codepage, or if it does not have a value, an exception is triggered.
 */
@Serializable
@SerialName(TvmContRegistersPushctrInst.MNEMONIC)
data class TvmContRegistersPushctrInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PUSHCTR"
    }
}

/**
 * Similar to `c[i] PUSHCTR`, but with `i`, `0 <= i <= 255`, taken from the stack.
 * Notice that this primitive is one of the few ''exotic'' primitives, which are not polymorphic like s
 * tack manipulation primitives, and at the same time do not have well-defined types of parameters and
 * return values, because the type of `x` depends on `i`.
 */
@Serializable
@SerialName(TvmContRegistersPushctrxInst.MNEMONIC)
data class TvmContRegistersPushctrxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PUSHCTRX"
    }
}

/**
 * Sets `c1` to `c0`. Equivalent to `c0 PUSHCTR` `c1 POPCTR`.
 */
@Serializable
@SerialName(TvmContRegistersSamealtInst.MNEMONIC)
data class TvmContRegistersSamealtInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SAMEALT"
    }
}

/**
 * Sets `c1` to `c0`, but first saves the old value of `c1` into the savelist of `c0`.
 * Equivalent to `c1 SAVE` `SAMEALT`.
 */
@Serializable
@SerialName(TvmContRegistersSamealtsaveInst.MNEMONIC)
data class TvmContRegistersSamealtsaveInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SAMEALTSAVE"
    }
}

/**
 * Saves the current value of `c(i)` into the savelist of continuation `c0`. If an entry for `c[i]` is
 * already present in the savelist of `c0`, nothing is done. Equivalent to `c[i] PUSHCTR` `c[i] SETRETC
 * TR`.
 */
@Serializable
@SerialName(TvmContRegistersSaveInst.MNEMONIC)
data class TvmContRegistersSaveInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SAVE"
    }
}

/**
 * Similar to `c[i] SAVE`, but saves the current value of `c[i]` into the savelist of `c1`, not `c0`.
 */
@Serializable
@SerialName(TvmContRegistersSavealtInst.MNEMONIC)
data class TvmContRegistersSavealtInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SAVEALT"
    }
}

/**
 * Equivalent to `c[i] SAVE` `c[i] SAVEALT`.
 */
@Serializable
@SerialName(TvmContRegistersSavebothInst.MNEMONIC)
data class TvmContRegistersSavebothInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SAVEBOTH"
    }
}

/**
 * Equivalent to `c1 PUSHCTR` `c[i] SETCONTCTR` `c1 POPCTR`.
 */
@Serializable
@SerialName(TvmContRegistersSetaltctrInst.MNEMONIC)
data class TvmContRegistersSetaltctrInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SETALTCTR"
    }
}

/**
 * Stores `x` into the savelist of continuation `c` as `c(i)`, and returns the resulting continuation `
 * c'`. Almost all operations with continuations may be expressed in terms of `SETCONTCTR`, `POPCTR`, a
 * nd `PUSHCTR`.
 */
@Serializable
@SerialName(TvmContRegistersSetcontctrInst.MNEMONIC)
data class TvmContRegistersSetcontctrInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SETCONTCTR"
    }
}

/**
 * Similar to `c[i] SETCONTCTR`, but with `0 <= i <= 255` from the stack.
 */
@Serializable
@SerialName(TvmContRegistersSetcontctrxInst.MNEMONIC)
data class TvmContRegistersSetcontctrxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SETCONTCTRX"
    }
}

/**
 * Sets `c1` to `compose1(compose0(c, c0), c1)`,
 * In this way, a subsequent `RETALT` will first execute `c`, then transfer control to the original `c0
 * `. This can be used, for instance, to exit from nested loops.
 */
@Serializable
@SerialName(TvmContRegistersSetexitaltInst.MNEMONIC)
data class TvmContRegistersSetexitaltInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SETEXITALT"
    }
}

/**
 * Equivalent to `c0 PUSHCTR` `c[i] SETCONTCTR` `c0 POPCTR`.
 */
@Serializable
@SerialName(TvmContRegistersSetretctrInst.MNEMONIC)
data class TvmContRegistersSetretctrInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SETRETCTR"
    }
}

/**
 * Computes `compose0(c, c0)`.
 */
@Serializable
@SerialName(TvmContRegistersThenretInst.MNEMONIC)
data class TvmContRegistersThenretInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "THENRET"
    }
}

/**
 * Computes `compose0(c, c1)`
 */
@Serializable
@SerialName(TvmContRegistersThenretaltInst.MNEMONIC)
data class TvmContRegistersThenretaltInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContRegistersInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "THENRETALT"
    }
}

/**
 * Leaves only the top `0 <= p <= 15` values in the current stack (somewhat similarly to `ONLYTOPX`), w
 * ith all the unused bottom values not discarded, but saved into continuation `c0` in the same way as
 * `SETCONTARGS` does.
 */
@Serializable
@SerialName(TvmContStackReturnargsInst.MNEMONIC)
data class TvmContStackReturnargsInst(
    override val location: TvmInstLocation,
    val p: Int, // uint
): TvmInst, TvmContStackInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+s''")

    companion object {
        const val MNEMONIC = "RETURNARGS"
    }
}

/**
 * Similar to `RETURNARGS`, but with Integer `0 <= p <= 255` taken from the stack.
 */
@Serializable
@SerialName(TvmContStackReturnvarargsInst.MNEMONIC)
data class TvmContStackReturnvarargsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContStackInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+s''")

    companion object {
        const val MNEMONIC = "RETURNVARARGS"
    }
}

/**
 * Pushes `0 <= r <= 15` values `x_1...x_r` into the stack of (a copy of) the continuation `c`, startin
 * g with `x_1`. When `n` is 15 (-1 in Fift notation), does nothing with `c.nargs`. For `0 <= n <= 14`,
 * sets `c.nargs` to the final size of the stack of `c'` plus `n`. In other words, transforms `c` into
 * a _closure_ or a _partially applied function_, with `0 <= n <= 14` arguments missing.
 */
@Serializable
@SerialName(TvmContStackSetcontargsNInst.MNEMONIC)
data class TvmContStackSetcontargsNInst(
    override val location: TvmInstLocation,
    val r: Int, // uint
    val n: Int, // uint
): TvmInst, TvmContStackInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+s''")

    companion object {
        const val MNEMONIC = "SETCONTARGS_N"
    }
}

/**
 * Similar to `SETCONTARGS`, but with `0 <= r <= 255` and `-1 <= n <= 255` taken from the stack.
 */
@Serializable
@SerialName(TvmContStackSetcontvarargsInst.MNEMONIC)
data class TvmContStackSetcontvarargsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContStackInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+s''")

    companion object {
        const val MNEMONIC = "SETCONTVARARGS"
    }
}

/**
 * `-1 <= n <= 255`
 * If `n=-1`, this operation does nothing (`c'=c`).
 * Otherwise its action is similar to `[n] SETNUMARGS`, but with `n` taken from the stack.
 */
@Serializable
@SerialName(TvmContStackSetnumvarargsInst.MNEMONIC)
data class TvmContStackSetnumvarargsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmContStackInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SETNUMVARARGS"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDebugDebugInst.MNEMONIC)
data class TvmDebugDebugInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
): TvmInst, TvmDebugInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "DEBUG"
    }
}

/**
 * `0 <= n < 16`. Length of `ssss` is `n+1` bytes.
 * `{string}` is a [string literal](https://github.com/Piterden/TON-docs/blob/master/Fift.%20A%20Brief%
 * 20Introduction.md#user-content-29-string-literals).
 * `DEBUGSTR`: `ssss` is the given string.
 * `DEBUGSTRI`: `ssss` is one-byte integer `0 <= x <= 255` followed by the given string.
 */
@Serializable
@SerialName(TvmDebugDebugstrInst.MNEMONIC)
data class TvmDebugDebugstrInst(
    override val location: TvmInstLocation,
    val s: TvmSubSliceSerializedLoader, // subslice
): TvmInst, TvmDebugInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "DEBUGSTR"
    }
}

/**
 * Deletes `n`-bit key, represented by a _Slice_ `k`, from dictionary `D`. If the key is present, retur
 * ns the modified dictionary `D'` and the success flag `-1`. Otherwise, returns the original dictionar
 * y `D` and `0`.
 */
@Serializable
@SerialName(TvmDictDeleteDictdelInst.MNEMONIC)
data class TvmDictDeleteDictdelInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictDeleteInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTDEL"
    }
}

/**
 * Deletes `n`-bit key, represented by a _Slice_ `k`, from dictionary `D`. If the key is present, retur
 * ns the modified dictionary `D'`, the original value `x` associated with the key `k` (represented by
 * a _Slice_), and the success flag `-1`. Otherwise, returns the original dictionary `D` and `0`.
 */
@Serializable
@SerialName(TvmDictDeleteDictdelgetInst.MNEMONIC)
data class TvmDictDeleteDictdelgetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictDeleteInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTDELGET"
    }
}

/**
 * Similar to `DICTDELGET`, but with `LDREF` `ENDS` applied to `x` on success, so that the value return
 * ed `c` is a _Cell_.
 */
@Serializable
@SerialName(TvmDictDeleteDictdelgetrefInst.MNEMONIC)
data class TvmDictDeleteDictdelgetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictDeleteInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTDELGETREF"
    }
}

/**
 * A version of `DICTDEL` with the key represented by a signed `n`-bit _Integer_ `i`. If `i` does not f
 * it into `n` bits, simply returns `D` `0` (''key not found, dictionary unmodified'').
 */
@Serializable
@SerialName(TvmDictDeleteDictidelInst.MNEMONIC)
data class TvmDictDeleteDictidelInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictDeleteInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIDEL"
    }
}

/**
 * `DICTDELGET`, but with `i` a signed `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictDeleteDictidelgetInst.MNEMONIC)
data class TvmDictDeleteDictidelgetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictDeleteInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIDELGET"
    }
}

/**
 * `DICTDELGETREF`, but with `i` a signed `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictDeleteDictidelgetrefInst.MNEMONIC)
data class TvmDictDeleteDictidelgetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictDeleteInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIDELGETREF"
    }
}

/**
 * Similar to `DICTIDEL`, but with `i` an unsigned `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictDeleteDictudelInst.MNEMONIC)
data class TvmDictDeleteDictudelInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictDeleteInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUDEL"
    }
}

/**
 * `DICTDELGET`, but with `i` an unsigned `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictDeleteDictudelgetInst.MNEMONIC)
data class TvmDictDeleteDictudelgetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictDeleteInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUDELGET"
    }
}

/**
 * `DICTDELGETREF`, but with `i` an unsigned `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictDeleteDictudelgetrefInst.MNEMONIC)
data class TvmDictDeleteDictudelgetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictDeleteInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUDELGETREF"
    }
}

/**
 * Looks up key `k` (represented by a _Slice_, the first `0 <= n <= 1023` data bits of which are used a
 * s a key) in dictionary `D` of type `HashmapE(n,X)` with `n`-bit keys.
 * On success, returns the value found as a _Slice_ `x`.
 */
@Serializable
@SerialName(TvmDictGetDictgetInst.MNEMONIC)
data class TvmDictGetDictgetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictGetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTGET"
    }
}

/**
 * Similar to `DICTGET`, but with a `LDREF` `ENDS` applied to `x` on success.
 * This operation is useful for dictionaries of type `HashmapE(n,^Y)`.
 */
@Serializable
@SerialName(TvmDictGetDictgetrefInst.MNEMONIC)
data class TvmDictGetDictgetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictGetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTGETREF"
    }
}

/**
 * Similar to `DICTGET`, but with a signed (big-endian) `n`-bit _Integer_ `i` as a key. If `i` does not
 * fit into `n` bits, returns `0`. If `i` is a `NaN`, throws an integer overflow exception.
 */
@Serializable
@SerialName(TvmDictGetDictigetInst.MNEMONIC)
data class TvmDictGetDictigetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictGetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIGET"
    }
}

/**
 * Combines `DICTIGET` with `DICTGETREF`: it uses signed `n`-bit _Integer_ `i` as a key and returns a _
 * Cell_ instead of a _Slice_ on success.
 */
@Serializable
@SerialName(TvmDictGetDictigetrefInst.MNEMONIC)
data class TvmDictGetDictigetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictGetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIGETREF"
    }
}

/**
 * Similar to `DICTIGET`, but with _unsigned_ (big-endian) `n`-bit _Integer_ `i` used as a key.
 */
@Serializable
@SerialName(TvmDictGetDictugetInst.MNEMONIC)
data class TvmDictGetDictugetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictGetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUGET"
    }
}

/**
 * Similar to `DICTIGETREF`, but with an unsigned `n`-bit _Integer_ key `i`.
 */
@Serializable
@SerialName(TvmDictGetDictugetrefInst.MNEMONIC)
data class TvmDictGetDictugetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictGetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUGETREF"
    }
}

/**
 * A variant of `DICTGETREF` that returns _Null_ instead of the value `c^?` if the key `k` is absent fr
 * om dictionary `D`.
 */
@Serializable
@SerialName(TvmDictMayberefDictgetoptrefInst.MNEMONIC)
data class TvmDictMayberefDictgetoptrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMayberefInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTGETOPTREF"
    }
}

/**
 * `DICTGETOPTREF`, but with `i` a signed `n`-bit integer. If the key `i` is out of range, also returns
 * _Null_.
 */
@Serializable
@SerialName(TvmDictMayberefDictigetoptrefInst.MNEMONIC)
data class TvmDictMayberefDictigetoptrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMayberefInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIGETOPTREF"
    }
}

/**
 * Similar to primitive `DICTSETGETOPTREF`, but using signed `n`-bit _Integer_ `i` as a key. If `i` doe
 * s not fit into `n` bits, throws a range checking exception.
 */
@Serializable
@SerialName(TvmDictMayberefDictisetgetoptrefInst.MNEMONIC)
data class TvmDictMayberefDictisetgetoptrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMayberefInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTISETGETOPTREF"
    }
}

/**
 * A variant of both `DICTGETOPTREF` and `DICTSETGETREF` that sets the value corresponding to key `k` i
 * n dictionary `D` to `c^?` (if `c^?` is _Null_, then the key is deleted instead), and returns the old
 * value `~c^?` (if the key `k` was absent before, returns _Null_ instead).
 */
@Serializable
@SerialName(TvmDictMayberefDictsetgetoptrefInst.MNEMONIC)
data class TvmDictMayberefDictsetgetoptrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMayberefInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTSETGETOPTREF"
    }
}

/**
 * `DICTGETOPTREF`, but with `i` an unsigned `n`-bit integer. If the key `i` is out of range, also retu
 * rns _Null_.
 */
@Serializable
@SerialName(TvmDictMayberefDictugetoptrefInst.MNEMONIC)
data class TvmDictMayberefDictugetoptrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMayberefInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUGETOPTREF"
    }
}

/**
 * Similar to primitive `DICTSETGETOPTREF`, but using unsigned `n`-bit _Integer_ `i` as a key.
 */
@Serializable
@SerialName(TvmDictMayberefDictusetgetoptrefInst.MNEMONIC)
data class TvmDictMayberefDictusetgetoptrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMayberefInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUSETGETOPTREF"
    }
}

/**
 * Similar to `DICTMAX`, but computes the maximal key `i` under the assumption that all keys are big-en
 * dian signed `n`-bit integers. Notice that the key and value returned may differ from those computed
 * by `DICTMAX` and `DICTUMAX`.
 */
@Serializable
@SerialName(TvmDictMinDictimaxInst.MNEMONIC)
data class TvmDictMinDictimaxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIMAX"
    }
}

/**
 * Similar to `DICTIMAX`, but returns the only reference in the value.
 */
@Serializable
@SerialName(TvmDictMinDictimaxrefInst.MNEMONIC)
data class TvmDictMinDictimaxrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIMAXREF"
    }
}

/**
 * Similar to `DICTMIN`, but computes the minimal key `i` under the assumption that all keys are big-en
 * dian signed `n`-bit integers. Notice that the key and value returned may differ from those computed
 * by `DICTMIN` and `DICTUMIN`.
 */
@Serializable
@SerialName(TvmDictMinDictiminInst.MNEMONIC)
data class TvmDictMinDictiminInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIMIN"
    }
}

/**
 * Similar to `DICTIMIN`, but returns the only reference in the value.
 */
@Serializable
@SerialName(TvmDictMinDictiminrefInst.MNEMONIC)
data class TvmDictMinDictiminrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIMINREF"
    }
}

/**
 * Similar to `DICTREMMAX`, but computes the minimal key `i` under the assumption that all keys are big
 * -endian signed `n`-bit integers. Notice that the key and value returned may differ from those comput
 * ed by `DICTREMMAX` and `DICTUREMMAX`.
 */
@Serializable
@SerialName(TvmDictMinDictiremmaxInst.MNEMONIC)
data class TvmDictMinDictiremmaxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIREMMAX"
    }
}

/**
 * Similar to `DICTIREMMAX`, but returns the only reference in the value.
 */
@Serializable
@SerialName(TvmDictMinDictiremmaxrefInst.MNEMONIC)
data class TvmDictMinDictiremmaxrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIREMMAXREF"
    }
}

/**
 * Similar to `DICTREMMIN`, but computes the minimal key `i` under the assumption that all keys are big
 * -endian signed `n`-bit integers. Notice that the key and value returned may differ from those comput
 * ed by `DICTREMMIN` and `DICTUREMMIN`.
 */
@Serializable
@SerialName(TvmDictMinDictiremminInst.MNEMONIC)
data class TvmDictMinDictiremminInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIREMMIN"
    }
}

/**
 * Similar to `DICTIREMMIN`, but returns the only reference in the value.
 */
@Serializable
@SerialName(TvmDictMinDictiremminrefInst.MNEMONIC)
data class TvmDictMinDictiremminrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIREMMINREF"
    }
}

/**
 * Computes the maximal key `k` (represented by a _Slice_ with `n` data bits) in dictionary `D`, and re
 * turns `k` along with the associated value `x`.
 */
@Serializable
@SerialName(TvmDictMinDictmaxInst.MNEMONIC)
data class TvmDictMinDictmaxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTMAX"
    }
}

/**
 * Similar to `DICTMAX`, but returns the only reference in the value.
 */
@Serializable
@SerialName(TvmDictMinDictmaxrefInst.MNEMONIC)
data class TvmDictMinDictmaxrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTMAXREF"
    }
}

/**
 * Computes the minimal key `k` (represented by a _Slice_ with `n` data bits) in dictionary `D`, and re
 * turns `k` along with the associated value `x`.
 */
@Serializable
@SerialName(TvmDictMinDictminInst.MNEMONIC)
data class TvmDictMinDictminInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTMIN"
    }
}

/**
 * Similar to `DICTMIN`, but returns the only reference in the value as a _Cell_ `c`.
 */
@Serializable
@SerialName(TvmDictMinDictminrefInst.MNEMONIC)
data class TvmDictMinDictminrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTMINREF"
    }
}

/**
 * Computes the maximal key `k` (represented by a _Slice_ with `n` data bits) in dictionary `D`, remove
 * s `k` from the dictionary, and returns `k` along with the associated value `x` and the modified dict
 * ionary `D'`.
 */
@Serializable
@SerialName(TvmDictMinDictremmaxInst.MNEMONIC)
data class TvmDictMinDictremmaxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTREMMAX"
    }
}

/**
 * Similar to `DICTREMMAX`, but returns the only reference in the value as a _Cell_ `c`.
 */
@Serializable
@SerialName(TvmDictMinDictremmaxrefInst.MNEMONIC)
data class TvmDictMinDictremmaxrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTREMMAXREF"
    }
}

/**
 * Computes the minimal key `k` (represented by a _Slice_ with `n` data bits) in dictionary `D`, remove
 * s `k` from the dictionary, and returns `k` along with the associated value `x` and the modified dict
 * ionary `D'`.
 */
@Serializable
@SerialName(TvmDictMinDictremminInst.MNEMONIC)
data class TvmDictMinDictremminInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTREMMIN"
    }
}

/**
 * Similar to `DICTREMMIN`, but returns the only reference in the value as a _Cell_ `c`.
 */
@Serializable
@SerialName(TvmDictMinDictremminrefInst.MNEMONIC)
data class TvmDictMinDictremminrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTREMMINREF"
    }
}

/**
 * Similar to `DICTMAX`, but returns the key as an unsigned `n`-bit _Integer_ `i`.
 */
@Serializable
@SerialName(TvmDictMinDictumaxInst.MNEMONIC)
data class TvmDictMinDictumaxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUMAX"
    }
}

/**
 * Similar to `DICTUMAX`, but returns the only reference in the value.
 */
@Serializable
@SerialName(TvmDictMinDictumaxrefInst.MNEMONIC)
data class TvmDictMinDictumaxrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUMAXREF"
    }
}

/**
 * Similar to `DICTMIN`, but returns the key as an unsigned `n`-bit _Integer_ `i`.
 */
@Serializable
@SerialName(TvmDictMinDictuminInst.MNEMONIC)
data class TvmDictMinDictuminInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUMIN"
    }
}

/**
 * Similar to `DICTUMIN`, but returns the only reference in the value.
 */
@Serializable
@SerialName(TvmDictMinDictuminrefInst.MNEMONIC)
data class TvmDictMinDictuminrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUMINREF"
    }
}

/**
 * Similar to `DICTREMMAX`, but returns the key as an unsigned `n`-bit _Integer_ `i`.
 */
@Serializable
@SerialName(TvmDictMinDicturemmaxInst.MNEMONIC)
data class TvmDictMinDicturemmaxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUREMMAX"
    }
}

/**
 * Similar to `DICTUREMMAX`, but returns the only reference in the value.
 */
@Serializable
@SerialName(TvmDictMinDicturemmaxrefInst.MNEMONIC)
data class TvmDictMinDicturemmaxrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUREMMAXREF"
    }
}

/**
 * Similar to `DICTREMMIN`, but returns the key as an unsigned `n`-bit _Integer_ `i`.
 */
@Serializable
@SerialName(TvmDictMinDicturemminInst.MNEMONIC)
data class TvmDictMinDicturemminInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUREMMIN"
    }
}

/**
 * Similar to `DICTUREMMIN`, but returns the only reference in the value.
 */
@Serializable
@SerialName(TvmDictMinDicturemminrefInst.MNEMONIC)
data class TvmDictMinDicturemminrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictMinInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUREMMINREF"
    }
}

/**
 * Computes the minimal key `k'` in dictionary `D` that is lexicographically greater than `k`, and retu
 * rns `k'` (represented by a _Slice_) along with associated value `x'` (also represented by a _Slice_)
 * .
 */
@Serializable
@SerialName(TvmDictNextDictgetnextInst.MNEMONIC)
data class TvmDictNextDictgetnextInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictNextInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTGETNEXT"
    }
}

/**
 * Similar to `DICTGETNEXT`, but computes the minimal key `k'` that is lexicographically greater than o
 * r equal to `k`.
 */
@Serializable
@SerialName(TvmDictNextDictgetnexteqInst.MNEMONIC)
data class TvmDictNextDictgetnexteqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictNextInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTGETNEXTEQ"
    }
}

/**
 * Similar to `DICTGETNEXT`, but computes the maximal key `k'` lexicographically smaller than `k`.
 */
@Serializable
@SerialName(TvmDictNextDictgetprevInst.MNEMONIC)
data class TvmDictNextDictgetprevInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictNextInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTGETPREV"
    }
}

/**
 * Similar to `DICTGETPREV`, but computes the maximal key `k'` lexicographically smaller than or equal
 * to `k`.
 */
@Serializable
@SerialName(TvmDictNextDictgetpreveqInst.MNEMONIC)
data class TvmDictNextDictgetpreveqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictNextInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTGETPREVEQ"
    }
}

/**
 * Similar to `DICTGETNEXT`, but interprets all keys in dictionary `D` as big-endian signed `n`-bit int
 * egers, and computes the minimal key `i'` that is larger than _Integer_ `i` (which does not necessari
 * ly fit into `n` bits).
 */
@Serializable
@SerialName(TvmDictNextDictigetnextInst.MNEMONIC)
data class TvmDictNextDictigetnextInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictNextInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIGETNEXT"
    }
}

/**
 * Similar to `DICTGETNEXTEQ`, but interprets keys as signed `n`-bit integers.
 */
@Serializable
@SerialName(TvmDictNextDictigetnexteqInst.MNEMONIC)
data class TvmDictNextDictigetnexteqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictNextInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIGETNEXTEQ"
    }
}

/**
 * Similar to `DICTGETPREV`, but interprets keys as signed `n`-bit integers.
 */
@Serializable
@SerialName(TvmDictNextDictigetprevInst.MNEMONIC)
data class TvmDictNextDictigetprevInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictNextInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIGETPREV"
    }
}

/**
 * Similar to `DICTGETPREVEQ`, but interprets keys as signed `n`-bit integers.
 */
@Serializable
@SerialName(TvmDictNextDictigetpreveqInst.MNEMONIC)
data class TvmDictNextDictigetpreveqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictNextInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIGETPREVEQ"
    }
}

/**
 * Similar to `DICTGETNEXT`, but interprets all keys in dictionary `D` as big-endian unsigned `n`-bit i
 * ntegers, and computes the minimal key `i'` that is larger than _Integer_ `i` (which does not necessa
 * rily fit into `n` bits, and is not necessarily non-negative).
 */
@Serializable
@SerialName(TvmDictNextDictugetnextInst.MNEMONIC)
data class TvmDictNextDictugetnextInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictNextInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUGETNEXT"
    }
}

/**
 * Similar to `DICTGETNEXTEQ`, but interprets keys as unsigned `n`-bit integers.
 */
@Serializable
@SerialName(TvmDictNextDictugetnexteqInst.MNEMONIC)
data class TvmDictNextDictugetnexteqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictNextInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUGETNEXTEQ"
    }
}

/**
 * Similar to `DICTGETPREV`, but interprets keys as unsigned `n`-bit integers.
 */
@Serializable
@SerialName(TvmDictNextDictugetprevInst.MNEMONIC)
data class TvmDictNextDictugetprevInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictNextInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUGETPREV"
    }
}

/**
 * Similar to `DICTGETPREVEQ`, but interprets keys a unsigned `n`-bit integers.
 */
@Serializable
@SerialName(TvmDictNextDictugetpreveqInst.MNEMONIC)
data class TvmDictNextDictugetpreveqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictNextInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUGETPREVEQ"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictPrefixPfxdictaddInst.MNEMONIC)
data class TvmDictPrefixPfxdictaddInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictPrefixInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "PFXDICTADD"
    }
}

/**
 * Combines `[n] DICTPUSHCONST` for `0 <= n <= 1023` with `PFXDICTGETJMP`.
 */
@Serializable
@SerialName(TvmDictPrefixPfxdictconstgetjmpInst.MNEMONIC)
data class TvmDictPrefixPfxdictconstgetjmpInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmDictPrefixInst, TvmRefOperandLoader {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "PFXDICTCONSTGETJMP"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictPrefixPfxdictdelInst.MNEMONIC)
data class TvmDictPrefixPfxdictdelInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictPrefixInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "PFXDICTDEL"
    }
}

/**
 * Similar to `PFXDICTGET`, but throws a cell deserialization failure exception on failure.
 */
@Serializable
@SerialName(TvmDictPrefixPfxdictgetInst.MNEMONIC)
data class TvmDictPrefixPfxdictgetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictPrefixInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "PFXDICTGET"
    }
}

/**
 * Similar to `PFXDICTGETJMP`, but `EXEC`utes the continuation found instead of jumping to it. On failu
 * re, throws a cell deserialization exception.
 */
@Serializable
@SerialName(TvmDictPrefixPfxdictgetexecInst.MNEMONIC)
data class TvmDictPrefixPfxdictgetexecInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictPrefixInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "PFXDICTGETEXEC"
    }
}

/**
 * Similar to `PFXDICTGETQ`, but on success `BLESS`es the value `x` into a _Continuation_ and transfers
 * control to it as if by a `JMPX`. On failure, returns `s` unchanged and continues execution.
 */
@Serializable
@SerialName(TvmDictPrefixPfxdictgetjmpInst.MNEMONIC)
data class TvmDictPrefixPfxdictgetjmpInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictPrefixInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "PFXDICTGETJMP"
    }
}

/**
 * Looks up the unique prefix of _Slice_ `s` present in the prefix code dictionary represented by `Cell
 * ^?` `D` and `0 <= n <= 1023`. If found, the prefix of `s` is returned as `s'`, and the corresponding
 * value (also a _Slice_) as `x`. The remainder of `s` is returned as a _Slice_ `s''`. If no prefix of
 * `s` is a key in prefix code dictionary `D`, returns the unchanged `s` and a zero flag to indicate f
 * ailure.
 */
@Serializable
@SerialName(TvmDictPrefixPfxdictgetqInst.MNEMONIC)
data class TvmDictPrefixPfxdictgetqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictPrefixInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "PFXDICTGETQ"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictPrefixPfxdictreplaceInst.MNEMONIC)
data class TvmDictPrefixPfxdictreplaceInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictPrefixInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "PFXDICTREPLACE"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictPrefixPfxdictsetInst.MNEMONIC)
data class TvmDictPrefixPfxdictsetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictPrefixInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "PFXDICTSET"
    }
}

/**
 * Loads (parses) a dictionary `D` from _Slice_ `s`, and returns the remainder of `s` as `s'`. May be a
 * pplied to dictionaries or to values of arbitrary `(^Y)?` types.
 */
@Serializable
@SerialName(TvmDictSerialLddictInst.MNEMONIC)
data class TvmDictSerialLddictInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSerialInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDDICT"
    }
}

/**
 * A quiet version of `LDDICT`.
 */
@Serializable
@SerialName(TvmDictSerialLddictqInst.MNEMONIC)
data class TvmDictSerialLddictqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSerialInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDDICTQ"
    }
}

/**
 * Loads (parses) a (_Slice_-represented) dictionary `s'` from _Slice_ `s`, and returns the remainder o
 * f `s` as `s''`.
 * This is a ''split function'' for all `HashmapE(n,X)` dictionary types.
 */
@Serializable
@SerialName(TvmDictSerialLddictsInst.MNEMONIC)
data class TvmDictSerialLddictsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSerialInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LDDICTS"
    }
}

/**
 * Preloads a dictionary `D` from _Slice_ `s`.
 * Approximately equivalent to `LDDICT` `DROP`.
 */
@Serializable
@SerialName(TvmDictSerialPlddictInst.MNEMONIC)
data class TvmDictSerialPlddictInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSerialInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDDICT"
    }
}

/**
 * A quiet version of `PLDDICT`.
 */
@Serializable
@SerialName(TvmDictSerialPlddictqInst.MNEMONIC)
data class TvmDictSerialPlddictqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSerialInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDDICTQ"
    }
}

/**
 * Preloads a (_Slice_-represented) dictionary `s'` from _Slice_ `s`.
 * Approximately equivalent to `LDDICTS` `DROP`.
 */
@Serializable
@SerialName(TvmDictSerialPlddictsInst.MNEMONIC)
data class TvmDictSerialPlddictsInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSerialInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PLDDICTS"
    }
}

/**
 * Equivalent to `LDDICT` `NIP`.
 */
@Serializable
@SerialName(TvmDictSerialSkipdictInst.MNEMONIC)
data class TvmDictSerialSkipdictInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSerialInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "SKIPDICT"
    }
}

/**
 * Stores dictionary `D` into _Builder_ `b`, returing the resulting _Builder_ `b'`.
 * In other words, if `D` is a cell, performs `STONE` and `STREF`; if `D` is _Null_, performs `NIP` and
 * `STZERO`; otherwise throws a type checking exception.
 */
@Serializable
@SerialName(TvmDictSerialStdictInst.MNEMONIC)
data class TvmDictSerialStdictInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSerialInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "STDICT"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictaddbInst.MNEMONIC)
data class TvmDictSetBuilderDictaddbInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTADDB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictaddgetbInst.MNEMONIC)
data class TvmDictSetBuilderDictaddgetbInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTADDGETB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictiaddbInst.MNEMONIC)
data class TvmDictSetBuilderDictiaddbInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIADDB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictiaddgetbInst.MNEMONIC)
data class TvmDictSetBuilderDictiaddgetbInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIADDGETB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictireplacebInst.MNEMONIC)
data class TvmDictSetBuilderDictireplacebInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIREPLACEB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictireplacegetbInst.MNEMONIC)
data class TvmDictSetBuilderDictireplacegetbInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIREPLACEGETB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictisetbInst.MNEMONIC)
data class TvmDictSetBuilderDictisetbInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTISETB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictisetgetbInst.MNEMONIC)
data class TvmDictSetBuilderDictisetgetbInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTISETGETB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictreplacebInst.MNEMONIC)
data class TvmDictSetBuilderDictreplacebInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTREPLACEB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictreplacegetbInst.MNEMONIC)
data class TvmDictSetBuilderDictreplacegetbInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTREPLACEGETB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictsetbInst.MNEMONIC)
data class TvmDictSetBuilderDictsetbInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTSETB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictsetgetbInst.MNEMONIC)
data class TvmDictSetBuilderDictsetgetbInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTSETGETB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictuaddbInst.MNEMONIC)
data class TvmDictSetBuilderDictuaddbInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUADDB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictuaddgetbInst.MNEMONIC)
data class TvmDictSetBuilderDictuaddgetbInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUADDGETB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictureplacebInst.MNEMONIC)
data class TvmDictSetBuilderDictureplacebInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUREPLACEB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictureplacegetbInst.MNEMONIC)
data class TvmDictSetBuilderDictureplacegetbInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUREPLACEGETB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictusetbInst.MNEMONIC)
data class TvmDictSetBuilderDictusetbInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUSETB"
    }
}

/**
    
 */
@Serializable
@SerialName(TvmDictSetBuilderDictusetgetbInst.MNEMONIC)
data class TvmDictSetBuilderDictusetgetbInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetBuilderInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUSETGETB"
    }
}

/**
 * An _Add_ counterpart of `DICTSET`: sets the value associated with key `k` in dictionary `D` to `x`,
 * but only if it is not already present in `D`.
 */
@Serializable
@SerialName(TvmDictSetDictaddInst.MNEMONIC)
data class TvmDictSetDictaddInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTADD"
    }
}

/**
 * An _Add_ counterpart of `DICTSETGET`: sets the value associated with key `k` in dictionary `D` to `x
 * `, but only if key `k` is not already present in `D`. Otherwise, just returns the old value `y` with
 * out changing the dictionary.
 */
@Serializable
@SerialName(TvmDictSetDictaddgetInst.MNEMONIC)
data class TvmDictSetDictaddgetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTADDGET"
    }
}

/**
 * An _Add_ counterpart of `DICTSETGETREF`.
 */
@Serializable
@SerialName(TvmDictSetDictaddgetrefInst.MNEMONIC)
data class TvmDictSetDictaddgetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTADDGETREF"
    }
}

/**
 * An _Add_ counterpart of `DICTSETREF`.
 */
@Serializable
@SerialName(TvmDictSetDictaddrefInst.MNEMONIC)
data class TvmDictSetDictaddrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTADDREF"
    }
}

/**
 * `DICTADD`, but with `i` a signed `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictiaddInst.MNEMONIC)
data class TvmDictSetDictiaddInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIADD"
    }
}

/**
 * `DICTADDGET`, but with `i` a signed `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictiaddgetInst.MNEMONIC)
data class TvmDictSetDictiaddgetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIADDGET"
    }
}

/**
 * `DICTADDGETREF`, but with `i` a signed `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictiaddgetrefInst.MNEMONIC)
data class TvmDictSetDictiaddgetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIADDGETREF"
    }
}

/**
 * `DICTADDREF`, but with `i` a signed `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictiaddrefInst.MNEMONIC)
data class TvmDictSetDictiaddrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIADDREF"
    }
}

/**
 * `DICTREPLACE`, but with `i` a signed `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictireplaceInst.MNEMONIC)
data class TvmDictSetDictireplaceInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIREPLACE"
    }
}

/**
 * `DICTREPLACEGET`, but with `i` a signed `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictireplacegetInst.MNEMONIC)
data class TvmDictSetDictireplacegetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIREPLACEGET"
    }
}

/**
 * `DICTREPLACEGETREF`, but with `i` a signed `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictireplacegetrefInst.MNEMONIC)
data class TvmDictSetDictireplacegetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIREPLACEGETREF"
    }
}

/**
 * `DICTREPLACEREF`, but with `i` a signed `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictireplacerefInst.MNEMONIC)
data class TvmDictSetDictireplacerefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIREPLACEREF"
    }
}

/**
 * Similar to `DICTSET`, but with the key represented by a (big-endian) signed `n`-bit integer `i`. If
 * `i` does not fit into `n` bits, a range check exception is generated.
 */
@Serializable
@SerialName(TvmDictSetDictisetInst.MNEMONIC)
data class TvmDictSetDictisetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTISET"
    }
}

/**
 * `DICTISETGET`, but with `i` a signed `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictisetgetInst.MNEMONIC)
data class TvmDictSetDictisetgetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTISETGET"
    }
}

/**
 * `DICTISETGETREF`, but with `i` a signed `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictisetgetrefInst.MNEMONIC)
data class TvmDictSetDictisetgetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTISETGETREF"
    }
}

/**
 * Similar to `DICTSETREF`, but with the key a signed `n`-bit integer as in `DICTISET`.
 */
@Serializable
@SerialName(TvmDictSetDictisetrefInst.MNEMONIC)
data class TvmDictSetDictisetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTISETREF"
    }
}

/**
 * A _Replace_ operation, which is similar to `DICTSET`, but sets the value of key `k` in dictionary `D
 * ` to `x` only if the key `k` was already present in `D`.
 */
@Serializable
@SerialName(TvmDictSetDictreplaceInst.MNEMONIC)
data class TvmDictSetDictreplaceInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTREPLACE"
    }
}

/**
 * A _Replace_ counterpart of `DICTSETGET`: on success, also returns the old value associated with the
 * key in question.
 */
@Serializable
@SerialName(TvmDictSetDictreplacegetInst.MNEMONIC)
data class TvmDictSetDictreplacegetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTREPLACEGET"
    }
}

/**
 * A _Replace_ counterpart of `DICTSETGETREF`.
 */
@Serializable
@SerialName(TvmDictSetDictreplacegetrefInst.MNEMONIC)
data class TvmDictSetDictreplacegetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTREPLACEGETREF"
    }
}

/**
 * A _Replace_ counterpart of `DICTSETREF`.
 */
@Serializable
@SerialName(TvmDictSetDictreplacerefInst.MNEMONIC)
data class TvmDictSetDictreplacerefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTREPLACEREF"
    }
}

/**
 * Sets the value associated with `n`-bit key `k` (represented by a _Slice_ as in `DICTGET`) in diction
 * ary `D` (also represented by a _Slice_) to value `x` (again a _Slice_), and returns the resulting di
 * ctionary as `D'`.
 */
@Serializable
@SerialName(TvmDictSetDictsetInst.MNEMONIC)
data class TvmDictSetDictsetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTSET"
    }
}

/**
 * Combines `DICTSET` with `DICTGET`: it sets the value corresponding to key `k` to `x`, but also retur
 * ns the old value `y` associated with the key in question, if present.
 */
@Serializable
@SerialName(TvmDictSetDictsetgetInst.MNEMONIC)
data class TvmDictSetDictsetgetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTSETGET"
    }
}

/**
 * Combines `DICTSETREF` with `DICTGETREF` similarly to `DICTSETGET`.
 */
@Serializable
@SerialName(TvmDictSetDictsetgetrefInst.MNEMONIC)
data class TvmDictSetDictsetgetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTSETGETREF"
    }
}

/**
 * Similar to `DICTSET`, but with the value set to a reference to _Cell_ `c`.
 */
@Serializable
@SerialName(TvmDictSetDictsetrefInst.MNEMONIC)
data class TvmDictSetDictsetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTSETREF"
    }
}

/**
 * `DICTADD`, but with `i` an unsigned `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictuaddInst.MNEMONIC)
data class TvmDictSetDictuaddInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUADD"
    }
}

/**
 * `DICTADDGET`, but with `i` an unsigned `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictuaddgetInst.MNEMONIC)
data class TvmDictSetDictuaddgetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUADDGET"
    }
}

/**
 * `DICTADDGETREF`, but with `i` an unsigned `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictuaddgetrefInst.MNEMONIC)
data class TvmDictSetDictuaddgetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUADDGETREF"
    }
}

/**
 * `DICTADDREF`, but with `i` an unsigned `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictuaddrefInst.MNEMONIC)
data class TvmDictSetDictuaddrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUADDREF"
    }
}

/**
 * `DICTREPLACE`, but with `i` an unsigned `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictureplaceInst.MNEMONIC)
data class TvmDictSetDictureplaceInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUREPLACE"
    }
}

/**
 * `DICTREPLACEGET`, but with `i` an unsigned `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictureplacegetInst.MNEMONIC)
data class TvmDictSetDictureplacegetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUREPLACEGET"
    }
}

/**
 * `DICTREPLACEGETREF`, but with `i` an unsigned `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictureplacegetrefInst.MNEMONIC)
data class TvmDictSetDictureplacegetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUREPLACEGETREF"
    }
}

/**
 * `DICTREPLACEREF`, but with `i` an unsigned `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictureplacerefInst.MNEMONIC)
data class TvmDictSetDictureplacerefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUREPLACEREF"
    }
}

/**
 * Similar to `DICTISET`, but with `i` an _unsigned_ `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictusetInst.MNEMONIC)
data class TvmDictSetDictusetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUSET"
    }
}

/**
 * `DICTISETGET`, but with `i` an unsigned `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictusetgetInst.MNEMONIC)
data class TvmDictSetDictusetgetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUSETGET"
    }
}

/**
 * `DICTISETGETREF`, but with `i` an unsigned `n`-bit integer.
 */
@Serializable
@SerialName(TvmDictSetDictusetgetrefInst.MNEMONIC)
data class TvmDictSetDictusetgetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUSETGETREF"
    }
}

/**
 * Similar to `DICTISETREF`, but with `i` unsigned.
 */
@Serializable
@SerialName(TvmDictSetDictusetrefInst.MNEMONIC)
data class TvmDictSetDictusetrefInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSetInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUSETREF"
    }
}

/**
 * Similar to `DICTIGETJMP`, but with `EXECUTE` instead of `JMPX`.
 */
@Serializable
@SerialName(TvmDictSpecialDictigetexecInst.MNEMONIC)
data class TvmDictSpecialDictigetexecInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSpecialInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIGETEXEC"
    }
}

/**
 * A variant of `DICTIGETEXEC` that returns index `i` on failure.
 */
@Serializable
@SerialName(TvmDictSpecialDictigetexeczInst.MNEMONIC)
data class TvmDictSpecialDictigetexeczInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSpecialInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIGETEXECZ"
    }
}

/**
 * Similar to `DICTIGET`, but with `x` `BLESS`ed into a continuation with a subsequent `JMPX` to it on
 * success. On failure, does nothing. This is useful for implementing `switch`/`case` constructions.
 */
@Serializable
@SerialName(TvmDictSpecialDictigetjmpInst.MNEMONIC)
data class TvmDictSpecialDictigetjmpInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSpecialInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIGETJMP"
    }
}

/**
 * A variant of `DICTIGETJMP` that returns index `i` on failure.
 */
@Serializable
@SerialName(TvmDictSpecialDictigetjmpzInst.MNEMONIC)
data class TvmDictSpecialDictigetjmpzInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSpecialInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTIGETJMPZ"
    }
}

/**
 * Pushes a non-empty constant dictionary `D` (as a `Cell^?`) along with its key length `0 <= n <= 1023
 * `, stored as a part of the instruction. The dictionary itself is created from the first of remaining
 * references of the current continuation. In this way, the complete `DICTPUSHCONST` instruction can b
 * e obtained by first serializing `xF4A4_`, then the non-empty dictionary itself (one `1` bit and a ce
 * ll reference), and then the unsigned 10-bit integer `n` (as if by a `STU 10` instruction). An empty
 * dictionary can be pushed by a `NEWDICT` primitive instead.
 */
@Serializable
@SerialName(TvmDictSpecialDictpushconstInst.MNEMONIC)
data class TvmDictSpecialDictpushconstInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmDictSpecialInst, TvmRefOperandLoader {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "DICTPUSHCONST"
    }
}

/**
 * Similar to `DICTUGETJMP`, but with `EXECUTE` instead of `JMPX`.
 */
@Serializable
@SerialName(TvmDictSpecialDictugetexecInst.MNEMONIC)
data class TvmDictSpecialDictugetexecInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSpecialInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUGETEXEC"
    }
}

/**
 * A variant of `DICTUGETEXEC` that returns index `i` on failure.
 */
@Serializable
@SerialName(TvmDictSpecialDictugetexeczInst.MNEMONIC)
data class TvmDictSpecialDictugetexeczInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSpecialInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUGETEXECZ"
    }
}

/**
 * Similar to `DICTIGETJMP`, but performs `DICTUGET` instead of `DICTIGET`.
 */
@Serializable
@SerialName(TvmDictSpecialDictugetjmpInst.MNEMONIC)
data class TvmDictSpecialDictugetjmpInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSpecialInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUGETJMP"
    }
}

/**
 * A variant of `DICTUGETJMP` that returns index `i` on failure.
 */
@Serializable
@SerialName(TvmDictSpecialDictugetjmpzInst.MNEMONIC)
data class TvmDictSpecialDictugetjmpzInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSpecialInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "DICTUGETJMPZ"
    }
}

/**
 * Constructs a subdictionary consisting of all keys beginning with prefix `k` (represented by a _Slice
 * _, the first `0 <= l <= n <= 1023` data bits of which are used as a key) of length `l` in dictionary
 * `D` of type `HashmapE(n,X)` with `n`-bit keys. On success, returns the new subdictionary of the sam
 * e type `HashmapE(n,X)` as a _Slice_ `D'`.
 */
@Serializable
@SerialName(TvmDictSubSubdictgetInst.MNEMONIC)
data class TvmDictSubSubdictgetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSubInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "SUBDICTGET"
    }
}

/**
 * Variant of `SUBDICTGET` with the prefix represented by a signed big-endian `l`-bit _Integer_ `x`, wh
 * ere necessarily `l <= 257`.
 */
@Serializable
@SerialName(TvmDictSubSubdictigetInst.MNEMONIC)
data class TvmDictSubSubdictigetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSubInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "SUBDICTIGET"
    }
}

/**
 * Variant of `SUBDICTRPGET` with the prefix represented by a signed big-endian `l`-bit _Integer_ `x`,
 * where necessarily `l <= 257`.
 */
@Serializable
@SerialName(TvmDictSubSubdictirpgetInst.MNEMONIC)
data class TvmDictSubSubdictirpgetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSubInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "SUBDICTIRPGET"
    }
}

/**
 * Similar to `SUBDICTGET`, but removes the common prefix `k` from all keys of the new dictionary `D'`,
 * which becomes of type `HashmapE(n-l,X)`.
 */
@Serializable
@SerialName(TvmDictSubSubdictrpgetInst.MNEMONIC)
data class TvmDictSubSubdictrpgetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSubInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "SUBDICTRPGET"
    }
}

/**
 * Variant of `SUBDICTGET` with the prefix represented by an unsigned big-endian `l`-bit _Integer_ `x`,
 * where necessarily `l <= 256`.
 */
@Serializable
@SerialName(TvmDictSubSubdictugetInst.MNEMONIC)
data class TvmDictSubSubdictugetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSubInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "SUBDICTUGET"
    }
}

/**
 * Variant of `SUBDICTRPGET` with the prefix represented by an unsigned big-endian `l`-bit _Integer_ `x
 * `, where necessarily `l <= 256`.
 */
@Serializable
@SerialName(TvmDictSubSubdicturpgetInst.MNEMONIC)
data class TvmDictSubSubdicturpgetInst(
    override val location: TvmInstLocation,
): TvmInst, TvmDictSubInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmSimpleGas

    companion object {
        const val MNEMONIC = "SUBDICTURPGET"
    }
}

/**
 * For `0 <= n < 2^11`, an encoding of `[n] THROW` for larger values of `n`.
 */
@Serializable
@SerialName(TvmExceptionsThrowInst.MNEMONIC)
data class TvmExceptionsThrowInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmExceptionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 84)

    companion object {
        const val MNEMONIC = "THROW"
    }
}

/**
 * Throws exception `0 <= n <= 63` with parameter zero.
 * In other words, it transfers control to the continuation in `c2`, pushing `0` and `n` into its stack
 * , and discarding the old stack altogether.
 */
@Serializable
@SerialName(TvmExceptionsThrowShortInst.MNEMONIC)
data class TvmExceptionsThrowShortInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmExceptionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 76)

    companion object {
        const val MNEMONIC = "THROW_SHORT"
    }
}

/**
 * Throws exception `0 <= n < 2^16` with parameter zero.
 * Approximately equivalent to `ZERO` `SWAP` `THROWARGANY`.
 */
@Serializable
@SerialName(TvmExceptionsThrowanyInst.MNEMONIC)
data class TvmExceptionsThrowanyInst(
    override val location: TvmInstLocation,
): TvmInst, TvmExceptionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 76)

    companion object {
        const val MNEMONIC = "THROWANY"
    }
}

/**
 * Throws exception `0 <= n < 2^16` with parameter zero only if `f!=0`.
 */
@Serializable
@SerialName(TvmExceptionsThrowanyifInst.MNEMONIC)
data class TvmExceptionsThrowanyifInst(
    override val location: TvmInstLocation,
): TvmInst, TvmExceptionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/76")

    companion object {
        const val MNEMONIC = "THROWANYIF"
    }
}

/**
 * Throws exception `0 <= n<2^16` with parameter zero only if `f=0`.
 */
@Serializable
@SerialName(TvmExceptionsThrowanyifnotInst.MNEMONIC)
data class TvmExceptionsThrowanyifnotInst(
    override val location: TvmInstLocation,
): TvmInst, TvmExceptionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/76")

    companion object {
        const val MNEMONIC = "THROWANYIFNOT"
    }
}

/**
 * Throws exception `0 <= n <  2^11` with parameter `x`, by copying `x` and `n` into the stack of `c2`
 * and transferring control to `c2`.
 */
@Serializable
@SerialName(TvmExceptionsThrowargInst.MNEMONIC)
data class TvmExceptionsThrowargInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmExceptionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 84)

    companion object {
        const val MNEMONIC = "THROWARG"
    }
}

/**
 * Throws exception `0 <= n < 2^16` with parameter `x`, transferring control to the continuation in `c2
 * `.
 * Approximately equivalent to `c2 PUSHCTR` `2 JMPXARGS`.
 */
@Serializable
@SerialName(TvmExceptionsThrowarganyInst.MNEMONIC)
data class TvmExceptionsThrowarganyInst(
    override val location: TvmInstLocation,
): TvmInst, TvmExceptionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 76)

    companion object {
        const val MNEMONIC = "THROWARGANY"
    }
}

/**
 * Throws exception `0 <= n<2^16` with parameter `x` only if `f!=0`.
 */
@Serializable
@SerialName(TvmExceptionsThrowarganyifInst.MNEMONIC)
data class TvmExceptionsThrowarganyifInst(
    override val location: TvmInstLocation,
): TvmInst, TvmExceptionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/76")

    companion object {
        const val MNEMONIC = "THROWARGANYIF"
    }
}

/**
 * Throws exception `0 <= n<2^16` with parameter `x` only if `f=0`.
 */
@Serializable
@SerialName(TvmExceptionsThrowarganyifnotInst.MNEMONIC)
data class TvmExceptionsThrowarganyifnotInst(
    override val location: TvmInstLocation,
): TvmInst, TvmExceptionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/76")

    companion object {
        const val MNEMONIC = "THROWARGANYIFNOT"
    }
}

/**
 * Throws exception `0 <= nn < 2^11` with parameter `x` only if integer `f!=0`.
 */
@Serializable
@SerialName(TvmExceptionsThrowargifInst.MNEMONIC)
data class TvmExceptionsThrowargifInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmExceptionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "34/84")

    companion object {
        const val MNEMONIC = "THROWARGIF"
    }
}

/**
 * Throws exception `0 <= n < 2^11` with parameter `x` only if integer `f=0`.
 */
@Serializable
@SerialName(TvmExceptionsThrowargifnotInst.MNEMONIC)
data class TvmExceptionsThrowargifnotInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmExceptionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "34/84")

    companion object {
        const val MNEMONIC = "THROWARGIFNOT"
    }
}

/**
 * For `0 <= n < 2^11`, an encoding of `[n] THROWIF` for larger values of `n`.
 */
@Serializable
@SerialName(TvmExceptionsThrowifInst.MNEMONIC)
data class TvmExceptionsThrowifInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmExceptionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "34/84")

    companion object {
        const val MNEMONIC = "THROWIF"
    }
}

/**
 * Throws exception `0 <= n <= 63` with  parameter zero only if integer `f!=0`.
 */
@Serializable
@SerialName(TvmExceptionsThrowifShortInst.MNEMONIC)
data class TvmExceptionsThrowifShortInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmExceptionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/76")

    companion object {
        const val MNEMONIC = "THROWIF_SHORT"
    }
}

/**
 * For `0 <= n < 2^11`, an encoding of `[n] THROWIFNOT` for larger values of `n`.
 */
@Serializable
@SerialName(TvmExceptionsThrowifnotInst.MNEMONIC)
data class TvmExceptionsThrowifnotInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmExceptionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "34/84")

    companion object {
        const val MNEMONIC = "THROWIFNOT"
    }
}

/**
 * Throws exception `0 <= n <= 63` with parameter zero only if integer `f=0`.
 */
@Serializable
@SerialName(TvmExceptionsThrowifnotShortInst.MNEMONIC)
data class TvmExceptionsThrowifnotShortInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmExceptionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26/76")

    companion object {
        const val MNEMONIC = "THROWIFNOT_SHORT"
    }
}

/**
 * Sets `c2` to `c'`, first saving the old value of `c2` both into the savelist of `c'` and into the sa
 * velist of the current continuation, which is stored into `c.c0` and `c'.c0`. Then runs `c` similarly
 * to `EXECUTE`. If `c` does not throw any exceptions, the original value of `c2` is automatically res
 * tored on return from `c`. If an exception occurs, the execution is transferred to `c'`, but the orig
 * inal value of `c2` is restored in the process, so that `c'` can re-throw the exception by `THROWANY`
 * if it cannot handle it by itself.
 */
@Serializable
@SerialName(TvmExceptionsTryInst.MNEMONIC)
data class TvmExceptionsTryInst(
    override val location: TvmInstLocation,
): TvmInst, TvmExceptionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "TRY"
    }
}

/**
 * Similar to `TRY`, but with `[p] [r] CALLXARGS` internally used instead of `EXECUTE`.
 * In this way, all but the top `0 <= p <= 15` stack elements will be saved into current continuation's
 * stack, and then restored upon return from either `c` or `c'`, with the top `0 <= r <= 15` values of
 * the resulting stack of `c` or `c'` copied as return values.
 */
@Serializable
@SerialName(TvmExceptionsTryargsInst.MNEMONIC)
data class TvmExceptionsTryargsInst(
    override val location: TvmInstLocation,
    val p: Int, // uint
    val r: Int, // uint
): TvmInst, TvmExceptionsInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "TRYARGS"
    }
}

/**
 * Does nothing.
 */
@Serializable
@SerialName(TvmStackBasicNopInst.MNEMONIC)
data class TvmStackBasicNopInst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "NOP"
    }
}

/**
 * Pops the old `s0` value into the old `s[i]`.
 */
@Serializable
@SerialName(TvmStackBasicPopInst.MNEMONIC)
data class TvmStackBasicPopInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmStackBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "POP"
    }
}

/**
 * Pushes a copy of the old `s[i]` into the stack.
 */
@Serializable
@SerialName(TvmStackBasicPushInst.MNEMONIC)
data class TvmStackBasicPushInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmStackBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "PUSH"
    }
}

/**
 * Interchanges `s0` with `s[i]`, `1 <= i <= 15`.
 */
@Serializable
@SerialName(TvmStackBasicXchg0iInst.MNEMONIC)
data class TvmStackBasicXchg0iInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmStackBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "XCHG_0I"
    }
}

/**
 * Interchanges `s0` with `s[ii]`, `0 <= ii <= 255`.
 */
@Serializable
@SerialName(TvmStackBasicXchg0iLongInst.MNEMONIC)
data class TvmStackBasicXchg0iLongInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmStackBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "XCHG_0I_LONG"
    }
}

/**
 * Interchanges `s1` with `s[i]`, `2 <= i <= 15`.
 */
@Serializable
@SerialName(TvmStackBasicXchg1iInst.MNEMONIC)
data class TvmStackBasicXchg1iInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmStackBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "XCHG_1I"
    }
}

/**
 * Interchanges `s[i]` with `s[j]`, `1 <= i < j <= 15`.
 */
@Serializable
@SerialName(TvmStackBasicXchgIjInst.MNEMONIC)
data class TvmStackBasicXchgIjInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
): TvmInst, TvmStackBasicInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "XCHG_IJ"
    }
}

/**
 * Drops `i` stack elements under the top `j` elements.
 * `1 <= i <= 15`, `0 <= j <= 15`
 * Equivalent to `[i+j] 0 REVERSE` `[i] BLKDROP` `[j] 0 REVERSE`.
 */
@Serializable
@SerialName(TvmStackComplexBlkdrop2Inst.MNEMONIC)
data class TvmStackComplexBlkdrop2Inst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BLKDROP2"
    }
}

/**
 * Equivalent to `DROP` performed `i` times.
 */
@Serializable
@SerialName(TvmStackComplexBlkdropInst.MNEMONIC)
data class TvmStackComplexBlkdropInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BLKDROP"
    }
}

/**
 * Equivalent to `PUSH s(j)` performed `i` times.
 * `1 <= i <= 15`, `0 <= j <= 15`.
 */
@Serializable
@SerialName(TvmStackComplexBlkpushInst.MNEMONIC)
data class TvmStackComplexBlkpushInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BLKPUSH"
    }
}

/**
 * Permutes two blocks `s[j+i+1] ... s[j+1]` and `s[j] ... s0`.
 * `0 <= i,j <= 15`
 * Equivalent to `[i+1] [j+1] REVERSE` `[j+1] 0 REVERSE` `[i+j+2] 0 REVERSE`.
 */
@Serializable
@SerialName(TvmStackComplexBlkswapInst.MNEMONIC)
data class TvmStackComplexBlkswapInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "BLKSWAP"
    }
}

/**
 * Pops integers `i`,`j` from the stack, then performs `[i] [j] BLKSWAP`.
 */
@Serializable
@SerialName(TvmStackComplexBlkswxInst.MNEMONIC)
data class TvmStackComplexBlkswxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "BLKSWX"
    }
}

/**
 * Pops integer `i` from the stack, then checks whether there are at least `i` elements, generating a s
 * tack underflow exception otherwise.
 */
@Serializable
@SerialName(TvmStackComplexChkdepthInst.MNEMONIC)
data class TvmStackComplexChkdepthInst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "18/58")

    companion object {
        const val MNEMONIC = "CHKDEPTH"
    }
}

/**
 * Pushes the current depth of the stack.
 */
@Serializable
@SerialName(TvmStackComplexDepthInst.MNEMONIC)
data class TvmStackComplexDepthInst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "DEPTH"
    }
}

/**
 * Equivalent to `DROP` `DROP`.
 */
@Serializable
@SerialName(TvmStackComplexDrop2Inst.MNEMONIC)
data class TvmStackComplexDrop2Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "DROP2"
    }
}

/**
 * Pops integer `i` from the stack, then performs `[i] BLKDROP`.
 */
@Serializable
@SerialName(TvmStackComplexDropxInst.MNEMONIC)
data class TvmStackComplexDropxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "DROPX"
    }
}

/**
 * Equivalent to `s1 s0 PUSH2`.
 */
@Serializable
@SerialName(TvmStackComplexDup2Inst.MNEMONIC)
data class TvmStackComplexDup2Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "DUP2"
    }
}

/**
 * Pops integer `i` from the stack, then performs `[i] 1 BLKSWAP`.
 */
@Serializable
@SerialName(TvmStackComplexMinusrollxInst.MNEMONIC)
data class TvmStackComplexMinusrollxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "-ROLLX"
    }
}

/**
 * Pops integer `i` from the stack, then removes all but the top `i` elements.
 */
@Serializable
@SerialName(TvmStackComplexOnlytopxInst.MNEMONIC)
data class TvmStackComplexOnlytopxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "ONLYTOPX"
    }
}

/**
 * Pops integer `i` from the stack, then leaves only the bottom `i` elements. Approximately equivalent
 * to `DEPTH` `SWAP` `SUB` `DROPX`.
 */
@Serializable
@SerialName(TvmStackComplexOnlyxInst.MNEMONIC)
data class TvmStackComplexOnlyxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "ONLYX"
    }
}

/**
 * Equivalent to `s3 s2 PUSH2`.
 */
@Serializable
@SerialName(TvmStackComplexOver2Inst.MNEMONIC)
data class TvmStackComplexOver2Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "OVER2"
    }
}

/**
 * Pops integer `i` from the stack, then performs `s[i] PUSH`.
 */
@Serializable
@SerialName(TvmStackComplexPickInst.MNEMONIC)
data class TvmStackComplexPickInst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "PICK"
    }
}

/**
 * Pops the old `s0` value into the old `s[ii]`.
 * `0 <= ii <= 255`
 */
@Serializable
@SerialName(TvmStackComplexPopLongInst.MNEMONIC)
data class TvmStackComplexPopLongInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "POP_LONG"
    }
}

/**
 * Equivalent to `s[i] PUSH` `SWAP` `s[j] s[k-1] PUXC`.
 */
@Serializable
@SerialName(TvmStackComplexPu2xcInst.MNEMONIC)
data class TvmStackComplexPu2xcInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
    val k: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "PU2XC"
    }
}

/**
 * Equivalent to `s[i] PUSH` `s[j+1] PUSH`.
 */
@Serializable
@SerialName(TvmStackComplexPush2Inst.MNEMONIC)
data class TvmStackComplexPush2Inst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PUSH2"
    }
}

/**
 * Equivalent to `s[i] PUSH` `s[j+1] s[k+1] PUSH2`.
 */
@Serializable
@SerialName(TvmStackComplexPush3Inst.MNEMONIC)
data class TvmStackComplexPush3Inst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
    val k: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "PUSH3"
    }
}

/**
 * Pushes a copy of the old `s[ii]` into the stack.
 * `0 <= ii <= 255`
 */
@Serializable
@SerialName(TvmStackComplexPushLongInst.MNEMONIC)
data class TvmStackComplexPushLongInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PUSH_LONG"
    }
}

/**
 * Equivalent to `s[i] PUSH` `s2 XCHG0` `s[j] s[k] XCHG2`.
 */
@Serializable
@SerialName(TvmStackComplexPuxc2Inst.MNEMONIC)
data class TvmStackComplexPuxc2Inst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
    val k: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "PUXC2"
    }
}

/**
 * Equivalent to `s[i] PUSH` `SWAP` `s[j] XCHG0`.
 */
@Serializable
@SerialName(TvmStackComplexPuxcInst.MNEMONIC)
data class TvmStackComplexPuxcInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "PUXC"
    }
}

/**
 * Equivalent to `s[i] s[j-1] PUXC` `s[k] PUSH`.
 */
@Serializable
@SerialName(TvmStackComplexPuxcpuInst.MNEMONIC)
data class TvmStackComplexPuxcpuInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
    val k: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "PUXCPU"
    }
}

/**
 * Reverses the order of `s[j+i+1] ... s[j]`.
 */
@Serializable
@SerialName(TvmStackComplexReverseInst.MNEMONIC)
data class TvmStackComplexReverseInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "REVERSE"
    }
}

/**
 * Pops integers `i`,`j` from the stack, then performs `[i] [j] REVERSE`.
 */
@Serializable
@SerialName(TvmStackComplexRevxInst.MNEMONIC)
data class TvmStackComplexRevxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "REVX"
    }
}

/**
 * Pops integer `i` from the stack, then performs `1 [i] BLKSWAP`.
 */
@Serializable
@SerialName(TvmStackComplexRollxInst.MNEMONIC)
data class TvmStackComplexRollxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "ROLLX"
    }
}

/**
 * Equivalent to `1 2 BLKSWAP` or to `s2 s1 XCHG2`.
 */
@Serializable
@SerialName(TvmStackComplexRotInst.MNEMONIC)
data class TvmStackComplexRotInst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "ROT"
    }
}

/**
 * Equivalent to `2 1 BLKSWAP` or to `s2 s2 XCHG2`.
 */
@Serializable
@SerialName(TvmStackComplexRotrevInst.MNEMONIC)
data class TvmStackComplexRotrevInst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "ROTREV"
    }
}

/**
 * Equivalent to `2 2 BLKSWAP` or to `s3 s2 XCHG2`.
 */
@Serializable
@SerialName(TvmStackComplexSwap2Inst.MNEMONIC)
data class TvmStackComplexSwap2Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "SWAP2"
    }
}

/**
 * Equivalent to `SWAP` `OVER` or to `s1 s1 XCPU`.
 */
@Serializable
@SerialName(TvmStackComplexTuckInst.MNEMONIC)
data class TvmStackComplexTuckInst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "TUCK"
    }
}

/**
 * Equivalent to `s[i] s[j] XCHG2` `s[k] PUSH`.
 */
@Serializable
@SerialName(TvmStackComplexXc2puInst.MNEMONIC)
data class TvmStackComplexXc2puInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
    val k: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "XC2PU"
    }
}

/**
 * Equivalent to `s1 s[i] XCHG` `s[j] XCHG0`.
 */
@Serializable
@SerialName(TvmStackComplexXchg2Inst.MNEMONIC)
data class TvmStackComplexXchg2Inst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "XCHG2"
    }
}

/**
 * Long form of `XCHG3`.
 */
@Serializable
@SerialName(TvmStackComplexXchg3AltInst.MNEMONIC)
data class TvmStackComplexXchg3AltInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
    val k: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "XCHG3_ALT"
    }
}

/**
 * Equivalent to `s2 s[i] XCHG` `s1 s[j] XCHG` `s[k] XCHG0`.
 */
@Serializable
@SerialName(TvmStackComplexXchg3Inst.MNEMONIC)
data class TvmStackComplexXchg3Inst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
    val k: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "XCHG3"
    }
}

/**
 * Pops integer `i` from the stack, then performs `s[i] XCHG`.
 */
@Serializable
@SerialName(TvmStackComplexXchgxInst.MNEMONIC)
data class TvmStackComplexXchgxInst(
    override val location: TvmInstLocation,
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "XCHGX"
    }
}

/**
 * Equivalent to `s[i] XCHG0` `s[j] s[k] PUSH2`.
 */
@Serializable
@SerialName(TvmStackComplexXcpu2Inst.MNEMONIC)
data class TvmStackComplexXcpu2Inst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
    val k: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "XCPU2"
    }
}

/**
 * Equivalent to `s[i] XCHG0` `s[j] PUSH`.
 */
@Serializable
@SerialName(TvmStackComplexXcpuInst.MNEMONIC)
data class TvmStackComplexXcpuInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "XCPU"
    }
}

/**
 * Equivalent to `s1 s[i] XCHG` `s[j] s[k-1] PUXC`.
 */
@Serializable
@SerialName(TvmStackComplexXcpuxcInst.MNEMONIC)
data class TvmStackComplexXcpuxcInst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
    val k: Int, // uint
): TvmInst, TvmStackComplexInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 34)

    companion object {
        const val MNEMONIC = "XCPUXC"
    }
}

/**
 * Unpacks a _Tuple_ `t=(x_1,...,x_m)` and returns its length `m`, but only if `m <= n <= 15`. Otherwis
 * e throws a type check exception.
 */
@Serializable
@SerialName(TvmTupleExplodeInst.MNEMONIC)
data class TvmTupleExplodeInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+m")

    companion object {
        const val MNEMONIC = "EXPLODE"
    }
}

/**
 * Similar to `n EXPLODE`, but with `0 <= n <= 255` taken from the stack.
 */
@Serializable
@SerialName(TvmTupleExplodevarInst.MNEMONIC)
data class TvmTupleExplodevarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+m")

    companion object {
        const val MNEMONIC = "EXPLODEVAR"
    }
}

/**
 * Recovers `x=(t_{i+1})_{j+1}` for `0 <= i,j <= 3`.
 * Equivalent to `[i] INDEX` `[j] INDEX`.
 */
@Serializable
@SerialName(TvmTupleIndex2Inst.MNEMONIC)
data class TvmTupleIndex2Inst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "INDEX2"
    }
}

/**
 * Recovers `x=t_{i+1}_{j+1}_{k+1}`.
 * `0 <= i,j,k <= 3`
 * Equivalent to `[i] [j] INDEX2` `[k] INDEX`.
 */
@Serializable
@SerialName(TvmTupleIndex3Inst.MNEMONIC)
data class TvmTupleIndex3Inst(
    override val location: TvmInstLocation,
    val i: Int, // uint
    val j: Int, // uint
    val k: Int, // uint
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "INDEX3"
    }
}

/**
 * Returns the `k`-th element of a _Tuple_ `t`.
 * `0 <= k <= 15`.
 */
@Serializable
@SerialName(TvmTupleIndexInst.MNEMONIC)
data class TvmTupleIndexInst(
    override val location: TvmInstLocation,
    val k: Int, // uint
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "INDEX"
    }
}

/**
 * Returns the `k`-th element of a _Tuple_ `t`, where `0 <= k <= 15`. In other words, returns `x_{k+1}`
 * if `t=(x_1,...,x_n)`. If `k>=n`, or if `t` is _Null_, returns a _Null_ instead of `x`.
 */
@Serializable
@SerialName(TvmTupleIndexqInst.MNEMONIC)
data class TvmTupleIndexqInst(
    override val location: TvmInstLocation,
    val k: Int, // uint
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "INDEXQ"
    }
}

/**
 * Similar to `k INDEX`, but with `0 <= k <= 254` taken from the stack.
 */
@Serializable
@SerialName(TvmTupleIndexvarInst.MNEMONIC)
data class TvmTupleIndexvarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "INDEXVAR"
    }
}

/**
 * Similar to `n INDEXQ`, but with `0 <= k <= 254` taken from the stack.
 */
@Serializable
@SerialName(TvmTupleIndexvarqInst.MNEMONIC)
data class TvmTupleIndexvarqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "INDEXVARQ"
    }
}

/**
 * Checks whether `x` is a _Null_, and returns `-1` or `0` accordingly.
 */
@Serializable
@SerialName(TvmTupleIsnullInst.MNEMONIC)
data class TvmTupleIsnullInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "ISNULL"
    }
}

/**
 * Returns `-1` or `0` depending on whether `t` is a _Tuple_.
 */
@Serializable
@SerialName(TvmTupleIstupleInst.MNEMONIC)
data class TvmTupleIstupleInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "ISTUPLE"
    }
}

/**
 * Returns the last element of a non-empty _Tuple_ `t`.
 */
@Serializable
@SerialName(TvmTupleLastInst.MNEMONIC)
data class TvmTupleLastInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "LAST"
    }
}

/**
 * Pushes the only value of type _Null_.
 */
@Serializable
@SerialName(TvmTupleNullInst.MNEMONIC)
data class TvmTupleNullInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 18)

    companion object {
        const val MNEMONIC = "NULL"
    }
}

/**
 * Pushes two nulls under the second stack entry from the top, but only if the topmost _Integer_ `y` is
 * non-zero.
 * Equivalent to `NULLROTRIF` `NULLROTRIF`.
 */
@Serializable
@SerialName(TvmTupleNullrotrif2Inst.MNEMONIC)
data class TvmTupleNullrotrif2Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "NULLROTRIF2"
    }
}

/**
 * Pushes a _Null_ under the second stack entry from the top, but only if the topmost _Integer_ `y` is
 * non-zero.
 */
@Serializable
@SerialName(TvmTupleNullrotrifInst.MNEMONIC)
data class TvmTupleNullrotrifInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "NULLROTRIF"
    }
}

/**
 * Pushes two nulls under the second stack entry from the top, but only if the topmost _Integer_ `y` is
 * zero.
 * Equivalent to `NULLROTRIFNOT` `NULLROTRIFNOT`.
 */
@Serializable
@SerialName(TvmTupleNullrotrifnot2Inst.MNEMONIC)
data class TvmTupleNullrotrifnot2Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "NULLROTRIFNOT2"
    }
}

/**
 * Pushes a _Null_ under the second stack entry from the top, but only if the topmost _Integer_ `y` is
 * zero. May be used for stack alignment after quiet primitives such as `LDUXQ`.
 */
@Serializable
@SerialName(TvmTupleNullrotrifnotInst.MNEMONIC)
data class TvmTupleNullrotrifnotInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "NULLROTRIFNOT"
    }
}

/**
 * Pushes two nulls under the topmost _Integer_ `x`, but only if `x!=0`.
 * Equivalent to `NULLSWAPIF` `NULLSWAPIF`.
 */
@Serializable
@SerialName(TvmTupleNullswapif2Inst.MNEMONIC)
data class TvmTupleNullswapif2Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "NULLSWAPIF2"
    }
}

/**
 * Pushes a _Null_ under the topmost _Integer_ `x`, but only if `x!=0`.
 */
@Serializable
@SerialName(TvmTupleNullswapifInst.MNEMONIC)
data class TvmTupleNullswapifInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "NULLSWAPIF"
    }
}

/**
 * Pushes two nulls under the topmost _Integer_ `x`, but only if `x=0`.
 * Equivalent to `NULLSWAPIFNOT` `NULLSWAPIFNOT`.
 */
@Serializable
@SerialName(TvmTupleNullswapifnot2Inst.MNEMONIC)
data class TvmTupleNullswapifnot2Inst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "NULLSWAPIFNOT2"
    }
}

/**
 * Pushes a _Null_ under the topmost _Integer_ `x`, but only if `x=0`. May be used for stack alignment
 * after quiet primitives such as `PLDUXQ`.
 */
@Serializable
@SerialName(TvmTupleNullswapifnotInst.MNEMONIC)
data class TvmTupleNullswapifnotInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "NULLSWAPIFNOT"
    }
}

/**
 * Similar to `TLEN`, but returns `-1` if `t` is not a _Tuple_.
 */
@Serializable
@SerialName(TvmTupleQtlenInst.MNEMONIC)
data class TvmTupleQtlenInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "QTLEN"
    }
}

/**
 * Computes _Tuple_ `t'` that differs from `t` only at position `t'_{k+1}`, which is set to `x`.
 * `0 <= k <= 15`
 * If `k >= |t|`, throws a range check exception.
 */
@Serializable
@SerialName(TvmTupleSetindexInst.MNEMONIC)
data class TvmTupleSetindexInst(
    override val location: TvmInstLocation,
    val k: Int, // uint
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+|t|")

    companion object {
        const val MNEMONIC = "SETINDEX"
    }
}

/**
 * Sets the `k`-th component of _Tuple_ `t` to `x`, where `0 <= k < 16`, and returns the resulting _Tup
 * le_ `t'`.
 * If `|t| <= k`, first extends the original _Tuple_ to length `n'=k+1` by setting all new components t
 * o _Null_. If the original value of `t` is _Null_, treats it as an empty _Tuple_. If `t` is not _Null
 * _ or _Tuple_, throws an exception. If `x` is _Null_ and either `|t| <= k` or `t` is _Null_, then alw
 * ays returns `t'=t` (and does not consume tuple creation gas).
 */
@Serializable
@SerialName(TvmTupleSetindexqInst.MNEMONIC)
data class TvmTupleSetindexqInst(
    override val location: TvmInstLocation,
    val k: Int, // uint
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+|t'|")

    companion object {
        const val MNEMONIC = "SETINDEXQ"
    }
}

/**
 * Similar to `k SETINDEX`, but with `0 <= k <= 254` taken from the stack.
 */
@Serializable
@SerialName(TvmTupleSetindexvarInst.MNEMONIC)
data class TvmTupleSetindexvarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+|t'|")

    companion object {
        const val MNEMONIC = "SETINDEXVAR"
    }
}

/**
 * Similar to `k SETINDEXQ`, but with `0 <= k <= 254` taken from the stack.
 */
@Serializable
@SerialName(TvmTupleSetindexvarqInst.MNEMONIC)
data class TvmTupleSetindexvarqInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+|t'|")

    companion object {
        const val MNEMONIC = "SETINDEXVARQ"
    }
}

/**
 * Returns the length of a _Tuple_.
 */
@Serializable
@SerialName(TvmTupleTlenInst.MNEMONIC)
data class TvmTupleTlenInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmFixedGas(value = 26)

    companion object {
        const val MNEMONIC = "TLEN"
    }
}

/**
 * Detaches the last element `x=x_n` from a non-empty _Tuple_ `t=(x_1,...,x_n)`, and returns both the r
 * esulting _Tuple_ `t'=(x_1,...,x_{n-1})` and the original last element `x`.
 */
@Serializable
@SerialName(TvmTupleTpopInst.MNEMONIC)
data class TvmTupleTpopInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+|t'|")

    companion object {
        const val MNEMONIC = "TPOP"
    }
}

/**
 * Appends a value `x` to a _Tuple_ `t=(x_1,...,x_n)`, but only if the resulting _Tuple_ `t'=(x_1,...,x
 * _n,x)` is of length at most 255. Otherwise throws a type check exception.
 */
@Serializable
@SerialName(TvmTupleTpushInst.MNEMONIC)
data class TvmTupleTpushInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+|t'|")

    companion object {
        const val MNEMONIC = "TPUSH"
    }
}

/**
 * Creates a new _Tuple_ `t=(x_1, ... ,x_n)` containing `n` values `x_1`,..., `x_n`.
 * `0 <= n <= 15`
 */
@Serializable
@SerialName(TvmTupleTupleInst.MNEMONIC)
data class TvmTupleTupleInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+n")

    companion object {
        const val MNEMONIC = "TUPLE"
    }
}

/**
 * Creates a new _Tuple_ `t` of length `n` similarly to `TUPLE`, but with `0 <= n <= 255` taken from th
 * e stack.
 */
@Serializable
@SerialName(TvmTupleTuplevarInst.MNEMONIC)
data class TvmTupleTuplevarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+n")

    companion object {
        const val MNEMONIC = "TUPLEVAR"
    }
}

/**
 * Unpacks first `0 <= k <= 15` elements of a _Tuple_ `t`.
 * If `|t|<k`, throws a type check exception.
 */
@Serializable
@SerialName(TvmTupleUnpackfirstInst.MNEMONIC)
data class TvmTupleUnpackfirstInst(
    override val location: TvmInstLocation,
    val k: Int, // uint
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+k")

    companion object {
        const val MNEMONIC = "UNPACKFIRST"
    }
}

/**
 * Similar to `n UNPACKFIRST`, but with `0 <= n <= 255` taken from the stack.
 */
@Serializable
@SerialName(TvmTupleUnpackfirstvarInst.MNEMONIC)
data class TvmTupleUnpackfirstvarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+n")

    companion object {
        const val MNEMONIC = "UNPACKFIRSTVAR"
    }
}

/**
 * Unpacks a _Tuple_ `t=(x_1,...,x_n)` of length equal to `0 <= n <= 15`.
 * If `t` is not a _Tuple_, or if `|t| != n`, a type check exception is thrown.
 */
@Serializable
@SerialName(TvmTupleUntupleInst.MNEMONIC)
data class TvmTupleUntupleInst(
    override val location: TvmInstLocation,
    val n: Int, // uint
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+n")

    companion object {
        const val MNEMONIC = "UNTUPLE"
    }
}

/**
 * Similar to `n UNTUPLE`, but with `0 <= n <= 255` taken from the stack.
 */
@Serializable
@SerialName(TvmTupleUntuplevarInst.MNEMONIC)
data class TvmTupleUntuplevarInst(
    override val location: TvmInstLocation,
): TvmInst, TvmTupleInst {
    override val mnemonic: String get() = MNEMONIC
    override val gasConsumption get() = TvmComplexGas(this, description = "26+n")

    companion object {
        const val MNEMONIC = "UNTUPLEVAR"
    }
}

fun SerializersModuleBuilder.registerTvmInstSerializer() {
    polymorphic(TvmInst::class) {
        subclass(TvmStackBasicNopInst::class)
        subclass(TvmStackBasicXchg0iInst::class)
        subclass(TvmStackBasicXchgIjInst::class)
        subclass(TvmStackBasicXchg0iLongInst::class)
        subclass(TvmStackBasicXchg1iInst::class)
        subclass(TvmStackBasicPushInst::class)
        subclass(TvmStackBasicPopInst::class)
        subclass(TvmStackComplexXchg3Inst::class)
        subclass(TvmStackComplexXchg2Inst::class)
        subclass(TvmStackComplexXcpuInst::class)
        subclass(TvmStackComplexPuxcInst::class)
        subclass(TvmStackComplexPush2Inst::class)
        subclass(TvmStackComplexXchg3AltInst::class)
        subclass(TvmStackComplexXc2puInst::class)
        subclass(TvmStackComplexXcpuxcInst::class)
        subclass(TvmStackComplexXcpu2Inst::class)
        subclass(TvmStackComplexPuxc2Inst::class)
        subclass(TvmStackComplexPuxcpuInst::class)
        subclass(TvmStackComplexPu2xcInst::class)
        subclass(TvmStackComplexPush3Inst::class)
        subclass(TvmStackComplexBlkswapInst::class)
        subclass(TvmStackComplexPushLongInst::class)
        subclass(TvmStackComplexPopLongInst::class)
        subclass(TvmStackComplexRotInst::class)
        subclass(TvmStackComplexRotrevInst::class)
        subclass(TvmStackComplexSwap2Inst::class)
        subclass(TvmStackComplexDrop2Inst::class)
        subclass(TvmStackComplexDup2Inst::class)
        subclass(TvmStackComplexOver2Inst::class)
        subclass(TvmStackComplexReverseInst::class)
        subclass(TvmStackComplexBlkdropInst::class)
        subclass(TvmStackComplexBlkpushInst::class)
        subclass(TvmStackComplexPickInst::class)
        subclass(TvmStackComplexRollxInst::class)
        subclass(TvmStackComplexMinusrollxInst::class)
        subclass(TvmStackComplexBlkswxInst::class)
        subclass(TvmStackComplexRevxInst::class)
        subclass(TvmStackComplexDropxInst::class)
        subclass(TvmStackComplexTuckInst::class)
        subclass(TvmStackComplexXchgxInst::class)
        subclass(TvmStackComplexDepthInst::class)
        subclass(TvmStackComplexChkdepthInst::class)
        subclass(TvmStackComplexOnlytopxInst::class)
        subclass(TvmStackComplexOnlyxInst::class)
        subclass(TvmStackComplexBlkdrop2Inst::class)
        subclass(TvmTupleNullInst::class)
        subclass(TvmTupleIsnullInst::class)
        subclass(TvmTupleTupleInst::class)
        subclass(TvmTupleIndexInst::class)
        subclass(TvmTupleUntupleInst::class)
        subclass(TvmTupleUnpackfirstInst::class)
        subclass(TvmTupleExplodeInst::class)
        subclass(TvmTupleSetindexInst::class)
        subclass(TvmTupleIndexqInst::class)
        subclass(TvmTupleSetindexqInst::class)
        subclass(TvmTupleTuplevarInst::class)
        subclass(TvmTupleIndexvarInst::class)
        subclass(TvmTupleUntuplevarInst::class)
        subclass(TvmTupleUnpackfirstvarInst::class)
        subclass(TvmTupleExplodevarInst::class)
        subclass(TvmTupleSetindexvarInst::class)
        subclass(TvmTupleIndexvarqInst::class)
        subclass(TvmTupleSetindexvarqInst::class)
        subclass(TvmTupleTlenInst::class)
        subclass(TvmTupleQtlenInst::class)
        subclass(TvmTupleIstupleInst::class)
        subclass(TvmTupleLastInst::class)
        subclass(TvmTupleTpushInst::class)
        subclass(TvmTupleTpopInst::class)
        subclass(TvmTupleNullswapifInst::class)
        subclass(TvmTupleNullswapifnotInst::class)
        subclass(TvmTupleNullrotrifInst::class)
        subclass(TvmTupleNullrotrifnotInst::class)
        subclass(TvmTupleNullswapif2Inst::class)
        subclass(TvmTupleNullswapifnot2Inst::class)
        subclass(TvmTupleNullrotrif2Inst::class)
        subclass(TvmTupleNullrotrifnot2Inst::class)
        subclass(TvmTupleIndex2Inst::class)
        subclass(TvmTupleIndex3Inst::class)
        subclass(TvmConstIntPushint4Inst::class)
        subclass(TvmConstIntPushint8Inst::class)
        subclass(TvmConstIntPushint16Inst::class)
        subclass(TvmConstIntPushintLongInst::class)
        subclass(TvmConstIntPushpow2Inst::class)
        subclass(TvmConstIntPushnanInst::class)
        subclass(TvmConstIntPushpow2decInst::class)
        subclass(TvmConstIntPushnegpow2Inst::class)
        subclass(TvmConstDataPushrefInst::class)
        subclass(TvmConstDataPushrefsliceInst::class)
        subclass(TvmConstDataPushrefcontInst::class)
        subclass(TvmConstDataPushsliceInst::class)
        subclass(TvmConstDataPushsliceRefsInst::class)
        subclass(TvmConstDataPushsliceLongInst::class)
        subclass(TvmConstDataPushcontInst::class)
        subclass(TvmConstDataPushcontShortInst::class)
        subclass(TvmArithmBasicAddInst::class)
        subclass(TvmArithmBasicSubInst::class)
        subclass(TvmArithmBasicSubrInst::class)
        subclass(TvmArithmBasicNegateInst::class)
        subclass(TvmArithmBasicIncInst::class)
        subclass(TvmArithmBasicDecInst::class)
        subclass(TvmArithmBasicAddconstInst::class)
        subclass(TvmArithmBasicMulconstInst::class)
        subclass(TvmArithmBasicMulInst::class)
        subclass(TvmArithmDivAdddivmodInst::class)
        subclass(TvmArithmDivAdddivmodrInst::class)
        subclass(TvmArithmDivAdddivmodcInst::class)
        subclass(TvmArithmDivDivInst::class)
        subclass(TvmArithmDivDivrInst::class)
        subclass(TvmArithmDivDivcInst::class)
        subclass(TvmArithmDivModInst::class)
        subclass(TvmArithmDivModrInst::class)
        subclass(TvmArithmDivModcInst::class)
        subclass(TvmArithmDivDivmodInst::class)
        subclass(TvmArithmDivDivmodrInst::class)
        subclass(TvmArithmDivDivmodcInst::class)
        subclass(TvmArithmDivAddrshiftmodVarInst::class)
        subclass(TvmArithmDivAddrshiftmodrInst::class)
        subclass(TvmArithmDivAddrshiftmodcInst::class)
        subclass(TvmArithmDivRshiftrVarInst::class)
        subclass(TvmArithmDivRshiftcVarInst::class)
        subclass(TvmArithmDivModpow2VarInst::class)
        subclass(TvmArithmDivModpow2rVarInst::class)
        subclass(TvmArithmDivModpow2cVarInst::class)
        subclass(TvmArithmDivRshiftmodVarInst::class)
        subclass(TvmArithmDivRshiftmodrVarInst::class)
        subclass(TvmArithmDivRshiftmodcVarInst::class)
        subclass(TvmArithmDivAddrshiftmodInst::class)
        subclass(TvmArithmDivAddrshiftrmodInst::class)
        subclass(TvmArithmDivAddrshiftcmodInst::class)
        subclass(TvmArithmDivRshiftrInst::class)
        subclass(TvmArithmDivRshiftcInst::class)
        subclass(TvmArithmDivModpow2Inst::class)
        subclass(TvmArithmDivModpow2rInst::class)
        subclass(TvmArithmDivModpow2cInst::class)
        subclass(TvmArithmDivRshiftmodInst::class)
        subclass(TvmArithmDivRshiftrmodInst::class)
        subclass(TvmArithmDivRshiftcmodInst::class)
        subclass(TvmArithmDivMuladddivmodInst::class)
        subclass(TvmArithmDivMuladddivmodrInst::class)
        subclass(TvmArithmDivMuladddivmodcInst::class)
        subclass(TvmArithmDivMuldivInst::class)
        subclass(TvmArithmDivMuldivrInst::class)
        subclass(TvmArithmDivMuldivcInst::class)
        subclass(TvmArithmDivMulmodInst::class)
        subclass(TvmArithmDivMulmodrInst::class)
        subclass(TvmArithmDivMulmodcInst::class)
        subclass(TvmArithmDivMuldivmodInst::class)
        subclass(TvmArithmDivMuldivmodrInst::class)
        subclass(TvmArithmDivMuldivmodcInst::class)
        subclass(TvmArithmDivMuladdrshiftmodInst::class)
        subclass(TvmArithmDivMuladdrshiftrmodInst::class)
        subclass(TvmArithmDivMuladdrshiftcmodInst::class)
        subclass(TvmArithmDivMulrshiftVarInst::class)
        subclass(TvmArithmDivMulrshiftrVarInst::class)
        subclass(TvmArithmDivMulrshiftcVarInst::class)
        subclass(TvmArithmDivMulmodpow2VarInst::class)
        subclass(TvmArithmDivMulmodpow2rVarInst::class)
        subclass(TvmArithmDivMulmodpow2cVarInst::class)
        subclass(TvmArithmDivMulrshiftmodVarInst::class)
        subclass(TvmArithmDivMulrshiftrmodVarInst::class)
        subclass(TvmArithmDivMulrshiftcmodVarInst::class)
        subclass(TvmArithmDivMulrshiftInst::class)
        subclass(TvmArithmDivMulrshiftrInst::class)
        subclass(TvmArithmDivMulrshiftcInst::class)
        subclass(TvmArithmDivMulmodpow2Inst::class)
        subclass(TvmArithmDivMulmodpow2rInst::class)
        subclass(TvmArithmDivMulmodpow2cInst::class)
        subclass(TvmArithmDivMulrshiftmodInst::class)
        subclass(TvmArithmDivMulrshiftrmodInst::class)
        subclass(TvmArithmDivMulrshiftcmodInst::class)
        subclass(TvmArithmDivLshiftadddivmodVarInst::class)
        subclass(TvmArithmDivLshiftadddivmodrVarInst::class)
        subclass(TvmArithmDivLshiftadddivmodcVarInst::class)
        subclass(TvmArithmDivLshiftdivVarInst::class)
        subclass(TvmArithmDivLshiftdivrVarInst::class)
        subclass(TvmArithmDivLshiftdivcVarInst::class)
        subclass(TvmArithmDivLshiftmodVarInst::class)
        subclass(TvmArithmDivLshiftmodrVarInst::class)
        subclass(TvmArithmDivLshiftmodcVarInst::class)
        subclass(TvmArithmDivLshiftdivmodVarInst::class)
        subclass(TvmArithmDivLshiftdivmodrVarInst::class)
        subclass(TvmArithmDivLshiftdivmodcVarInst::class)
        subclass(TvmArithmDivLshiftadddivmodInst::class)
        subclass(TvmArithmDivLshiftadddivmodrInst::class)
        subclass(TvmArithmDivLshiftadddivmodcInst::class)
        subclass(TvmArithmDivLshiftdivInst::class)
        subclass(TvmArithmDivLshiftdivrInst::class)
        subclass(TvmArithmDivLshiftdivcInst::class)
        subclass(TvmArithmDivLshiftmodInst::class)
        subclass(TvmArithmDivLshiftmodrInst::class)
        subclass(TvmArithmDivLshiftmodcInst::class)
        subclass(TvmArithmDivLshiftdivmodInst::class)
        subclass(TvmArithmDivLshiftdivmodrInst::class)
        subclass(TvmArithmDivLshiftdivmodcInst::class)
        subclass(TvmArithmLogicalLshiftInst::class)
        subclass(TvmArithmLogicalRshiftInst::class)
        subclass(TvmArithmLogicalLshiftVarInst::class)
        subclass(TvmArithmLogicalRshiftVarInst::class)
        subclass(TvmArithmLogicalPow2Inst::class)
        subclass(TvmArithmLogicalAndInst::class)
        subclass(TvmArithmLogicalOrInst::class)
        subclass(TvmArithmLogicalXorInst::class)
        subclass(TvmArithmLogicalNotInst::class)
        subclass(TvmArithmLogicalFitsInst::class)
        subclass(TvmArithmLogicalUfitsInst::class)
        subclass(TvmArithmLogicalFitsxInst::class)
        subclass(TvmArithmLogicalUfitsxInst::class)
        subclass(TvmArithmLogicalBitsizeInst::class)
        subclass(TvmArithmLogicalUbitsizeInst::class)
        subclass(TvmArithmLogicalMinInst::class)
        subclass(TvmArithmLogicalMaxInst::class)
        subclass(TvmArithmLogicalMinmaxInst::class)
        subclass(TvmArithmLogicalAbsInst::class)
        subclass(TvmArithmQuietQaddInst::class)
        subclass(TvmArithmQuietQsubInst::class)
        subclass(TvmArithmQuietQsubrInst::class)
        subclass(TvmArithmQuietQnegateInst::class)
        subclass(TvmArithmQuietQincInst::class)
        subclass(TvmArithmQuietQdecInst::class)
        subclass(TvmArithmQuietQmulInst::class)
        subclass(TvmArithmQuietQadddivmodInst::class)
        subclass(TvmArithmQuietQadddivmodrInst::class)
        subclass(TvmArithmQuietQadddivmodcInst::class)
        subclass(TvmArithmQuietQdivInst::class)
        subclass(TvmArithmQuietQdivrInst::class)
        subclass(TvmArithmQuietQdivcInst::class)
        subclass(TvmArithmQuietQmodInst::class)
        subclass(TvmArithmQuietQmodrInst::class)
        subclass(TvmArithmQuietQmodcInst::class)
        subclass(TvmArithmQuietQdivmodInst::class)
        subclass(TvmArithmQuietQdivmodrInst::class)
        subclass(TvmArithmQuietQdivmodcInst::class)
        subclass(TvmArithmQuietQaddrshiftmodInst::class)
        subclass(TvmArithmQuietQaddrshiftmodrInst::class)
        subclass(TvmArithmQuietQaddrshiftmodcInst::class)
        subclass(TvmArithmQuietQrshiftrVarInst::class)
        subclass(TvmArithmQuietQrshiftcVarInst::class)
        subclass(TvmArithmQuietQmodpow2VarInst::class)
        subclass(TvmArithmQuietQmodpow2rVarInst::class)
        subclass(TvmArithmQuietQmodpow2cVarInst::class)
        subclass(TvmArithmQuietQrshiftmodVarInst::class)
        subclass(TvmArithmQuietQrshiftmodrVarInst::class)
        subclass(TvmArithmQuietQrshiftmodcVarInst::class)
        subclass(TvmArithmQuietQaddrshiftrmodInst::class)
        subclass(TvmArithmQuietQaddrshiftcmodInst::class)
        subclass(TvmArithmQuietQrshiftrInst::class)
        subclass(TvmArithmQuietQrshiftcInst::class)
        subclass(TvmArithmQuietQmodpow2Inst::class)
        subclass(TvmArithmQuietQmodpow2rInst::class)
        subclass(TvmArithmQuietQmodpow2cInst::class)
        subclass(TvmArithmQuietQrshiftmodInst::class)
        subclass(TvmArithmQuietQrshiftrmodInst::class)
        subclass(TvmArithmQuietQrshiftcmodInst::class)
        subclass(TvmArithmQuietQmuladddivmodInst::class)
        subclass(TvmArithmQuietQmuladddivmodrInst::class)
        subclass(TvmArithmQuietQmuladddivmodcInst::class)
        subclass(TvmArithmQuietQmuldivInst::class)
        subclass(TvmArithmQuietQmuldivrInst::class)
        subclass(TvmArithmQuietQmuldivcInst::class)
        subclass(TvmArithmQuietQmulmodInst::class)
        subclass(TvmArithmQuietQmulmodrInst::class)
        subclass(TvmArithmQuietQmulmodcInst::class)
        subclass(TvmArithmQuietQmuldivmodInst::class)
        subclass(TvmArithmQuietQmuldivmodrInst::class)
        subclass(TvmArithmQuietQmuldivmodcInst::class)
        subclass(TvmArithmQuietQmuladdrshiftmodInst::class)
        subclass(TvmArithmQuietQmuladdrshiftrmodInst::class)
        subclass(TvmArithmQuietQmuladdrshiftcmodInst::class)
        subclass(TvmArithmQuietQmulrshiftVarInst::class)
        subclass(TvmArithmQuietQmulrshiftrVarInst::class)
        subclass(TvmArithmQuietQmulrshiftcVarInst::class)
        subclass(TvmArithmQuietQmulmodpow2VarInst::class)
        subclass(TvmArithmQuietQmulmodpow2rVarInst::class)
        subclass(TvmArithmQuietQmulmodpow2cVarInst::class)
        subclass(TvmArithmQuietQmulrshiftmodVarInst::class)
        subclass(TvmArithmQuietQmulrshiftrmodVarInst::class)
        subclass(TvmArithmQuietQmulrshiftcmodVarInst::class)
        subclass(TvmArithmQuietQmulrshiftInst::class)
        subclass(TvmArithmQuietQmulrshiftrInst::class)
        subclass(TvmArithmQuietQmulrshiftcInst::class)
        subclass(TvmArithmQuietQmulmodpow2Inst::class)
        subclass(TvmArithmQuietQmulmodpow2rInst::class)
        subclass(TvmArithmQuietQmulmodpow2cInst::class)
        subclass(TvmArithmQuietQmulrshiftmodInst::class)
        subclass(TvmArithmQuietQmulrshiftrmodInst::class)
        subclass(TvmArithmQuietQmulrshiftcmodInst::class)
        subclass(TvmArithmQuietQlshiftadddivmodVarInst::class)
        subclass(TvmArithmQuietQlshiftadddivmodrVarInst::class)
        subclass(TvmArithmQuietQlshiftadddivmodcVarInst::class)
        subclass(TvmArithmQuietQlshiftdivVarInst::class)
        subclass(TvmArithmQuietQlshiftdivrVarInst::class)
        subclass(TvmArithmQuietQlshiftdivcVarInst::class)
        subclass(TvmArithmQuietQlshiftmodVarInst::class)
        subclass(TvmArithmQuietQlshiftmodrVarInst::class)
        subclass(TvmArithmQuietQlshiftmodcVarInst::class)
        subclass(TvmArithmQuietQlshiftdivmodVarInst::class)
        subclass(TvmArithmQuietQlshiftdivmodrVarInst::class)
        subclass(TvmArithmQuietQlshiftdivmodcVarInst::class)
        subclass(TvmArithmQuietQlshiftadddivmodInst::class)
        subclass(TvmArithmQuietQlshiftadddivmodrInst::class)
        subclass(TvmArithmQuietQlshiftadddivmodcInst::class)
        subclass(TvmArithmQuietQlshiftdivInst::class)
        subclass(TvmArithmQuietQlshiftdivrInst::class)
        subclass(TvmArithmQuietQlshiftdivcInst::class)
        subclass(TvmArithmQuietQlshiftmodInst::class)
        subclass(TvmArithmQuietQlshiftmodrInst::class)
        subclass(TvmArithmQuietQlshiftmodcInst::class)
        subclass(TvmArithmQuietQlshiftdivmodInst::class)
        subclass(TvmArithmQuietQlshiftdivmodrInst::class)
        subclass(TvmArithmQuietQlshiftdivmodcInst::class)
        subclass(TvmArithmQuietQlshiftInst::class)
        subclass(TvmArithmQuietQrshiftInst::class)
        subclass(TvmArithmQuietQlshiftVarInst::class)
        subclass(TvmArithmQuietQrshiftVarInst::class)
        subclass(TvmArithmQuietQpow2Inst::class)
        subclass(TvmArithmQuietQandInst::class)
        subclass(TvmArithmQuietQorInst::class)
        subclass(TvmArithmQuietQxorInst::class)
        subclass(TvmArithmQuietQnotInst::class)
        subclass(TvmArithmQuietQfitsInst::class)
        subclass(TvmArithmQuietQufitsInst::class)
        subclass(TvmArithmQuietQfitsxInst::class)
        subclass(TvmArithmQuietQufitsxInst::class)
        subclass(TvmCompareIntSgnInst::class)
        subclass(TvmCompareIntLessInst::class)
        subclass(TvmCompareIntEqualInst::class)
        subclass(TvmCompareIntLeqInst::class)
        subclass(TvmCompareIntGreaterInst::class)
        subclass(TvmCompareIntNeqInst::class)
        subclass(TvmCompareIntGeqInst::class)
        subclass(TvmCompareIntCmpInst::class)
        subclass(TvmCompareIntEqintInst::class)
        subclass(TvmCompareIntLessintInst::class)
        subclass(TvmCompareIntGtintInst::class)
        subclass(TvmCompareIntNeqintInst::class)
        subclass(TvmCompareIntIsnanInst::class)
        subclass(TvmCompareIntChknanInst::class)
        subclass(TvmCompareOtherSemptyInst::class)
        subclass(TvmCompareOtherSdemptyInst::class)
        subclass(TvmCompareOtherSremptyInst::class)
        subclass(TvmCompareOtherSdfirstInst::class)
        subclass(TvmCompareOtherSdlexcmpInst::class)
        subclass(TvmCompareOtherSdeqInst::class)
        subclass(TvmCompareOtherSdpfxInst::class)
        subclass(TvmCompareOtherSdpfxrevInst::class)
        subclass(TvmCompareOtherSdppfxInst::class)
        subclass(TvmCompareOtherSdppfxrevInst::class)
        subclass(TvmCompareOtherSdsfxInst::class)
        subclass(TvmCompareOtherSdsfxrevInst::class)
        subclass(TvmCompareOtherSdpsfxInst::class)
        subclass(TvmCompareOtherSdpsfxrevInst::class)
        subclass(TvmCompareOtherSdcntlead0Inst::class)
        subclass(TvmCompareOtherSdcntlead1Inst::class)
        subclass(TvmCompareOtherSdcnttrail0Inst::class)
        subclass(TvmCompareOtherSdcnttrail1Inst::class)
        subclass(TvmCellBuildNewcInst::class)
        subclass(TvmCellBuildEndcInst::class)
        subclass(TvmCellBuildStiInst::class)
        subclass(TvmCellBuildStuInst::class)
        subclass(TvmCellBuildStrefInst::class)
        subclass(TvmCellBuildStbrefrInst::class)
        subclass(TvmCellBuildStsliceInst::class)
        subclass(TvmCellBuildStixInst::class)
        subclass(TvmCellBuildStuxInst::class)
        subclass(TvmCellBuildStixrInst::class)
        subclass(TvmCellBuildStuxrInst::class)
        subclass(TvmCellBuildStixqInst::class)
        subclass(TvmCellBuildStuxqInst::class)
        subclass(TvmCellBuildStixrqInst::class)
        subclass(TvmCellBuildStuxrqInst::class)
        subclass(TvmCellBuildStiAltInst::class)
        subclass(TvmCellBuildStuAltInst::class)
        subclass(TvmCellBuildStirInst::class)
        subclass(TvmCellBuildSturInst::class)
        subclass(TvmCellBuildStiqInst::class)
        subclass(TvmCellBuildStuqInst::class)
        subclass(TvmCellBuildStirqInst::class)
        subclass(TvmCellBuildSturqInst::class)
        subclass(TvmCellBuildStrefAltInst::class)
        subclass(TvmCellBuildStbrefInst::class)
        subclass(TvmCellBuildStsliceAltInst::class)
        subclass(TvmCellBuildStbInst::class)
        subclass(TvmCellBuildStrefrInst::class)
        subclass(TvmCellBuildStbrefrAltInst::class)
        subclass(TvmCellBuildStslicerInst::class)
        subclass(TvmCellBuildStbrInst::class)
        subclass(TvmCellBuildStrefqInst::class)
        subclass(TvmCellBuildStbrefqInst::class)
        subclass(TvmCellBuildStsliceqInst::class)
        subclass(TvmCellBuildStbqInst::class)
        subclass(TvmCellBuildStrefrqInst::class)
        subclass(TvmCellBuildStbrefrqInst::class)
        subclass(TvmCellBuildStslicerqInst::class)
        subclass(TvmCellBuildStbrqInst::class)
        subclass(TvmCellBuildStrefconstInst::class)
        subclass(TvmCellBuildStref2constInst::class)
        subclass(TvmCellBuildEndxcInst::class)
        subclass(TvmCellBuildStile4Inst::class)
        subclass(TvmCellBuildStule4Inst::class)
        subclass(TvmCellBuildStile8Inst::class)
        subclass(TvmCellBuildStule8Inst::class)
        subclass(TvmCellBuildBdepthInst::class)
        subclass(TvmCellBuildBbitsInst::class)
        subclass(TvmCellBuildBrefsInst::class)
        subclass(TvmCellBuildBbitrefsInst::class)
        subclass(TvmCellBuildBrembitsInst::class)
        subclass(TvmCellBuildBremrefsInst::class)
        subclass(TvmCellBuildBrembitrefsInst::class)
        subclass(TvmCellBuildBchkbitsInst::class)
        subclass(TvmCellBuildBchkbitsVarInst::class)
        subclass(TvmCellBuildBchkrefsInst::class)
        subclass(TvmCellBuildBchkbitrefsInst::class)
        subclass(TvmCellBuildBchkbitsqInst::class)
        subclass(TvmCellBuildBchkbitsqVarInst::class)
        subclass(TvmCellBuildBchkrefsqInst::class)
        subclass(TvmCellBuildBchkbitrefsqInst::class)
        subclass(TvmCellBuildStzeroesInst::class)
        subclass(TvmCellBuildStonesInst::class)
        subclass(TvmCellBuildStsameInst::class)
        subclass(TvmCellBuildStsliceconstInst::class)
        subclass(TvmCellParseCtosInst::class)
        subclass(TvmCellParseEndsInst::class)
        subclass(TvmCellParseLdiInst::class)
        subclass(TvmCellParseLduInst::class)
        subclass(TvmCellParseLdrefInst::class)
        subclass(TvmCellParseLdrefrtosInst::class)
        subclass(TvmCellParseLdsliceInst::class)
        subclass(TvmCellParseLdixInst::class)
        subclass(TvmCellParseLduxInst::class)
        subclass(TvmCellParsePldixInst::class)
        subclass(TvmCellParsePlduxInst::class)
        subclass(TvmCellParseLdixqInst::class)
        subclass(TvmCellParseLduxqInst::class)
        subclass(TvmCellParsePldixqInst::class)
        subclass(TvmCellParsePlduxqInst::class)
        subclass(TvmCellParseLdiAltInst::class)
        subclass(TvmCellParseLduAltInst::class)
        subclass(TvmCellParsePldiInst::class)
        subclass(TvmCellParsePlduInst::class)
        subclass(TvmCellParseLdiqInst::class)
        subclass(TvmCellParseLduqInst::class)
        subclass(TvmCellParsePldiqInst::class)
        subclass(TvmCellParsePlduqInst::class)
        subclass(TvmCellParsePlduzInst::class)
        subclass(TvmCellParseLdslicexInst::class)
        subclass(TvmCellParsePldslicexInst::class)
        subclass(TvmCellParseLdslicexqInst::class)
        subclass(TvmCellParsePldslicexqInst::class)
        subclass(TvmCellParseLdsliceAltInst::class)
        subclass(TvmCellParsePldsliceInst::class)
        subclass(TvmCellParseLdsliceqInst::class)
        subclass(TvmCellParsePldsliceqInst::class)
        subclass(TvmCellParseSdcutfirstInst::class)
        subclass(TvmCellParseSdskipfirstInst::class)
        subclass(TvmCellParseSdcutlastInst::class)
        subclass(TvmCellParseSdskiplastInst::class)
        subclass(TvmCellParseSdsubstrInst::class)
        subclass(TvmCellParseSdbeginsxInst::class)
        subclass(TvmCellParseSdbeginsxqInst::class)
        subclass(TvmCellParseSdbeginsInst::class)
        subclass(TvmCellParseSdbeginsqInst::class)
        subclass(TvmCellParseScutfirstInst::class)
        subclass(TvmCellParseSskipfirstInst::class)
        subclass(TvmCellParseScutlastInst::class)
        subclass(TvmCellParseSskiplastInst::class)
        subclass(TvmCellParseSubsliceInst::class)
        subclass(TvmCellParseSplitInst::class)
        subclass(TvmCellParseSplitqInst::class)
        subclass(TvmCellParseXctosInst::class)
        subclass(TvmCellParseXloadInst::class)
        subclass(TvmCellParseXloadqInst::class)
        subclass(TvmCellParseSchkbitsInst::class)
        subclass(TvmCellParseSchkrefsInst::class)
        subclass(TvmCellParseSchkbitrefsInst::class)
        subclass(TvmCellParseSchkbitsqInst::class)
        subclass(TvmCellParseSchkrefsqInst::class)
        subclass(TvmCellParseSchkbitrefsqInst::class)
        subclass(TvmCellParsePldrefvarInst::class)
        subclass(TvmCellParseSbitsInst::class)
        subclass(TvmCellParseSrefsInst::class)
        subclass(TvmCellParseSbitrefsInst::class)
        subclass(TvmCellParsePldrefidxInst::class)
        subclass(TvmCellParseLdile4Inst::class)
        subclass(TvmCellParseLdule4Inst::class)
        subclass(TvmCellParseLdile8Inst::class)
        subclass(TvmCellParseLdule8Inst::class)
        subclass(TvmCellParsePldile4Inst::class)
        subclass(TvmCellParsePldule4Inst::class)
        subclass(TvmCellParsePldile8Inst::class)
        subclass(TvmCellParsePldule8Inst::class)
        subclass(TvmCellParseLdile4qInst::class)
        subclass(TvmCellParseLdule4qInst::class)
        subclass(TvmCellParseLdile8qInst::class)
        subclass(TvmCellParseLdule8qInst::class)
        subclass(TvmCellParsePldile4qInst::class)
        subclass(TvmCellParsePldule4qInst::class)
        subclass(TvmCellParsePldile8qInst::class)
        subclass(TvmCellParsePldule8qInst::class)
        subclass(TvmCellParseLdzeroesInst::class)
        subclass(TvmCellParseLdonesInst::class)
        subclass(TvmCellParseLdsameInst::class)
        subclass(TvmCellParseSdepthInst::class)
        subclass(TvmCellParseCdepthInst::class)
        subclass(TvmCellParseClevelInst::class)
        subclass(TvmCellParseClevelmaskInst::class)
        subclass(TvmCellParseChashiInst::class)
        subclass(TvmCellParseCdepthiInst::class)
        subclass(TvmCellParseChashixInst::class)
        subclass(TvmCellParseCdepthixInst::class)
        subclass(TvmContBasicExecuteInst::class)
        subclass(TvmContBasicJmpxInst::class)
        subclass(TvmContBasicCallxargsInst::class)
        subclass(TvmContBasicCallxargsVarInst::class)
        subclass(TvmContBasicJmpxargsInst::class)
        subclass(TvmContBasicRetargsInst::class)
        subclass(TvmContBasicRetInst::class)
        subclass(TvmContBasicRetaltInst::class)
        subclass(TvmContBasicBranchInst::class)
        subclass(TvmContBasicCallccInst::class)
        subclass(TvmContBasicJmpxdataInst::class)
        subclass(TvmContBasicCallccargsInst::class)
        subclass(TvmContBasicCallxvarargsInst::class)
        subclass(TvmContBasicRetvarargsInst::class)
        subclass(TvmContBasicJmpxvarargsInst::class)
        subclass(TvmContBasicCallccvarargsInst::class)
        subclass(TvmContBasicCallrefInst::class)
        subclass(TvmContBasicJmprefInst::class)
        subclass(TvmContBasicJmprefdataInst::class)
        subclass(TvmContBasicRetdataInst::class)
        subclass(TvmContBasicRunvmInst::class)
        subclass(TvmContBasicRunvmxInst::class)
        subclass(TvmContConditionalIfretInst::class)
        subclass(TvmContConditionalIfnotretInst::class)
        subclass(TvmContConditionalIfInst::class)
        subclass(TvmContConditionalIfnotInst::class)
        subclass(TvmContConditionalIfjmpInst::class)
        subclass(TvmContConditionalIfnotjmpInst::class)
        subclass(TvmContConditionalIfelseInst::class)
        subclass(TvmContConditionalIfrefInst::class)
        subclass(TvmContConditionalIfnotrefInst::class)
        subclass(TvmContConditionalIfjmprefInst::class)
        subclass(TvmContConditionalIfnotjmprefInst::class)
        subclass(TvmContConditionalCondselInst::class)
        subclass(TvmContConditionalCondselchkInst::class)
        subclass(TvmContConditionalIfretaltInst::class)
        subclass(TvmContConditionalIfnotretaltInst::class)
        subclass(TvmContConditionalIfrefelseInst::class)
        subclass(TvmContConditionalIfelserefInst::class)
        subclass(TvmContConditionalIfrefelserefInst::class)
        subclass(TvmContConditionalIfbitjmpInst::class)
        subclass(TvmContConditionalIfnbitjmpInst::class)
        subclass(TvmContConditionalIfbitjmprefInst::class)
        subclass(TvmContConditionalIfnbitjmprefInst::class)
        subclass(TvmContLoopsRepeatInst::class)
        subclass(TvmContLoopsRepeatendInst::class)
        subclass(TvmContLoopsUntilInst::class)
        subclass(TvmContLoopsUntilendInst::class)
        subclass(TvmContLoopsWhileInst::class)
        subclass(TvmContLoopsWhileendInst::class)
        subclass(TvmContLoopsAgainInst::class)
        subclass(TvmContLoopsAgainendInst::class)
        subclass(TvmContLoopsRepeatbrkInst::class)
        subclass(TvmContLoopsRepeatendbrkInst::class)
        subclass(TvmContLoopsUntilbrkInst::class)
        subclass(TvmContLoopsUntilendbrkInst::class)
        subclass(TvmContLoopsWhilebrkInst::class)
        subclass(TvmContLoopsWhileendbrkInst::class)
        subclass(TvmContLoopsAgainbrkInst::class)
        subclass(TvmContLoopsAgainendbrkInst::class)
        subclass(TvmContStackSetcontargsNInst::class)
        subclass(TvmContStackReturnargsInst::class)
        subclass(TvmContStackReturnvarargsInst::class)
        subclass(TvmContStackSetcontvarargsInst::class)
        subclass(TvmContStackSetnumvarargsInst::class)
        subclass(TvmContCreateBlessInst::class)
        subclass(TvmContCreateBlessvarargsInst::class)
        subclass(TvmContCreateBlessargsInst::class)
        subclass(TvmContRegistersPushctrInst::class)
        subclass(TvmContRegistersPopctrInst::class)
        subclass(TvmContRegistersSetcontctrInst::class)
        subclass(TvmContRegistersSetretctrInst::class)
        subclass(TvmContRegistersSetaltctrInst::class)
        subclass(TvmContRegistersPopsaveInst::class)
        subclass(TvmContRegistersSaveInst::class)
        subclass(TvmContRegistersSavealtInst::class)
        subclass(TvmContRegistersSavebothInst::class)
        subclass(TvmContRegistersPushctrxInst::class)
        subclass(TvmContRegistersPopctrxInst::class)
        subclass(TvmContRegistersSetcontctrxInst::class)
        subclass(TvmContRegistersComposInst::class)
        subclass(TvmContRegistersComposaltInst::class)
        subclass(TvmContRegistersComposbothInst::class)
        subclass(TvmContRegistersAtexitInst::class)
        subclass(TvmContRegistersAtexitaltInst::class)
        subclass(TvmContRegistersSetexitaltInst::class)
        subclass(TvmContRegistersThenretInst::class)
        subclass(TvmContRegistersThenretaltInst::class)
        subclass(TvmContRegistersInvertInst::class)
        subclass(TvmContRegistersBoolevalInst::class)
        subclass(TvmContRegistersSamealtInst::class)
        subclass(TvmContRegistersSamealtsaveInst::class)
        subclass(TvmContDictCalldictInst::class)
        subclass(TvmContDictCalldictLongInst::class)
        subclass(TvmContDictJmpdictInst::class)
        subclass(TvmContDictPreparedictInst::class)
        subclass(TvmExceptionsThrowShortInst::class)
        subclass(TvmExceptionsThrowifShortInst::class)
        subclass(TvmExceptionsThrowifnotShortInst::class)
        subclass(TvmExceptionsThrowInst::class)
        subclass(TvmExceptionsThrowargInst::class)
        subclass(TvmExceptionsThrowifInst::class)
        subclass(TvmExceptionsThrowargifInst::class)
        subclass(TvmExceptionsThrowifnotInst::class)
        subclass(TvmExceptionsThrowargifnotInst::class)
        subclass(TvmExceptionsThrowanyInst::class)
        subclass(TvmExceptionsThrowarganyInst::class)
        subclass(TvmExceptionsThrowanyifInst::class)
        subclass(TvmExceptionsThrowarganyifInst::class)
        subclass(TvmExceptionsThrowanyifnotInst::class)
        subclass(TvmExceptionsThrowarganyifnotInst::class)
        subclass(TvmExceptionsTryInst::class)
        subclass(TvmExceptionsTryargsInst::class)
        subclass(TvmDictSerialStdictInst::class)
        subclass(TvmDictSerialSkipdictInst::class)
        subclass(TvmDictSerialLddictsInst::class)
        subclass(TvmDictSerialPlddictsInst::class)
        subclass(TvmDictSerialLddictInst::class)
        subclass(TvmDictSerialPlddictInst::class)
        subclass(TvmDictSerialLddictqInst::class)
        subclass(TvmDictSerialPlddictqInst::class)
        subclass(TvmDictGetDictgetInst::class)
        subclass(TvmDictGetDictgetrefInst::class)
        subclass(TvmDictGetDictigetInst::class)
        subclass(TvmDictGetDictigetrefInst::class)
        subclass(TvmDictGetDictugetInst::class)
        subclass(TvmDictGetDictugetrefInst::class)
        subclass(TvmDictSetDictsetInst::class)
        subclass(TvmDictSetDictsetrefInst::class)
        subclass(TvmDictSetDictisetInst::class)
        subclass(TvmDictSetDictisetrefInst::class)
        subclass(TvmDictSetDictusetInst::class)
        subclass(TvmDictSetDictusetrefInst::class)
        subclass(TvmDictSetDictsetgetInst::class)
        subclass(TvmDictSetDictsetgetrefInst::class)
        subclass(TvmDictSetDictisetgetInst::class)
        subclass(TvmDictSetDictisetgetrefInst::class)
        subclass(TvmDictSetDictusetgetInst::class)
        subclass(TvmDictSetDictusetgetrefInst::class)
        subclass(TvmDictSetDictreplaceInst::class)
        subclass(TvmDictSetDictreplacerefInst::class)
        subclass(TvmDictSetDictireplaceInst::class)
        subclass(TvmDictSetDictireplacerefInst::class)
        subclass(TvmDictSetDictureplaceInst::class)
        subclass(TvmDictSetDictureplacerefInst::class)
        subclass(TvmDictSetDictreplacegetInst::class)
        subclass(TvmDictSetDictreplacegetrefInst::class)
        subclass(TvmDictSetDictireplacegetInst::class)
        subclass(TvmDictSetDictireplacegetrefInst::class)
        subclass(TvmDictSetDictureplacegetInst::class)
        subclass(TvmDictSetDictureplacegetrefInst::class)
        subclass(TvmDictSetDictaddInst::class)
        subclass(TvmDictSetDictaddrefInst::class)
        subclass(TvmDictSetDictiaddInst::class)
        subclass(TvmDictSetDictiaddrefInst::class)
        subclass(TvmDictSetDictuaddInst::class)
        subclass(TvmDictSetDictuaddrefInst::class)
        subclass(TvmDictSetDictaddgetInst::class)
        subclass(TvmDictSetDictaddgetrefInst::class)
        subclass(TvmDictSetDictiaddgetInst::class)
        subclass(TvmDictSetDictiaddgetrefInst::class)
        subclass(TvmDictSetDictuaddgetInst::class)
        subclass(TvmDictSetDictuaddgetrefInst::class)
        subclass(TvmDictSetBuilderDictsetbInst::class)
        subclass(TvmDictSetBuilderDictisetbInst::class)
        subclass(TvmDictSetBuilderDictusetbInst::class)
        subclass(TvmDictSetBuilderDictsetgetbInst::class)
        subclass(TvmDictSetBuilderDictisetgetbInst::class)
        subclass(TvmDictSetBuilderDictusetgetbInst::class)
        subclass(TvmDictSetBuilderDictreplacebInst::class)
        subclass(TvmDictSetBuilderDictireplacebInst::class)
        subclass(TvmDictSetBuilderDictureplacebInst::class)
        subclass(TvmDictSetBuilderDictreplacegetbInst::class)
        subclass(TvmDictSetBuilderDictireplacegetbInst::class)
        subclass(TvmDictSetBuilderDictureplacegetbInst::class)
        subclass(TvmDictSetBuilderDictaddbInst::class)
        subclass(TvmDictSetBuilderDictiaddbInst::class)
        subclass(TvmDictSetBuilderDictuaddbInst::class)
        subclass(TvmDictSetBuilderDictaddgetbInst::class)
        subclass(TvmDictSetBuilderDictiaddgetbInst::class)
        subclass(TvmDictSetBuilderDictuaddgetbInst::class)
        subclass(TvmDictDeleteDictdelInst::class)
        subclass(TvmDictDeleteDictidelInst::class)
        subclass(TvmDictDeleteDictudelInst::class)
        subclass(TvmDictDeleteDictdelgetInst::class)
        subclass(TvmDictDeleteDictdelgetrefInst::class)
        subclass(TvmDictDeleteDictidelgetInst::class)
        subclass(TvmDictDeleteDictidelgetrefInst::class)
        subclass(TvmDictDeleteDictudelgetInst::class)
        subclass(TvmDictDeleteDictudelgetrefInst::class)
        subclass(TvmDictMayberefDictgetoptrefInst::class)
        subclass(TvmDictMayberefDictigetoptrefInst::class)
        subclass(TvmDictMayberefDictugetoptrefInst::class)
        subclass(TvmDictMayberefDictsetgetoptrefInst::class)
        subclass(TvmDictMayberefDictisetgetoptrefInst::class)
        subclass(TvmDictMayberefDictusetgetoptrefInst::class)
        subclass(TvmDictPrefixPfxdictsetInst::class)
        subclass(TvmDictPrefixPfxdictreplaceInst::class)
        subclass(TvmDictPrefixPfxdictaddInst::class)
        subclass(TvmDictPrefixPfxdictdelInst::class)
        subclass(TvmDictNextDictgetnextInst::class)
        subclass(TvmDictNextDictgetnexteqInst::class)
        subclass(TvmDictNextDictgetprevInst::class)
        subclass(TvmDictNextDictgetpreveqInst::class)
        subclass(TvmDictNextDictigetnextInst::class)
        subclass(TvmDictNextDictigetnexteqInst::class)
        subclass(TvmDictNextDictigetprevInst::class)
        subclass(TvmDictNextDictigetpreveqInst::class)
        subclass(TvmDictNextDictugetnextInst::class)
        subclass(TvmDictNextDictugetnexteqInst::class)
        subclass(TvmDictNextDictugetprevInst::class)
        subclass(TvmDictNextDictugetpreveqInst::class)
        subclass(TvmDictMinDictminInst::class)
        subclass(TvmDictMinDictminrefInst::class)
        subclass(TvmDictMinDictiminInst::class)
        subclass(TvmDictMinDictiminrefInst::class)
        subclass(TvmDictMinDictuminInst::class)
        subclass(TvmDictMinDictuminrefInst::class)
        subclass(TvmDictMinDictmaxInst::class)
        subclass(TvmDictMinDictmaxrefInst::class)
        subclass(TvmDictMinDictimaxInst::class)
        subclass(TvmDictMinDictimaxrefInst::class)
        subclass(TvmDictMinDictumaxInst::class)
        subclass(TvmDictMinDictumaxrefInst::class)
        subclass(TvmDictMinDictremminInst::class)
        subclass(TvmDictMinDictremminrefInst::class)
        subclass(TvmDictMinDictiremminInst::class)
        subclass(TvmDictMinDictiremminrefInst::class)
        subclass(TvmDictMinDicturemminInst::class)
        subclass(TvmDictMinDicturemminrefInst::class)
        subclass(TvmDictMinDictremmaxInst::class)
        subclass(TvmDictMinDictremmaxrefInst::class)
        subclass(TvmDictMinDictiremmaxInst::class)
        subclass(TvmDictMinDictiremmaxrefInst::class)
        subclass(TvmDictMinDicturemmaxInst::class)
        subclass(TvmDictMinDicturemmaxrefInst::class)
        subclass(TvmDictSpecialDictigetjmpInst::class)
        subclass(TvmDictSpecialDictugetjmpInst::class)
        subclass(TvmDictSpecialDictigetexecInst::class)
        subclass(TvmDictSpecialDictugetexecInst::class)
        subclass(TvmDictSpecialDictpushconstInst::class)
        subclass(TvmDictPrefixPfxdictgetqInst::class)
        subclass(TvmDictPrefixPfxdictgetInst::class)
        subclass(TvmDictPrefixPfxdictgetjmpInst::class)
        subclass(TvmDictPrefixPfxdictgetexecInst::class)
        subclass(TvmDictPrefixPfxdictconstgetjmpInst::class)
        subclass(TvmDictSpecialDictigetjmpzInst::class)
        subclass(TvmDictSpecialDictugetjmpzInst::class)
        subclass(TvmDictSpecialDictigetexeczInst::class)
        subclass(TvmDictSpecialDictugetexeczInst::class)
        subclass(TvmDictSubSubdictgetInst::class)
        subclass(TvmDictSubSubdictigetInst::class)
        subclass(TvmDictSubSubdictugetInst::class)
        subclass(TvmDictSubSubdictrpgetInst::class)
        subclass(TvmDictSubSubdictirpgetInst::class)
        subclass(TvmDictSubSubdicturpgetInst::class)
        subclass(TvmAppGasAcceptInst::class)
        subclass(TvmAppGasSetgaslimitInst::class)
        subclass(TvmAppGasGasconsumedInst::class)
        subclass(TvmAppGasCommitInst::class)
        subclass(TvmAppRndRandu256Inst::class)
        subclass(TvmAppRndRandInst::class)
        subclass(TvmAppRndSetrandInst::class)
        subclass(TvmAppRndAddrandInst::class)
        subclass(TvmAppConfigGetparamInst::class)
        subclass(TvmAppConfigConfigdictInst::class)
        subclass(TvmAppConfigConfigparamInst::class)
        subclass(TvmAppConfigConfigoptparamInst::class)
        subclass(TvmAppConfigPrevmcblocksInst::class)
        subclass(TvmAppConfigPrevkeyblockInst::class)
        subclass(TvmAppConfigGlobalidInst::class)
        subclass(TvmAppConfigGetgasfeeInst::class)
        subclass(TvmAppConfigGetstoragefeeInst::class)
        subclass(TvmAppConfigGetforwardfeeInst::class)
        subclass(TvmAppConfigGetprecompiledgasInst::class)
        subclass(TvmAppConfigGetoriginalfwdfeeInst::class)
        subclass(TvmAppConfigGetgasfeesimpleInst::class)
        subclass(TvmAppConfigGetforwardfeesimpleInst::class)
        subclass(TvmAppGlobalGetglobvarInst::class)
        subclass(TvmAppGlobalGetglobInst::class)
        subclass(TvmAppGlobalSetglobvarInst::class)
        subclass(TvmAppGlobalSetglobInst::class)
        subclass(TvmAppCryptoHashcuInst::class)
        subclass(TvmAppCryptoHashsuInst::class)
        subclass(TvmAppCryptoSha256uInst::class)
        subclass(TvmAppCryptoHashextSha256Inst::class)
        subclass(TvmAppCryptoHashextSha512Inst::class)
        subclass(TvmAppCryptoHashextBlake2bInst::class)
        subclass(TvmAppCryptoHashextKeccak256Inst::class)
        subclass(TvmAppCryptoHashextKeccak512Inst::class)
        subclass(TvmAppCryptoHashextrSha256Inst::class)
        subclass(TvmAppCryptoHashextrSha512Inst::class)
        subclass(TvmAppCryptoHashextrBlake2bInst::class)
        subclass(TvmAppCryptoHashextrKeccak256Inst::class)
        subclass(TvmAppCryptoHashextrKeccak512Inst::class)
        subclass(TvmAppCryptoHashextaSha256Inst::class)
        subclass(TvmAppCryptoHashextaSha512Inst::class)
        subclass(TvmAppCryptoHashextaBlake2bInst::class)
        subclass(TvmAppCryptoHashextaKeccak256Inst::class)
        subclass(TvmAppCryptoHashextaKeccak512Inst::class)
        subclass(TvmAppCryptoHashextarSha256Inst::class)
        subclass(TvmAppCryptoHashextarSha512Inst::class)
        subclass(TvmAppCryptoHashextarBlake2bInst::class)
        subclass(TvmAppCryptoHashextarKeccak256Inst::class)
        subclass(TvmAppCryptoHashextarKeccak512Inst::class)
        subclass(TvmAppCryptoChksignuInst::class)
        subclass(TvmAppCryptoChksignsInst::class)
        subclass(TvmAppCryptoEcrecoverInst::class)
        subclass(TvmAppCryptoP256ChksignuInst::class)
        subclass(TvmAppCryptoP256ChksignsInst::class)
        subclass(TvmAppCryptoRist255FromhashInst::class)
        subclass(TvmAppCryptoRist255ValidateInst::class)
        subclass(TvmAppCryptoRist255AddInst::class)
        subclass(TvmAppCryptoRist255SubInst::class)
        subclass(TvmAppCryptoRist255MulInst::class)
        subclass(TvmAppCryptoRist255MulbaseInst::class)
        subclass(TvmAppCryptoRist255PushlInst::class)
        subclass(TvmAppCryptoRist255QvalidateInst::class)
        subclass(TvmAppCryptoRist255QaddInst::class)
        subclass(TvmAppCryptoRist255QsubInst::class)
        subclass(TvmAppCryptoRist255QmulInst::class)
        subclass(TvmAppCryptoRist255QmulbaseInst::class)
        subclass(TvmAppCryptoBlsVerifyInst::class)
        subclass(TvmAppCryptoBlsAggregateInst::class)
        subclass(TvmAppCryptoBlsFastaggregateverifyInst::class)
        subclass(TvmAppCryptoBlsAggregateverifyInst::class)
        subclass(TvmAppCryptoBlsG1AddInst::class)
        subclass(TvmAppCryptoBlsG1SubInst::class)
        subclass(TvmAppCryptoBlsG1NegInst::class)
        subclass(TvmAppCryptoBlsG1MulInst::class)
        subclass(TvmAppCryptoBlsG1MultiexpInst::class)
        subclass(TvmAppCryptoBlsG1ZeroInst::class)
        subclass(TvmAppCryptoBlsMapToG1Inst::class)
        subclass(TvmAppCryptoBlsG1IngroupInst::class)
        subclass(TvmAppCryptoBlsG1IszeroInst::class)
        subclass(TvmAppCryptoBlsG2AddInst::class)
        subclass(TvmAppCryptoBlsG2SubInst::class)
        subclass(TvmAppCryptoBlsG2NegInst::class)
        subclass(TvmAppCryptoBlsG2MulInst::class)
        subclass(TvmAppCryptoBlsG2MultiexpInst::class)
        subclass(TvmAppCryptoBlsG2ZeroInst::class)
        subclass(TvmAppCryptoBlsMapToG2Inst::class)
        subclass(TvmAppCryptoBlsG2IngroupInst::class)
        subclass(TvmAppCryptoBlsG2IszeroInst::class)
        subclass(TvmAppCryptoBlsPairingInst::class)
        subclass(TvmAppCryptoBlsPushrInst::class)
        subclass(TvmAppMiscCdatasizeqInst::class)
        subclass(TvmAppMiscCdatasizeInst::class)
        subclass(TvmAppMiscSdatasizeqInst::class)
        subclass(TvmAppMiscSdatasizeInst::class)
        subclass(TvmAppCurrencyLdgramsInst::class)
        subclass(TvmAppCurrencyLdvarint16Inst::class)
        subclass(TvmAppCurrencyStgramsInst::class)
        subclass(TvmAppCurrencyStvarint16Inst::class)
        subclass(TvmAppCurrencyLdvaruint32Inst::class)
        subclass(TvmAppCurrencyLdvarint32Inst::class)
        subclass(TvmAppCurrencyStvaruint32Inst::class)
        subclass(TvmAppCurrencyStvarint32Inst::class)
        subclass(TvmAppAddrLdmsgaddrInst::class)
        subclass(TvmAppAddrLdmsgaddrqInst::class)
        subclass(TvmAppAddrParsemsgaddrInst::class)
        subclass(TvmAppAddrParsemsgaddrqInst::class)
        subclass(TvmAppAddrRewritestdaddrInst::class)
        subclass(TvmAppAddrRewritestdaddrqInst::class)
        subclass(TvmAppAddrRewritevaraddrInst::class)
        subclass(TvmAppAddrRewritevaraddrqInst::class)
        subclass(TvmAppActionsSendrawmsgInst::class)
        subclass(TvmAppActionsRawreserveInst::class)
        subclass(TvmAppActionsRawreservexInst::class)
        subclass(TvmAppActionsSetcodeInst::class)
        subclass(TvmAppActionsSetlibcodeInst::class)
        subclass(TvmAppActionsChangelibInst::class)
        subclass(TvmAppActionsSendmsgInst::class)
        subclass(TvmDebugDebugInst::class)
        subclass(TvmDebugDebugstrInst::class)
        subclass(TvmCodepageSetcpInst::class)
        subclass(TvmCodepageSetcpSpecialInst::class)
        subclass(TvmCodepageSetcpxInst::class)
    }
}
