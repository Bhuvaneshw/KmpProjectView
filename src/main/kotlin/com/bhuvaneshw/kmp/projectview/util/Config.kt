package com.bhuvaneshw.kmp.projectview.util

import com.bhuvaneshw.kmp.preference.PreferenceState
import com.intellij.ide.projectView.ViewSettings
import com.intellij.openapi.project.Project

data class Config(
    val project: Project,
    val viewSettings: ViewSettings,
    val preference: () -> PreferenceState,
)
