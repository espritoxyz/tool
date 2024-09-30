package org.usvm.machine

import io.ksmt.solver.wrapper.bv2int.KBv2IntRewriter.SignednessMode
import io.ksmt.solver.wrapper.bv2int.KBv2IntRewriterConfig
import io.ksmt.solver.wrapper.bv2int.KBv2IntSolver
import io.ksmt.solver.yices.KYicesSolver
import io.ksmt.solver.z3.KZ3Solver
import org.usvm.machine.types.TvmType
import org.usvm.UBv32SizeExprProvider
import org.usvm.UComponents
import org.usvm.UContext
import org.usvm.USizeExprProvider
import org.usvm.machine.intblast.Bv2IntExprFilter
import org.usvm.machine.intblast.Bv2IntSolverWrapper
import org.usvm.machine.types.TvmTypeSystem
import org.usvm.solver.USolverBase
import org.usvm.solver.UTypeSolver
import org.usvm.types.UTypeSystem
import kotlin.time.Duration.Companion.milliseconds

class TvmComponents : UComponents<TvmType, TvmSizeSort> {
    private val closeableResources = mutableListOf<AutoCloseable>()
    override val useSolverForForks: Boolean
        get() = true

    override fun <Context : UContext<TvmSizeSort>> mkSizeExprProvider(ctx: Context): USizeExprProvider<TvmSizeSort> {
        return UBv32SizeExprProvider(ctx)
    }

    val typeSystem = TvmTypeSystem()

    override fun <Context : UContext<TvmSizeSort>> mkSolver(ctx: Context): USolverBase<TvmType> {
        val (translator, decoder) = buildTranslatorAndLazyDecoder(ctx)

        val solver = Bv2IntSolverWrapper(
            bv2intSolver = KBv2IntSolver(
                ctx,
                KZ3Solver(ctx),
                KBv2IntRewriterConfig(signednessMode = SignednessMode.SIGNED)
            ),
            regularSolver = KYicesSolver(ctx),
            exprFilter = Bv2IntExprFilter(
                ctx,
                excludeNonConstBvand = true,
                excludeNonConstShift = true,
                excludeNonlinearArith = false
            ),
        )

        closeableResources += solver

        val typeSolver = UTypeSolver(typeSystem)
        return USolverBase(ctx, solver, typeSolver, translator, decoder, 1000.milliseconds)
    }

    override fun mkTypeSystem(ctx: UContext<TvmSizeSort>): UTypeSystem<TvmType> {
        return typeSystem
    }

    fun close() {
        closeableResources.forEach(AutoCloseable::close)
    }
}