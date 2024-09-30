package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.util.withTooltip
import com.acutecoder.kmp.projectview.util.withoutTooltip
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

open class ImportantVirtualFolderNode(
    project: Project,
    protected val directory: PsiDirectory,
    settings: ViewSettings,
    private val showOnTop: Boolean = true,
    private val highlight: Boolean = false,
    private val showOnlyName: Boolean = true,
    private val tooltip: String? = directory.name,
) : PsiDirectoryNode(project, directory, settings) {

    override fun getWeight(): Int {
        return if (showOnTop) 0 else super.getWeight()
    }

    override fun update(data: PresentationData) {
        super.update(data)

        val icon =
            if (highlight) AllIcons.Modules.TestRoot
            else AllIcons.Modules.SourceRoot

        data.setIcon(
            tooltip?.let { icon.withTooltip(it) }
                ?: icon.withoutTooltip()
        )

        if (showOnlyName) {
            data.clearText()
            data.presentableText = directory.name
        }
    }
}
