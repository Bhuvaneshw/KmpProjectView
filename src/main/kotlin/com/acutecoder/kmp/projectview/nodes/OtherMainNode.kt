package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.util.withoutTooltip
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

class OtherMainNode(
    project: Project,
    directory: PsiDirectory,
    settings: ViewSettings,
) : PsiDirectoryNode(project, directory, settings) {

    override fun update(data: PresentationData) {
        super.update(data)
        data.setIcon(AllIcons.Modules.SourceRoot.withoutTooltip())
        data.tooltip = "Source set"
    }

}
