package com.bhuvaneshw.kmp.projectview.nodes

import com.bhuvaneshw.kmp.projectview.module.GradleModuleHelper
import com.bhuvaneshw.kmp.projectview.module.listAndAddChildren
import com.bhuvaneshw.kmp.projectview.util.Config
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
            add = children::add
        )
        return children
    }

    override fun getWeight(): Int = additionalWeight

    override fun contains(file: VirtualFile): Boolean {
        return rootDirectory.virtualFile == file || rootDirectory.virtualFile.path.let { rootPath ->
            file.path.startsWith(rootPath)
        }
    }
}
