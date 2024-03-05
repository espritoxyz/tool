package org.usvm.machine

import org.ton.bytecode.TvmType
import org.usvm.UBv32Sort
import org.usvm.UComponents
import org.usvm.UContext

// TODO: There is no size sort in TVM because of absence of arrays, but we need to represent cell data as boolean arrays
//  with size no more than 1023

// TODO make it Bv16
typealias USizeSort = UBv32Sort

class TvmContext(components : UComponents<TvmType, USizeSort>) : UContext<USizeSort>(components) {

}
