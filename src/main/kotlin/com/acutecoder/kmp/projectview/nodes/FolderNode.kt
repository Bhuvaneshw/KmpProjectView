package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.helper.executor.RegenerateResClassExecutor
import com.acutecoder.kmp.preference.PluginPreference
import com.acutecoder.kmp.projectview.module.GradleModuleHelper
import com.acutecoder.kmp.projectview.module.ModuleType
import com.acutecoder.kmp.projectview.module.hasAtLeastOneKmpOrCmpModule
import com.acutecoder.kmp.projectview.module.listAndAddChildren
import com.acutecoder.kmp.projectview.module.listAndAddChildrenAsModule
import com.acutecoder.kmp.projectview.module.moduleType
import com.acutecoder.kmp.projectview.util.Config
import com.acutecoder.kmp.projectview.util.Constants
import com.acutecoder.kmp.projectview.util.OnFileChangeListener
import com.acutecoder.kmp.projectview.util.findSrcDirectory
import com.acutecoder.kmp.projectview.util.isAncestorOf
import com.acutecoder.kmp.projectview.util.withTooltip
import com.acutecoder.kmp.projectview.util.withoutTooltip
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.ui.SimpleTextAttributes

@Suppress("FunctionName")
fun FolderNode(
    config: Config,
    folder: PsiDirectory,
    additionalWeight: Int = 0,
    isLabelEnabled: Boolean = true
) =
    if (folder.isDirectory && folder.name.lowercase() == "kotlin") PsiDirectoryNode(
        config.project,
        folder,
        config.viewSettings
    )
    else CustomFolderNode(config, folder, additionalWeight, isLabelEnabled)

private class CustomFolderNode(
    private val config: Config,
    private val folder: PsiDirectory,
    private val additionalWeight: Int = 0,
    private val isLabelEnabled: Boolean = true
) : ProjectViewNode<PsiDirectory>(
    config.project,
    folder.moduleType()
        .let { if (it != ModuleType.Unknown) folder.findSrcDirectory() ?: folder else folder },
    config.viewSettings
) {

    private val preferences = PluginPreference.getInstance().state
    private val moduleType by lazy { folder.moduleType() }

    init {
        if (
            preferences.run { regenerateResClassFeatureEnabled && autoRegenerateResClassFeatureEnabled }
            && folder.name.contains("resource", true)
        ) {
            PsiManager.getInstance(config.project)
                .addPsiTreeChangeListener(OnFileChangeListener(folder) {
                    RegenerateResClassExecutor.executeSafe(config.project)
                }) {}
        }
    }

    override fun update(presentation: PresentationData) {
        val isModule = moduleType != ModuleType.Unknown
        val module = if (isModule) ModuleUtilCore.findModuleForPsiElement(folder) else null
        val moduleName =
            if (isModule && module != null) GradleModuleHelper.getModuleName(module) else folder.name

        val icon = if (isModule) AllIcons.Nodes.Module
        else if (moduleName.equals("kotlin", true)) AllIcons.Modules.SourceRoot
        else if (moduleName.contains("resource", true) || moduleName.equals(
                "res",
                true
            )
        ) AllIcons.Modules.ResourcesRoot
        else AllIcons.Nodes.Folder

        presentation.setIcon(
            if (isModule && preferences.isTooltipEnabled)
                icon.withTooltip("${moduleType.displayName} module")
            else icon.withoutTooltip()
        )

        val attributes = if (isModule)
            SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES
        else SimpleTextAttributes.REGULAR_ATTRIBUTES
        presentation.addText(moduleName, attributes)

        if (isModule && preferences.showKmpModuleSideText)
            presentation.addText(" ($moduleType)", SimpleTextAttributes.GRAY_ATTRIBUTES)

        if (isModule && isLabelEnabled && !preferences.separateNodeForSubstitutedProject) {
            val rootPath =
                GradleModuleHelper.getBuildRootPath(config.project, folder.virtualFile.path)
            if (rootPath != null) {
                val projectName = GradleModuleHelper.getProjectName(config.project, rootPath)
                presentation.addText(" [$projectName]", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES)
            }
        }
    }

    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> {
        val children = mutableListOf<AbstractTreeNode<*>>()

        if (moduleType != ModuleType.Unknown)
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
            groupGradleAndOtherFiles = folder.hasAtLeastOneKmpOrCmpModule(),
            add = children::add,
            weightOffset = additionalWeight,
            isLabelEnabled = isLabelEnabled
        )

        return children
    }

    override fun getWeight(): Int = Constants.DEFAULT_WEIGHT + additionalWeight

    override fun contains(file: VirtualFile): Boolean {
        return folder.virtualFile == file || folder.virtualFile.isAncestorOf(file)
    }
}
