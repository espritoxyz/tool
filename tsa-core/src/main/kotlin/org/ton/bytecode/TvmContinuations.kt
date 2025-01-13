package org.ton.bytecode

import org.usvm.UExpr
import org.usvm.machine.MethodId
import org.usvm.machine.TvmContext.TvmInt257Sort
import org.usvm.machine.state.C0Register
import org.usvm.machine.state.C1Register
import org.usvm.machine.state.C2Register
import org.usvm.machine.state.C3Register
import org.usvm.machine.state.C4Register
import org.usvm.machine.state.C5Register
import org.usvm.machine.state.C7Register


data class TvmRegisterSavelist(
    val c0: C0Register? = null,
    val c1: C1Register? = null,
    val c2: C2Register? = null,
    val c3: C3Register? = null,
    val c4: C4Register? = null,
    val c5: C5Register? = null,
    val c7: C7Register? = null,
)

sealed interface TvmContinuation {
    // TODO codepage, stack, nargs
    val savelist: TvmRegisterSavelist

    /**
     * @return copy with [savelist] set to [newSavelist]
     */
    fun updateSavelist(newSavelist: TvmRegisterSavelist = TvmRegisterSavelist()): TvmContinuation
}

/**
 * A continuation used to mark the end of a successful program execution with exit code [exitCode]
 */
data class TvmQuitContinuation(
    val exitCode: UInt
) : TvmContinuation {
    override val savelist = TvmRegisterSavelist()

    override fun updateSavelist(newSavelist: TvmRegisterSavelist): TvmQuitContinuation = this
}

/**
 * Default exception handler
 */
data object TvmExceptionContinuation : TvmContinuation {
    override val savelist: TvmRegisterSavelist = TvmRegisterSavelist()

    override fun updateSavelist(newSavelist: TvmRegisterSavelist): TvmExceptionContinuation = this
}

data class TvmOrdContinuation(
    val stmt: TvmInst,
    override val savelist: TvmRegisterSavelist = TvmRegisterSavelist(),
) : TvmContinuation {
    constructor(
        codeBlock: TvmCodeBlock,
        savelist: TvmRegisterSavelist = TvmRegisterSavelist()
    ) : this(codeBlock.instList.first(), savelist)

    override fun updateSavelist(newSavelist: TvmRegisterSavelist): TvmOrdContinuation = copy(savelist = newSavelist)
}

/**
 * [TvmOrdContinuation] wrapper that marks the [method] return site
 */
data class TvmMethodReturnContinuation(
    val method: MethodId,
    val returnSite: TvmOrdContinuation,
) : TvmContinuation {
    override val savelist: TvmRegisterSavelist
        get() = returnSite.savelist

    override fun updateSavelist(newSavelist: TvmRegisterSavelist): TvmContinuation =
        copy(returnSite = returnSite.updateSavelist(newSavelist))
}

/**
 * A continuation used to count loop iterations using [TvmArtificialLoopEntranceInst]
 */
data class TvmLoopEntranceContinuation(
    val loopBody: TvmContinuation,
    val id: UInt,
    val parentLocation: TvmInstLocation,
    override val savelist: TvmRegisterSavelist = TvmRegisterSavelist()
) : TvmContinuation {

    val codeBlock = TvmLambda(
        mutableListOf(
            TvmArtificialLoopEntranceInst(id, TvmInstLambdaLocation(0).also { it.parent = parentLocation }),
            TvmArtificialJmpToContInst(loopBody, TvmInstLambdaLocation(1).also { it.parent = parentLocation }),
        )
    )

    override fun updateSavelist(newSavelist: TvmRegisterSavelist): TvmLoopEntranceContinuation =
        copy(savelist = newSavelist)
}

data class TvmUntilContinuation(
    val body: TvmLoopEntranceContinuation,
    val after: TvmContinuation,
    override val savelist: TvmRegisterSavelist = TvmRegisterSavelist(),
) : TvmContinuation {

    override fun updateSavelist(newSavelist: TvmRegisterSavelist): TvmUntilContinuation = copy(savelist = newSavelist)
}

data class TvmRepeatContinuation(
    val body: TvmLoopEntranceContinuation,
    val after: TvmContinuation,
    val count: UExpr<TvmInt257Sort>,
    override val savelist: TvmRegisterSavelist = TvmRegisterSavelist(),
) : TvmContinuation {

    override fun updateSavelist(newSavelist: TvmRegisterSavelist): TvmRepeatContinuation = copy(savelist = newSavelist)
}

/**
 * @property isCondition flag indicating which continuation is currently running
 */
data class TvmWhileContinuation(
    val condition: TvmLoopEntranceContinuation,
    val body: TvmContinuation,
    val after: TvmContinuation,
    val isCondition: Boolean,
    override val savelist: TvmRegisterSavelist = TvmRegisterSavelist(),
) : TvmContinuation {

    override fun updateSavelist(newSavelist: TvmRegisterSavelist): TvmWhileContinuation = copy(savelist = newSavelist)
}

data class TvmAgainContinuation(
    val body: TvmLoopEntranceContinuation,
    override val savelist: TvmRegisterSavelist = TvmRegisterSavelist(),
) : TvmContinuation {
    override fun updateSavelist(newSavelist: TvmRegisterSavelist): TvmAgainContinuation = copy(savelist = newSavelist)
}