@file:Suppress("UnstableApiUsage")

package com.explyt.ton.intellij.utils.coroutines

import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.readActionBlocking
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.ide.progress.withModalProgress
import kotlinx.coroutines.*
import kotlin.coroutines.EmptyCoroutineContext

suspend fun <T> readActionInBackground(action: () -> T): T =
    withContext(Dispatchers.IO) {
        readAction {
            coroutineContext.ensureActive()
            action()
        }
    }

suspend fun <T> withBackgroundProgressOnPooledThread(
    project: Project,
    progressBarTitle: String,
    action: suspend () -> T,
): T =
    withContext(Dispatchers.IO) {
        withBackgroundProgress(project, progressBarTitle) {
            coroutineContext.ensureActive()
            action()
        }
    }

suspend fun <T> readActionBlockingWithModalProgress(
    project: Project,
    progressBarTitle: String,
    action: () -> T,
): T =
    withContext(Dispatchers.IO) {
        withModalProgress(project, progressBarTitle) {
            readActionBlocking {
                coroutineContext.ensureActive()
                action()
            }
        }
    }

@Deprecated("Modal progress bar is shown even when `action` suspends, try using other techniques")
suspend fun <T> runBlockingSuspendableReadAction(
    project: Project,
    progressBarTitle: String,
    action: suspend () -> T,
): T = withModalProgress(project, progressBarTitle) {
    val job = coroutineContext[Job]
    readActionBlocking {
        runBlocking {
            withContext(job ?: EmptyCoroutineContext) {
                action()
            }
        }
    }
}
