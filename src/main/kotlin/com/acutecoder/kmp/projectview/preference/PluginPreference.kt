package com.acutecoder.kmp.projectview.preference

import com.intellij.openapi.components.*

@Service
@State(name = "PluginPreferences", storages = [Storage("KmpProjectViewPluginPreferences.xml")])
class PluginPreference : PersistentStateComponent<PreferenceState> {

    private var preferenceState = PreferenceState()

    override fun getState(): PreferenceState {
        return preferenceState
    }

    override fun loadState(state: PreferenceState) {
        preferenceState = state
    }

    companion object {
        fun getInstance(): PluginPreference {
            return service()
        }
    }
}
