package org.usvm.machine.state

import kotlinx.serialization.Serializable
import org.usvm.machine.state.TvmMethodResult.*
import org.ton.bytecode.TvmCodeBlock
import org.usvm.UBv32Sort
import org.usvm.UExpr

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
        val stack: TvmStack,
    ) : TvmMethodResult

    /**
     * A method exited with non-successful exit code.
     */
    @Serializable
    data class TvmFailure(
        val exit: TvmExit,
        val type: TvmFailureType
    ) : TvmMethodResult

    @Serializable
    sealed interface TvmExit {
        val exitCode: UInt
        val ruleName: String
    }
}

/**
 * In some cases, TvmExit is not enough to identify the type of the failure.
 * For example, cellUnderflow can occur due to real programmer's error, or
 * due to the fact that we generated input values with bad structure.
 * TvmFailureType is used to distinguish these situations.
 */
@Serializable
enum class TvmFailureType {
    /**
     * Error due to bad input object structure. In this case the structure is fixed.
     *
     * Example: input_slice~load_bits(128), when len(input_slice) < 128
     */
    FixedStructuralError,
    /**
     * Error due to bad input object structure, that has symbolic constraints.
     *
     * Example: input_slice~load_bits(input_x), when len(input_slice) < input_x
     */
    SymbolicStructuralError,
    /**
     * Real programmer's error.
     *
     * Example: s = "a"; s~load_bits(128);
     */
    RealError,
    /**
     * Extra failure information couldn't be inferred.
     */
    UnknownError
}

// TODO standard exit code should be placed in codepage 0?
// TODO add integer underflow?
@Serializable
object TvmIntegerOverflowError : TvmExit {
    override val exitCode: UInt = 4u
    override val ruleName: String = "integer-overflow"

    override fun toString(): String = "TVM integer overflow, exit code: $exitCode"
}

@Serializable
object TvmIntegerOutOfRangeError : TvmExit {
    override val exitCode: UInt = 5u
    override val ruleName: String = "integer-out-of-range"

    override fun toString(): String = "TVM integer out of expected range, exit code: $exitCode" // TODO add expected range to the message?
}

// TODO add expected type
@Serializable
object TvmTypeCheckError : TvmExit {
    override val exitCode: UInt = 7u
    override val ruleName: String = "wrong-type"

    override fun toString(): String = "TVM type check error, exit code: $exitCode"
}

@Serializable
object TvmCellOverflowError : TvmExit {
    override val exitCode: UInt = 8u
    override val ruleName: String = "cell-overflow"

    override fun toString(): String = "TVM cell overflow, exit code: $exitCode"
}

@Serializable
object TvmCellUnderflowError : TvmExit {
    override val exitCode: UInt = 9u
    override val ruleName: String = "cell-underflow"

    override fun toString(): String = "TVM cell underflow, exit code: $exitCode"
}

data class TvmOutOfGas(val consumedGas: UExpr<UBv32Sort>, val gasLimit: UExpr<UBv32Sort>) : TvmExit {
    override val exitCode: UInt = 13u
    override val ruleName: String = "out-of-gas"

    override fun toString(): String =
        "TVM out of gas error (exit code: $exitCode): gas consumed: $consumedGas, limit: $gasLimit"
}

@Serializable
data class TvmUnknownFailure(override val exitCode: UInt): TvmExit {
    override val ruleName: String = "user-defined-error"

    override fun toString(): String = "TVM user defined error with exit code $exitCode"
}

// TODO add remaining
