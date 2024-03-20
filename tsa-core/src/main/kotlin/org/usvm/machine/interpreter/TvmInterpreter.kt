package org.usvm.machine.interpreter

import io.ksmt.expr.KBitVec32Value
import io.ksmt.expr.KBitVecValue
import io.ksmt.sort.KBvSort
import io.ksmt.utils.BvUtils.bigIntValue
import io.ksmt.utils.cast
import mu.KLogging
import org.ton.bytecode.TvmAliasInst
import org.ton.bytecode.TvmArithmBasicAddInst
import org.ton.bytecode.TvmArithmBasicAddconstInst
import org.ton.bytecode.TvmArithmBasicDecInst
import org.ton.bytecode.TvmArithmBasicIncInst
import org.ton.bytecode.TvmArithmBasicInst
import org.ton.bytecode.TvmArithmBasicMulInst
import org.ton.bytecode.TvmArithmBasicMulconstInst
import org.ton.bytecode.TvmArithmBasicSubInst
import org.ton.bytecode.TvmArithmDivDivInst
import org.ton.bytecode.TvmArithmDivInst
import org.ton.bytecode.TvmArithmDivModInst
import org.ton.bytecode.TvmBuilderType
import org.ton.bytecode.TvmCellBuildEndcInst
import org.ton.bytecode.TvmCellBuildInst
import org.ton.bytecode.TvmCellBuildNewcInst
import org.ton.bytecode.TvmCellBuildStuInst
import org.ton.bytecode.TvmCellParseCtosInst
import org.ton.bytecode.TvmCellParseEndsInst
import org.ton.bytecode.TvmCellParseInst
import org.ton.bytecode.TvmCellParseLdrefInst
import org.ton.bytecode.TvmCellParseLduInst
import org.ton.bytecode.TvmCellType
import org.ton.bytecode.TvmCellValue
import org.ton.bytecode.TvmCodepageInst
import org.ton.bytecode.TvmCompareIntEqintInst
import org.ton.bytecode.TvmCompareIntGreaterInst
import org.ton.bytecode.TvmCompareIntInst
import org.ton.bytecode.TvmCompareIntLeqInst
import org.ton.bytecode.TvmCompareIntLessInst
import org.ton.bytecode.TvmCompareIntNeqInst
import org.ton.bytecode.TvmCompareIntSgnInst
import org.ton.bytecode.TvmCompareOtherInst
import org.ton.bytecode.TvmCompareOtherSemptyInst
import org.ton.bytecode.TvmConstDataInst
import org.ton.bytecode.TvmConstDataPushcontShortInst
import org.ton.bytecode.TvmConstDataPushsliceInst
import org.ton.bytecode.TvmConstIntInst
import org.ton.bytecode.TvmConstIntOneAliasInst
import org.ton.bytecode.TvmConstIntPushint16Inst
import org.ton.bytecode.TvmConstIntPushint4Inst
import org.ton.bytecode.TvmConstIntPushint8Inst
import org.ton.bytecode.TvmConstIntPushintLongInst
import org.ton.bytecode.TvmConstIntPushnanInst
import org.ton.bytecode.TvmConstIntPushnegpow2Inst
import org.ton.bytecode.TvmConstIntPushpow2Inst
import org.ton.bytecode.TvmConstIntPushpow2decInst
import org.ton.bytecode.TvmConstIntTenAliasInst
import org.ton.bytecode.TvmConstIntTrueAliasInst
import org.ton.bytecode.TvmConstIntTwoAliasInst
import org.ton.bytecode.TvmConstIntZeroAliasInst
import org.ton.bytecode.TvmContBasicExecuteInst
import org.ton.bytecode.TvmContBasicInst
import org.ton.bytecode.TvmContBasicRetInst
import org.ton.bytecode.TvmContConditionalIfelseInst
import org.ton.bytecode.TvmContConditionalIfjmpInst
import org.ton.bytecode.TvmContConditionalIfretInst
import org.ton.bytecode.TvmContConditionalInst
import org.ton.bytecode.TvmContDictCalldictInst
import org.ton.bytecode.TvmContDictInst
import org.ton.bytecode.TvmContRegistersInst
import org.ton.bytecode.TvmContRegistersPopctrInst
import org.ton.bytecode.TvmContRegistersPushctrInst
import org.ton.bytecode.TvmContinuationType
import org.ton.bytecode.TvmContinuationValue
import org.ton.bytecode.TvmContractCode
import org.ton.bytecode.TvmDebugInst
import org.ton.bytecode.TvmDictSpecialDictigetjmpzInst
import org.ton.bytecode.TvmDictSpecialDictpushconstInst
import org.ton.bytecode.TvmDictSpecialInst
import org.ton.bytecode.TvmExceptionsInst
import org.ton.bytecode.TvmExceptionsThrowShortInst
import org.ton.bytecode.TvmExceptionsThrowargInst
import org.ton.bytecode.TvmField
import org.ton.bytecode.TvmFieldImpl
import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmIntegerType
import org.ton.bytecode.TvmLambda
import org.ton.bytecode.TvmNullType
import org.ton.bytecode.TvmSliceType
import org.ton.bytecode.TvmStackBasicInst
import org.ton.bytecode.TvmStackBasicNopInst
import org.ton.bytecode.TvmStackBasicPopInst
import org.ton.bytecode.TvmStackBasicPushInst
import org.ton.bytecode.TvmStackBasicXchg0iInst
import org.ton.bytecode.TvmStackBasicXchg0iLongInst
import org.ton.bytecode.TvmStackBasicXchg1iInst
import org.ton.bytecode.TvmStackBasicXchgIjInst
import org.ton.bytecode.TvmStackComplexBlkdrop2Inst
import org.ton.bytecode.TvmStackComplexInst
import org.ton.bytecode.TvmSubSliceSerializedLoader
import org.ton.bytecode.TvmTupleInst
import org.ton.bytecode.TvmTupleNullInst
import org.ton.bytecode.TvmTupleType
import org.ton.bytecode.TvmType
import org.ton.cell.Cell
import org.ton.targets.TvmTarget
import org.usvm.StepResult
import org.usvm.StepScope
import org.usvm.UBoolExpr
import org.usvm.UBvSort
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.UInterpreter
import org.usvm.URegisterReading
import org.usvm.USort
import org.usvm.api.writeField
import org.usvm.collection.field.UFieldLValue
import org.usvm.forkblacklists.UForkBlackList
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.Companion.MAX_DATA_LENGTH
import org.usvm.machine.TvmContext.Companion.MAX_REFS_NUMBER
import org.usvm.machine.state.C3Register
import org.usvm.machine.state.C4Register
import org.usvm.machine.state.TvmCellOverflow
import org.usvm.machine.state.TvmCellUnderflow
import org.usvm.machine.state.TvmIntegerOverflow
import org.usvm.machine.state.TvmMethodResult
import org.usvm.machine.state.TvmRegisters
import org.usvm.machine.state.TvmStack
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.TvmUnknownFailure
import org.usvm.machine.state.doWithStateCtx
import org.usvm.machine.state.generateSymbolicCell
import org.usvm.machine.state.lastStmt
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.readCellRef
import org.usvm.machine.state.returnFromMethod
import org.usvm.machine.state.writeCellRef
import org.usvm.mkSizeAddExpr
import org.usvm.mkSizeExpr
import org.usvm.mkSizeGeExpr
import org.usvm.mkSizeLeExpr
import org.usvm.mkSizeLtExpr
import org.usvm.sizeSort
import org.usvm.solver.USatResult
import org.usvm.targets.UTargetsSet
import org.usvm.util.write
import java.math.BigInteger

