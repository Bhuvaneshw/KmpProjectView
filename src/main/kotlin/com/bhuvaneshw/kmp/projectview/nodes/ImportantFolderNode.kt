package com.bhuvaneshw.kmp.projectview.nodes

import com.bhuvaneshw.kmp.preference.PluginPreference
import com.bhuvaneshw.kmp.projectview.module.listAndAddChildren
import com.bhuvaneshw.kmp.projectview.module.listAndAddChildrenAsModule
import com.bhuvaneshw.kmp.projectview.module.moduleType
import com.bhuvaneshw.kmp.projectview.util.Config
import com.bhuvaneshw.kmp.projectview.util.Constants
import com.bhuvaneshw.kmp.projectview.util.isAncestorOf
import com.bhuvaneshw.kmp.projectview.util.withTooltip
import com.bhuvaneshw.kmp.projectview.util.withoutTooltip
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.ui.SimpleTextAttributes
import javax.swing.Icon

class ImportantFolderNode(
    private val config: Config,
    private val folder: PsiDirectory,
    private val showOnTop: Boolean,
    private val defaultIcon: Icon? = null,
    private val additionalWeight: Int = 0,
    private val isLabelEnabled: Boolean = true
) : ProjectViewNode<PsiDirectory>(config.project, folder, config.viewSettings) {

    private val preferences = PluginPreference.getInstance().state
    private val moduleType by lazy { folder.moduleType() }

    override fun update(presentation: PresentationData) {
        val moduleName = folder.name
        val icon = defaultIcon ?: if (moduleName.equals(Constants.Folder.KOTLIN, true)) AllIcons.Modules.SourceRoot
        else if (moduleName.contains(Constants.Folder.RESOURCE, true) || moduleName.equals(Constants.Folder.RES, true))
            AllIcons.Modules.ResourcesRoot
        else AllIcons.Nodes.Folder

        presentation.setIcon(
            if (defaultIcon != null && preferences.isTooltipEnabled)
                icon.withTooltip("$moduleName source set")
            else icon.withoutTooltip()
        )

        presentation.addText(moduleName, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
    }

    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> {
        val children = mutableListOf<AbstractTreeNode<*>>()

        if (moduleType.isGradleModule())
            listAndAddChildrenAsModule(
                config = config,
                baseDirectory = folder,
                add = children::add,
                weightOffset = additionalWeight,
                isLabelEnabled = isLabelEnabled
            )
        else listAndAddChildren(
            config = config,
            baseDirectory = folder,
            add = children::add,
            weightOffset = additionalWeight,
            isLabelEnabled = isLabelEnabled
        )

        return children
    }

    override fun getWeight(): Int {
        var weight = if (showOnTop) 0 else Constants.Weight.DEFAULT_WEIGHT - 1
        if (folder.name.equals(Constants.Folder.KOTLIN, ignoreCase = true)) {
            weight -= 1
        }
        return weight + additionalWeight
    }

    override fun contains(file: VirtualFile): Boolean {
        return folder.virtualFile == file || folder.virtualFile.isAncestorOf(file)
    }
}
