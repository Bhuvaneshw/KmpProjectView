package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.preference.PreferenceState
import com.acutecoder.kmp.projectview.util.ModuleType
import com.acutecoder.kmp.projectview.util.moduleType
import com.acutecoder.kmp.projectview.util.withTooltip
import com.acutecoder.kmp.projectview.util.withoutTooltip
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

class OtherMainNode(
    project: Project,
    private val directory: PsiDirectory,
    settings: ViewSettings,
    private val preference: PreferenceState,
) : VirtualFolderNode(project, directory, directory.name, AllIcons.Nodes.Folder, settings) {

    override fun update(presentation: PresentationData) {
        super.update(presentation)

        val icon = AllIcons.Modules.SourceRoot
        presentation.setIcon(
            if (preference.isTooltipEnabled)
                icon.withTooltip("Source set")
            else icon.withoutTooltip()
        )

        if (preference.showModuleNameOnly) {
            presentation.clearText()
            presentation.presentableText = directory.name
        }
    }

    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> {
        val children = mutableListOf<AbstractTreeNode<*>>()

        val hasAtLeastOneKmpModule = directory.children.any {
            it.moduleType().let { type -> type == ModuleType.KMP || type == ModuleType.CMP }
        }

        listAndAddChildren(directory, project, settings, hasAtLeastOneKmpModule) {
            children.add(it)
        }

        return children
    }
}
