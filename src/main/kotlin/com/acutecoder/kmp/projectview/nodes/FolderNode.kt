package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.preference.PluginPreference
import com.acutecoder.kmp.projectview.util.*
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.ProjectViewImpl
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.ui.SimpleTextAttributes
import javax.swing.Icon

class FolderNode(
    project: Project,
    private val folder: PsiDirectory,
    viewSettings: ViewSettings,
    private val moduleName: String = folder.name,
    private val icon: Icon = AllIcons.Nodes.Folder,
    private val moduleType: ModuleType? = null,
) : ProjectViewNode<PsiDirectory>(project, folder, viewSettings) {

    private val preferences = PluginPreference.getInstance().state

    init {
        val changeListener = TreeChangeListener { event ->
            if (event.parent == folder)
                ProjectViewImpl.getInstance(project).refresh()
        }

        PsiManager.getInstance(project).addPsiTreeChangeListener(changeListener) {
            PsiManager.getInstance(project).removePsiTreeChangeListener(changeListener)
        }
    }

    override fun update(presentation: PresentationData) {
        presentation.setIcon(
            if (preferences.isTooltipEnabled)
                icon.withTooltip(if (moduleType != null) "${moduleType.displayName} module" else "Module")
            else icon.withoutTooltip()
        )

        presentation.addText(moduleName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        if (moduleType != null && preferences.showKmpModuleSideText)
            presentation.addText(" ($moduleType)", SimpleTextAttributes.GRAY_ATTRIBUTES)
    }

    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> {
        val children = mutableListOf<AbstractTreeNode<*>>()

        listAndAddChildren(folder, project, settings, false) {
            children.add(it)
        }

        return children
    }

    override fun getWeight(): Int = Constants.DEFAULT_WEIGHT

    override fun contains(file: VirtualFile): Boolean {
        return folder.virtualFile == file || folder.virtualFile.isAncestorOf(file)
    }
}
