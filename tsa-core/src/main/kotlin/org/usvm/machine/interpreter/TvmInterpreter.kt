package org.usvm.machine.interpreter

import io.ksmt.expr.KBitVecValue
import io.ksmt.expr.KInterpretedValue
import io.ksmt.sort.KBvSort
import io.ksmt.utils.BvUtils.bigIntValue
import io.ksmt.utils.BvUtils.bvMaxValueSigned
import io.ksmt.utils.BvUtils.bvMinValueSigned
import io.ksmt.utils.BvUtils.toBigIntegerSigned
import kotlinx.collections.immutable.persistentListOf
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
import org.ton.bytecode.TvmArithmDivInst
import org.ton.bytecode.TvmArithmLogicalAbsInst
import org.ton.bytecode.TvmArithmLogicalAndInst
import org.ton.bytecode.TvmArithmLogicalBitsizeInst
import org.ton.bytecode.TvmArithmLogicalChkbitAliasInst
import org.ton.bytecode.TvmArithmLogicalChkboolAliasInst
import org.ton.bytecode.TvmArithmLogicalFitsInst
import org.ton.bytecode.TvmArithmLogicalFitsxInst
import org.ton.bytecode.TvmArithmLogicalInst
import org.ton.bytecode.TvmArithmLogicalLshiftInst
import org.ton.bytecode.TvmArithmLogicalLshiftVarInst
import org.ton.bytecode.TvmArithmLogicalMaxInst
import org.ton.bytecode.TvmArithmLogicalMinInst
import org.ton.bytecode.TvmArithmLogicalMinmaxInst
import org.ton.bytecode.TvmArithmLogicalNotInst
import org.ton.bytecode.TvmArithmLogicalOrInst
import org.ton.bytecode.TvmArithmLogicalPow2Inst
import org.ton.bytecode.TvmArithmLogicalRshiftInst
import org.ton.bytecode.TvmArithmLogicalRshiftVarInst
import org.ton.bytecode.TvmArithmLogicalUbitsizeInst
import org.ton.bytecode.TvmArithmLogicalUfitsInst
import org.ton.bytecode.TvmArithmLogicalUfitsxInst
import org.ton.bytecode.TvmArithmLogicalXorInst
import org.ton.bytecode.TvmArtificialImplicitRetInst
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
import org.ton.bytecode.TvmCodeBlock
import org.ton.bytecode.TvmCodepageInst
import org.ton.bytecode.TvmCompareIntChknanInst
import org.ton.bytecode.TvmCompareIntCmpInst
import org.ton.bytecode.TvmCompareIntEqintInst
import org.ton.bytecode.TvmCompareIntEqualInst
import org.ton.bytecode.TvmCompareIntGeqInst
import org.ton.bytecode.TvmCompareIntGreaterInst
import org.ton.bytecode.TvmCompareIntGtintInst
import org.ton.bytecode.TvmCompareIntInst
import org.ton.bytecode.TvmCompareIntIsnanInst
import org.ton.bytecode.TvmCompareIntLeqInst
import org.ton.bytecode.TvmCompareIntLessInst
import org.ton.bytecode.TvmCompareIntLessintInst
import org.ton.bytecode.TvmCompareIntNeqInst
import org.ton.bytecode.TvmCompareIntNeqintInst
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
import org.ton.bytecode.TvmContLoopsInst
import org.ton.bytecode.TvmContRegistersInst
import org.ton.bytecode.TvmContRegistersPopctrInst
import org.ton.bytecode.TvmContRegistersPushctrInst
import org.ton.bytecode.TvmContinuationValue
import org.ton.bytecode.TvmContractCode
import org.ton.bytecode.TvmDebugInst
import org.ton.bytecode.TvmDictInst
import org.ton.bytecode.TvmDictSpecialDictigetjmpzInst
import org.ton.bytecode.TvmDictSpecialDictpushconstInst
import org.ton.bytecode.TvmDictSpecialInst
import org.ton.bytecode.TvmExceptionsInst
import org.ton.bytecode.TvmExceptionsThrowShortInst
import org.ton.bytecode.TvmExceptionsThrowargInst
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
import org.ton.bytecode.TvmStackComplexBlkdropInst
import org.ton.bytecode.TvmStackComplexBlkpushInst
import org.ton.bytecode.TvmStackComplexBlkswapInst
import org.ton.bytecode.TvmStackComplexBlkswxInst
import org.ton.bytecode.TvmStackComplexChkdepthInst
import org.ton.bytecode.TvmStackComplexDepthInst
import org.ton.bytecode.TvmStackComplexDrop2Inst
import org.ton.bytecode.TvmStackComplexDropxInst
import org.ton.bytecode.TvmStackComplexDup2Inst
import org.ton.bytecode.TvmStackComplexInst
import org.ton.bytecode.TvmStackComplexMinusrollxInst
import org.ton.bytecode.TvmStackComplexOnlytopxInst
import org.ton.bytecode.TvmStackComplexOnlyxInst
import org.ton.bytecode.TvmStackComplexOver2Inst
import org.ton.bytecode.TvmStackComplexPickInst
import org.ton.bytecode.TvmStackComplexPopLongInst
import org.ton.bytecode.TvmStackComplexPu2xcInst
import org.ton.bytecode.TvmStackComplexPush2Inst
import org.ton.bytecode.TvmStackComplexPush3Inst
import org.ton.bytecode.TvmStackComplexPushLongInst
import org.ton.bytecode.TvmStackComplexPuxc2Inst
import org.ton.bytecode.TvmStackComplexPuxcInst
import org.ton.bytecode.TvmStackComplexPuxcpuInst
import org.ton.bytecode.TvmStackComplexReverseInst
import org.ton.bytecode.TvmStackComplexRevxInst
import org.ton.bytecode.TvmStackComplexRollAliasInst
import org.ton.bytecode.TvmStackComplexRollrevAliasInst
import org.ton.bytecode.TvmStackComplexRollxInst
import org.ton.bytecode.TvmStackComplexRot2AliasInst
import org.ton.bytecode.TvmStackComplexRotInst
import org.ton.bytecode.TvmStackComplexRotrevInst
import org.ton.bytecode.TvmStackComplexSwap2Inst
import org.ton.bytecode.TvmStackComplexTuckInst
import org.ton.bytecode.TvmStackComplexXc2puInst
import org.ton.bytecode.TvmStackComplexXchg2Inst
import org.ton.bytecode.TvmStackComplexXchg3AltInst
import org.ton.bytecode.TvmStackComplexXchg3Inst
import org.ton.bytecode.TvmStackComplexXchgxInst
import org.ton.bytecode.TvmStackComplexXcpu2Inst
import org.ton.bytecode.TvmStackComplexXcpuInst
import org.ton.bytecode.TvmStackComplexXcpuxcInst
import org.ton.bytecode.TvmSubSliceSerializedLoader
import org.ton.bytecode.TvmTupleInst
import org.ton.bytecode.TvmTupleIsnullInst
import org.ton.bytecode.TvmTupleNullInst
import org.ton.bytecode.TvmTupleNullrotrif2Inst
import org.ton.bytecode.TvmTupleNullrotrifInst
import org.ton.bytecode.TvmTupleNullrotrifnot2Inst
import org.ton.bytecode.TvmTupleNullrotrifnotInst
import org.ton.bytecode.TvmTupleNullswapif2Inst
import org.ton.bytecode.TvmTupleNullswapifInst
import org.ton.bytecode.TvmTupleNullswapifnot2Inst
import org.ton.bytecode.TvmTupleNullswapifnotInst
import org.ton.bytecode.TvmType
import org.ton.cell.Cell
import org.ton.targets.TvmTarget
import org.usvm.StepResult
import org.usvm.StepScope
import org.usvm.UBoolExpr
import org.usvm.UBvSort
import org.usvm.UExpr
import org.usvm.UInterpreter
import org.usvm.api.makeSymbolicPrimitive
import org.usvm.api.readField
import org.usvm.api.writeField
import org.usvm.collection.field.UFieldLValue
import org.usvm.constraints.UPathConstraints
import org.usvm.forkblacklists.UForkBlackList
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.Companion.MAX_DATA_LENGTH
import org.usvm.machine.TvmContext.Companion.cellDataField
import org.usvm.machine.TvmContext.Companion.cellDataLengthField
import org.usvm.machine.TvmContext.Companion.cellRefsLengthField
import org.usvm.machine.TvmContext.Companion.sliceCellField
import org.usvm.machine.TvmContext.Companion.sliceDataPosField
import org.usvm.machine.TvmContext.Companion.sliceRefPosField
import org.usvm.machine.state.C3Register
import org.usvm.machine.state.C4Register
import org.usvm.machine.state.TvmCellOverflow
import org.usvm.machine.state.TvmCellUnderflow
import org.usvm.machine.state.TvmIntegerOutOfRange
import org.usvm.machine.state.TvmIntegerOverflow
import org.usvm.machine.state.TvmRefEmptyValue
import org.usvm.machine.state.TvmRegisters
import org.usvm.machine.state.TvmStack
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.TvmUnknownFailure
import org.usvm.machine.state.builderCopy
import org.usvm.machine.state.builderStoreDataBits
import org.usvm.machine.state.bvMaxValueSignedExtended
import org.usvm.machine.state.bvMaxValueUnsignedExtended
import org.usvm.machine.state.bvMinValueSignedExtended
import org.usvm.machine.state.calcOnStateCtx
import org.usvm.machine.state.doWithStateCtx
import org.usvm.machine.state.generateSymbolicCell
import org.usvm.machine.state.lastStmt
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.returnFromMethod
import org.usvm.machine.state.setDefaultGasUsage
import org.usvm.machine.state.setFailure
import org.usvm.machine.state.setGasUsage
import org.usvm.machine.state.signedIntegerFitsBits
import org.usvm.machine.state.sliceCopy
import org.usvm.machine.state.sliceLoadDataBits
import org.usvm.machine.state.sliceLoadNextRef
import org.usvm.machine.state.sliceMoveDataPtr
import org.usvm.machine.state.sliceMoveRefPtr
import org.usvm.machine.state.takeLastBuilder
import org.usvm.machine.state.takeLastCell
import org.usvm.machine.state.takeLastContinuation
import org.usvm.machine.state.takeLastInt
import org.usvm.machine.state.takeLastSlice
import org.usvm.machine.state.unsignedIntegerFitsBits
import org.usvm.memory.UMemory
import org.usvm.memory.UWritableMemory
import org.usvm.mkSizeAddExpr
import org.usvm.mkSizeExpr
import org.usvm.mkSizeGeExpr
import org.usvm.mkSizeLeExpr
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

    private val dictOperationInterpreter = TvmDictOperationInterpreter(ctx)
    private val loopsInterpreter = TvmLoopsInterpreter(ctx)
    private val arithDivInterpreter = TvmArithDivInterpreter(ctx)

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
        val pathConstraints = UPathConstraints<TvmType>(ctx)
        val memory = UMemory<TvmType, TvmCodeBlock>(ctx, pathConstraints.typeConstraints)
        val refEmptyValue = memory.initializeEmptyRefValues()

        val state = TvmState(
            ctx = ctx,
            entrypoint = method,
            currentContinuation = currentContinuation,
            stack = stack,
            registers = registers,
            memory = memory,
            pathConstraints = pathConstraints,
            emptyRefValue = refEmptyValue,
            gasUsage = persistentListOf(),
            targets = UTargetsSet.from(targets)
        )
        val solver = ctx.solver<TvmType>()

        val model = (solver.check(state.pathConstraints) as USatResult).model
        state.models = listOf(model)

        state.callStack.push(method, returnSite = null)
        state.newStmt(method.instList.first())

        return state
    }

    private fun UWritableMemory<TvmType>.initializeEmptyRefValues(): TvmRefEmptyValue = with(ctx) {
        val emptyCell = allocStatic(TvmCellType)
        writeField(emptyCell, cellDataField, cellDataSort, mkBv(0, cellDataSort), guard = trueExpr)
        writeField(emptyCell, cellRefsLengthField, sizeSort, mkSizeExpr(0), guard = trueExpr)
        writeField(emptyCell, cellDataLengthField, sizeSort, mkSizeExpr(0), guard = trueExpr)

        val emptyBuilder = allocStatic(TvmBuilderType)
        writeField(emptyBuilder, cellDataField, cellDataSort, mkBv(0, cellDataSort), guard = trueExpr)
        writeField(emptyBuilder, cellRefsLengthField, sizeSort, mkSizeExpr(0), guard = trueExpr)
        writeField(emptyBuilder, cellDataLengthField, sizeSort, mkSizeExpr(0), guard = trueExpr)

        val emptySlice = allocStatic(TvmBuilderType)
        writeField(emptySlice, sliceCellField, addressSort, emptyCell, guard = trueExpr)
        writeField(emptySlice, sliceRefPosField, sizeSort, mkSizeExpr(0), guard = trueExpr)
        writeField(emptySlice, sliceDataPosField, sizeSort, mkSizeExpr(0), guard = trueExpr)

        TvmRefEmptyValue(emptyCell, emptySlice, emptyBuilder)
    }

    override fun step(state: TvmState): StepResult<TvmState> {
        val stmt = state.lastStmt

        logger.debug("Step: {}", stmt)

        val initialGasUsage = state.gasUsage

        val scope = StepScope(state, forkBlackList)

        // handle exception firstly
//        val result = state.methodResult
//        if (result is JcMethodResult.JcException) {
//            handleException(scope, result, stmt)
//            return scope.stepResult()
//        }

        visit(scope, stmt)

        return scope.stepResult().apply {
            if (state.gasUsage === initialGasUsage || forkedStates.any { it.gasUsage === initialGasUsage }) {
                TODO("Gas usage was not updated after: $stmt")
            }
        }
    }

    private fun visit(scope: TvmStepScope, stmt: TvmInst) {
        when (stmt) {
            is TvmStackBasicInst -> visitBasicStackInst(scope, stmt)
            is TvmStackComplexInst -> visitComplexStackInst(scope, stmt)
            is TvmConstIntInst -> visitConstantIntInst(scope, stmt)
            is TvmConstDataInst -> visitConstantDataInst(scope, stmt)
            is TvmArithmBasicInst -> visitArithmeticInst(scope, stmt)
            is TvmArithmDivInst -> arithDivInterpreter.visitArithmeticDivInst(scope, stmt)
            is TvmArithmLogicalInst -> visitArithmeticLogicalInst(scope, stmt)
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
            is TvmDictInst -> dictOperationInterpreter.visitTvmDictInst(scope, stmt)
            is TvmContLoopsInst -> loopsInterpreter.visitTvmContLoopsInst(scope, stmt)
            else -> TODO("$stmt")
        }
    }

    private fun visitBasicStackInst(scope: TvmStepScope, stmt: TvmStackBasicInst) {
        scope.setDefaultGasUsage(stmt)

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
            newStmt(stmt.nextStmt())
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
        scope.setDefaultGasUsage(stmt)

        when (stmt) {
            is TvmStackComplexBlkdrop2Inst -> scope.doWithState {
                stack.blkDrop2(stmt.i, stmt.j)
            }
            is TvmStackComplexReverseInst -> scope.doWithState {
                stack.reverse(stmt.i + 2, stmt.j)
            }
            is TvmStackComplexBlkswapInst -> scope.doWithState {
                stack.doBlkSwap(stmt.i, stmt.j)
            }
            is TvmStackComplexRotInst -> scope.doWithState {
                stack.doBlkSwap(0, 1)
            }
            is TvmStackComplexBlkdropInst -> scope.doWithState {
                stack.blkDrop2(stmt.i, 0)
            }
            is TvmStackComplexBlkpushInst -> scope.doWithState {
                repeat(stmt.i) {
                    stack.push(stmt.j)
                }
            }
            is TvmStackComplexBlkswxInst -> scope.doWithState {
                val j = stack.takeLastInt()
                val i = stack.takeLastInt()
                val concreteI = i.extractConcrete(stmt)
                val concreteJ = j.extractConcrete(stmt)
                stack.doBlkSwap(concreteI - 1, concreteJ - 1)
            }

            is TvmStackComplexDrop2Inst -> scope.doWithState {
                stack.pop(0)
                stack.pop(0)
            }
            is TvmStackComplexDropxInst -> scope.doWithState {
                val i = stack.takeLastInt()
                val concreteI = i.extractConcrete(stmt)
                stack.blkDrop2(concreteI, 0)
            }
            is TvmStackComplexDup2Inst -> scope.doWithState {
                stack.push(1)
                stack.push(1)
            }
            is TvmStackComplexPopLongInst -> doPop(scope, stmt.i)
            is TvmStackComplexPush2Inst -> scope.doWithState {
                stack.push(stmt.i)
                stack.push(stmt.j + 1)
            }
            is TvmStackComplexPush3Inst -> scope.doWithState {
                stack.push(stmt.i)
                stack.push(stmt.j + 1)
                stack.push(stmt.k + 2)
            }
            is TvmStackComplexPushLongInst -> doPush(scope, stmt.i)
            is TvmStackComplexXchg2Inst -> scope.doWithState {
                stack.doXchg2(stmt.i, stmt.j)
            }
            is TvmStackComplexOver2Inst -> scope.doWithState {
                stack.push(3)
                stack.push(3)
            }
            is TvmStackComplexSwap2Inst -> scope.doWithState {
                stack.doBlkSwap(1, 1)
            }
            is TvmStackComplexXcpuInst -> scope.doWithState {
                stack.swap(stmt.i, 0)
                stack.push(stmt.j)
            }
            is TvmStackComplexTuckInst -> scope.doWithState {
                stack.swap(0, 1)
                stack.push(1)
            }
            is TvmStackComplexMinusrollxInst -> scope.doWithState {
                val i = stack.takeLastInt()
                val concreteI = i.extractConcrete(stmt)
                stack.doBlkSwap(concreteI - 1, 0)
            }
            is TvmStackComplexRollxInst -> scope.doWithState {
                val i = stack.takeLastInt()
                val concreteI = i.extractConcrete(stmt)
                stack.doBlkSwap(0, concreteI - 1)
            }
            is TvmStackComplexPickInst -> scope.doWithState {
                val i = stack.takeLastInt()
                val concreteI = i.extractConcrete(stmt)
                stack.push(concreteI)
            }
            is TvmStackComplexPuxcInst -> scope.doWithState {
                stack.doPuxc(stmt.i, stmt.j - 1)
            }
            is TvmStackComplexRevxInst -> scope.doWithState {
                val j = stack.takeLastInt()
                val i = stack.takeLastInt()
                val concreteI = i.extractConcrete(stmt)
                val concreteJ = j.extractConcrete(stmt)
                stack.reverse(concreteI, concreteJ)
            }
            is TvmStackComplexRotrevInst -> scope.doWithState {
                stack.swap(1, 2)
                stack.swap(0, 2)
            }
            is TvmStackComplexXchgxInst -> scope.doWithState {
                val i = stack.takeLastInt()
                val concreteI = i.extractConcrete(stmt)
                stack.swap(0, concreteI)
            }
            is TvmStackComplexPu2xcInst -> scope.doWithState {
                stack.push(stmt.i)
                stack.swap(0, 1)
                stack.doPuxc(stmt.j, stmt.k - 1)
            }
            is TvmStackComplexPuxc2Inst -> scope.doWithState {
                stack.push(stmt.i)
                stack.swap(0, 2)
                stack.doXchg2(stmt.j, stmt.k)
            }
            is TvmStackComplexPuxcpuInst -> scope.doWithState {
                stack.doPuxc(stmt.i, stmt.j - 1)
                stack.push(stmt.k)
            }
            is TvmStackComplexXc2puInst -> scope.doWithState {
                stack.doXchg2(stmt.i, stmt.j)
                stack.push(stmt.k)
            }
            is TvmStackComplexXchg3Inst -> scope.doWithState {
                stack.doXchg3(stmt.i, stmt.j, stmt.k)
            }
            is TvmStackComplexXchg3AltInst -> scope.doWithState {
                stack.doXchg3(stmt.i, stmt.j, stmt.k)
            }
            is TvmStackComplexXcpu2Inst -> scope.doWithState {
                stack.swap(stmt.i, 0)
                stack.push(stmt.j)
                stack.push(stmt.k + 1)
            }
            is TvmStackComplexXcpuxcInst -> scope.doWithState {
                stack.swap(1, stmt.i)
                stack.doPuxc(stmt.j, stmt.k - 1)
            }

            is TvmStackComplexDepthInst -> TODO("Cannot implement stack depth yet (TvmStackComplexDepthInst)")
            is TvmStackComplexChkdepthInst -> TODO("Cannot implement stack depth yet (TvmStackComplexChkdepthInst)")
            is TvmStackComplexOnlytopxInst -> TODO("??")
            is TvmStackComplexOnlyxInst -> TODO("??")

            // aliases (there are todos in their resolveAlias):
            is TvmStackComplexRollAliasInst -> TODO()
            is TvmStackComplexRollrevAliasInst -> TODO()
            is TvmStackComplexRot2AliasInst -> TODO()
        }

        scope.doWithState {
            newStmt(stmt.nextStmt())
        }
    }

    private fun TvmStack.doBlkSwap(i: Int, j: Int) {
        reverse(i + 1, j + 1)
        reverse(j + 1, 0)
        reverse(i + j + 2, 0)
    }

    private fun TvmStack.doPuxc(i: Int, j: Int) {
        push(i)
        swap(0, 1)
        swap(0, j + 1)
    }

    private fun TvmStack.doXchg2(i: Int, j: Int) {
        swap(1, i)
        swap(0, j)
    }

    private fun TvmStack.doXchg3(i: Int, j: Int, k: Int) {
        swap(2, i)
        swap(1, j)
        swap(0, k)
    }

    private fun visitConstantIntInst(scope: TvmStepScope, stmt: TvmConstIntInst) {
        scope.setDefaultGasUsage(stmt)
        scope.doWithState {
            val value = stmt.bv257value(ctx)
            stack.add(value, TvmIntegerType)
            newStmt(stmt.nextStmt())
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
            is TvmConstIntPushnanInst -> TODO("NaN value")
            is TvmConstIntPushpow2Inst -> {
                check(x in 0..255) { "Unexpected power $x" }

                if (x == 255) {
                    TODO("NaN value")
                }

                BigInteger.valueOf(2).pow(x + 1).toBv257()
            }
            is TvmConstIntPushnegpow2Inst -> {
                check(x in 0..255) { "Unexpected power $x" }
                // todo: nothing in docs about nan
                BigInteger.valueOf(-2).pow(x + 1).toBv257()
            }
            is TvmConstIntPushpow2decInst -> {
                check(x in 0..255) { "Unexpected power $x" }
                // todo: nothing in docs about nan
                (BigInteger.valueOf(2).pow(x + 1) - BigInteger.ONE).toBv257()
            }
            is TvmConstIntTenAliasInst -> resolveAlias().bv257value(ctx)
            is TvmConstIntTrueAliasInst -> resolveAlias().bv257value(ctx)
            is TvmConstIntTwoAliasInst -> resolveAlias().bv257value(ctx)
            is TvmConstIntOneAliasInst -> resolveAlias().bv257value(ctx)
            is TvmConstIntZeroAliasInst -> resolveAlias().bv257value(ctx)
        }
    }

    private fun visitConstantDataInst(scope: TvmStepScope, stmt: TvmConstDataInst) {
        scope.setDefaultGasUsage(stmt)

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
                    newStmt(stmt.nextStmt())
                }
            }
            else -> TODO("$stmt")
        }
    }

    private fun visitPushContShortInst(scope: TvmStepScope, stmt: TvmConstDataPushcontShortInst) {
        scope.doWithState {
            val lambda = TvmLambda(stmt.s.toMutableList())
            val continuationValue = TvmContinuationValue(lambda, stack, registers)

            stack += continuationValue
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitArithmeticInst(scope: TvmStepScope, stmt: TvmArithmBasicInst) {
        scope.setDefaultGasUsage(stmt)

        with(ctx) {
            val result = when (stmt) {
                is TvmArithmBasicAddInst -> {
                    val (secondOperand, firstOperand) = scope.calcOnState {
                        stack.takeLastInt() to stack.takeLastInt()
                    }
                    // TODO optimize using ksmt implementation?
                    val resNoOverflow = mkBvAddNoOverflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkOverflow(resNoOverflow, scope) ?: return
                    val resNoUnderflow = mkBvAddNoUnderflowExpr(firstOperand, secondOperand)
                    checkUnderflow(resNoUnderflow, scope) ?: return

                    mkBvAddExpr(firstOperand, secondOperand)
                }

                is TvmArithmBasicSubInst -> {
                    val (secondOperand, firstOperand) = scope.calcOnState {
                        stack.takeLastInt() to stack.takeLastInt()
                    }
                    // TODO optimize using ksmt implementation?
                    val resNoOverflow = mkBvSubNoOverflowExpr(firstOperand, secondOperand)
                    checkOverflow(resNoOverflow, scope) ?: return
                    val resNoUnderflow = mkBvSubNoUnderflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkUnderflow(resNoUnderflow, scope) ?: return

                    mkBvSubExpr(firstOperand, secondOperand)
                }

                is TvmArithmBasicMulInst -> {
                    val (secondOperand, firstOperand) = scope.calcOnState {
                        stack.takeLastInt() to stack.takeLastInt()
                    }
                    // TODO optimize using ksmt implementation?
                    val resNoOverflow = mkBvMulNoOverflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkOverflow(resNoOverflow, scope) ?: return
                    val resNoUnderflow = mkBvMulNoUnderflowExpr(firstOperand, secondOperand)
                    checkUnderflow(resNoUnderflow, scope) ?: return

                    mkBvMulExpr(firstOperand, secondOperand)
                }
//            else -> error("Unknown stmt: $stmt")
                is TvmArithmBasicAddconstInst -> {
                    val firstOperand = scope.calcOnState { stack.takeLastInt() }
                    val secondOperand = stmt.c.toBv257()

                    // TODO optimize using ksmt implementation?
                    val resNoOverflow = mkBvAddNoOverflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkOverflow(resNoOverflow, scope) ?: return
                    val resNoUnderflow = mkBvAddNoUnderflowExpr(firstOperand, secondOperand)
                    checkUnderflow(resNoUnderflow, scope) ?: return

                    mkBvAddExpr(firstOperand, secondOperand)
                }
                is TvmArithmBasicMulconstInst -> {
                    val firstOperand = scope.calcOnState { stack.takeLastInt() }
                    val secondOperand = stmt.c.toBv257()

                    // TODO optimize using ksmt implementation?
                    val resNoOverflow = mkBvMulNoOverflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkOverflow(resNoOverflow, scope) ?: return
                    val resNoUnderflow = mkBvMulNoUnderflowExpr(firstOperand, secondOperand)
                    checkUnderflow(resNoUnderflow, scope) ?: return

                    mkBvMulExpr(firstOperand, secondOperand)
                }

                is TvmArithmBasicIncInst -> {
                    val firstOperand = scope.calcOnState { stack.takeLastInt() }
                    val secondOperand = oneValue

                    // TODO optimize using ksmt implementation?
                    val resNoOverflow = mkBvAddNoOverflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkOverflow(resNoOverflow, scope) ?: return
                    val resNoUnderflow = mkBvAddNoUnderflowExpr(firstOperand, secondOperand)
                    checkUnderflow(resNoUnderflow, scope) ?: return

                    mkBvAddExpr(firstOperand, secondOperand)
                }
                is TvmArithmBasicDecInst -> {
                    val firstOperand = scope.calcOnState { stack.takeLastInt() }
                    val secondOperand = oneValue

                    // TODO optimize using ksmt implementation?
                    val resNoOverflow = mkBvSubNoOverflowExpr(firstOperand, secondOperand)
                    checkOverflow(resNoOverflow, scope) ?: return
                    val resNoUnderflow = mkBvSubNoUnderflowExpr(firstOperand, secondOperand, isSigned = true)
                    checkUnderflow(resNoUnderflow, scope) ?: return

                    mkBvSubExpr(firstOperand, secondOperand)
                }
                else -> TODO("$stmt")
            }

            scope.doWithState {
                stack.add(result, TvmIntegerType)
                newStmt(stmt.nextStmt())
            }
        }
    }

    private fun checkOverflow(noOverflowExpr: UBoolExpr, scope: TvmStepScope): Unit? = scope.fork(
        noOverflowExpr,
        blockOnFalseState = setFailure(TvmIntegerOverflow)
    )

    private fun checkUnderflow(noUnderflowExpr: UBoolExpr, scope: TvmStepScope): Unit? = scope.fork(
        noUnderflowExpr,
        blockOnFalseState = setFailure(TvmIntegerOverflow)
    )

    private fun checkOutOfRange(notOutOfRangeExpr: UBoolExpr, scope: TvmStepScope): Unit? = scope.fork(
        condition = notOutOfRangeExpr,
        blockOnFalseState = setFailure(TvmIntegerOutOfRange)
    )

    private fun visitArithmeticLogicalInst(scope: TvmStepScope, stmt: TvmArithmLogicalInst): Unit = with(ctx) {
        val result: UExpr<UBvSort> = when (stmt) {
            is TvmArithmLogicalOrInst -> {
                scope.setDefaultGasUsage(stmt)

                val (secondOperand, firstOperand) = scope.calcOnState { stack.takeLastInt() to stack.takeLastInt() }
                mkBvOrExpr(firstOperand, secondOperand)
            }
            is TvmArithmLogicalXorInst -> {
                scope.setDefaultGasUsage(stmt)

                val (secondOperand, firstOperand) = scope.calcOnState { stack.takeLastInt() to stack.takeLastInt() }
                mkBvXorExpr(firstOperand, secondOperand)
            }
            is TvmArithmLogicalAndInst -> {
                scope.setDefaultGasUsage(stmt)

                val (secondOperand, firstOperand) = scope.calcOnState { stack.takeLastInt() to stack.takeLastInt() }
                mkBvAndExpr(firstOperand, secondOperand)
            }
            is TvmArithmLogicalNotInst -> {
                scope.doWithState { setGasUsage(18) } // todo: 26 in docs, but 18 in concrete execution

                val value = scope.calcOnState { stack.takeLastInt() }
                mkBvNotExpr(value)
            }
            is TvmArithmLogicalAbsInst -> {
                scope.setDefaultGasUsage(stmt)

                val value = scope.calcOnState { stack.takeLastInt() }
                checkOverflow(mkBvNegationNoOverflowExpr(value), scope) ?: return

                mkIte(
                    mkBvSignedLessExpr(value, zeroValue),
                    mkBvNegationExpr(value),
                    value
                )
            }
            is TvmArithmLogicalMaxInst -> {
                scope.setDefaultGasUsage(stmt)

                val (secondOperand, firstOperand) = scope.calcOnState { stack.takeLastInt() to stack.takeLastInt() }

                mkIte(
                    condition = mkBvSignedGreaterOrEqualExpr(firstOperand, secondOperand),
                    trueBranch = firstOperand,
                    falseBranch = secondOperand
                )
            }
            is TvmArithmLogicalMinInst -> {
                scope.setDefaultGasUsage(stmt)

                val (secondOperand, firstOperand) = scope.calcOnState { stack.takeLastInt() to stack.takeLastInt() }

                mkIte(
                    condition = mkBvSignedGreaterOrEqualExpr(firstOperand, secondOperand),
                    trueBranch = secondOperand,
                    falseBranch = firstOperand
                )
            }
            is TvmArithmLogicalMinmaxInst -> {
                scope.setDefaultGasUsage(stmt)

                val (secondOperand, firstOperand) = scope.calcOnState { stack.takeLastInt() to stack.takeLastInt() }

                val min = mkIte(
                    condition = mkBvSignedGreaterOrEqualExpr(firstOperand, secondOperand),
                    trueBranch = secondOperand,
                    falseBranch = firstOperand
                )
                val max = mkIte(
                    condition = mkBvSignedGreaterOrEqualExpr(firstOperand, secondOperand),
                    trueBranch = firstOperand,
                    falseBranch = secondOperand
                )

                scope.doWithState {
                    stack.add(min, TvmIntegerType)
                    stack.add(max, TvmIntegerType)
                    newStmt(stmt.nextStmt())
                }

                return
            }
            is TvmArithmLogicalPow2Inst -> {
                scope.setDefaultGasUsage(stmt)

                val exp = scope.calcOnState { stack.takeLastInt() }
                val notOutOfRangeExpr = unsignedIntegerFitsBits(exp, 10u)
                checkOutOfRange(notOutOfRangeExpr, scope) ?: return

                val resNoOverflow = unsignedIntegerFitsBits(exp, 8u)
                checkOverflow(resNoOverflow, scope) ?: return

                mkBvShiftLeftExpr(oneValue, exp)
            }
            is TvmArithmLogicalLshiftInst -> {
                scope.setDefaultGasUsage(stmt)

                val value = scope.calcOnState { stack.takeLastInt() }
                val shift = stmt.c + 1
                val shiftValue = shift.toBv257()
                check(shift in 1..256) { "Unexpected shift $shift" }

                val maxArgValue = mkBvArithShiftRightExpr(bvMaxValueSigned(TvmContext.INT_BITS), shiftValue)
                val minArgValue = mkBvArithShiftRightExpr(bvMinValueSigned(TvmContext.INT_BITS), shiftValue)
                val resNoOverflow = mkAnd(
                    mkBvSignedLessOrEqualExpr(minArgValue, value),
                    mkBvSignedLessOrEqualExpr(value, maxArgValue)
                )
                checkOverflow(resNoOverflow, scope) ?: return

                mkBvShiftLeftExpr(value, shift.toBv257())
            }
            is TvmArithmLogicalLshiftVarInst -> {
                scope.setDefaultGasUsage(stmt)

                val (shift, value) = scope.calcOnState { stack.takeLastInt() to stack.takeLastInt() }
                val notOutOfRangeExpr = unsignedIntegerFitsBits(shift, 10u)
                checkOutOfRange(notOutOfRangeExpr, scope) ?: return

                val maxArgValue = mkBvArithShiftRightExpr(bvMaxValueSigned(TvmContext.INT_BITS), shift)
                val minArgValue = mkBvArithShiftRightExpr(bvMinValueSigned(TvmContext.INT_BITS), shift)
                val resNoOverflow = mkAnd(
                    mkBvSignedLessOrEqualExpr(shift, 256.toBv257()),
                    mkBvSignedLessOrEqualExpr(minArgValue, value),
                    mkBvSignedLessOrEqualExpr(value, maxArgValue),
                )

                checkOverflow(resNoOverflow, scope) ?: return

                mkBvShiftLeftExpr(value, shift)
            }
            is TvmArithmLogicalRshiftInst -> {
                scope.doWithState { setGasUsage(26) } // todo: 18 in docs, but 26 in concrete execution

                val value = scope.calcOnState { stack.takeLastInt() }
                val shift = stmt.c + 1
                check(shift in 1..256) { "Unexpected shift $shift" }

                mkBvArithShiftRightExpr(value, shift.toBv257())
            }
            is TvmArithmLogicalRshiftVarInst -> {
                scope.setDefaultGasUsage(stmt)

                val (shift, value) = scope.calcOnState { stack.takeLastInt() to stack.takeLastInt() }
                val notOutOfRangeExpr = unsignedIntegerFitsBits(shift, 10u)
                checkOutOfRange(notOutOfRangeExpr, scope) ?: return

                mkBvArithShiftRightExpr(value, shift)
            }
            is TvmArithmLogicalFitsInst -> {
                scope.doWithState { setGasUsage(26) }

                val value = scope.calcOnState { stack.takeLastInt() }
                val sizeBits = stmt.c + 1
                check(sizeBits in 1..256) { "Unexpected sizeBits $sizeBits" }

                val resNoOverflow = signedIntegerFitsBits(value, sizeBits.toUInt())
                checkOverflow(resNoOverflow, scope) ?: return

                value
            }
            is TvmArithmLogicalFitsxInst -> {
                scope.doWithState { setGasUsage(26) }

                val (sizeBits, value) = scope.calcOnState { stack.takeLastInt() to stack.takeLastInt() }
                val notOutOfRangeExpr = unsignedIntegerFitsBits(sizeBits, 10u)
                checkOutOfRange(notOutOfRangeExpr, scope) ?: return

                val resNoOverflow = mkOr(
                    mkBvSignedGreaterOrEqualExpr(sizeBits, intBitsValue),
                    mkAnd(
                        mkBvSignedLessOrEqualExpr(bvMinValueSignedExtended(sizeBits), value),
                        mkBvSignedLessOrEqualExpr(value, bvMaxValueSignedExtended(sizeBits)),
                    ),
                )
                checkOverflow(resNoOverflow, scope) ?: return

                value
            }
            is TvmArithmLogicalUfitsInst -> {
                scope.doWithState { setGasUsage(26) }

                val value = scope.calcOnState { stack.takeLastInt() }
                val sizeBits = stmt.c + 1
                check(sizeBits in 1..256) { "Unexpected sizeBits $sizeBits" }

                val resNoOverflow = unsignedIntegerFitsBits(value, sizeBits.toUInt())
                checkOverflow(resNoOverflow, scope) ?: return

                value
            }
            is TvmArithmLogicalUfitsxInst -> {
                scope.doWithState { setGasUsage(26) }

                val (sizeBits, value) = scope.calcOnState { stack.takeLastInt() to stack.takeLastInt() }
                val notOutOfRangeExpr = unsignedIntegerFitsBits(sizeBits, 10u)
                checkOutOfRange(notOutOfRangeExpr, scope) ?: return
                
                val sizeBitsUpperBound = mkBvSubExpr(intBitsValue, oneValue)

                val notNegativeValue = mkBvSignedGreaterOrEqualExpr(value, zeroValue)
                val resNoOverflow = mkAnd(
                    notNegativeValue,
                    mkOr(
                        mkBvSignedGreaterOrEqualExpr(sizeBits, sizeBitsUpperBound),
                        mkBvSignedLessOrEqualExpr(value, bvMaxValueUnsignedExtended(sizeBits)),
                    ),
                )
                checkOverflow(resNoOverflow, scope) ?: return

                value
            }
            is TvmArithmLogicalBitsizeInst -> {
                scope.setDefaultGasUsage(stmt)

                val value = scope.calcOnState { stack.takeLastInt() }
                val symbolicSizeBits = scope.calcOnState { makeSymbolicPrimitive(int257sort) }

                val disjArgs = mutableListOf(
                    mkAnd(signedIntegerFitsBits(value, 0u), symbolicSizeBits eq zeroValue)
                )
                var prevMinValue: UExpr<UBvSort> = bvMinValueSignedExtended(zeroValue)
                var prevMaxValue: UExpr<UBvSort> = bvMaxValueSignedExtended(zeroValue)
                for (sizeBits in 1..TvmContext.INT_BITS.toInt()) {
                    val minValue = bvMinValueSignedExtended(sizeBits.toBv257())
                    val maxValue = bvMaxValueSignedExtended(sizeBits.toBv257())
                    val smallestCond = mkOr(
                        mkBvSignedLessExpr(value, prevMinValue),
                        mkBvSignedGreaterExpr(value, prevMaxValue),
                    )
                    val arg = mkAnd(
                        smallestCond,
                        signedIntegerFitsBits(value, sizeBits.toUInt()),
                        symbolicSizeBits eq sizeBits.toBv257(),
                    )

                    disjArgs.add(arg)

                    prevMinValue = minValue
                    prevMaxValue = maxValue
                }

                scope.assert(mkOr(disjArgs))
                symbolicSizeBits
            }
            is TvmArithmLogicalUbitsizeInst -> {
                scope.setDefaultGasUsage(stmt)

                val value = scope.calcOnState { stack.takeLastInt() }
                val notOutOfRangeExpr = mkBvSignedGreaterOrEqualExpr(value, zeroValue)
                checkOutOfRange(notOutOfRangeExpr, scope) ?: return

                val symbolicSizeBits = scope.calcOnState { makeSymbolicPrimitive(int257sort) }

                val disjArgs = mutableListOf(
                    mkAnd(unsignedIntegerFitsBits(value, 0u), symbolicSizeBits eq zeroValue)
                )
                var prevMaxValue: UExpr<UBvSort> = bvMaxValueUnsignedExtended(zeroValue)
                for (sizeBits in 1 until TvmContext.INT_BITS.toInt()) {
                    val maxValue = bvMaxValueUnsignedExtended(sizeBits.toBv257())
                    val smallestCond = mkBvSignedGreaterExpr(value, prevMaxValue)
                    val arg = mkAnd(
                        smallestCond,
                        unsignedIntegerFitsBits(value, sizeBits.toUInt()),
                        symbolicSizeBits eq sizeBits.toBv257(),
                    )

                    disjArgs.add(arg)

                    prevMaxValue = maxValue
                }

                scope.assert(mkOr(disjArgs))
                symbolicSizeBits
            }

            is TvmArithmLogicalChkbitAliasInst -> return visitArithmeticLogicalInst(scope, stmt.resolveAlias())
            is TvmArithmLogicalChkboolAliasInst -> return visitArithmeticLogicalInst(scope, stmt.resolveAlias())
        }

        scope.doWithState {
            stack.add(result, TvmIntegerType)
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitComparisonIntInst(scope: TvmStepScope, stmt: TvmCompareIntInst) = with(ctx) {
        scope.setDefaultGasUsage(stmt)

        when (stmt) {
            is TvmAliasInst -> visit(scope, stmt.resolveAlias())
            is TvmCompareIntEqintInst -> scope.doWithState {
                val x = stack.takeLastInt()
                val y = stmt.y.toBv257()
                val expr = x eq y
                putBooleanAndToNewStmt(stmt, expr)
            }
            is TvmCompareIntNeqintInst -> scope.doWithState {
                val x = stack.takeLastInt()
                val y = stmt.y.toBv257()
                val expr = (x eq y).not()
                putBooleanAndToNewStmt(stmt, expr)
            }
            is TvmCompareIntGtintInst -> scope.doWithState {
                val x = stack.takeLastInt()
                val y = stmt.y.toBv257()
                val expr = mkBvSignedGreaterExpr(x, y)
                putBooleanAndToNewStmt(stmt, expr)
            }
            is TvmCompareIntLessintInst -> scope.doWithState {
                val x = stack.takeLastInt()
                val y = stmt.y.toBv257()
                val expr = mkBvSignedLessExpr(x, y)
                putBooleanAndToNewStmt(stmt, expr)
            }
            is TvmCompareIntEqualInst -> scope.doWithState {
                val y = stack.takeLastInt()
                val x = stack.takeLastInt()
                val expr = x eq y
                putBooleanAndToNewStmt(stmt, expr)
            }
            is TvmCompareIntNeqInst -> scope.doWithState {
                val y = stack.takeLastInt()
                val x = stack.takeLastInt()
                val expr = (x eq y).not()
                putBooleanAndToNewStmt(stmt, expr)
            }
            is TvmCompareIntGreaterInst -> scope.doWithState {
                val y = stack.takeLastInt()
                val x = stack.takeLastInt()
                val expr = mkBvSignedGreaterExpr(x, y)
                putBooleanAndToNewStmt(stmt, expr)
            }
            is TvmCompareIntGeqInst -> scope.doWithState {
                val y = stack.takeLastInt()
                val x = stack.takeLastInt()
                val expr = mkBvSignedGreaterOrEqualExpr(x, y)
                putBooleanAndToNewStmt(stmt, expr)
            }
            is TvmCompareIntLessInst -> scope.doWithState {
                val y = stack.takeLastInt()
                val x = stack.takeLastInt()
                val expr = mkBvSignedLessExpr(x, y)
                putBooleanAndToNewStmt(stmt, expr)
            }
            is TvmCompareIntLeqInst -> scope.doWithState {
                val y = stack.takeLastInt()
                val x = stack.takeLastInt()
                val expr = mkBvSignedLessOrEqualExpr(x, y)
                putBooleanAndToNewStmt(stmt, expr)
            }
            is TvmCompareIntCmpInst -> scope.doWithState {
                val y = stack.takeLastInt()
                val x = stack.takeLastInt()
                doCmp(stmt, x, y)
            }
            is TvmCompareIntSgnInst -> scope.doWithState {
                val x = stack.takeLastInt()
                doCmp(stmt, x, zeroValue)
            }
            is TvmCompareIntChknanInst -> TODO()
            is TvmCompareIntIsnanInst -> TODO()
        }
    }

    private fun TvmState.doCmp(stmt: TvmInst, x: UExpr<UBvSort>, y: UExpr<UBvSort>) {
        val value = with(ctx) {
            mkIte(x eq y, zeroValue, mkIte(mkBvSignedLessExpr(x, y), minusOneValue, oneValue))
        }
        stack.add(value, TvmIntegerType)
        newStmt(stmt.nextStmt())
    }

    private fun TvmState.putBooleanAndToNewStmt(stmt: TvmInst, expr: UBoolExpr) {
        val value = ctx.mkIte(expr, ctx.trueValue, ctx.falseValue)
        stack.add(value, TvmIntegerType)
        newStmt(stmt.nextStmt())
    }

    private fun visitComparisonOtherInst(scope: TvmStepScope, stmt: TvmCompareOtherInst) {
        when (stmt) {
            is TvmCompareOtherSemptyInst -> {
                scope.setDefaultGasUsage(stmt)

                with(ctx) {
                    val slice = scope.calcOnState { stack.takeLastSlice() }

                    val cell = scope.calcOnState { memory.readField(slice, sliceCellField, addressSort) }
                    val dataPos = scope.calcOnState { memory.readField(slice, sliceDataPosField, sizeSort) }
                    val refsPos = scope.calcOnState { memory.readField(slice, sliceRefPosField, sizeSort) }
                    val dataLength = scope.calcOnState { memory.readField(cell, cellDataLengthField, sizeSort) }
                    val refsLength = scope.calcOnState { memory.readField(cell, cellRefsLengthField, sizeSort) }

                    val isRemainingDataEmptyConstraint = mkSizeGeExpr(dataPos, dataLength)
                    val areRemainingRefsEmpty = mkSizeGeExpr(refsPos, refsLength)

                    scope.fork(
                        mkAnd(isRemainingDataEmptyConstraint, areRemainingRefsEmpty),
                        blockOnFalseState = {
                            stack.add(falseValue, TvmIntegerType)
                            newStmt(stmt.nextStmt())
                        },
                        blockOnTrueState = {
                            stack.add(trueValue, TvmIntegerType)
                            newStmt(stmt.nextStmt())
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
        scope.setDefaultGasUsage(stmt)

        val slice = scope.calcOnState { stack.takeLastSlice() }
        val updatedSlice = scope.calcOnState {
            memory.allocConcrete(TvmSliceType).also { sliceCopy(slice, it) }
        }

        scope.doWithState {
            val ref = scope.sliceLoadNextRef(slice) ?: return@doWithState
            sliceMoveRefPtr(updatedSlice)

            stack.add(ref, TvmCellType)
            stack.add(updatedSlice, TvmSliceType)

            newStmt(stmt.nextStmt())
        }
    }

    private fun visitEndSliceInst(scope: TvmStepScope, stmt: TvmCellParseEndsInst) {
        scope.setDefaultGasUsage(stmt)

        with(ctx) {
            val slice = scope.calcOnState { stack.takeLastSlice() }

            val cell = scope.calcOnState { memory.readField(slice, sliceCellField, addressSort) }

            val dataLength = scope.calcOnState { memory.readField(cell, cellDataLengthField, sizeSort) }
            val refsLength = scope.calcOnState { memory.readField(cell, cellRefsLengthField, sizeSort) }
            val dataPos = scope.calcOnState { memory.readField(slice, sliceDataPosField, sizeSort) }
            val refsPos = scope.calcOnState { memory.readField(slice, sliceRefPosField, sizeSort) }

            val isRemainingDataEmptyConstraint = mkSizeGeExpr(dataPos, dataLength)
            val areRemainingRefsEmpty = mkSizeGeExpr(refsPos, refsLength)

            scope.fork(
                mkAnd(isRemainingDataEmptyConstraint, areRemainingRefsEmpty),
                blockOnFalseState = setFailure(TvmCellUnderflow)
            ) ?: return

            scope.doWithState {
                newStmt(stmt.nextStmt())
            }
        }
    }

    private fun visitLoadUnsignedIntInst(
        scope: TvmStepScope,
        stmt: TvmCellParseLduInst
    ) {
        scope.setDefaultGasUsage(stmt)

        val slice = scope.calcOnState { stack.takeLastSlice() }
        val updatedSlice = scope.calcOnState {
            memory.allocConcrete(TvmSliceType).also { sliceCopy(slice, it) }
        }

        val bitsLen = stmt.c + 1

        scope.doWithStateCtx {
            val value = scope.sliceLoadDataBits(slice, bitsLen)?.unsignedExtendToInteger() ?: return@doWithStateCtx
            sliceMoveDataPtr(updatedSlice, bitsLen)

            stack.add(value, TvmIntegerType)
            stack.add(updatedSlice, TvmSliceType)
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitCellToSliceInst(
        scope: TvmStepScope,
        stmt: TvmCellParseCtosInst
    ) {
        /**
         * todo: Transforming a cell into a slice costs 100 gas units if the cell is loading
         * for the first time and 25 for subsequent loads during the same transaction
         * */
        scope.doWithState { setGasUsage(118) }

        with(ctx) {
            scope.doWithState {
                val cell = stack.takeLastCell()

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
                newStmt(stmt.nextStmt())
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
        scope.setDefaultGasUsage(stmt)

        with(ctx) {
            scope.doWithState {
                val builder = stack.takeLastBuilder()
                val updatedBuilder = memory.allocConcrete(TvmBuilderType).also { builderCopy(builder, it) }

                val bits = stmt.c + 1
                val bvSort = mkBvSort(bits.toUInt())
                val intValue = stack.takeLastInt()
                if (intValue !is KBitVecValue) {
                    error("Not concrete value to store")
                }

                // TODO how to check out if range if we have already taken the value with the right sort?

                val builderDataLength = memory.readField(builder, cellDataLengthField, sizeSort)

                val newDataLength = mkSizeAddExpr(builderDataLength, mkSizeExpr(bits))
                val canWriteConstraint = mkSizeLeExpr(newDataLength, mkSizeExpr(MAX_DATA_LENGTH))

                scope.fork(
                    canWriteConstraint,
                    blockOnFalseState = setFailure(TvmCellOverflow)
                ) ?: return@doWithState

                builderStoreDataBits(updatedBuilder, mkBv(intValue.bigIntValue(), bvSort))

                stack.add(updatedBuilder, TvmBuilderType)
                newStmt(stmt.nextStmt())
            }
        }
    }

    private fun visitNewCellInst(scope: TvmStepScope, stmt: TvmCellBuildNewcInst) {
        scope.setDefaultGasUsage(stmt)

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
                newStmt(stmt.nextStmt())
            }
        }
    }

    private fun visitEndCellInst(scope: TvmStepScope, stmt: TvmCellBuildEndcInst) {
        scope.setDefaultGasUsage(stmt)

        val builder = scope.calcOnState { stack.takeLastBuilder() }
        val cell = scope.calcOnState {
            // TODO static or concrete
            memory.allocConcrete(TvmCellType).also { builderCopy(builder, it) }
        }

        scope.doWithState {
            stack.add(cell, TvmCellType)
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitTvmSaveControlFlowInst(
        scope: TvmStepScope,
        stmt: TvmContRegistersInst
    ) {
        scope.setDefaultGasUsage(stmt)

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

                    newStmt(stmt.nextStmt())

                    // TODO save to the correct register
                }
            }
            else -> TODO("$stmt")
        }
    }

    private fun visitTvmPushCtrInst(scope: TvmStepScope, stmt: TvmContRegistersPushctrInst) {
        scope.setDefaultGasUsage(stmt)

        scope.doWithState {
            // TODO use it!
            val registerIndex = stmt.i

            // TODO should we use real persistent or always consider it fully symbolic?
//            val data = registers.c4?.value?.value?.toSymbolic(scope) ?: mkSymbolicCell(scope)
            when (registerIndex) {
                4 -> {
                    val data = registers.c4?.value?.value ?: run {
                        val symbolicCell = generateSymbolicCell()
                        registers.c4 = C4Register(TvmCellValue(symbolicCell))
                        symbolicCell
                    }
                    stack.add(data, TvmCellType)
                    newStmt(stmt.nextStmt())
                }
                3 -> {
                    val mainMethod = contractCode.methods[Int.MAX_VALUE]
                        ?: error("No main method found")
                    val continuationValue = TvmContinuationValue(mainMethod, stack, registers)
                    stack += continuationValue
                    newStmt(stmt.nextStmt())
                }
                else -> TODO("Not yet implemented")
            }

        }
    }

    private fun visitTvmBasicControlFlowInst(
        scope: TvmStepScope,
        stmt: TvmContBasicInst
    ) {
        scope.setDefaultGasUsage(stmt)

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
            is TvmContBasicRetInst, is TvmArtificialImplicitRetInst -> {
                scope.doWithState {
                    returnFromMethod()
                }
            }
            else -> TODO("$stmt")
        }
    }

    private fun visitTvmConditionalControlFlowInst(
        scope: TvmStepScope,
        stmt: TvmContConditionalInst
    ) {
        scope.setDefaultGasUsage(stmt)

        when (stmt) {
            is TvmContConditionalIfretInst -> {
                scope.doWithState {
                    val operand = stack.takeLastInt()
                    with(ctx) {
                        val neqZero = mkEq(operand, falseValue).not()
                        scope.fork(
                            neqZero,
                            blockOnFalseState = { newStmt(stmt.nextStmt()) }
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
                    blockOnFalseState = { newStmt(stmt.nextStmt()) }
                )
            }
        }
    }

    private fun visitTvmDictionaryJumpInst(scope: TvmStepScope, stmt: TvmContDictInst) {
        scope.setDefaultGasUsage(stmt)

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

                    val nextStmt = stmt.nextStmt()
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
        scope.setDefaultGasUsage(stmt)

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
        scope.setDefaultGasUsage(stmt)

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

                scope.doWithState { newStmt(stmt.nextStmt()) }
            }
            else -> TODO("$stmt")
        }
    }

    private fun visitTvmTupleInst(scope: TvmStepScope, stmt: TvmTupleInst) {
        scope.setDefaultGasUsage(stmt)

        when (stmt) {
            is TvmAliasInst -> return visitTvmTupleInst(scope, stmt.resolveAlias() as TvmTupleInst)
            is TvmTupleNullInst -> scope.doWithStateCtx {
                stack.add(nullValue, TvmNullType)
                newStmt(stmt.nextStmt())
            }
            is TvmTupleIsnullInst -> scope.doWithStateCtx {
                val isNull = stack.lastIsNull()
                stack.pop(0)
                stack.add(if (isNull) trueValue else falseValue, TvmIntegerType)
                newStmt(stmt.nextStmt())
            }
            is TvmTupleNullswapifInst -> doPushNullIf(scope, stmt, swapIfZero = false, nullsCount = 1, skipOneEntryUnderTop = false)
            is TvmTupleNullswapif2Inst -> doPushNullIf(scope, stmt, swapIfZero = false, nullsCount = 2, skipOneEntryUnderTop = false)
            is TvmTupleNullrotrifInst -> doPushNullIf(scope, stmt, swapIfZero = false, nullsCount = 1, skipOneEntryUnderTop = true)
            is TvmTupleNullrotrif2Inst -> doPushNullIf(scope, stmt, swapIfZero = false, nullsCount = 2, skipOneEntryUnderTop = true)
            is TvmTupleNullswapifnotInst -> doPushNullIf(scope, stmt, swapIfZero = true, nullsCount = 1, skipOneEntryUnderTop = false)
            is TvmTupleNullswapifnot2Inst -> doPushNullIf(scope, stmt, swapIfZero = true, nullsCount = 2, skipOneEntryUnderTop = false)
            is TvmTupleNullrotrifnotInst -> doPushNullIf(scope, stmt, swapIfZero = true, nullsCount = 1, skipOneEntryUnderTop = true)
            is TvmTupleNullrotrifnot2Inst -> doPushNullIf(scope, stmt, swapIfZero = true, nullsCount = 2, skipOneEntryUnderTop = true)
            else -> TODO("$stmt")
        }
    }

    private fun doPushNullIf(
        scope: TvmStepScope,
        stmt: TvmTupleInst,
        swapIfZero: Boolean,
        nullsCount: Int,
        skipOneEntryUnderTop: Boolean
    ) {
        val value = scope.calcOnState { stack.takeLastInt() }
        val condition = scope.calcOnStateCtx {
            val cond = mkEq(value, zeroValue)
            if (swapIfZero) cond else cond.not()
        }
        scope.fork(
            condition,
            blockOnTrueState = {
                val entryUnderTop = if (skipOneEntryUnderTop) stack.takeLastEntry() else null

                repeat(nullsCount) {
                    stack.add(ctx.nullValue, TvmNullType)
                }

                if (entryUnderTop != null) stack.addStackEntry(entryUnderTop)

                stack.add(value, TvmIntegerType)
                newStmt(stmt.nextStmt())
            },
            blockOnFalseState = {
                stack.add(value, TvmIntegerType)
                newStmt(stmt.nextStmt())
            }
        )
    }

    private fun visitDebugInst(scope: TvmStepScope, stmt: TvmDebugInst) {
        scope.setDefaultGasUsage(stmt)

        // Do nothing
        scope.doWithState { newStmt(stmt.nextStmt()) }
    }

    private fun visitCodepageInst(scope: TvmStepScope, stmt: TvmCodepageInst) {
        scope.setDefaultGasUsage(stmt)

        // Do nothing
        scope.doWithState { newStmt(stmt.nextStmt()) }
    }

    context(TvmContext)
    private fun TvmSubSliceSerializedLoader.bitsToBv(): KBitVecValue<UBvSort> {
        // todo: check bits order
        return mkBv(bits.joinToString(""), bits.size.toUInt())
    }

    private fun UExpr<KBvSort>.extractConcrete(inst: TvmInst): Int {
        if (this !is KInterpretedValue)
            TODO("symbolic value in $inst")
        return (this as KBitVecValue<*>).toBigIntegerSigned().toInt()
    }
}
