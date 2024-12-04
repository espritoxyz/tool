package org.usvm.machine

import io.ksmt.solver.KTheory
import io.ksmt.solver.wrapper.bv2int.KBv2IntRewriter.SignednessMode
import io.ksmt.solver.wrapper.bv2int.KBv2IntRewriterConfig
import io.ksmt.solver.wrapper.bv2int.KBv2IntSolver
import io.ksmt.solver.yices.KYicesSolver
import io.ksmt.solver.z3.KZ3Solver
import org.usvm.UBv32SizeExprProvider
import org.usvm.UComponents
import org.usvm.UContext
import org.usvm.UMachineOptions
import org.usvm.USizeExprProvider
import org.usvm.machine.intblast.Bv2IntExprFilter
import org.usvm.machine.intblast.Bv2IntSolverWrapper
import org.usvm.machine.types.TvmType
import org.usvm.machine.types.TvmTypeSystem
import org.usvm.solver.USolverBase
import org.usvm.solver.UTypeSolver
import org.usvm.types.UTypeSystem

class TvmComponents(
    private val options: UMachineOptions,
) : UComponents<TvmType, TvmSizeSort> {
    private val closeableResources = mutableListOf<AutoCloseable>()
    override val useSolverForForks: Boolean
        get() = true

    override fun <Context : UContext<TvmSizeSort>> mkSizeExprProvider(ctx: Context): USizeExprProvider<TvmSizeSort> {
        return UBv32SizeExprProvider(ctx)
    }

    val typeSystem = TvmTypeSystem()

    override fun <Context : UContext<TvmSizeSort>> mkSolver(ctx: Context): USolverBase<TvmType> {
        val (translator, decoder) = buildTranslatorAndLazyDecoder(ctx)

        val bvSolver = KYicesSolver(ctx).apply {
            configure {
                optimizeForTheories(setOf(KTheory.UF, KTheory.Array, KTheory.BV))
            }
        }
        val intSolver = KZ3Solver(ctx).apply {
            configure {
                optimizeForTheories(setOf(KTheory.UF, KTheory.Array, KTheory.LIA, KTheory.NIA))
            }
        }
        val solver = Bv2IntSolverWrapper(
            bv2intSolver = KBv2IntSolver(
                ctx,
                intSolver,
                KBv2IntRewriterConfig(signednessMode = SignednessMode.SIGNED)
            ),
            regularSolver = bvSolver,
            exprFilter = Bv2IntExprFilter(
                ctx,
                excludeNonConstBvand = true,
                excludeNonConstShift = true,
                excludeNonlinearArith = false
            ),
        )

        closeableResources += solver

        val typeSolver = UTypeSolver(typeSystem)
        return USolverBase(ctx, solver, typeSolver, translator, decoder, options.solverTimeout)
    }

    override fun mkTypeSystem(ctx: UContext<TvmSizeSort>): UTypeSystem<TvmType> {
        return typeSystem
    }

    fun close() {
        closeableResources.forEach(AutoCloseable::close)
    }
}