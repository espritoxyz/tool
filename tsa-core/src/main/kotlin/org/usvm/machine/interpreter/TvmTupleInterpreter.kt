package org.usvm.machine.interpreter

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.ton.bytecode.TvmAliasInst
import org.ton.bytecode.TvmComplexGas
import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmIntegerType
import org.ton.bytecode.TvmNullType
import org.ton.bytecode.TvmTupleExplodeInst
import org.ton.bytecode.TvmTupleExplodevarInst
import org.ton.bytecode.TvmTupleIndex2Inst
import org.ton.bytecode.TvmTupleIndex3Inst
import org.ton.bytecode.TvmTupleIndexInst
import org.ton.bytecode.TvmTupleIndexqInst
import org.ton.bytecode.TvmTupleIndexvarInst
import org.ton.bytecode.TvmTupleIndexvarqInst
import org.ton.bytecode.TvmTupleInst
import org.ton.bytecode.TvmTupleIsnullInst
import org.ton.bytecode.TvmTupleIstupleInst
import org.ton.bytecode.TvmTupleLastInst
import org.ton.bytecode.TvmTupleNullInst
import org.ton.bytecode.TvmTupleNullrotrif2Inst
import org.ton.bytecode.TvmTupleNullrotrifInst
import org.ton.bytecode.TvmTupleNullrotrifnot2Inst
import org.ton.bytecode.TvmTupleNullrotrifnotInst
import org.ton.bytecode.TvmTupleNullswapif2Inst
import org.ton.bytecode.TvmTupleNullswapifInst
import org.ton.bytecode.TvmTupleNullswapifnot2Inst
import org.ton.bytecode.TvmTupleNullswapifnotInst
import org.ton.bytecode.TvmTupleQtlenInst
import org.ton.bytecode.TvmTupleSetindexInst
import org.ton.bytecode.TvmTupleSetindexqInst
import org.ton.bytecode.TvmTupleSetindexvarInst
import org.ton.bytecode.TvmTupleSetindexvarqInst
import org.ton.bytecode.TvmTupleTlenInst
import org.ton.bytecode.TvmTupleTpopInst
import org.ton.bytecode.TvmTupleTpushInst
import org.ton.bytecode.TvmTupleTupleInst
import org.ton.bytecode.TvmTupleTuplevarInst
import org.ton.bytecode.TvmTupleUnpackfirstInst
import org.ton.bytecode.TvmTupleUnpackfirstvarInst
import org.ton.bytecode.TvmTupleUntupleInst
import org.ton.bytecode.TvmTupleUntuplevarInst
import org.usvm.machine.TvmContext
import org.usvm.machine.state.SIMPLE_GAS_USAGE
import org.usvm.machine.state.TvmIntegerOutOfRange
import org.usvm.machine.state.TvmStack
import org.usvm.machine.state.TvmStack.TvmStackTupleValueConcreteNew
import org.usvm.machine.state.TvmTypeCheckError
import org.usvm.machine.state.calcOnStateCtx
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.consumeGas
import org.usvm.machine.state.doWithStateCtx
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.setFailure
import org.usvm.machine.state.takeLastInt
import org.usvm.machine.state.takeLastTuple

