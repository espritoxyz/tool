package org.usvm.machine.state

import org.ton.bytecode.TvmBuilderType
import org.ton.bytecode.TvmCellType
import org.ton.bytecode.TvmContinuationType
import org.ton.bytecode.TvmContinuationValue
import org.ton.bytecode.TvmIntegerType
import org.ton.bytecode.TvmReferenceType
import org.ton.bytecode.TvmSliceType
import org.ton.bytecode.TvmTupleType
import org.usvm.UBvSort
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.api.writeField
import org.usvm.isStaticHeapRef
import org.usvm.machine.TvmContext
import org.usvm.mkSizeExpr
import org.usvm.sizeSort

fun TvmStack.takeLastInt(): UExpr<UBvSort> {
    val intStackValue = takeLast(TvmIntegerType) { id ->
        ctx.mkRegisterReading(id, ctx.int257sort)
    }

    return intStackValue.intValue
}

context(TvmState)
fun TvmStack.takeLastCell(): UHeapRef =
    takeLastRef(this, TvmCellType, TvmStack.TvmStackValue::cellValue)

context(TvmState)
fun TvmStack.takeLastSlice(): UHeapRef =
    takeLastRef(this, TvmSliceType, TvmStack.TvmStackValue::sliceValue).also {
        if (it.isCellViewInputRef()) {
            // TODO hack! Assume that all input slices were not read, that means dataPos == 0 and refsPos == 0
            with(ctx) {
                memory.writeField(it, TvmContext.sliceDataPosField, sizeSort, mkSizeExpr(0), guard = trueExpr)
                memory.writeField(it, TvmContext.sliceRefPosField, sizeSort, mkSizeExpr(0), guard = trueExpr)
            }
        }
    }

context(TvmState)
fun TvmStack.takeLastBuilder(): UHeapRef =
    takeLastRef(this, TvmBuilderType, TvmStack.TvmStackValue::builderValue).also {
        if (it.isCellViewInputRef()) {
            // TODO hack! Assume that all input builder were not written, that means dataLength == 0 and refsLength == 0
            with(ctx) {
                memory.writeField(it, TvmContext.cellDataLengthField, sizeSort, mkSizeExpr(0), guard = trueExpr)
                memory.writeField(it, TvmContext.cellRefsLengthField, sizeSort, mkSizeExpr(0), guard = trueExpr)
            }
        }
    }

context(TvmState)
fun TvmStack.takeLastTuple(): UHeapRef =
    takeLastRef(this, TvmTupleType, TvmStack.TvmStackValue::tupleValue)

fun TvmStack.takeLastContinuation(): TvmContinuationValue {
    val continuationStackValue = takeLast(TvmContinuationType) { _ ->
        error("Unexpected continuation as an input")
    }

    return continuationStackValue.continuationValue
}

context(TvmState)
private fun takeLastRef(
    stack: TvmStack,
    referenceType: TvmReferenceType,
    extractValue: TvmStack.TvmStackValue.() -> UHeapRef
): UHeapRef {
    val lastRefValue = stack.takeLast(referenceType) {
        generateSymbolicRef(referenceType)
    }

    return lastRefValue.extractValue()
}

/**
 * For now, we make static refs for all used (popped from the stack) input non-primitive values.
 * Any possible ite expressions could be made only in the [org.usvm.machine.state.TvmCellRefsRegion.readCellRef] method,
 * which is used only for cells, that do not require any input constraints. So, any cell view ([TvmSliceType] or [TvmBuilderType])
 * could be either static ref or allocated ref, that was allocated in the body of the method.
 */
private fun UHeapRef.isCellViewInputRef(): Boolean = isStaticHeapRef(this)
