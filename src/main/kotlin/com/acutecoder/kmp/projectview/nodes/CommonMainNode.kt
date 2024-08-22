package com.acutecoder.kmp.projectview.nodes

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

class CommonMainNode(
    project: Project,
    directory: PsiDirectory,
    settings: ViewSettings,
) : PsiDirectoryNode(project, directory, settings) {

    override fun getWeight(): Int {
        return 0
    }

    override fun update(data: PresentationData) {
        super.update(data)
        data.setIcon(AllIcons.Modules.TestRoot)
        data.tooltip = "Common Main source set"
    }
}
