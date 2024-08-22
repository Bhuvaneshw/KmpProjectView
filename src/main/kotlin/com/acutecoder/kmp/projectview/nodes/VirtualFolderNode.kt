package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.util.ModuleType
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes
import javax.swing.Icon

open class VirtualFolderNode(
    project: Project,
    private val moduleName: String,
    private val icon: Icon,
    viewSettings: ViewSettings,
    private val moduleType: ModuleType? = null,
    private val showOnTop: Boolean = false,
) : ProjectViewNode<String>(project, moduleName, viewSettings) {

    val children = mutableListOf<AbstractTreeNode<*>>()

    override fun update(presentation: PresentationData) {
        presentation.setIcon(icon)
        presentation.addText(moduleName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        if (moduleType != null) {
            presentation.addText(" ($moduleType)", SimpleTextAttributes.GRAY_ATTRIBUTES)
            presentation.tooltip = "${moduleType.displayName} module"
        }
    }

    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> {
        return children
    }

    override fun getWeight(): Int {
        return if (showOnTop) 0 else super.getWeight()
    }

    override fun contains(file: VirtualFile): Boolean {
        children.forEach {
            if (it.mayContain(file))
                return true
        }

        return false
    }
}
