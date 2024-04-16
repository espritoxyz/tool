package org.usvm.machine.interpreter

import org.ton.bytecode.TvmAppCryptoChksignuInst
import org.ton.bytecode.TvmAppCryptoHashcuInst
import org.ton.bytecode.TvmAppCryptoHashsuInst
import org.ton.bytecode.TvmAppCryptoInst
import org.ton.bytecode.TvmBuilderType
import org.ton.bytecode.TvmCellType
import org.ton.bytecode.TvmIntegerType
import org.ton.bytecode.TvmReferenceType
import org.ton.bytecode.TvmSliceType
import org.usvm.UHeapRef
import org.usvm.api.makeSymbolicPrimitive
import org.usvm.machine.TvmContext
import org.usvm.machine.state.TvmStack
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.slicePreloadDataBits
import org.usvm.machine.state.takeLastBuilder
import org.usvm.machine.state.takeLastCell
import org.usvm.machine.state.takeLastInt
import org.usvm.machine.state.takeLastSlice
import org.usvm.machine.state.throwCellUnderflowError
import org.usvm.machine.state.throwTypeCheckError

class TvmCryptoInterpreter(private val ctx: TvmContext) {
    fun visitCryptoStmt(scope: TvmStepScope, stmt: TvmAppCryptoInst) {
        when (stmt) {
            is TvmAppCryptoHashsuInst -> visitSingleHashInst(scope, stmt, operandType = TvmSliceType)
            is TvmAppCryptoHashcuInst -> visitSingleHashInst(scope, stmt, operandType = TvmCellType)
            is TvmAppCryptoChksignuInst -> visitCheckSignatureInst(scope, stmt)
            else -> TODO("$stmt")
        }
    }

    private fun visitSingleHashInst(scope: TvmStepScope, stmt: TvmAppCryptoInst, operandType: TvmReferenceType) {
        require(operandType != TvmBuilderType) {
            "A single hash function for builders does not exist"
        }

        scope.consumeDefaultGas(stmt)

        scope.calcOnState {
            stack.popHashableStackValue(operandType)

            // TODO hash must be deterministic - make a region for representation hashes?
            val hash = makeSymbolicPrimitive(ctx.int257sort)

            stack.add(hash, TvmIntegerType)
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitCheckSignatureInst(scope: TvmStepScope, stmt: TvmAppCryptoChksignuInst) {
        scope.consumeDefaultGas(stmt)

        val key = scope.calcOnState { stack.takeLastInt() }
        val signature = scope.calcOnState { stack.takeLastSlice() }
        if (signature == null) {
            scope.doWithState(throwTypeCheckError)
            return
        }

        val hash = scope.calcOnState { stack.takeLastInt() }

        // Check that signature is correct - it contains at least 512 bits
        val bits = scope.slicePreloadDataBits(signature, bits = 512)
        if (bits == null) {
            scope.doWithState {
                throwCellUnderflowError(this)
            }

            return
        }

        // TODO do real check?
        val condition = scope.calcOnState { makeSymbolicPrimitive(ctx.boolSort) }
        with(ctx) {
            scope.fork(
                condition,
                blockOnTrueState = {
                    stack.add(zeroValue, TvmIntegerType)
                    newStmt(stmt.nextStmt())
                },
                blockOnFalseState =  {
                    stack.add(minusOneValue, TvmIntegerType)
                    newStmt(stmt.nextStmt())
                }
            )
        }
    }

    context(TvmState)
    private fun TvmStack.popHashableStackValue(referenceType: TvmReferenceType): UHeapRef? =
        when (referenceType) {
            TvmBuilderType -> takeLastBuilder()
            TvmCellType -> takeLastCell()
            TvmSliceType -> takeLastSlice()
        }
}
