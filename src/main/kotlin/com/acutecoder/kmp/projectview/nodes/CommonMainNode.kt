package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.preference.PreferenceState
import com.acutecoder.kmp.projectview.util.withoutTooltip
import com.acutecoder.kmp.projectview.util.withTooltip
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

class CommonMainNode(
    project: Project,
    private val directory: PsiDirectory,
    settings: ViewSettings,
    private val preference: PreferenceState,
) : PsiDirectoryNode(project, directory, settings) {

    override fun getWeight(): Int {
        return if (preference.showCommonMainOnTop) 0 else super.getWeight()
    }

    override fun update(data: PresentationData) {
        super.update(data)

        val icon =
            if (preference.differentiateCommonMain) AllIcons.Modules.TestRoot
            else AllIcons.Modules.SourceRoot

        data.setIcon(
            if (preference.isTooltipEnabled)
                icon.withTooltip("Common Main source set")
            else icon.withoutTooltip()
        )

        if (preference.showModuleNameOnly) {
            data.clearText()
            data.presentableText = directory.name
        }
    }
}
