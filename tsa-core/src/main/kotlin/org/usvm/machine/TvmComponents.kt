package org.usvm.machine

import io.ksmt.solver.z3.KZ3Solver
import org.ton.bytecode.TvmCellType
import org.ton.bytecode.TvmType
import org.usvm.UBv32SizeExprProvider
import org.usvm.UComponents
import org.usvm.UContext
import org.usvm.USizeExprProvider
import org.usvm.solver.USolverBase
import org.usvm.solver.UTypeSolver
import org.usvm.types.USingleTypeStream
import org.usvm.types.UTypeStream
import org.usvm.types.UTypeSystem
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class TvmTypeSystem : UTypeSystem<TvmType> {
    override val typeOperationsTimeout: Duration
        get() = TODO("Not yet implemented")

    override fun findSubtypes(type: TvmType): Sequence<TvmType> = sequenceOf(type)

    override fun hasCommonSubtype(type: TvmType, types: Collection<TvmType>): Boolean = type in types

    override fun isFinal(type: TvmType): Boolean = true

    override fun isInstantiable(type: TvmType): Boolean = true

    override fun isSupertype(supertype: TvmType, type: TvmType): Boolean = supertype == type

    private val topTypeStream by lazy { USingleTypeStream(this, TvmCellType) }

    override fun topTypeStream(): UTypeStream<TvmType> = topTypeStream
}

class TvmComponents : UComponents<TvmType, TvmSizeSort> {
    override val useSolverForForks: Boolean
        get() = true

    override fun <Context : UContext<TvmSizeSort>> mkSizeExprProvider(ctx: Context): USizeExprProvider<TvmSizeSort> {
        return UBv32SizeExprProvider(ctx)
    }

    override fun <Context : UContext<TvmSizeSort>> mkSolver(ctx: Context): USolverBase<TvmType> {
        val (translator, decoder) = buildTranslatorAndLazyDecoder(ctx)

        val solver = KZ3Solver(ctx)
        val typeSolver = UTypeSolver(TvmTypeSystem())
        return USolverBase(ctx, solver, typeSolver, translator, decoder, 1000.milliseconds)
    }

    override fun mkTypeSystem(ctx: UContext<TvmSizeSort>): UTypeSystem<TvmType> {
        return TvmTypeSystem()
    }
}