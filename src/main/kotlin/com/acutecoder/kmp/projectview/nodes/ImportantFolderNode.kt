package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.module.hasAtLeastOneKmpOrCmpModule
import com.acutecoder.kmp.projectview.module.moduleType
import com.acutecoder.kmp.projectview.preference.PluginPreference
import com.acutecoder.kmp.projectview.util.*
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
) : ProjectViewNode<PsiDirectory>(config.project, folder, config.viewSettings) {

    private val preferences = PluginPreference.getInstance().state

//    init {
//        val changeListener = TreeChangeListener { event ->
//            event.parent.let { parent ->
//                if ((parent is PsiFileSystemItem) && folder.virtualFile.isAncestorOf(parent.virtualFile))
//                    ProjectViewImpl.getInstance(config.project)?.refresh()
//            }
//        }
//
//        PsiManager.getInstance(config.project).addPsiTreeChangeListener(changeListener) {
//            PsiManager.getInstance(config.project).removePsiTreeChangeListener(changeListener)
//        }
//    }

    override fun update(presentation: PresentationData) {
        val moduleName = folder.name
        val icon = defaultIcon ?: if (moduleName.equals("kotlin", true)) AllIcons.Modules.SourceRoot
        else if (moduleName.contains("resource", true) || moduleName.equals("res", true)) AllIcons.Modules.ResourcesRoot
        else AllIcons.Nodes.Folder

        presentation.setIcon(
            if (defaultIcon != null && preferences.isTooltipEnabled)
                icon.withTooltip("$moduleName source set")
            else icon.withoutTooltip()
        )

        presentation.addText(moduleName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }

    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> {
        val children = mutableListOf<AbstractTreeNode<*>>()

        if (folder.moduleType().isKmpOrCmp())
            listAndAddChildrenAsModule(
                config = config,
                baseDirectory = folder,
                add = children::add
            )
        else listAndAddChildren(
            config = config,
            baseDirectory = folder,
            groupGradleAndOtherFiles = folder.hasAtLeastOneKmpOrCmpModule(),
            add = children::add
        )

        return children
    }

    override fun getWeight(): Int = if (showOnTop) 0 else Constants.DEFAULT_WEIGHT - 1

    override fun contains(file: VirtualFile): Boolean {
        return folder.virtualFile == file || folder.virtualFile.isAncestorOf(file)
    }
}
