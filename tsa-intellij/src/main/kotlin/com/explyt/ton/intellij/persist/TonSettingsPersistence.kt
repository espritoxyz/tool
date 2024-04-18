package com.explyt.ton.intellij.persist

import com.explyt.ton.intellij.model.TonStdLibKind
import com.intellij.openapi.components.*
import java.io.File

@Service(Service.Level.APP)
@State(name = "ExplytTonPersistence", storages = [Storage("explytTonPersistence.xml")])
class TonSettingsPersistence : SimplePersistentStateComponent<TonSettings>(TonSettings()) {
    var tonStdLibs: Map<TonStdLibKind, File?>
        get() = state.tonStdLibPaths.mapValues { (_, path) -> File(path).takeIf { it.exists() } }
        set(value) {
            value.forEach { (tonStdLibKind, file) ->
                if (file != null) {
                    state.tonStdLibPaths[tonStdLibKind] = file.absolutePath
                }
            }
        }

    companion object {
        fun getInstance() = service<TonSettingsPersistence>()
    }
}
