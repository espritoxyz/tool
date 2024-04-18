package com.explyt.ton.intellij.utils

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Service(Service.Level.PROJECT)
class ExplytFileManager(private val project: Project) {

    suspend fun openFileInEditor(file: File) {
        return withContext(Dispatchers.Main) {
            VfsUtil.findFile(file.toPath(), /*refreshIfNeeded = */ true)?.let { virtualFile ->
                FileEditorManager.getInstance(project).openFile(
                    virtualFile,
                    /*focusEditor = */ true
                )
            }
        }
    }

    companion object {
        fun getInstance(project: Project) = project.service<ExplytFileManager>()
    }
}
