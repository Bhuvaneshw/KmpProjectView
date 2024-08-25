package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.preference.PluginPreference
import com.acutecoder.kmp.projectview.util.ModuleType
import com.acutecoder.kmp.projectview.util.isAncestorOf
import com.acutecoder.kmp.projectview.util.withoutTooltip
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.ProjectViewImpl
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.PsiTreeChangeListener
import com.intellij.ui.SimpleTextAttributes
import javax.swing.Icon

class VirtualFolderNode(
    project: Project,
    var folder: PsiDirectory,
    private val moduleName: String,
    private val icon: Icon,
    viewSettings: ViewSettings,
    private val moduleType: ModuleType? = null,
    private val showOnTop: Boolean = false,
) : ProjectViewNode<PsiDirectory>(project, folder, viewSettings) {

    val children = mutableListOf<AbstractTreeNode<*>>()
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
        presentation.setIcon(icon.withoutTooltip())
        presentation.addText(moduleName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        if (moduleType != null) {
            if (preferences.showKmpModuleSideText)
                presentation.addText(" ($moduleType)", SimpleTextAttributes.GRAY_ATTRIBUTES)
            if (preferences.isTooltipEnabled)
                presentation.tooltip = "${moduleType.displayName} module"
        } else if (preferences.isTooltipEnabled)
            presentation.tooltip = "Module"
    }

    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> {
        return children
    }

    override fun getWeight(): Int {
        return if (showOnTop) 0 else super.getWeight()
    }

    override fun contains(file: VirtualFile): Boolean {
        return folder.virtualFile == file || folder.virtualFile.isAncestorOf(file)
    }
}

class TreeChangeListener(private val handlePsiChange: (PsiTreeChangeEvent) -> Unit) : PsiTreeChangeListener {
    override fun beforeChildAddition(event: PsiTreeChangeEvent) {}

    override fun beforeChildRemoval(event: PsiTreeChangeEvent) {}

    override fun beforeChildReplacement(event: PsiTreeChangeEvent) {}

    override fun beforeChildMovement(event: PsiTreeChangeEvent) {}

    override fun beforeChildrenChange(event: PsiTreeChangeEvent) {}

    override fun beforePropertyChange(event: PsiTreeChangeEvent) {}

    override fun childAdded(event: PsiTreeChangeEvent) {
        handlePsiChange(event)
    }

    override fun childRemoved(event: PsiTreeChangeEvent) {
        handlePsiChange(event)
    }

    override fun childReplaced(event: PsiTreeChangeEvent) {}

    override fun childrenChanged(event: PsiTreeChangeEvent) {
        handlePsiChange(event)
    }

    override fun childMoved(event: PsiTreeChangeEvent) {
        handlePsiChange(event)
    }

    override fun propertyChanged(event: PsiTreeChangeEvent) {}
}