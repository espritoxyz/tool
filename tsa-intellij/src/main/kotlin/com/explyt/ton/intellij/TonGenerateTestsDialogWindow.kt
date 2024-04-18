package com.explyt.ton.intellij

import com.explyt.ton.intellij.model.TonGenerateTestModel
import com.explyt.ton.intellij.model.TonStdLibKind
import com.explyt.ton.intellij.utils.requiredStdLibs
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import java.io.File
import javax.swing.JComponent

class TonGenerateTestsDialogWindow(
    private val model: TonGenerateTestModel
) : DialogWrapper(model.project) {
    init {
        title = "Generate TON Tests"
        isResizable = false
        init()
        updateUiElements()
    }

    // initialized in createCenterPanel()
    private lateinit var fileUnderTestField: JBTextField
    private lateinit var stdLibChoosers: Map<TonStdLibKind, TextFieldWithBrowseButton>

    override fun createCenterPanel(): JComponent = panel {
        row("File under test:") {
            fileUnderTestField = textField().component.apply {
                isEnabled = false
                isEditable = false
            }
        }

        stdLibChoosers = TonStdLibKind.entries.associateWith { tonStdLibKind ->
            row("${tonStdLibKind.shortLabel}:") {}.textFieldWithBrowseButton(
                browseDialogTitle = tonStdLibKind.longLabel,
                fileChooserDescriptor = tonStdLibKind.fileChooserDescriptor,
            ) {
                model.tonStdLibs[tonStdLibKind] = File(it.path)
                updateUiElements()
                it.path
            }.component.apply {
                // only allow edits via browse button for simplicity
                isEditable = false
            }
        }
    }

    /**
     * Updates UI state to reflect current [model] state
     */
    private fun updateUiElements() {
        fileUnderTestField.text = model.fileUnderTest.presentableName
        stdLibChoosers.forEach { (tonStdLibKind, fileChooser) ->
            val text = model.tonStdLibs[tonStdLibKind]?.absolutePath
            fileChooser.text = text ?: "Select ${tonStdLibKind.shortLabel}"
            fileChooser.textField.foreground = JBColor.RED.takeIf { text == null }
        }
        isOKActionEnabled = model.fileType.requiredStdLibs.all { model.tonStdLibs[it] != null }
    }
}
