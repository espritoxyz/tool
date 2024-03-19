package org.usvm.machine.state

import org.ton.bytecode.TvmCellValue
import org.ton.bytecode.TvmContinuationValue
import org.ton.bytecode.TvmTupleValue
import org.ton.bytecode.TvmValue

interface TvmRegister<Value : TvmValue> {
    var value: Value
}

// TODO registers should contain symbolic values, not concrete?
data class C0Register(override var value: TvmContinuationValue) : TvmRegister<TvmContinuationValue>
data class C1Register(override var value: TvmContinuationValue) : TvmRegister<TvmContinuationValue>
data class C2Register(override var value: TvmContinuationValue) : TvmRegister<TvmContinuationValue>
data class C3Register(override var value: TvmContinuationValue) : TvmRegister<TvmContinuationValue>
data class C4Register(override var value: TvmCellValue) : TvmRegister<TvmCellValue>
data class C5Register(override var value: TvmCellValue) : TvmRegister<TvmCellValue>
data class C7Register(override var value: TvmTupleValue = TvmTupleValue.empty()) : TvmRegister<TvmTupleValue>

// TODO make them not-null?
data class TvmRegisters(
    var c0: C0Register? = null,
    var c1: C1Register? = null,
    var c2: C2Register? = null,
    var c3: C3Register? = null,
    var c4: C4Register? = null,
    var c5: C5Register? = null,
    var c7: C7Register? = null
)
