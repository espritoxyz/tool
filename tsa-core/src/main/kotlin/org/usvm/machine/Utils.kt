package org.usvm.machine

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.BvUtils.toBigIntegerSigned
import org.ton.bytecode.TvmCodeBlock
import org.ton.bytecode.TvmMethod
import org.usvm.UBvSort
import org.usvm.UExpr
import org.usvm.test.resolver.TvmTestDataCellValue
import org.usvm.test.resolver.TvmTestSliceValue
import java.nio.file.Path
import kotlin.io.path.Path

@Suppress("NOTHING_TO_INLINE")
inline fun UExpr<out UBvSort>.bigIntValue() = (this as KBitVecValue<*>).toBigIntegerSigned()
@Suppress("NOTHING_TO_INLINE")
inline fun UExpr<out UBvSort>.intValue() = bigIntValue().toInt()

fun TvmCodeBlock.extractMethodId(): MethodId = (this as? TvmMethod)?.id
    ?: error("Cannot extract method id from $this")

inline fun <T> tryCatchIf(condition: Boolean, body: () -> T, exceptionHandler: (Throwable) -> T): T {
    if (!condition) {
        return body()
    }

    return runCatching {
        body()
    }.getOrElse(exceptionHandler)
}

/**
 * @return remaining data bits and refs as a cell
 */
fun truncateSliceCell(slice: TvmTestSliceValue): TvmTestDataCellValue {
    val truncatedCellData = slice.cell.data.drop(slice.dataPos)
    val truncatedCellRefs = slice.cell.refs.drop(slice.refPos)

    // TODO handle cell type loads
    return TvmTestDataCellValue(truncatedCellData, truncatedCellRefs)
}

inline fun <reified T> getResourcePath(path: String): Path {
    return T::class.java.getResource(path)?.path?.let { Path(it) }
        ?: error("Resource $path was not found")
}
