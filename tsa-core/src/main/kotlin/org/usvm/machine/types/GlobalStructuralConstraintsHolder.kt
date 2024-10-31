package org.usvm.machine.types

import org.usvm.UBoolExpr
import org.usvm.isTrue
import org.usvm.machine.TvmStepScopeManager

class GlobalStructuralConstraintsHolder {
    private val constraints = hashSetOf<UBoolExpr>()

    fun addStructuralConstraint(constraint: UBoolExpr) {
        if (constraint.isTrue) {
            return
        }

        constraints.add(constraint)
    }

    fun applyTo(stepScope: TvmStepScopeManager): Unit? {
        // TODO: memorize already applied constraints
        var result: Unit? = Unit
        constraints.forEach {
            // we might want to apply structural constraints to forked states (with erroneous paths)
            // even if we do not have a valid curState
            if (stepScope.canBeProcessed) {
                stepScope.assert(it) ?: run {
                    result = null
                }
            }
            stepScope.filterForkedStatesOnCondition(it)
        }
        return result
    }
}
