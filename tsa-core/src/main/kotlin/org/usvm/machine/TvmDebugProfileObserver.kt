package org.usvm.machine

import org.ton.bytecode.TvmArtificialInst
import org.ton.bytecode.TvmCodeBlock
import org.ton.bytecode.TvmContDictCalldictInst
import org.ton.bytecode.TvmContDictCalldictLongInst
import org.ton.bytecode.TvmContOperand1Inst
import org.ton.bytecode.TvmContOperand2Inst
import org.ton.bytecode.TvmContOperandInst
import org.ton.bytecode.TvmContractCode
import org.ton.bytecode.TvmInst
import org.ton.bytecode.TvmInstLambdaLocation
import org.ton.bytecode.TvmInstLocation
import org.ton.bytecode.TvmLambda
import org.ton.bytecode.TvmMethod
import org.usvm.machine.state.TvmState
import org.usvm.statistics.UDebugProfileObserver

fun getTvmDebugProfileObserver(code: TvmContractCode) =
    UDebugProfileObserver(
        TvmStatementOperations(code),
        profilerOptions = UDebugProfileObserver.Options(
            printNonVisitedStatements = true,
            padInstructionEnd = 50,
        )
    )

private class TvmStatementOperations(
    private val code: TvmContractCode
): UDebugProfileObserver.StatementOperations<TvmInst, TvmCodeBlock, TvmState> {
    override fun getMethodOfStatement(statement: TvmInst): TvmCodeBlock {
        var curLoc = statement.location
        while (curLoc is TvmInstLambdaLocation) {
            curLoc = curLoc.parent
        }
        return curLoc.codeBlock
    }

    override fun getMethodToCallIfCallStatement(statement: TvmInst): TvmMethod? {
        return when (statement) {
            is TvmContDictCalldictInst -> {
                code.methods[statement.n.toBigInteger()]
            }
            is TvmContDictCalldictLongInst -> {
                code.methods[statement.n.toBigInteger()]
            }
            else -> {
                null
            }
        }
    }

    override fun getAllMethodStatements(method: TvmCodeBlock): List<TvmInst> {
        return getAllMethodStatements(method.instList)
    }

    private fun getAllMethodStatements(instList: List<TvmInst>): List<TvmInst> {
        return instList.flatMap {
            when (it) {
                !is TvmContOperandInst -> {
                    if (it is TvmArtificialInst) emptyList() else listOf(it)
                }
                is TvmContOperand1Inst -> {
                    listOf(it) + getAllMethodStatements(it.c)
                }
                is TvmContOperand2Inst -> {
                    listOf(it) + getAllMethodStatements(it.c1) + getAllMethodStatements(it.c2)
                }
            }
        }
    }

    override fun getStatementIndexInMethod(statement: TvmInst): Int {
        // will be called only with option [printNonVisitedStatements] set to true
        TODO("not implemented")
    }

    override fun printMethodName(method: TvmCodeBlock): String {
        return when (method) {
            is TvmMethod -> "Method ${method.id}"
            is TvmLambda -> "TvmLambda"
        }
    }

    override fun printStatement(statement: TvmInst): String {
        val name = statement.mnemonic
        var prefix = if (statement.location is TvmInstLambdaLocation) {
            ">"
        } else {
            ""
        }
        var curLoc: TvmInstLocation = statement.location
        while (curLoc is TvmInstLambdaLocation) {
            prefix = "-$prefix"
            curLoc = curLoc.parent
        }
        return "$prefix$name"
    }
}