package com.explyt.ton.intellij.utils

import com.explyt.ton.intellij.model.TonFileType
import com.explyt.ton.intellij.model.TonFileType.*
import com.explyt.ton.intellij.model.TonStdLibKind

val TonFileType.requiredStdLibs: List<TonStdLibKind>
    get() = when (this) {
        FIF -> listOf(TonStdLibKind.FIFT, TonStdLibKind.FUNC)
        FC -> listOf(TonStdLibKind.FUNC)
    }
