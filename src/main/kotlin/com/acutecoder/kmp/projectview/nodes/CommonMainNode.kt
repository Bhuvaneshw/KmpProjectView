package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.preference.PreferenceState
import com.intellij.ide.projectView.ViewSettings
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

class CommonMainNode(
    project: Project,
    directory: PsiDirectory,
    settings: ViewSettings,
    preference: PreferenceState,
) : ImportantVirtualFolderNode(
    project = project,
    directory = directory,
    settings = settings,
    showOnTop = preference.showCommonMainOnTop,
    highlight = preference.differentiateCommonMain,
    showOnlyName = preference.showModuleNameOnly,
    tooltip = "Common Main source set",
)
