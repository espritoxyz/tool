package com.explyt.ton.intellij.model

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory

enum class TonStdLibKind(
    val shortLabel: String,
    val longLabel: String,
    val fileChooserDescriptor: FileChooserDescriptor,
) {
    FIFT(
        shortLabel = "fiftstdlib",
        longLabel = "Fift StdLib (fiftstdlib)",
        fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
    ),
    FUNC(
        shortLabel = "stdlib.fc",
        longLabel = "Func StdLib (stdlib.fc)",
        fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
    )
}
