package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.util.*
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.util.treeView.AbstractTreeNode
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
) : VirtualFolderNode(project, directory, directory.name, AllIcons.Nodes.Folder, settings) {

    override fun getWeight(): Int {
        return if (showOnTop) 0 else Constants.DEFAULT_WEIGHT
    }

    override fun update(presentation: PresentationData) {
        super.update(presentation)

        val icon =
            if (highlight) AllIcons.Modules.TestRoot
            else AllIcons.Modules.SourceRoot

        presentation.setIcon(
            tooltip?.let { icon.withTooltip(it) }
                ?: icon.withoutTooltip()
        )

        if (showOnlyName) {
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
