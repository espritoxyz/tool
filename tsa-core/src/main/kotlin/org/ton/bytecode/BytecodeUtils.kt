package org.ton.bytecode

const val TIME_PARAMETER_IDX: Int = 3

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