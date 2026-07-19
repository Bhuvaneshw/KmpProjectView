package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.module.GradleModuleHelper
import com.acutecoder.kmp.projectview.module.hasAtLeastOneKmpOrCmpModule
import com.acutecoder.kmp.projectview.module.listAndAddChildren
import com.acutecoder.kmp.projectview.util.Config
import com.acutecoder.kmp.projectview.util.Constants
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.ui.SimpleTextAttributes

class BuildRootNode(
    private val config: Config,
    private val rootDirectory: PsiDirectory,
    private val additionalWeight: Int = 0,
) : ProjectViewNode<PsiDirectory>(config.project, rootDirectory, config.viewSettings) {

    private val projectName =
        GradleModuleHelper.getProjectName(config.project, rootDirectory.virtualFile.path)

    override fun update(presentation: PresentationData) {
        presentation.setIcon(AllIcons.Nodes.ModuleGroup)
        presentation.addText(projectName, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
    }

    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> {
        val children = mutableListOf<AbstractTreeNode<*>>()
        listAndAddChildren(
            config = config,
            baseDirectory = rootDirectory,
            groupGradleAndOtherFiles = rootDirectory.hasAtLeastOneKmpOrCmpModule(),
            add = children::add
        )
        return children
    }

    override fun getWeight(): Int = Constants.DEFAULT_WEIGHT - 10 + additionalWeight

    override fun contains(file: VirtualFile): Boolean {
        return rootDirectory.virtualFile == file || rootDirectory.virtualFile.path.let { rootPath ->
            file.path.startsWith(rootPath)
        }
    }
}