class TvmTupleInterpreter(private val ctx: TvmContext) {
    fun visitTvmTupleInst(scope: TvmStepScope, stmt: TvmTupleInst) {
        if (stmt.gasConsumption !is TvmComplexGas) {
            scope.consumeDefaultGas(stmt)
        }

        when (stmt) {
            is TvmTupleTupleInst -> visitMakeTupleInst(scope, stmt)
            is TvmTupleUntupleInst -> visitUntupleInst(scope, stmt)
            is TvmTupleTlenInst -> visitGetTupleLenInst(scope, stmt, quiet = false)
            is TvmTupleQtlenInst -> visitGetTupleLenInst(scope, stmt, quiet = true)
            is TvmTupleIndexInst -> {
                doConcreteGet(scope, stmt, stmt.k, quiet = false) ?: return
                scope.doWithState { newStmt(stmt.nextStmt()) }
            }
            is TvmTupleIndexqInst -> {
                doConcreteGet(scope, stmt, stmt.k, quiet = true) ?: return
                scope.doWithState { newStmt(stmt.nextStmt()) }
            }
            is TvmTupleIndex2Inst -> {
                doIndex2(scope, stmt, stmt.i, stmt.j) ?: return
                scope.doWithState { newStmt(stmt.nextStmt()) }
            }
            is TvmTupleIndex3Inst -> {
                doIndex3(scope, stmt) ?: return
                scope.doWithState { newStmt(stmt.nextStmt()) }
            }
            is TvmTupleSetindexInst -> doConcreteSet(scope, stmt, stmt.k, quiet = false)
            is TvmTupleSetindexqInst -> doConcreteSet(scope, stmt, stmt.k, quiet = true)
            is TvmTupleNullInst -> visitNullInst(scope, stmt)
            is TvmTupleIsnullInst -> visitIsNull(scope, stmt)
            is TvmTupleIstupleInst -> visitIsTupleInst(scope, stmt)
            is TvmTupleNullswapifInst -> doPushNullIf(scope, stmt, swapIfZero = false, nullsCount = 1, skipOneEntryUnderTop = false)
            is TvmTupleNullswapif2Inst -> doPushNullIf(scope, stmt, swapIfZero = false, nullsCount = 2, skipOneEntryUnderTop = false)
            is TvmTupleNullrotrifInst -> doPushNullIf(scope, stmt, swapIfZero = false, nullsCount = 1, skipOneEntryUnderTop = true)
            is TvmTupleNullrotrif2Inst -> doPushNullIf(scope, stmt, swapIfZero = false, nullsCount = 2, skipOneEntryUnderTop = true)
            is TvmTupleNullswapifnotInst -> doPushNullIf(scope, stmt, swapIfZero = true, nullsCount = 1, skipOneEntryUnderTop = false)
            is TvmTupleNullswapifnot2Inst -> doPushNullIf(scope, stmt, swapIfZero = true, nullsCount = 2, skipOneEntryUnderTop = false)
            is TvmTupleNullrotrifnotInst -> doPushNullIf(scope, stmt, swapIfZero = true, nullsCount = 1, skipOneEntryUnderTop = true)
            is TvmTupleNullrotrifnot2Inst -> doPushNullIf(scope, stmt, swapIfZero = true, nullsCount = 2, skipOneEntryUnderTop = true)
            is TvmTupleExplodeInst -> TODO()
            is TvmTupleExplodevarInst -> TODO()
            is TvmTupleIndexvarInst -> TODO()
            is TvmTupleIndexvarqInst -> TODO()
            is TvmTupleLastInst -> TODO()
            is TvmTupleSetindexvarInst -> TODO()
            is TvmTupleSetindexvarqInst -> TODO()
            is TvmTupleTpopInst -> TODO()
            is TvmTupleTpushInst -> TODO()
            is TvmTupleTuplevarInst -> TODO()
            is TvmTupleUnpackfirstInst -> TODO()
            is TvmTupleUnpackfirstvarInst -> TODO()
            is TvmTupleUntuplevarInst -> TODO()
            is TvmAliasInst -> visitTvmTupleInst(scope, stmt.resolveAlias() as TvmTupleInst)
        }
    }

