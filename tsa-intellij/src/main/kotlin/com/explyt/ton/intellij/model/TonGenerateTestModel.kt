package com.explyt.ton.intellij.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

data class TonGenerateTestModel(
    val project: Project,
    val fileUnderTest: VirtualFile,
    val fileType: TonFileType,
    val tonStdLibs: MutableMap<TonStdLibKind, File?> = TonStdLibKind.entries
        .associateWith { null }
        .toMutableMap()
) {
    var fiftStdLib: File?
        get() = tonStdLibs[TonStdLibKind.FIFT]
        set(value) {
            tonStdLibs[TonStdLibKind.FIFT] = value
        }

    var funcStdLib: File?
        get() = tonStdLibs[TonStdLibKind.FUNC]
        set(value) {
            tonStdLibs[TonStdLibKind.FUNC] = value
        }
}
