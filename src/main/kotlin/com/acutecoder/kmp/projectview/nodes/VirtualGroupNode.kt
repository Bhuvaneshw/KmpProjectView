package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.util.withoutTooltip
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes
import javax.swing.Icon

class VirtualGroupNode<T : Any>(
    project: Project,
    private val folderName: String,
    element: T,
    private val icon: Icon,
    viewSettings: ViewSettings,
    private val isTooltipEnabled: Boolean,
    private val tooltip: String = folderName,
    private val weight: Int = 100,
) : ProjectViewNode<T>(project, element, viewSettings) {

    val children = mutableListOf<AbstractTreeNode<*>>()

    override fun update(presentation: PresentationData) {
        presentation.setIcon(icon.withoutTooltip())
        presentation.addText(folderName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        if (isTooltipEnabled)
            presentation.tooltip = tooltip
    }

    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> {
        return children
    }

    override fun getWeight(): Int {
        return weight
    }

    override fun contains(file: VirtualFile): Boolean {
        return false
    }
}
