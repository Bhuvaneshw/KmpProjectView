package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.preference.PreferenceState
import com.acutecoder.kmp.projectview.util.withoutTooltip
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.ui.SimpleTextAttributes

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

        data.setIcon(
            (if (preference.differentiateCommonMain) AllIcons.Modules.TestRoot
            else AllIcons.Modules.SourceRoot).withoutTooltip()
        )

        if (preference.isTooltipEnabled)
            data.tooltip = "Common Main source set"
        if (preference.showModuleNameOnly)
            data.addText(directory.virtualFile.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }
}
