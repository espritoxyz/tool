package org.ton.bytecode

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.usvm.machine.MethodId

// TODO is it a real entity?
@Serializable
sealed class TvmCodeBlock {
    abstract val instList: List<TvmInst>

    protected fun initLocationsCodeBlock() {
        instList.forEach {
            it.location.codeBlock = this
        }
    }
}

@Serializable
@SerialName("TvmMethod")
data class TvmMethod(
    val id: @Contextual MethodId,
    @SerialName("instList")
    private val instListRaw: MutableList<TvmInst>
) : TvmCodeBlock() {
    override val instList: List<TvmInst>
        get() = instListRaw

    init {
        setLocationParents(instList, parent = null)
        instListRaw += TvmArtificialImplicitRetInst(TvmInstMethodLocation(id, instListRaw.size))
        initLocationsCodeBlock()
    }

    override fun toString(): String = "TvmMethod(id=$id)"

    private fun setLocationParents(instList: List<TvmInst>, parent: TvmInstLocation?) {
        instList.forEach {
            if (parent != null) {
                check(it.location is TvmInstLambdaLocation) {
                    "unexpected location: ${it.location}"
                }
                (it.location as TvmInstLambdaLocation).parent = parent
            }
            when (it) {
                !is TvmContOperandInst -> {
                    // do nothing
                }
                is TvmContOperand1Inst -> {
                    setLocationParents(it.c, it.location)
                }
                is TvmContOperand2Inst -> {
                    setLocationParents(it.c1, it.location)
                    setLocationParents(it.c2, it.location)
                }
            }
        }
    }
}

// An artificial entity representing instructions in continuation
@Serializable
@SerialName("TvmLambda")
data class TvmLambda(
    @SerialName("instList")
    private val instListRaw: MutableList<TvmInst>
) : TvmCodeBlock() {
    override val instList: List<TvmInst>
        get() = instListRaw

    init {
        initLocationsCodeBlock()
        instListRaw += returnStmt()
    }

    private fun returnStmt(): TvmArtificialImplicitRetInst {
        check(instList.isNotEmpty()) {
            "TvmLambda must not be empty"
        }
        val lastStmt = instList.last()
        return TvmArtificialImplicitRetInst(lastStmt.location.increment())
    }
}