typealias TvmStepScope = StepScope<TvmState, TvmType, TvmInst, TvmContext>

// TODO there are a lot of `scope.calcOnState` and `scope.doWithState` invocations that are not inline - optimize it
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
        // TODO for now, ignore contract data value
//        registers.c4 = C4Register(TvmCellValue(contractData))

        val stack = TvmStack(ctx)
        val state = TvmState(ctx, method, currentContinuation, stack, registers, targets = UTargetsSet.from(targets))
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

        visit(scope, stmt)

        return scope.stepResult()
    }

    private fun visit(scope: TvmStepScope, stmt: TvmInst) {
        when (stmt) {
            is TvmStackBasicInst -> visitBasicStackInst(scope, stmt)
            is TvmStackComplexInst -> visitComplexStackInst(scope, stmt)
            is TvmConstIntInst -> visitConstantIntInst(scope, stmt)
            is TvmConstDataInst -> visitConstantDataInst(scope, stmt)
            is TvmArithmBasicInst -> visitArithmeticInst(scope, stmt)
            is TvmArithmDivInst -> visitArithmeticDivInst(scope, stmt)
            is TvmCompareIntInst -> visitComparisonIntInst(scope, stmt)
            is TvmCompareOtherInst -> visitComparisonOtherInst(scope, stmt)
            is TvmCellBuildInst -> visitCellBuildInst(scope, stmt)
            is TvmCellParseInst -> visitCellParseInst(scope, stmt)
            is TvmContBasicInst -> visitTvmBasicControlFlowInst(scope, stmt)
            is TvmContConditionalInst -> visitTvmConditionalControlFlowInst(scope, stmt)
            is TvmContRegistersInst -> visitTvmSaveControlFlowInst(scope, stmt)
            is TvmContDictInst -> visitTvmDictionaryJumpInst(scope, stmt)
            is TvmExceptionsInst -> visitExceptionInst(scope, stmt)
            is TvmDebugInst -> visitDebugInst(scope, stmt)
            is TvmCodepageInst -> visitCodepageInst(scope, stmt)
            is TvmDictSpecialInst -> visitDictControlFlowInst(scope, stmt)
            is TvmTupleInst -> visitTvmTupleInst(scope, stmt)
            else -> TODO("$stmt")
        }
    }

    private fun visitBasicStackInst(scope: TvmStepScope, stmt: TvmStackBasicInst) {
        when (stmt) {
            is TvmStackBasicNopInst -> {
                // Do nothing
            }
            is TvmStackBasicPopInst -> doPop(scope, stmt.i)
            is TvmStackBasicPushInst -> doPush(scope, stmt.i)
            is TvmStackBasicXchg0iInst -> doXchg(scope, stmt.i, 0)
            is TvmStackBasicXchgIjInst -> doXchg(scope, stmt.i, stmt.j)
            is TvmStackBasicXchg1iInst -> doXchg(scope, stmt.i, 1)
            is TvmStackBasicXchg0iLongInst -> doXchg(scope, stmt.i, 0)
            is TvmAliasInst -> visit(scope, stmt.resolveAlias())
        }

        scope.doWithState {
            newStmt(stmt.nextStmt(contractCode, currentContinuation))
        }
    }

    private fun doXchg(scope: TvmStepScope, first: Int, second: Int) {
        scope.doWithState {
            stack.swap(first, second)
        }
    }

    private fun doPop(scope: TvmStepScope, i: Int) {
        scope.doWithState {
            stack.pop(i)
        }
    }

    private fun doPush(scope: TvmStepScope, i: Int) {
        scope.doWithState {
            stack.push(i)
        }
    }

    private fun visitComplexStackInst(
        scope: TvmStepScope,
        stmt: TvmStackComplexInst
    ) {
        when (stmt) {
            is TvmStackComplexBlkdrop2Inst -> {
                scope.doWithState {
                    stack.blkDrop2(stmt.i, stmt.j)
                    newStmt(stmt.nextStmt(contractCode, currentContinuation))
                }
            }
            else -> TODO("$stmt")
        }
    }

    private fun visitConstantIntInst(scope: TvmStepScope, stmt: TvmConstIntInst) {
        scope.doWithState {
            val value = stmt.bv257value(ctx)
            stack.add(value, TvmIntegerType)
            newStmt(stmt.nextStmt(contractCode, currentContinuation))
        }
    }

    private fun TvmConstIntInst.bv257value(ctx: TvmContext): UExpr<UBvSort> = with(ctx) {
        when (this@bv257value) {
            is TvmConstIntPushint4Inst -> {
                check(i in 0..15) { "Unexpected $i" }
                val x = if (i > 10) i - 16 else i // Normalize wrt docs
                x.toBv257()
            }
            is TvmConstIntPushint8Inst -> x.toBv257()
            is TvmConstIntPushint16Inst -> x.toBv257()
            is TvmConstIntPushintLongInst -> BigInteger(x).toBv257()
            is TvmConstIntPushnanInst -> TODO()
            is TvmConstIntPushnegpow2Inst -> TODO()
            is TvmConstIntPushpow2Inst -> TODO()
            is TvmConstIntPushpow2decInst -> TODO()
            is TvmConstIntTenAliasInst -> resolveAlias().bv257value(ctx)
            is TvmConstIntTrueAliasInst -> resolveAlias().bv257value(ctx)
            is TvmConstIntTwoAliasInst -> resolveAlias().bv257value(ctx)
            is TvmConstIntOneAliasInst -> resolveAlias().bv257value(ctx)
            is TvmConstIntZeroAliasInst -> resolveAlias().bv257value(ctx)
        }
    }

    private fun visitConstantDataInst(scope: TvmStepScope, stmt: TvmConstDataInst) {
        when (stmt) {
            is TvmConstDataPushcontShortInst -> visitPushContShortInst(scope, stmt)
            is TvmConstDataPushsliceInst -> {
                check(stmt.s.refs.isEmpty()) { "Unexpected refs in $stmt" }

                scope.doWithStateCtx {
                    val cell = memory.allocConcrete(TvmCellType)
                    val slice = memory.allocConcrete(TvmSliceType)

                    memory.writeField(cell, cellRefsLengthField, sizeSort, mkSizeExpr(0), guard = trueExpr)

                    val sliceBits = stmt.s.bitsToBv()
                    val bitLength = sliceBits.sort.sizeBits.toInt()
                    val sliceData = mkBvZeroExtensionExpr(MAX_DATA_LENGTH - bitLength, sliceBits)
                    memory.writeField(cell, cellDataField, cellDataSort, sliceData, guard = trueExpr)
                    memory.writeField(cell, cellDataLengthField, sizeSort, mkSizeExpr(bitLength), guard = trueExpr)

                    memory.writeField(slice, sliceCellField, addressSort, cell, guard = trueExpr)
                    memory.writeField(slice, sliceDataPosField, sizeSort, mkSizeExpr(0), guard = trueExpr)
                    memory.writeField(slice, sliceRefPosField, sizeSort, mkSizeExpr(0), guard = trueExpr)

                    stack.add(slice, TvmSliceType)
                    newStmt(stmt.nextStmt(contractCode, currentContinuation))
                }
            }
            else -> TODO("$stmt")
        }
    }

    private fun visitPushContShortInst(scope: TvmStepScope, stmt: TvmConstDataPushcontShortInst) {
        scope.doWithState {
            val lambda = TvmLambda(stmt.s)
            val continuationValue = TvmContinuationValue(lambda, stack, registers)

            stack += continuationValue
            currentContinuation = continuationValue
            newStmt(stmt.nextStmt(contractCode, currentContinuation))
        }
    }

    private fun visitArithmeticInst(scope: TvmStepScope, stmt: TvmArithmBasicInst) {
        with(ctx) {
            val result = when (stmt) {
                is TvmArithmBasicAddInst -> {
                    val (secondOperand, firstOperand) = scope.calcOnState {
                        stack.takeLastInt() to stack.takeLastInt()
                    }
                    // TODO optimize using ksmt implementation?
                    val resOverflow = mkBvAddNoOverflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkOverflow(resOverflow, scope) ?: return
                    val resUnderflow = mkBvAddNoUnderflowExpr(firstOperand, secondOperand)
                    checkUnderflow(resUnderflow, scope) ?: return

                    mkBvAddExpr(firstOperand, secondOperand)
                }

                is TvmArithmBasicSubInst -> {
                    val (secondOperand, firstOperand) = scope.calcOnState {
                        stack.takeLastInt() to stack.takeLastInt()
                    }
                    // TODO optimize using ksmt implementation?
                    val resOverflow = mkBvSubNoOverflowExpr(firstOperand, secondOperand)
                    checkOverflow(resOverflow, scope) ?: return
                    val resUnderflow = mkBvSubNoUnderflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkUnderflow(resUnderflow, scope) ?: return

                    mkBvSubExpr(firstOperand, secondOperand)
                }

                is TvmArithmBasicMulInst -> {
                    val (secondOperand, firstOperand) = scope.calcOnState {
                        stack.takeLastInt() to stack.takeLastInt()
                    }
                    // TODO optimize using ksmt implementation?
                    val resOverflow = mkBvMulNoOverflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkOverflow(resOverflow, scope) ?: return
                    val resUnderflow = mkBvMulNoUnderflowExpr(firstOperand, secondOperand)
                    checkUnderflow(resUnderflow, scope) ?: return

                    mkBvMulExpr(firstOperand, secondOperand)
                }
//            else -> error("Unknown stmt: $stmt")
                is TvmArithmBasicAddconstInst -> {
                    val firstOperand = scope.calcOnState { stack.takeLastInt() }
                    val secondOperand = stmt.c.toBv257()

                    // TODO optimize using ksmt implementation?
                    val resOverflow = mkBvAddNoOverflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkOverflow(resOverflow, scope) ?: return
                    val resUnderflow = mkBvAddNoUnderflowExpr(firstOperand, secondOperand)
                    checkUnderflow(resUnderflow, scope) ?: return

                    mkBvAddExpr(firstOperand, secondOperand)
                }
                is TvmArithmBasicMulconstInst -> {
                    val firstOperand = scope.calcOnState { stack.takeLastInt() }
                    val secondOperand = stmt.c.toBv257()

                    // TODO optimize using ksmt implementation?
                    val resOverflow = mkBvMulNoOverflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkOverflow(resOverflow, scope) ?: return
                    val resUnderflow = mkBvMulNoUnderflowExpr(firstOperand, secondOperand)
                    checkUnderflow(resUnderflow, scope) ?: return

                    mkBvMulExpr(firstOperand, secondOperand)
                }

                is TvmArithmBasicIncInst -> {
                    val firstOperand = scope.calcOnState { stack.takeLastInt() }
                    val secondOperand = oneValue

                    // TODO optimize using ksmt implementation?
                    val resOverflow = mkBvAddNoOverflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkOverflow(resOverflow, scope) ?: return
                    val resUnderflow = mkBvAddNoUnderflowExpr(firstOperand, secondOperand)
                    checkUnderflow(resUnderflow, scope) ?: return

                    mkBvAddExpr(firstOperand, secondOperand)
                }
                is TvmArithmBasicDecInst -> {
                    val firstOperand = scope.calcOnState { stack.takeLastInt() }
                    val secondOperand = oneValue

                    // TODO optimize using ksmt implementation?
                    val resOverflow = mkBvSubNoOverflowExpr(firstOperand, secondOperand)
                    checkOverflow(resOverflow, scope) ?: return
                    val resUnderflow = mkBvSubNoUnderflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkUnderflow(resUnderflow, scope) ?: return

                    mkBvSubExpr(firstOperand, secondOperand)
                }
                else -> TODO("$stmt")
            }

            scope.doWithState {
                stack.add(result, TvmIntegerType)
                newStmt(stmt.nextStmt(contractCode, currentContinuation))
            }
        }
    }

    private fun visitArithmeticDivInst(scope: TvmStepScope, stmt: TvmArithmDivInst) {
        with(ctx) {
            val result = when (stmt) {
                is TvmArithmDivDivInst -> {
                    val (secondOperand, firstOperand) = scope.calcOnState {
                        stack.takeLastInt() to stack.takeLastInt()
                    }
                    checkDivisionByZero(secondOperand, scope) ?: return

                    mkBvSignedDivExpr(firstOperand, secondOperand)
                }

                is TvmArithmDivModInst -> {
                    val (secondOperand, firstOperand) = scope.calcOnState {
                        stack.takeLastInt() to stack.takeLastInt()
                    }
                    checkDivisionByZero(secondOperand, scope) ?: return

                    mkBvSignedModExpr(firstOperand, secondOperand)
                }

                else -> TODO("$stmt")
            }

            scope.doWithState {
                stack.add(result, TvmIntegerType)
                newStmt(stmt.nextStmt(contractCode, currentContinuation))
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

    private fun visitComparisonIntInst(scope: TvmStepScope, stmt: TvmCompareIntInst) {
        when (stmt) {
            is TvmCompareIntEqintInst -> scope.doWithState {
                val x = stack.takeLastInt()
                val y = ctx.mkBv(stmt.y, x.sort)

                scope.fork(
                    ctx.mkEq(x, y),
                    blockOnFalseState = {
                        stack.add(ctx.falseValue, TvmIntegerType)
                        newStmt(stmt.nextStmt(contractCode, currentContinuation))
                    },
                    blockOnTrueState = {
                        stack.add(ctx.trueValue, TvmIntegerType)
                        newStmt(stmt.nextStmt(contractCode, currentContinuation))
                    }
                )
            }
            is TvmCompareIntGreaterInst -> TODO()
            is TvmCompareIntLeqInst -> TODO()
            is TvmCompareIntLessInst -> TODO()
            is TvmCompareIntNeqInst -> TODO()
            is TvmCompareIntSgnInst -> TODO()
            else -> TODO("Unknown stmt: $stmt")
        }
    }

    private fun visitComparisonOtherInst(scope: TvmStepScope, stmt: TvmCompareOtherInst) {
        when (stmt) {
            is TvmCompareOtherSemptyInst -> {
                with(ctx) {
                    val slice = scope.calcOnState { stack.takeLastSlice() }

                    val cellFieldLValue = UFieldLValue(addressSort, slice, sliceCellField)
                    val cell = scope.calcOnState { memory.read(cellFieldLValue) }

                    val dataLengthLValue = UFieldLValue(sizeSort, cell, cellDataLengthField)
                    val dataLength = scope.calcOnState { memory.read(dataLengthLValue) }

                    val refsLengthLValue = UFieldLValue(sizeSort, cell, cellRefsLengthField)
                    val refsLength = scope.calcOnState { memory.read(refsLengthLValue) }

                    val dataPosLValue = UFieldLValue(sizeSort, slice, sliceDataPosField)
                    val dataPos = scope.calcOnState { memory.read(dataPosLValue) }

                    val refsPosLValue = UFieldLValue(sizeSort, slice, sliceRefPosField)
                    val refsPos = scope.calcOnState { memory.read(refsPosLValue) }

                    val isRemainingDataEmptyConstraint = mkSizeGeExpr(dataPos, dataLength)
                    val areRemainingRefsEmpty = mkSizeGeExpr(refsPos, refsLength)

                    scope.fork(
                        mkAnd(isRemainingDataEmptyConstraint, areRemainingRefsEmpty),
                        blockOnFalseState = {
                            stack.add(falseValue, TvmIntegerType)
                            newStmt(stmt.nextStmt(contractCode, currentContinuation))
                        },
                        blockOnTrueState = {
                            stack.add(trueValue, TvmIntegerType)
                            newStmt(stmt.nextStmt(contractCode, currentContinuation))
                        },
                    )
                }
            }

            else -> TODO("$stmt")
        }
    }

    private fun visitCellParseInst(
        scope: TvmStepScope,
        stmt: TvmCellParseInst
    ) {
        when (stmt) {
            is TvmCellParseCtosInst -> visitCellToSliceInst(scope, stmt)
            is TvmCellParseEndsInst -> visitEndSliceInst(scope, stmt)
            is TvmCellParseLdrefInst -> visitLoadRefInst(scope, stmt)
            is TvmCellParseLduInst -> visitLoadUnsignedIntInst(scope, stmt)
            else -> TODO("Unknown stmt: $stmt")
        }
    }

    private fun visitLoadRefInst(scope: TvmStepScope, stmt: TvmCellParseLdrefInst) {
        with(ctx) {
            val slice = scope.calcOnState { stack.takeLastSlice() }
            val updatedSlice = scope.calcOnState { memory.allocConcrete(TvmSliceType) }

            val cellFieldLValue = UFieldLValue(addressSort, slice, sliceCellField)
            val cell = scope.calcOnState { memory.read(cellFieldLValue) }

            val refsLengthLValue = UFieldLValue(sizeSort, cell, cellRefsLengthField)
            val refsLength = scope.calcOnState { memory.read(refsLengthLValue) }

            val correctnessConstraint = mkAnd(
                mkSizeLeExpr(mkSizeExpr(0), refsLength),
                mkSizeLeExpr(refsLength, mkSizeExpr(MAX_REFS_NUMBER)),
            )
            scope.assert(correctnessConstraint)
                ?: error("Cannot ensure correctness for number of refs in cell $cell")

            val refsPosLValue = UFieldLValue(sizeSort, slice, sliceRefPosField)
            // TODO hack!
            if (slice is URegisterReading) {
                scope.doWithState { memory.write(refsPosLValue, mkSizeExpr(0)) }
            }
            val refsPos = scope.calcOnState { memory.read(refsPosLValue) }

            val readingConstraint = mkSizeLtExpr(refsPos, refsLength)

            scope.fork(
                readingConstraint,
                blockOnFalseState = setFailure(TvmCellUnderflow)
            ) ?: return

            scope.doWithState {
                val ref = readCellRef(cell, refsPos)

                val newRefsPos = mkSizeAddExpr(refsPos, mkSizeExpr(1))

                val updatedRefsPosLValue = UFieldLValue(sizeSort, updatedSlice, sliceRefPosField)

                val dataPosLValue = UFieldLValue(sizeSort, slice, sliceDataPosField)
                val dataPos = memory.read(dataPosLValue)

                memory.write(updatedRefsPosLValue, newRefsPos)
                memory.write(UFieldLValue(sizeSort, updatedSlice, sliceDataPosField), dataPos)
                memory.write(UFieldLValue(addressSort, updatedSlice, sliceCellField), cell)

                stack.add(ref, TvmCellType)
                stack.add(updatedSlice, TvmSliceType)

                newStmt(stmt.nextStmt(contractCode, currentContinuation))
            }
        }
    }

    private fun visitEndSliceInst(scope: TvmStepScope, stmt: TvmCellParseEndsInst) {
        with(ctx) {
            val slice = scope.calcOnState { stack.takeLastSlice() }

            val cellFieldLValue = UFieldLValue(addressSort, slice, sliceCellField)
            val cell = scope.calcOnState { memory.read(cellFieldLValue) }

            val dataLengthLValue = UFieldLValue(sizeSort, cell, cellDataLengthField)
            val dataLength = scope.calcOnState { memory.read(dataLengthLValue) }

            val dataPosLValue = UFieldLValue(sizeSort, slice, sliceDataPosField)
            val dataPos = scope.calcOnState { memory.read(dataPosLValue) }

            val refsPosLValue = UFieldLValue(sizeSort, slice, sliceRefPosField)
            val refsPos = scope.calcOnState { memory.read(refsPosLValue) }

            val refsLengthLValue = UFieldLValue(sizeSort, cell, cellRefsLengthField)
            val refsLength = scope.calcOnState { memory.read(refsLengthLValue) }

            val isRemainingDataEmptyConstraint = mkSizeGeExpr(dataPos, dataLength)
            val areRemainingRefsEmpty = mkSizeGeExpr(refsPos, refsLength)

            scope.fork(
                mkAnd(isRemainingDataEmptyConstraint, areRemainingRefsEmpty),
                blockOnFalseState = setFailure(TvmCellUnderflow)
            ) ?: return

            scope.doWithState {
                newStmt(stmt.nextStmt(contractCode, currentContinuation))
            }
        }
    }

    private fun visitLoadUnsignedIntInst(
        scope: TvmStepScope,
        stmt: TvmCellParseLduInst
    ) {
        with(ctx) {
            val slice = scope.calcOnState { stack.takeLastSlice() }
            val updatedSlice = scope.calcOnState { memory.allocConcrete(TvmSliceType) }

            val bitsLen = stmt.c + 1

            val cellFieldLValue = UFieldLValue(addressSort, slice, sliceCellField)
            val cell = scope.calcOnState { memory.read(cellFieldLValue) }

            val dataFieldLValue = UFieldLValue(cellDataSort, cell, cellDataField)
            val data = scope.calcOnState { memory.read(dataFieldLValue) }

            val dataLengthLValue = UFieldLValue(sizeSort, cell, cellDataLengthField)
            val dataLength = scope.calcOnState { memory.read(dataLengthLValue) }

            val correctnessConstraint = mkAnd(
                mkSizeLeExpr(mkSizeExpr(0), dataLength),
                mkSizeLeExpr(dataLength, mkSizeExpr(MAX_DATA_LENGTH)),
            )
            scope.assert(correctnessConstraint)
                ?: error("Cannot ensure correctness for data length in cell $cell")

            val dataPosLValue = UFieldLValue(sizeSort, slice, sliceDataPosField)
            // TODO hack!
            if (slice is URegisterReading) {
                scope.doWithState { memory.write(dataPosLValue, mkSizeExpr(0)) }
            }

            val dataPos = scope.calcOnState { memory.read(dataPosLValue) }

            val bitsSizeExpr = mkSizeExpr(bitsLen)
            val readingEnd = mkSizeAddExpr(dataPos, bitsSizeExpr)
            val readingConstraint = mkSizeLeExpr(readingEnd, dataLength)

            scope.fork(
                readingConstraint,
                blockOnFalseState = setFailure(TvmCellUnderflow)
            ) ?: return

            val concreteBegin = (dataPos as? KBitVec32Value)?.intValue
                ?: error("Unknown data pos in slice")
            val concreteEndIncluding = concreteBegin + bitsLen - 1
            val readBits = mkBvExtractExpr(high = concreteEndIncluding, low = concreteBegin, value = data)
            val extendedToIntBits = mkBvZeroExtensionExpr(257 - bitsLen, readBits)

            scope.doWithState {
                val newDataPos = mkSizeAddExpr(dataPos, bitsSizeExpr)
                val refsPosLValue = UFieldLValue(sizeSort, slice, sliceRefPosField)
                val refsPos = memory.read(refsPosLValue)

                memory.write(UFieldLValue(sizeSort, updatedSlice, sliceDataPosField), newDataPos)
                memory.write(UFieldLValue(sizeSort, updatedSlice, sliceRefPosField), refsPos)
                memory.write(UFieldLValue(addressSort, updatedSlice, sliceCellField), cell)

                stack.add(extendedToIntBits, TvmIntegerType)
                stack.add(updatedSlice, TvmSliceType)
                newStmt(stmt.nextStmt(contractCode, currentContinuation))
            }
        }
    }

    private fun visitCellToSliceInst(
        scope: TvmStepScope,
        stmt: TvmCellParseCtosInst
    ) {
        with(ctx) {
            scope.doWithState {
                val cell = stack.takeLastCell()
//                ensureCellCorrectness(cell, scope)

                val slice = memory.allocConcrete(TvmSliceType) // TODO concrete or static?
                stack.add(slice, TvmSliceType)

                val sliceDataPosLValue = UFieldLValue(sizeSort, slice, sliceDataPosField)
                val sliceRefPosLValue = UFieldLValue(sizeSort, slice, sliceRefPosField)
                val sliceCellLValue = UFieldLValue(addressSort, slice, sliceCellField)

                memory.write(sliceDataPosLValue, mkSizeExpr(0))
                memory.write(sliceRefPosLValue, mkSizeExpr(0))
                memory.write(sliceCellLValue, cell)
            }

            scope.doWithState {
                newStmt(stmt.nextStmt(contractCode, currentContinuation))
            }
        }
    }

    private fun visitCellBuildInst(
        scope: TvmStepScope,
        stmt: TvmCellBuildInst
    ) {
        when (stmt) {
            is TvmCellBuildEndcInst -> visitEndCellInst(scope, stmt)
            is TvmCellBuildNewcInst -> visitNewCellInst(scope, stmt)
            is TvmCellBuildStuInst -> visitStoreUnsignedIntInst(scope, stmt)
            else -> TODO("$stmt")
        }
    }

    private fun visitStoreUnsignedIntInst(scope: TvmStepScope, stmt: TvmCellBuildStuInst) {
        with(ctx) {
            scope.doWithState {
                val builder = stack.takeLastBuilder()
                val updatedBuilder = memory.allocConcrete(TvmBuilderType)

                val bits = stmt.c + 1
                val bvSort = mkBvSort(bits.toUInt())
                val intValue = stack.takeLastInt()
                if (intValue !is KBitVecValue) {
                    error("Not concrete value to store")
                }

                // TODO how to check out if range if we have already taken the value with the right sort?

                val builderDataLValue = UFieldLValue(cellDataSort, builder, cellDataField)
                val builderDataLengthLValue = UFieldLValue(sizeSort, builder, cellDataLengthField)
                val builderRefsLengthLValue = UFieldLValue(sizeSort, builder, cellRefsLengthField)

                val builderData = memory.read(builderDataLValue)
                val builderDataLength = memory.read(builderDataLengthLValue)
                val builderRefsLength = memory.read(builderRefsLengthLValue)

                val newDataLength = mkSizeAddExpr(builderDataLength, mkSizeExpr(bits))

                val canWriteConstraint = mkSizeLeExpr(newDataLength, mkSizeExpr(MAX_DATA_LENGTH))

                scope.fork(
                    canWriteConstraint,
                    blockOnFalseState = setFailure(TvmCellOverflow)
                ) ?: return@doWithState

                val valueWithDataSort = mkBv(intValue.stringValue, MAX_DATA_LENGTH.toUInt())
                val builderLengthWithDataSort = (builderDataLength as? KBitVecValue)
                    ?.let { mkBv(it.stringValue, MAX_DATA_LENGTH.toUInt()) }
                    ?: error("Not concrete builder length")
                val shiftedValue = mkBvShiftLeftExpr(valueWithDataSort, builderLengthWithDataSort)
                val extendedData = mkBvOrExpr(builderData, shiftedValue)

                val updatedBuilderDataLValue = UFieldLValue(cellDataSort, updatedBuilder, cellDataField)
                val updatedBuilderDataLengthLValue = UFieldLValue(sizeSort, updatedBuilder, cellDataLengthField)
                val updatedBuilderRefsLengthLValue = UFieldLValue(sizeSort, updatedBuilder, cellRefsLengthField)

                memory.write(updatedBuilderDataLValue, extendedData)
                memory.write(updatedBuilderDataLengthLValue, newDataLength)
                memory.write(updatedBuilderRefsLengthLValue, builderRefsLength)
                for (i in 0 until MAX_REFS_NUMBER) {
                    val refIdx = mkSizeExpr(i)
                    val ref = readCellRef(builder, refIdx)
                    writeCellRef(updatedBuilder, refIdx, ref)
                }

                stack.add(updatedBuilder, TvmBuilderType)
                newStmt(stmt.nextStmt(contractCode, currentContinuation))
            }
        }
    }

    private fun visitNewCellInst(scope: TvmStepScope, stmt: TvmCellBuildNewcInst) {
        with(ctx) {
            scope.doWithState {
                val builder = memory.allocConcrete(TvmBuilderType) // TODO static or concrete

                val builderDataLValue = UFieldLValue(cellDataSort, builder, cellDataField)
                val builderDataLengthLValue = UFieldLValue(sizeSort, builder, cellDataLengthField)
                val builderRefsLengthLValue = UFieldLValue(sizeSort, builder, cellRefsLengthField)

                memory.write(builderDataLValue, mkBv(BigInteger.ZERO, cellDataSort))
                memory.write(builderDataLengthLValue, mkSizeExpr(0))
                memory.write(builderRefsLengthLValue, mkSizeExpr(0))

                stack.add(builder, TvmBuilderType)
                newStmt(stmt.nextStmt(contractCode, currentContinuation))
            }
        }
    }

    private fun visitEndCellInst(scope: TvmStepScope, stmt: TvmCellBuildEndcInst) {
        with(ctx) {
            val builder = scope.calcOnState { stack.takeLastBuilder() }
            val cell = scope.calcOnState { memory.allocConcrete(TvmCellType) } // TODO static or concrete

            val builderDataLValue = UFieldLValue(cellDataSort, builder, cellDataField)
            val builderDataLengthLValue = UFieldLValue(sizeSort, builder, cellDataLengthField)
            val builderRefsLengthLValue = UFieldLValue(sizeSort, builder, cellRefsLengthField)

            val builderData = scope.calcOnState { memory.read(builderDataLValue) }
            val builderDataLength = scope.calcOnState { memory.read(builderDataLengthLValue) }
            val builderRefsLength = scope.calcOnState { memory.read(builderRefsLengthLValue) }

            scope.doWithState {
                memory.write(UFieldLValue(cellDataSort, cell, cellDataField), builderData)
                memory.write(UFieldLValue(sizeSort, cell, cellDataLengthField), builderDataLength)
                memory.write(UFieldLValue(sizeSort, cell, cellRefsLengthField), builderRefsLength)
                for (i in 0 until MAX_REFS_NUMBER) {
                    val refIdx = mkSizeExpr(i)
                    val ref = readCellRef(builder, refIdx)
                    writeCellRef(cell, refIdx, ref)
                }

                stack.add(cell, TvmCellType)
                newStmt(stmt.nextStmt(contractCode, currentContinuation))
            }
        }
    }

    private fun visitTvmSaveControlFlowInst(
        scope: TvmStepScope,
        stmt: TvmContRegistersInst
    ) {
        when (stmt) {
            is TvmContRegistersPushctrInst -> visitTvmPushCtrInst(scope, stmt)
            is TvmContRegistersPopctrInst -> {
                scope.doWithState {
                    val registerIndex = stmt.i
                    // TODO for now, assume we always use c4
                    require(registerIndex == 4) {
                        "POPCTR is supported only for c4 but got $registerIndex register"
                    }
                    stack.takeLastCell()

                    newStmt(stmt.nextStmt(contractCode, currentContinuation))

                    // TODO save to the correct register
                }
            }
            else -> TODO("$stmt")
        }
    }

    private fun visitTvmPushCtrInst(scope: TvmStepScope, stmt: TvmContRegistersPushctrInst) {
        scope.doWithState {
            // TODO use it!
            val registerIndex = stmt.i

            // TODO should we use real persistent or always consider it fully symbolic?
//            val data = registers.c4?.value?.value?.toSymbolic(scope) ?: mkSymbolicCell(scope)
            when (registerIndex) {
                4 -> {
                    val data = registers.c4?.value?.value ?: run {
                        val symbolicCell = mkSymbolicCell(scope)
                        registers.c4 = C4Register(TvmCellValue(symbolicCell))
                        symbolicCell
                    }
                    stack.add(data, TvmCellType)
                    newStmt(stmt.nextStmt(contractCode, currentContinuation))
                }
                3 -> {
                    val mainMethod = contractCode.methods[Int.MAX_VALUE]
                        ?: error("No main method found")
                    val continuationValue = TvmContinuationValue(mainMethod, stack, registers)
                    stack += continuationValue
                    newStmt(stmt.nextStmt(contractCode, currentContinuation))
                }
                else -> TODO("Not yet implemented")
            }

        }
    }

    private fun mkSymbolicCell(scope: TvmStepScope): UHeapRef =
        scope.calcOnState {
            val cell = memory.allocStatic(TvmCellType) // TODO static?
            cell
        }

    private fun visitTvmBasicControlFlowInst(
        scope: TvmStepScope,
        stmt: TvmContBasicInst
    ) {
        when (stmt) {
            is TvmContBasicExecuteInst -> {
                scope.doWithState {
                    val continuationValue = stack.takeLastContinuation()

                    // TODO really?
//                        registers = continuation.registers
//                        stack = continuation.stack

                    currentContinuation = continuationValue
                    // TODO discard remainder of the current continuation?
                    newStmt(continuationValue.codeBlock.instList.first())
                }
            }
            is TvmContBasicRetInst -> {
                scope.doWithState { returnFromMethod() }
            }
            else -> TODO("$stmt")
        }
    }

    private fun visitTvmConditionalControlFlowInst(
        scope: TvmStepScope,
        stmt: TvmContConditionalInst
    ) {
        when (stmt) {
            is TvmContConditionalIfretInst -> {
                scope.doWithState {
                    val operand = stack.takeLastInt()
                    with(ctx) {
                        val neqZero = mkEq(operand, falseValue).not()
                        scope.fork(
                            neqZero,
                            blockOnFalseState = { newStmt(stmt.nextStmt(contractCode, currentContinuation)) }
                        ) ?: return@with

                        // TODO check NaN for integer overflow exception

                        scope.doWithState { returnFromMethod() }
                    }
                }
            }

            is TvmContConditionalIfjmpInst -> visitIfJmpInst(scope, stmt)
            is TvmContConditionalIfelseInst -> visitIfElseInst(scope, stmt)
            else -> TODO("$stmt")
        }
    }

    private fun visitIfElseInst(scope: TvmStepScope, stmt: TvmContConditionalIfelseInst) {
        with(ctx) {
            scope.doWithState {
                val firstContinuation = stack.takeLastContinuation()
                val secondContinuation = stack.takeLastContinuation()
                val flag = stack.takeLastInt()
                val ifConstraint = mkEq(flag, falseValue).not()

                scope.fork(
                    ifConstraint,
                    blockOnTrueState = {
                        // TODO really?
//                        registers = continuation.registers
//                        stack = continuation.stack

                        currentContinuation = firstContinuation
                        // TODO discard remainder of the current continuation?
                        newStmt(firstContinuation.codeBlock.instList.first())
                    },
                    blockOnFalseState = {
                        // TODO really?
//                        registers = continuation.registers
//                        stack = continuation.stack

                        currentContinuation = secondContinuation
                        // TODO discard remainder of the current continuation?
                        newStmt(secondContinuation.codeBlock.instList.first())
                    }
                )
            }
        }
    }

    private fun visitIfJmpInst(scope: TvmStepScope, stmt: TvmContConditionalIfjmpInst) {
        with(ctx) {
            scope.doWithState {
                val (continuation, flag) = stack.takeLastContinuation() to stack.takeLastInt()
                val ifConstraint = mkEq(flag, falseValue).not()

                scope.fork(
                    ifConstraint,
                    blockOnTrueState = {
                        // TODO really?
//                        registers = continuation.registers
//                        stack = continuation.stack

                        currentContinuation = continuation
                        //  The remainder of the previous current continuation cc is discarded.
                        newStmt(continuation.codeBlock.instList.first())
                    },
                    blockOnFalseState = { newStmt(stmt.nextStmt(contractCode, currentContinuation)) }
                )
            }
        }
    }

    private fun visitTvmDictionaryJumpInst(scope: TvmStepScope, stmt: TvmContDictInst) {
        when (stmt) {
            is TvmContDictCalldictInst -> {
                val methodId = stmt.n

                scope.doWithState {
//                    stack += argument.toBv257()
////                    val c3Continuation = registers.c3!!.value
//                    val contractMethod = contractCode.methods[0]!!
////                    val continuationStmt = c3Continuation.method.instList[c3Continuation.currentInstIndex]
//                    val continuationStmt = contractMethod.instList.first()
//                    val nextStmt = stmt.nextStmt(contractCode, currentContinuation)
//
//                    callStack.push(contractCode.methods[continuationStmt.location.methodId]!!, nextStmt)
//                    newStmt(continuationStmt)

                    val nextStmt = stmt.nextStmt(contractCode, currentContinuation)
                    val nextMethod = contractCode.methods[methodId] ?: error("Unknown method with id $methodId")
                    val methodFirstStmt = nextMethod.instList.first()
                    callStack.push(nextMethod, nextStmt)

                    currentContinuation = TvmContinuationValue(nextMethod, stack, registers) // TODO use these stack and registers?
                    newStmt(methodFirstStmt)
                }
            }
            else -> TODO("Unknown stmt: $stmt")
        }
    }

    private fun visitExceptionInst(scope: TvmStepScope, stmt: TvmExceptionsInst) {
        when (stmt) {
            is TvmExceptionsThrowargInst -> scope.doWithState { methodResult = TvmUnknownFailure(stmt.n.toUInt()) }
            is TvmExceptionsThrowShortInst -> scope.doWithState { methodResult = TvmUnknownFailure(stmt.n.toUInt()) }
            else -> TODO("Unknown stmt: $stmt")
        }
    }

    private fun visitDictControlFlowInst(
        scope: TvmStepScope,
        stmt: TvmDictSpecialInst
    ) {
        when (stmt) {
            is TvmDictSpecialDictigetjmpzInst -> {
                val methodId =
                    (scope.calcOnState { stack.takeLastInt() } as KBitVecValue<KBvSort>).bigIntValue()
                val method = contractCode.methods[methodId.toInt()]!!
                val methodFirstStmt = method.instList.first()

                scope.doWithState {
                    // The remainder of the previous current continuation cc is discarded.
//                    val nextStmt = stmt.nextStmt(contractCode, currentContinuation)
//                    callStack.push(method, nextStmt)

                    currentContinuation = TvmContinuationValue(method, stack, registers) // TODO use these stack and registers?
                    newStmt(methodFirstStmt)
                }
            }

            is TvmDictSpecialDictpushconstInst -> {
                val keyLength = stmt.n
                val currentContinuation = scope.calcOnState { currentContinuation }
//                val nextRef = currentContinuation.slice.loadRef()

                scope.calcOnState {
//                    stack += ctx.mkHe
                }

                scope.doWithState { newStmt(stmt.nextStmt(contractCode, currentContinuation)) }
            }
            else -> TODO("$stmt")
        }
    }

    private fun visitTvmTupleInst(scope: TvmStepScope, stmt: TvmTupleInst) {
        when (stmt) {
            is TvmAliasInst -> return visitTvmTupleInst(scope, stmt.resolveAlias() as TvmTupleInst)
            is TvmTupleNullInst -> scope.doWithStateCtx {
                stack.add(nullValue, TvmNullType)
                newStmt(stmt.nextStmt(contractCode, currentContinuation))
            }
            else -> TODO("$stmt")
        }
    }

    private fun visitDebugInst(scope: TvmStepScope, stmt: TvmDebugInst) {
        // Do nothing
        scope.doWithState { newStmt(stmt.nextStmt(contractCode, currentContinuation)) }
    }

    private fun visitCodepageInst(scope: TvmStepScope, stmt: TvmCodepageInst) {
        // Do nothing
        scope.doWithState { newStmt(stmt.nextStmt(contractCode, currentContinuation)) }
    }

    context(TvmState)
    private fun TvmStack.takeLastCell(): UHeapRef {
        val cellStackValue = takeLast(TvmCellType) { _ ->
            generateSymbolicCell()
        }
        
        return cellStackValue.cellValue
    }

    context(TvmState)
    private fun TvmStack.takeLastInt(): UExpr<UBvSort> {
        val intStackValue = takeLast(TvmIntegerType) { id ->
            ctx.mkRegisterReading(id, ctx.int257sort)
        }
        
        return intStackValue.intValue
    }

    private fun TvmStack.takeLastContinuation(): TvmContinuationValue {
        val continuationStackValue = takeLast(TvmContinuationType) { _ ->
            error("Unexpected continuation as an input")
        }

        return continuationStackValue.continuationValue
    }

    context(TvmState)
    private fun TvmStack.takeLastSlice(): UHeapRef {
        val sliceStackValue = takeLast(TvmSliceType) { id ->
            ctx.mkRegisterReading(id, ctx.addressSort)
        }

        return sliceStackValue.sliceValue
    }

    context(TvmState)
    private fun TvmStack.takeLastBuilder(): UHeapRef {
        val builderStackValue = takeLast(TvmBuilderType) { id ->
            ctx.mkRegisterReading(id, ctx.addressSort)
        }

        return builderStackValue.builderValue
    }

    context(TvmState)
    private fun TvmStack.takeLastTuple(): UHeapRef {
        val tupleStackValue = takeLast(TvmTupleType) { id ->
            ctx.mkRegisterReading(id, ctx.addressSort)
        }

        return tupleStackValue.tupleValue
    }

    context(TvmContext)
    private fun TvmSubSliceSerializedLoader.bitsToBv(): KBitVecValue<UBvSort> {
        // todo: check bits order
        return mkBv(bits.joinToString(""), bits.size.toUInt())
    }

    private val cellDataField: TvmField = TvmFieldImpl(TvmCellType, "data")
    private val cellDataLengthField: TvmField = TvmFieldImpl(TvmCellType, "dataLength")
    private val cellRefsLengthField: TvmField = TvmFieldImpl(TvmCellType, "refsLength")

    private val sliceDataPosField: TvmField = TvmFieldImpl(TvmSliceType, "dataPos")
    private val sliceRefPosField: TvmField = TvmFieldImpl(TvmSliceType, "refPos")
    private val sliceCellField: TvmField = TvmFieldImpl(TvmSliceType, "cell")
}
