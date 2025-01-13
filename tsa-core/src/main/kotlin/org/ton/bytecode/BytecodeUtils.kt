package org.ton.bytecode

const val TAG_PARAMETER_IDX: Int = 0
const val ACTIONS_PARAMETER_IDX: Int = 1
const val MSGS_SENT_PARAMETER_IDX: Int = 2
const val TIME_PARAMETER_IDX: Int = 3
const val BLOCK_TIME_PARAMETER_IDX: Int = 4
const val TRANSACTION_TIME_PARAMETER_IDX: Int = 5
const val SEED_PARAMETER_IDX: Int = 6
const val BALANCE_PARAMETER_IDX: Int = 7
const val ADDRESS_PARAMETER_IDX: Int = 8
const val CONFIG_PARAMETER_IDX: Int = 9
const val CODE_PARAMETER_IDX: Int = 10
const val INCOMING_VALUE_PARAMETER_IDX: Int = 11
const val STORAGE_FEES_PARAMETER_IDX: Int = 12
const val PREV_BLOCK_PARAMETER_IDX: Int = 13

fun TvmInst.resolvedAlias(): TvmInst =
    if (this is TvmAliasInst) resolveAlias() else this

fun List<TvmInst>.flattenStatements(): List<TvmInst> {
    val statements = mutableListOf<TvmInst>()
    val stack = mutableListOf(TvmInstList(this))

    while (stack.isNotEmpty()) {
        val instList = stack.removeLast()

        instList.list.forEach { stmt ->
            if (stmt is TvmArtificialInst) {
                return@forEach
            }

            statements.add(stmt)
            extractInstLists(stmt).forEach(stack::add)
        }
    }

    return statements
}

private fun extractInstLists(stmt: TvmInst): Sequence<TvmInstList> =
    when (stmt) {
        is TvmContOperand1Inst -> sequenceOf(stmt.c)
        is TvmContOperand2Inst -> sequenceOf(stmt.c1, stmt.c2)
        else -> emptySequence()
    }