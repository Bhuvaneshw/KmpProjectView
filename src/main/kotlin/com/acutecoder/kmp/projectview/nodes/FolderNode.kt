package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.helper.executor.RegenerateResClassExecutor
import com.acutecoder.kmp.preference.PluginPreference
import com.acutecoder.kmp.projectview.module.hasAtLeastOneKmpOrCmpModule
import com.acutecoder.kmp.projectview.module.listAndAddChildren
import com.acutecoder.kmp.projectview.module.listAndAddChildrenAsModule
import com.acutecoder.kmp.projectview.module.moduleType
import com.acutecoder.kmp.projectview.util.*
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.ui.SimpleTextAttributes

@Suppress("FunctionName")
fun FolderNode(config: Config, folder: PsiDirectory) =
    if (folder.isDirectory && folder.name.lowercase() == "kotlin") PsiDirectoryNode(
        config.project,
        folder,
        config.viewSettings
    )
    else CustomFolderNode(config, folder)

private class CustomFolderNode(
    private val config: Config,
    private val folder: PsiDirectory,
) : ProjectViewNode<PsiDirectory>(
    config.project,
    folder.moduleType().let { if (it.isKmpOrCmp()) folder.findSrcDirectory() ?: folder else folder },
    config.viewSettings
) {

    private val preferences = PluginPreference.getInstance().state

    init {
        val state = PluginPreference.getInstance().state
        if (
            state.run { regenerateResClassFeatureEnabled && autoRegenerateResClassFeatureEnabled }
            && folder.name.contains("resource", true)
        ) {
            PsiManager.getInstance(config.project).addPsiTreeChangeListener(OnFileChangeListener(folder) {
                RegenerateResClassExecutor.executeSafe(config.project)
            }) {}
        }
    }

    override fun update(presentation: PresentationData) {
        val moduleName = folder.name
        val moduleType = folder.moduleType()
        val icon = if (moduleType.isKmpOrCmp()) AllIcons.Nodes.Module
        else if (moduleName.equals("kotlin", true)) AllIcons.Modules.SourceRoot
        else if (moduleName.contains("resource", true) || moduleName.equals("res", true)) AllIcons.Modules.ResourcesRoot
        else AllIcons.Nodes.Folder

        presentation.setIcon(
            if (moduleType.isKmpOrCmp() && preferences.isTooltipEnabled)
                icon.withTooltip("${moduleType.displayName} module")
            else icon.withoutTooltip()
        )

        presentation.addText(moduleName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        if (moduleType.isKmpOrCmp() && preferences.showKmpModuleSideText)
            presentation.addText(" ($moduleType)", SimpleTextAttributes.GRAY_ATTRIBUTES)
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

    override fun getWeight(): Int = Constants.DEFAULT_WEIGHT

    override fun contains(file: VirtualFile): Boolean {
        return folder.virtualFile == file || folder.virtualFile.isAncestorOf(file)
    }
}
