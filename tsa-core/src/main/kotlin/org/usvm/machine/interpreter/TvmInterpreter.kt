package org.usvm.machine.interpreter

import io.ksmt.expr.KBitVecValue
import io.ksmt.sort.KBvSort
import io.ksmt.utils.BvUtils.bigIntValue
import io.ksmt.utils.cast
import mu.KLogging
import org.example.org.ton.bytecode.TvmContractCode
import org.example.org.ton.bytecode.TvmField
import org.example.org.ton.bytecode.TvmFieldImpl
import org.example.org.usvm.machine.state.TvmCellUnderflow
import org.example.org.usvm.machine.state.TvmIntegerOverflow
import org.example.org.usvm.machine.state.TvmMethodResult
import org.ton.bytecode.*
import org.ton.targets.TvmTarget
import org.ton.bytecode.TvmType
import org.ton.cell.Cell
import org.usvm.*
import org.usvm.collection.array.UArrayIndexLValue
import org.usvm.collection.array.length.UArrayLengthLValue
import org.usvm.collection.field.UFieldLValue
import org.usvm.forkblacklists.UForkBlackList
import org.usvm.machine.TvmContext
import org.usvm.machine.state.*
import org.usvm.solver.USatResult
import org.usvm.targets.UTargetsSet
import org.usvm.util.write
import java.math.BigInteger

typealias TvmStepScope = StepScope<TvmState, TvmType, TvmInst, TvmContext>

