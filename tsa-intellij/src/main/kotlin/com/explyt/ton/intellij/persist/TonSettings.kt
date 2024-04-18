package com.explyt.ton.intellij.persist

import com.explyt.ton.intellij.model.TonStdLibKind
import com.intellij.openapi.components.BaseState

class TonSettings : BaseState() {
    var tonStdLibPaths by map<TonStdLibKind, String>()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as TonSettings

        return tonStdLibPaths == other.tonStdLibPaths
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + tonStdLibPaths.hashCode()
        return result
    }
}
