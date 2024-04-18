package com.explyt.ton.intellij.utils.coroutines

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Base [CoroutineScope] for all Explyt plugin tasks.
 * This [CoroutineScope] is cancelled when user closes the project or unloads Explyt plugin.
 */
val Project.explytCoroutineScope: CoroutineScope
    get() = ExplytIntellijCoroutineScopeHolder.getInstance(this).coroutineScope

/**
 * Base [CoroutineContext] for all Explyt plugin tasks.
 * This [CoroutineContext] is cancelled when user closes the project or unloads Explyt plugin.
 */
val Project.explytCoroutineContext: CoroutineContext
    get() = explytCoroutineScope.coroutineContext

@Service(Service.Level.PROJECT)
class ExplytIntellijCoroutineScopeHolder(val coroutineScope: CoroutineScope) {
    companion object {
        @JvmStatic
        fun getInstance(project: Project): ExplytIntellijCoroutineScopeHolder = project.service()
    }
}