    private fun visitMakeTupleInst(scope: TvmStepScope, stmt: TvmTupleTupleInst) {
        val size = stmt.n
        check(size in 0..15) {
            "Unexpected tuple size $size"
        }

        scope.doWithState {
            consumeGas(SIMPLE_GAS_USAGE + size)
        }

        scope.doWithStateCtx {
            val tupleElements = List(size) { stack.takeLastEntry() }.asReversed()
            val tupleConcreteStackEntry = TvmStackTupleValueConcreteNew(ctx, tupleElements.toPersistentList())

            stack.addTuple(tupleConcreteStackEntry)
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitUntupleInst(scope: TvmStepScope, stmt: TvmTupleUntupleInst) {
        val size = stmt.n
        check(size in 0..15) {
            "Unexpected tuple size $size"
        }

        scope.doWithState {
            consumeGas(SIMPLE_GAS_USAGE)
        }

        val tuple = scope.takeLastTuple()
        if (tuple == null) {
            scope.doWithState(setFailure(TvmTypeCheckError))
            return
        }

        when (tuple) {
            is TvmStackTupleValueConcreteNew -> {
                if (size != tuple.concreteSize) {
                    scope.doWithState { setFailure(TvmTypeCheckError)(this) }
                    return
                }

                scope.doWithState {
                    tuple.entries.forEach { stack.addStackEntry(it) }
                }
            }

            is TvmStack.TvmStackTupleValueInputValue -> {
                with(ctx) {
                    scope.assert(tuple.size eq size.toBv257())
                        ?: error("Cannot make $tuple size equal to $size")
                }

                scope.doWithStateCtx {
                    (0..<size).forEach {
                        val element = tuple[it, stack]
                        stack.addStackEntry(element)
                    }
                }
            }
        }

        scope.doWithState {
            consumeGas(size) // Successful untupling
            newStmt(stmt.nextStmt())
        }
    }

    private fun visitGetTupleLenInst(scope: TvmStepScope, stmt: TvmTupleInst, quiet: Boolean) {
        val tuple = scope.takeLastTuple()
        if (tuple == null) {
            if (!quiet) {
                scope.doWithState(setFailure(TvmTypeCheckError))
            } else {
                scope.doWithStateCtx {
                    stack.add(minusOneValue, TvmIntegerType)
                    newStmt(stmt.nextStmt())
                }
            }

            return
        }

        scope.doWithStateCtx {
            stack.add(tuple.size, TvmIntegerType)
            newStmt(stmt.nextStmt())
        }
    }

    private fun doConcreteGet(scope: TvmStepScope, stmt: TvmInst, index: Int, quiet: Boolean): Unit? {
        check(index in 0..15) {
            "Unexpected tuple index $index"
        }

        val lastIsNull = scope.calcOnState { stack.lastIsNull() }
        if (lastIsNull) {
            if (!quiet) {
                scope.doWithState(setFailure(TvmTypeCheckError))
                return null
            }

            return Unit
        }

        val tuple = scope.takeLastTuple()
        if (tuple == null) {
            scope.doWithState(setFailure(TvmTypeCheckError))
            return null
        }

        val size = tuple.size

        scope.fork(
            with(ctx) { mkBvSignedLessExpr(index.toBv257(), size) },
            blockOnFalseState = {
                if (quiet) {
                    stack.add(ctx.nullValue, TvmNullType)
                    newStmt(stmt.nextStmt())
                } else {
                    setFailure(TvmIntegerOutOfRange)(this)
                }
            }
        ) ?: return null

        scope.doWithState {
            val element = tuple[index, stack]
            stack.addStackEntry(element)
        }

        return Unit
    }

    private fun doConcreteSet(scope: TvmStepScope, stmt: TvmInst, index: Int, quiet: Boolean) {
        check(index in 0..15) {
            "Unexpected tuple index $index"
        }

        scope.doWithStateCtx {
            consumeGas(SIMPLE_GAS_USAGE)
        }

        val (isValueNull, value) = scope.calcOnState { stack.lastIsNull() to stack.takeLastEntry() }
        val lastIsNull = scope.calcOnState { stack.lastIsNull() }
        if (lastIsNull) {
            scope.doWithState {
                if (quiet) {
                    stack.pop(0)
                    TvmStackTupleValueConcreteNew(ctx, persistentListOf())
                } else {
                    setFailure(TvmTypeCheckError)(this)
                }
            }

            return
        }

        val tuple = scope.takeLastTuple()
        if (tuple == null) {
            scope.doWithState(setFailure(TvmTypeCheckError))
            return
        }

        val size = tuple.size

        scope.fork(
            with(ctx) { mkBvSignedLessExpr(index.toBv257(), size) },
            blockOnFalseState = {
                if (quiet) {
                    TODO("How to extend tuple with symbolic number of null values?")
                } else {
                    setFailure(TvmIntegerOutOfRange)(this)
                }
            }
        ) ?: return

        val updatedTuple = if (isValueNull && quiet) {
            tuple // Do not consume gas!
        } else {
            tuple.set(index, value)
        }

        scope.doWithStateCtx {
            stack.addTuple(updatedTuple)
            consumeGas(size.extractToSizeSort())
            newStmt(stmt.nextStmt())
        }
    }

    private fun doIndex2(scope: TvmStepScope, stmt: TvmTupleInst, i: Int, j: Int): Unit? {
        check(i in 0..3) {
            "Unexpected index $i in $stmt"
        }
        check(j in 0..3) {
            "Unexpected index $i in $stmt"
        }
        doConcreteGet(scope, stmt, i, quiet = false) ?: return null
        doConcreteGet(scope, stmt, j, quiet = false) ?: return null

        return Unit
    }

    private fun doIndex3(scope: TvmStepScope, stmt: TvmTupleIndex3Inst): Unit? {
        val k = stmt.k
        check(k in 0..3) {
            "Unexpected index $k in $stmt"
        }

        doIndex2(scope, stmt, stmt.i, stmt.j) ?: return null
        doConcreteGet(scope, stmt, stmt.k, quiet = false) ?: return null

        return Unit
    }

    private fun visitIsNull(scope: TvmStepScope, stmt: TvmTupleIsnullInst) = scope.doWithStateCtx {
        val isNull = stack.lastIsNull()
        stack.pop(0)
        stack.add(if (isNull) trueValue else falseValue, TvmIntegerType)
        newStmt(stmt.nextStmt())
    }

    private fun visitIsTupleInst(scope: TvmStepScope, stmt: TvmTupleIstupleInst) = scope.doWithStateCtx {
        val lastTuple = scope.takeLastTuple()
        stack.add(if (lastTuple != null) trueValue else falseValue, TvmIntegerType)
        newStmt(stmt.nextStmt())
    }

    private fun visitNullInst(scope: TvmStepScope, stmt: TvmTupleNullInst) = scope.doWithStateCtx {
        stack.add(nullValue, TvmNullType)
        newStmt(stmt.nextStmt())
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
}