class TvmInterpreter(
    private val ctx: TvmContext,
    private val contractCode: TvmContractCode,
    var forkBlackList: UForkBlackList<TvmState, TvmInst> = UForkBlackList.createDefault(),
) : UInterpreter<TvmState>() {
    companion object {
        val logger = object : KLogging() {}.logger
    }

    fun getInitialState(contractCode: TvmContractCode, contractData: Cell, methodId: Int, targets: List<TvmTarget> = emptyList()): TvmState {
        /*val contract = contractCode.methods[0]!!
        val registers = TvmRegisters()
        val currentContinuation = TvmContinuationValue(
            contract,
            TvmStack(ctx),
            TvmRegisters()
        )
        registers.c3 = C3Register(currentContinuation)

        val stack = TvmStack(ctx)
        stack += ctx.mkBv(BigInteger.valueOf(methodId.toLong()), int257sort)
        val state = TvmState(ctx, contract, *//*registers, *//*currentContinuation, stack, TvmCellValue(contractData), targets = UTargetsSet.from(targets))

        val solver = ctx.solver<TvmType>()

        val model = (solver.check(state.pathConstraints) as USatResult).model
        state.models = listOf(model)

        state.callStack.push(contract, returnSite = null)
        state.newStmt(contract.instList.first())

        return state*/
        val method = contractCode.methods[methodId] ?: error("Unknown method $methodId")
        val registers = TvmRegisters()
        val currentContinuation = TvmContinuationValue(
            method,
            TvmStack(ctx),
            TvmRegisters()
        )
        registers.c3 = C3Register(currentContinuation)

        val stack = TvmStack(ctx)
        val state = TvmState(ctx, method, currentContinuation, stack, TvmCellValue(contractData), targets = UTargetsSet.from(targets))
        val solver = ctx.solver<TvmType>()

        val model = (solver.check(state.pathConstraints) as USatResult).model
        state.models = listOf(model)

        state.callStack.push(method, returnSite = null)
        state.newStmt(method.instList.first())

        return state
    }

    override fun step(state: TvmState): StepResult<TvmState> {
        val stmt = state.lastStmt

        logger.debug("Step: {}", stmt)

        val scope = StepScope(state, forkBlackList)

        // handle exception firstly
//        val result = state.methodResult
//        if (result is JcMethodResult.JcException) {
//            handleException(scope, result, stmt)
//            return scope.stepResult()
//        }

        when (stmt) {
            is TvmStackInst -> visitStackInst(scope, stmt)
            is TvmTupleInst -> visitTupleInst(scope, stmt)
            is TvmConstantInst -> visitConstantInst(scope, stmt)
            is TvmArithmeticInst -> visitArithmeticInst(scope, stmt)
            is TvmComparisonInst -> visitComparisonInst(scope, stmt)
            is TvmCellInst -> visitCellInst(scope, stmt)
            is TvmControlFlowInst -> visitControlFlowInst(scope, stmt)
            is TvmExceptionInst -> visitExceptionInst(scope, stmt)
            is TvmDictionaryInst -> visitDictionaryInst(scope, stmt)
            is TvmBlockchainInst -> visitBlockchainInst(scope, stmt)
            is TvmDebugInst -> visitDebugInst(scope, stmt)
            is TvmCodepageInst -> visitCodepageInst(scope, stmt)
//            else -> error("Unknown stmt: $stmt")
        }

        return scope.stepResult()
    }

    private fun visitStackInst(scope: TvmStepScope, stmt: TvmStackInst) {
        when (stmt) {
            is TvmBasicStackInst -> visitBasicStackInst(scope, stmt)
            is TvmCompoundStackInst -> visitCompoundStackInst(scope, stmt)
            is TvmExoticStackInst -> visitExoticStackInst(scope, stmt)
//            else -> error("Unknown stack instruction: $stmt")
        }
    }

    private fun visitBasicStackInst(scope: StepScope<TvmState, TvmType, TvmInst, TvmContext>, stmt: TvmBasicStackInst) {
        when (stmt) {
            is TvmStackNopInst -> {
                // Do nothing
            }
            is TvmStackPopInst -> {
                scope.doWithState {
                    stack.pop(stmt.i)
                }
            }
            is TvmStackPushInst -> {
                scope.doWithState {
                    stack.push(stmt.i)
                }
            }
            is TvmStackXchg0IInst -> {
                // TODO it is not optimal, optimize with [] operators
                val first = stmt.i
                val second = 0

                scope.doWithState {
                    stack.swap(first, second)
                }
            }
            is TvmStackXchgIJInst -> {
                // TODO it is not optimal, optimize with [] operators
                val first = stmt.i
                val second = stmt.j

                scope.doWithState {
                    stack.swap(first, second)
                }
            }
        }

        scope.doWithState {
            newStmt(stmt.nextStmt(contractCode))
        }
    }

    private fun visitCompoundStackInst(
        scope: StepScope<TvmState, TvmType, TvmInst, TvmContext>,
        stmt: TvmCompoundStackInst
    ) {
        TODO("Not yet implemented")
    }

    private fun visitExoticStackInst(
        scope: StepScope<TvmState, TvmType, TvmInst, TvmContext>,
        stmt: TvmExoticStackInst
    ) {
        when (stmt) {
            is TvmBlkDrop2Inst -> {
                scope.doWithState {
                    stack.blkDrop2(stmt.i, stmt.j)
                    newStmt(stmt.nextStmt(contractCode))
                }
            }
        }
    }

    private fun visitTupleInst(scope: TvmStepScope, stmt: TvmTupleInst) {
        when (stmt) {
            is TvmNullInst -> TODO()
            is TvmIsNullInst -> TODO()
            is TvmMkTupleInst -> TODO()
            is TvmNilInst -> TODO()
            is TvmCheckIsTupleInst -> TODO()
            is TvmIsTupleInst -> TODO()
            is TvmTupleExplodeInst -> TODO()
            is TvmTupleIndexQInst -> TODO()
            is TvmTupleIndexInst -> TODO()
            is TvmTupleLenInst -> TODO()
            is TvmTupleSetInst -> TODO()
            is TvmTupleSetQInst -> TODO()
            is TvmUntupleInst -> TODO()
            else -> error("Unknown stack instruction: $stmt")
        }
    }

    private fun visitConstantInst(scope: TvmStepScope, stmt: TvmConstantInst) {
        when (stmt) {
            is TvmPushInt4Inst -> {
                val value = stmt.i
                scope.doWithState {
                    stack += value.toBv257()
                }
            }
            is TvmPushInt8Inst -> {
                val value = stmt.x
                scope.doWithState {
                    stack += value.toBv257()
                }
            }
            is TvmPushInt16Inst -> {
                val value = stmt.x
                scope.doWithState {
                    stack += value.toBv257()
                }
            }

            is TvmPushRefInst -> TODO()
//            else -> error("Unknown stmt: $stmt")
        }

        scope.doWithState {
            newStmt(stmt.nextStmt(contractCode))
        }
    }

    private fun visitArithmeticInst(scope: TvmStepScope, stmt: TvmArithmeticInst) {
        with(ctx) {
            val result = when (stmt) {
                is TvmAddInst -> {
                    val (secondOperand, firstOperand) = scope.calcOnState {
                        stack.removeLast(int257sort) to stack.removeLast(int257sort)
                    }
                    // TODO optimize using ksmt implementation?
                    val resOverflow = mkBvAddNoOverflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkOverflow(resOverflow, scope) ?: return
                    val resUnderflow = mkBvAddNoUnderflowExpr(firstOperand, secondOperand)
                    checkUnderflow(resUnderflow, scope) ?: return

                    mkBvAddExpr(firstOperand, secondOperand)
                }

                is TvmSubInst -> {
                    val (secondOperand, firstOperand) = scope.calcOnState {
                        stack.removeLast(int257sort) to stack.removeLast(int257sort)
                    }
                    // TODO optimize using ksmt implementation?
                    val resOverflow = mkBvSubNoOverflowExpr(firstOperand, secondOperand)
                    checkOverflow(resOverflow, scope) ?: return
                    val resUnderflow = mkBvSubNoUnderflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkUnderflow(resUnderflow, scope) ?: return

                    mkBvSubExpr(firstOperand, secondOperand)
                }

                is TvmMulInst -> {
                    val (secondOperand, firstOperand) = scope.calcOnState {
                        stack.removeLast(int257sort) to stack.removeLast(int257sort)
                    }
                    // TODO optimize using ksmt implementation?
                    val resOverflow = mkBvMulNoOverflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkOverflow(resOverflow, scope) ?: return
                    val resUnderflow = mkBvMulNoUnderflowExpr(firstOperand, secondOperand)
                    checkUnderflow(resUnderflow, scope) ?: return

                    mkBvMulExpr(firstOperand, secondOperand)
                }

                is TvmDivInst -> {
                    val (secondOperand, firstOperand) = scope.calcOnState {
                        stack.removeLast(int257sort) to stack.removeLast(int257sort)
                    }
                    checkDivisionByZero(secondOperand, scope) ?: return

                    mkBvSignedDivExpr(firstOperand, secondOperand)
                }

                is TvmModInst -> {
                    val (secondOperand, firstOperand) = scope.calcOnState {
                        stack.removeLast(int257sort) to stack.removeLast(int257sort)
                    }
                    checkDivisionByZero(secondOperand, scope) ?: return

                    mkBvSignedModExpr(firstOperand, secondOperand)
                }
//            else -> error("Unknown stmt: $stmt")
                is TvmAddConstInst -> {
                    val firstOperand = scope.calcOnState { stack.removeLast(int257sort) }
                    val secondOperand = stmt.c.toBv257()

                    // TODO optimize using ksmt implementation?
                    val resOverflow = mkBvAddNoOverflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkOverflow(resOverflow, scope) ?: return
                    val resUnderflow = mkBvAddNoUnderflowExpr(firstOperand, secondOperand)
                    checkUnderflow(resUnderflow, scope) ?: return

                    mkBvAddExpr(firstOperand, secondOperand)
                }
                is TvmMulConstInst -> {
                    val firstOperand = scope.calcOnState { stack.removeLast(int257sort) }
                    val secondOperand = stmt.c.toBv257()

                    // TODO optimize using ksmt implementation?
                    val resOverflow = mkBvMulNoOverflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkOverflow(resOverflow, scope) ?: return
                    val resUnderflow = mkBvMulNoUnderflowExpr(firstOperand, secondOperand)
                    checkUnderflow(resUnderflow, scope) ?: return

                    mkBvMulExpr(firstOperand, secondOperand)
                }
            }

            scope.doWithState {
                stack += result
                newStmt(stmt.nextStmt(contractCode))
            }
        }
    }

    private fun checkDivisionByZero(expr: UExpr<out USort>, scope: TvmStepScope) = with(ctx) {
        val sort = expr.sort
        if (sort !is UBvSort) {
            return Unit
        }
        val neqZero = mkEq(expr.cast(), mkBv(0, sort)).not()
        scope.fork(
            neqZero,
            blockOnFalseState = setFailure(TvmIntegerOverflow)
        )
    }

    private fun checkOverflow(overflowExpr: UBoolExpr, scope: TvmStepScope): Unit? = scope.fork(
        overflowExpr,
        blockOnFalseState = setFailure(TvmIntegerOverflow)
    )

    private fun checkUnderflow(underflowExpr: UBoolExpr, scope: TvmStepScope): Unit? = scope.fork(
        underflowExpr,
        blockOnFalseState = setFailure(TvmIntegerOverflow)
    )

    private fun setFailure(failure: TvmMethodResult.TvmFailure): (TvmState) -> Unit = { state ->
        state.methodResult = failure
    }

    private fun visitComparisonInst(scope: TvmStepScope, stmt: TvmComparisonInst) {
        when (stmt) {
            is TvmEqualInst -> TODO()
            is TvmGreaterInst -> TODO()
            is TvmLeqInst -> TODO()
            is TvmLessInst -> TODO()
            is TvmNeqInst -> TODO()
            is TvmSemptyInst -> {
                scope.doWithState {
                    // TODO for now always push false, but later do it properly
                    @Suppress("UNUSED_VARIABLE")
                    val slice = stack.removeLast(ctx.addressSort)
                    stack += falseValue
                    newStmt(stmt.nextStmt(contractCode))
                }
            }
            is TvmSignInst -> TODO()
//            else -> error("Unknown stmt: $stmt")
        }
    }

    private fun visitCellInst(scope: TvmStepScope, stmt: TvmCellInst) {
        when (stmt) {
            is TvmCellSerializationInst -> visitCellSerializationInst(scope, stmt)
            is TvmCellDeserializationInst -> visitCellDeserializationInst(scope, stmt)
//            else -> error("Unknown stmt: $stmt")
        }
    }

    private fun visitCellDeserializationInst(
        scope: StepScope<TvmState, TvmType, TvmInst, TvmContext>,
        stmt: TvmCellDeserializationInst
    ) {
        when (stmt) {
            is TvmCellToSliceInst -> visitCellToSliceInst(scope, stmt)
            is TvmEndSliceInst -> TODO()
            is TvmLoadRef -> TODO()
            is TvmLoadUnsignedIntInst -> visitLoadUnsignedIntInst(scope, stmt)
//            else -> error("Unknown stmt: $stmt")
        }
    }

    private fun visitLoadUnsignedIntInst(
        scope: StepScope<TvmState, TvmType, TvmInst, TvmContext>,
        stmt: TvmLoadUnsignedIntInst
    ) {
        with(ctx) {
            val slice = scope.calcOnState { stack.removeLast(addressSort) }
            val bitsLen = stmt.c

            val dataFieldLValue = UFieldLValue(addressSort, slice, cellDataField)
            val data = scope.calcOnState { memory.read(dataFieldLValue) }

            val dataLengthLValue = UArrayLengthLValue(data, TvmBoolType, sizeSort)
            val dataLength = scope.calcOnState { memory.read(dataLengthLValue) }

            val dataPosLValue = UFieldLValue(sizeSort, slice, sliceDataPosField)
            val dataPos = scope.calcOnState { memory.read(dataPosLValue) }

            val readingEnd = mkSizeAddExpr(dataPos, mkSizeExpr(bitsLen))
            val readingConstraint = mkBvSignedLessOrEqualExpr(readingEnd, dataLength)

            scope.fork(
                readingConstraint,
                blockOnFalseState = setFailure(TvmCellUnderflow)
            ) ?: return

            scope.doWithState {
                newStmt(stmt.nextStmt(contractCode))
            }
        }
    }

    private fun visitCellToSliceInst(
        scope: StepScope<TvmState, TvmType, TvmInst, TvmContext>,
        stmt: TvmCellToSliceInst
    ) {
        TODO("Not yet implemented")
    }

    private fun visitCellSerializationInst(
        scope: StepScope<TvmState, TvmType, TvmInst, TvmContext>,
        stmt: TvmCellSerializationInst
    ) {
        with(ctx) {
            val constraint = scope.calcOnState {
                val cell = stack.removeLast(addressSort)
                val slice = memory.allocConcrete(TvmSliceType)
                stack += slice

                val cellDataLValue = UFieldLValue(addressSort, cell, cellDataField)
                val cellIdLValue = UFieldLValue(bv32Sort, cell, cellIdField)
                val cellRefsLValue = UFieldLValue(addressSort, cell, cellRefsField)

                val sliceDataLValue = UFieldLValue(addressSort, slice, cellDataField)
                val sliceIdLValue = UFieldLValue(bv32Sort, slice, cellIdField)
                val sliceDataPosLValue = UFieldLValue(sizeSort, slice, sliceDataPosField)
                val sliceRefPosLValue = UFieldLValue(sizeSort, slice, sliceRefPosField)
                val sliceRefsLValue = UFieldLValue(addressSort, slice, cellRefsField)

                val cellData = memory.read(cellDataLValue)
                val cellId = memory.read(cellIdLValue)
                val cellRefs = memory.read(cellRefsLValue)

                val sliceData = memory.read(sliceDataLValue)
//                val sliceId = memory.read(sliceIdLValue)
//                val sliceDataPos = memory.read(sliceDataPosLValue)
//                val sliceRefPos = memory.read(sliceRefPosLValue)
                val sliceRefs = memory.read(sliceRefsLValue)

                // TODO should we add length constraints?
                val dataConstraint = mkHeapRefEq(cellData, sliceData)
                val refsConstraint = mkHeapRefEq(cellRefs, sliceRefs)

                memory.write(sliceIdLValue, cellId)
                memory.write(sliceDataPosLValue, mkSizeExpr(0))
                memory.write(sliceRefPosLValue, mkSizeExpr(0))

                mkAnd(dataConstraint, refsConstraint)
            }

            scope.assert(constraint) ?: TODO("Specify error")

            scope.doWithState {
                newStmt(stmt.nextStmt(contractCode))
            }
        }
    }

    private fun visitControlFlowInst(scope: TvmStepScope, stmt: TvmControlFlowInst) {
        when (stmt) {
            is TvmUnconditionalControlFlowInst -> visitTvmUnconditionalControlFlowInst(scope, stmt)
            is TvmConditionalControlFlowInst -> visitTvmConditionalControlFlowInst(scope, stmt)
            is TvmLoopInst -> TODO()
            is TvmSaveControlFlowInst -> TODO()
            is TvmDictionaryJumpInst -> visitTvmDictionaryJumpInst(scope, stmt)
//            else -> error("Unknown stmt: $stmt")
        }
    }

    private fun visitTvmUnconditionalControlFlowInst(
        scope: TvmStepScope,
        stmt: TvmUnconditionalControlFlowInst
    ) {
        when (stmt) {
            is TvmExecuteInst -> TODO()
            is TvmJumpXInst -> TODO()
            is TvmReturnInst -> {
                scope.doWithState { returnFromMethod() }
            }
        }
    }

    private fun visitTvmConditionalControlFlowInst(
        scope: TvmStepScope,
        stmt: TvmConditionalControlFlowInst
    ) {
        when (stmt) {
            is TvmIfInst -> TODO()
            is TvmIfRetInst -> {
                scope.doWithState {
                    val operand = stack.removeLast(int257sort)
                    with(ctx) {
                        val neqZero = mkEq(operand, falseValue).not()
                        scope.fork(
                            neqZero,
                            blockOnFalseState = { newStmt(stmt.nextStmt(contractCode)) }
                        ) ?: return@with

                        // TODO check NaN for integer overflow exception

                        scope.doWithState { returnFromMethod() }
                    }
                }
            }
        }
    }

    private fun visitTvmDictionaryJumpInst(scope: TvmStepScope, stmt: TvmDictionaryJumpInst) {
        when (stmt) {
            is TvmCallDictInst -> {
                val methodId = stmt.n

                scope.doWithState {
//                    stack += argument.toBv257()
////                    val c3Continuation = registers.c3!!.value
//                    val contractMethod = contractCode.methods[0]!!
////                    val continuationStmt = c3Continuation.method.instList[c3Continuation.currentInstIndex]
//                    val continuationStmt = contractMethod.instList.first()
//                    val nextStmt = stmt.nextStmt(contractCode)
//
//                    callStack.push(contractCode.methods[continuationStmt.location.methodId]!!, nextStmt)
//                    newStmt(continuationStmt)

                    val nextStmt = stmt.nextStmt(contractCode)
                    val nextMethod = contractCode.methods[methodId] ?: error("Unknown method with id $methodId")
                    val methodFirstStmt = nextMethod.instList.first()
                    callStack.push(nextMethod, nextStmt)

                    newStmt(methodFirstStmt)
                }
            }
//            else -> error("Unknown stmt: $stmt")
        }
    }

    private fun visitExceptionInst(scope: TvmStepScope, stmt: TvmExceptionInst) {
        when (stmt) {
            is TvmThrowInst -> TODO()
            is TvmHandlingExceptionInst -> TODO()
//            else -> error("Unknown stmt: $stmt")
        }
    }

    private fun visitDictionaryInst(scope: TvmStepScope, stmt: TvmDictionaryInst) {
        when (stmt) {
            is TvmDictCreationInst -> visitDictCreationInst(scope, stmt)
            is TvmDictSerializationInst -> visitDictSerializationInst(scope, stmt)
            is TvmDictGetInst -> visitDictGetInst(scope, stmt)
            is TvmDictModificationInst -> visitDictModificationInst(scope, stmt)
            is TvmDictControlFlowInst -> visitDictControlFlowInst(scope, stmt)
//            else -> error("Unknown stmt: $stmt")
        }
    }

    private fun visitDictControlFlowInst(
        scope: StepScope<TvmState, TvmType, TvmInst, TvmContext>,
        stmt: TvmDictControlFlowInst
    ) {
        when (stmt) {
            is TvmDictGetJumpZInst -> {
                val methodId =
                    (scope.calcOnState { stack.removeLast(int257sort) } as KBitVecValue<KBvSort>).bigIntValue()
                val method = contractCode.methods[methodId.toInt()]!!
                val methodFirstStmt = method.instList.first()

                scope.doWithState {
                    val nextStmt = stmt.nextStmt(contractCode)
                    callStack.push(method, nextStmt)
                    newStmt(methodFirstStmt)
                }
            }

            is TvmDictPushConst -> {
                val keyLength = stmt.n
                val currentContinuation = scope.calcOnState { currentContinuation }
//                val nextRef = currentContinuation.slice.loadRef()

                scope.calcOnState {
//                    stack += ctx.mkHe
                }

                scope.doWithState { newStmt(stmt.nextStmt(contractCode)) }
            }
        }
    }

    private fun visitDictModificationInst(
        scope: StepScope<TvmState, TvmType, TvmInst, TvmContext>,
        stmt: TvmDictModificationInst
    ) {
        TODO("Not yet implemented")
    }

    private fun visitDictGetInst(scope: StepScope<TvmState, TvmType, TvmInst, TvmContext>, stmt: TvmDictGetInst) {
        TODO("Not yet implemented")
    }

    private fun visitDictSerializationInst(
        scope: StepScope<TvmState, TvmType, TvmInst, TvmContext>,
        stmt: TvmDictSerializationInst
    ) {
        TODO("Not yet implemented")
    }

    private fun visitDictCreationInst(
        scope: StepScope<TvmState, TvmType, TvmInst, TvmContext>,
        stmt: TvmDictCreationInst
    ) {
        TODO("Not yet implemented")
    }

    private fun visitBlockchainInst(scope: TvmStepScope, stmt: TvmBlockchainInst) {
        when (stmt) {
            else -> error("Unknown stmt: $stmt")
        }
    }

    private fun visitDebugInst(scope: TvmStepScope, stmt: TvmDebugInst) {
        when (stmt) {
            is TvmAnyDebugInst -> {
                // Do nothing

            }
//            else -> error("Unknown stmt: $stmt")
        }
    }

    private fun visitCodepageInst(scope: TvmStepScope, stmt: TvmCodepageInst) {
        when (stmt) {
            is TvmSetZeroCodepageInst -> {
                // Do nothing
            }
//            else -> error("Unknown stmt: $stmt")
            is TvmSetCodepageInst -> {
                // Do nothing
            }
        }

        scope.doWithState { newStmt(stmt.nextStmt(contractCode)) }
    }

    private fun ensureCellCorrectness(cell: UHeapRef, scope: TvmStepScope) {
        with(ctx) {
            val dataLValue = UFieldLValue(addressSort, cell, cellDataField)
            val idLValue = UFieldLValue(bv32Sort, cell, cellIdField)
            val refsLValue = UFieldLValue(addressSort, cell, cellRefsField)

            scope.calcOnState {
                val data = memory.read(dataLValue)
                val id = memory.read(idLValue)
                val refs = memory.read(refsLValue)

                val dataLengthLValue = UArrayLengthLValue(data, TvmBoolType, sizeSort)
                val refsLengthLValue = UArrayLengthLValue(refs, TvmBoolType, sizeSort)

                val dataLength = memory.read(dataLengthLValue)
                val refsLength = memory.read(refsLengthLValue)

                val dataNotNullConstraint = mkHeapRefEq(data, nullRef).not()
                val refsNotNullConstraint = mkHeapRefEq(refs, nullRef).not()
                val dataLengthConstraint = mkOr(
                    mkBvSignedLessOrEqualExpr(mkBv(0), dataLength),
                    mkBvSignedLessOrEqualExpr(dataLength, mkBv(1023)), // TODO move to the constant
                )
                val refsLengthConstraint = mkOr(
                    mkBvSignedLessOrEqualExpr(mkBv(0), refsLength),
                    mkBvSignedLessOrEqualExpr(refsLength, mkBv(4)), // TODO move to the constant
                )

                val acyclicConstraints = (0..4).map {
                    val refCellReadingLValue = UArrayIndexLValue(addressSort, refs, mkBv(it), TvmCellType)
                    val refCell = memory.read(refCellReadingLValue)
                    val refCellIdLValue = UFieldLValue(bv32Sort, refCell, cellIdField)
                    val refCellId = memory.read(refCellIdLValue)

                    // Making constraint that all refs have ids > than current cell id ensures acyclic (as top-sort)
                    mkBvSignedLessExpr(id, refCellId)
                }

                scope.assert(
                    mkAnd(
                        dataNotNullConstraint,
                        refsNotNullConstraint,
                        dataLengthConstraint,
                        refsLengthConstraint,
                        *acyclicConstraints.toTypedArray()
                    )
                ) ?: TODO("Specify error")
            }
        }
    }

    private fun Int.toBv257(): KBitVecValue<UBvSort> = ctx.mkBv(BigInteger.valueOf(toLong()), int257sort)

    private val int257sort: UBvSort = ctx.mkBvSort(257u)

    private val trueValue: KBitVecValue<UBvSort> = (-1).toBv257()
    private val falseValue: KBitVecValue<UBvSort> = 0.toBv257()

    private val cellDataField: TvmField = TvmFieldImpl(TvmCellType, "data")
    private val cellIdField: TvmField = TvmFieldImpl(TvmCellType, "id")
    private val cellRefsField: TvmField = TvmFieldImpl(TvmCellType, "refs")

    private val sliceDataPosField: TvmField = TvmFieldImpl(TvmSliceType, "dataPos")
    private val sliceRefPosField: TvmField = TvmFieldImpl(TvmSliceType, "refPos")
}
