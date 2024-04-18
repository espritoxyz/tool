package com.explyt.ton.intellij

import com.explyt.ton.intellij.model.TonFileType
import com.explyt.ton.intellij.utils.coroutines.explytCoroutineScope
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TonGenerateTestAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        project.explytCoroutineScope.launch(Dispatchers.Main) {
            TonTestGenerator.getInstance(project).generateTests(file)
        }
    }

    override fun update(e: AnActionEvent) {
        val extension = e.getData(CommonDataKeys.VIRTUAL_FILE)?.extension
        e.presentation.isVisible = e.project != null
                && extension != null
                && TonFileType.fromExtension(extension) != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
