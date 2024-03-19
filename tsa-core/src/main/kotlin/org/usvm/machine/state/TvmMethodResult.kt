package org.usvm.machine.state

import org.usvm.machine.state.TvmMethodResult.*
import org.ton.bytecode.TvmCodeBlock
import org.usvm.machine.state.TvmStack

/**
 * Represents a result of a method invocation.
 */
sealed interface TvmMethodResult {
    /**
     * No call was performed.
     */
    data object NoCall : TvmMethodResult

    /**
     * A [method] successfully returned.
     */
    class TvmSuccess(
        val method: TvmCodeBlock,
        val stack: TvmStack,
    ) : TvmMethodResult

    /**
     * A method exited with non-successful exit code.
     */
    interface TvmFailure : TvmMethodResult {
        val exitCode: UInt
    }
}

// TODO standard exit code should be placed in codepage 0?
// TODO add integer underflow?
object TvmIntegerOverflow : TvmFailure {
    override val exitCode: UInt = 4u

    override fun toString(): String = "TVM integer overflow, exit code: $exitCode"
}

object TvmIntegerOutOfRange : TvmFailure {
    override val exitCode: UInt = 5u

    override fun toString(): String = "TVM integer out of expected range, exit code: $exitCode" // TODO add expected range to the message?
}

object TvmCellOverflow : TvmFailure {
    override val exitCode: UInt = 8u

    override fun toString(): String = "TVM cell underflow, exit code: $exitCode"
}

object TvmCellUnderflow : TvmFailure {
    override val exitCode: UInt = 9u

    override fun toString(): String = "TVM cell underflow, exit code: $exitCode"
}

data class TvmUnknownFailure(override val exitCode: UInt): TvmFailure

// TODO add remaining
