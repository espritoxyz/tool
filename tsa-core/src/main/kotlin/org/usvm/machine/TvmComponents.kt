package org.example.org.usvm.machine

import io.ksmt.solver.z3.KZ3Solver
import org.ton.bytecode.TvmType
import org.usvm.*
import org.usvm.machine.USizeSort
import org.usvm.solver.USolverBase
import org.usvm.solver.UTypeSolver
import org.usvm.types.UTypeStream
import org.usvm.types.UTypeSystem
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class TvmTypeSystem() : UTypeSystem<TvmType> {
    override val typeOperationsTimeout: Duration
        get() = TODO("Not yet implemented")

    override fun findSubtypes(type: TvmType): Sequence<TvmType> {
        TODO("Not yet implemented")
    }

    override fun hasCommonSubtype(type: TvmType, types: Collection<TvmType>): Boolean {
        TODO("Not yet implemented")
    }

    override fun isFinal(type: TvmType): Boolean {
        TODO("Not yet implemented")
    }

    override fun isInstantiable(type: TvmType): Boolean {
        TODO("Not yet implemented")
    }

    override fun isSupertype(supertype: TvmType, type: TvmType): Boolean {
        TODO("Not yet implemented")
    }

    override fun topTypeStream(): UTypeStream<TvmType> {
        TODO("Not yet implemented")
    }
}

class TvmComponents : UComponents<TvmType, USizeSort> {
    override val useSolverForForks: Boolean
        get() = true

    override fun <Context : UContext<USizeSort>> mkSizeExprProvider(ctx: Context): USizeExprProvider<USizeSort> {
        return UBv32SizeExprProvider(ctx)
    }

    override fun <Context : UContext<USizeSort>> mkSolver(ctx: Context): USolverBase<TvmType> {
        val (translator, decoder) = buildTranslatorAndLazyDecoder(ctx)

        val solver = KZ3Solver(ctx)
        val typeSolver = UTypeSolver(TvmTypeSystem())
        return USolverBase(ctx, solver, typeSolver, translator, decoder, 1000.milliseconds)
    }

    override fun mkTypeSystem(ctx: UContext<USizeSort>): UTypeSystem<TvmType> {
        return TvmTypeSystem()
    }
}