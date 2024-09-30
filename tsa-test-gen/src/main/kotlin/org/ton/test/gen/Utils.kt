package org.ton.test.gen

import java.math.BigInteger
import org.usvm.test.resolver.TvmTestDataCellValue
import org.usvm.test.resolver.TvmTestSliceValue

fun String.binaryToHex(): String = BigInteger(this, 2).toString(16)

fun String.binaryToSignedDecimal(): String {
    val binaryString = this

    val signBit = binaryString.first().digitToInt()
    val sign = BigInteger.valueOf(signBit.toLong()).shiftLeft(length - 1)
    val resultBigInteger = BigInteger("0" + binaryString.drop(1), 2) - sign

    return resultBigInteger.toString(10)
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
