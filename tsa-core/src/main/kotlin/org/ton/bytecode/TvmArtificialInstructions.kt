package org.ton.bytecode

import org.usvm.UBvSort
import org.usvm.UExpr

sealed interface TvmArtificialInst : TvmInst {
    override val gasConsumption: TvmGas
        get() = TvmFixedGas(value = 0)
}

sealed interface TvmLoopArtificialInst : TvmArtificialInst, TvmContLoopsInst {
    val originalInst: TvmContLoopsInst
}

data class TvmArtificialRepeatInst(
    override val originalInst: TvmContLoopsInst,
    val continuationValue: TvmContinuationValue,
    val loopRepeats: UExpr<UBvSort>,
    val executeUntilEnd: Boolean,
) : TvmLoopArtificialInst {
    override val location: TvmInstLocation
        get() = originalInst.location

    override val mnemonic: String
        get() = "artificial_repeat_${originalInst.mnemonic}"
}

data class TvmArtificialUntilInst(
    override val originalInst: TvmContLoopsInst,
    val continuationValue: TvmContinuationValue,
    val executeUntilEnd: Boolean,
) : TvmLoopArtificialInst {
    override val location: TvmInstLocation
        get() = originalInst.location

    override val mnemonic: String
        get() = "artificial_until_${originalInst.mnemonic}"
}

data class TvmArtificialWhileStartInst(
    override val originalInst: TvmContLoopsInst,
    val continuationValue: TvmContinuationValue,
    val conditionContinuation: TvmContinuationValue,
    val executeUntilEnd: Boolean,
) : TvmLoopArtificialInst {
    override val location: TvmInstLocation
        get() = originalInst.location

    override val mnemonic: String
        get() = "artificial_while_start_${originalInst.mnemonic}"
}

data class TvmArtificialWhileEndInst(
    override val originalInst: TvmContLoopsInst,
    val continuationValue: TvmContinuationValue,
    val conditionContinuation: TvmContinuationValue,
    val executeUntilEnd: Boolean,
) : TvmLoopArtificialInst {
    override val location: TvmInstLocation
        get() = originalInst.location

    override val mnemonic: String
        get() = "artificial_while_end_${originalInst.mnemonic}"
}

data class TvmArtificialAgainInst(
    override val originalInst: TvmContLoopsInst,
    val continuationValue: TvmContinuationValue,
    val executeUntilEnd: Boolean,
) : TvmLoopArtificialInst {
    override val location: TvmInstLocation
        get() = originalInst.location

    override val mnemonic: String
        get() = "artificial_again_${originalInst.mnemonic}"
}
