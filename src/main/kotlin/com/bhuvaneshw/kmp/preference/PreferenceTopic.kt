package com.bhuvaneshw.kmp.preference

import com.intellij.util.messages.Topic

@Topic.AppLevel
val KMP_PREFERENCE_CHANGE = Topic(PreferenceChangeListener::class.java)

interface PreferenceChangeListener {
    fun onPreferenceChange()
}
