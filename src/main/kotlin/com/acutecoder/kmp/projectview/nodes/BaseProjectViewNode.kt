package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.module.hasAtLeastOneKmpOrCmpModule
import com.acutecoder.kmp.projectview.module.listAndAddChildren
import com.acutecoder.kmp.projectview.util.Config
import com.intellij.ide.projectView.impl.nodes.ProjectViewProjectNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import java.util.*

class BaseProjectViewNode(private val config: Config) : ProjectViewProjectNode(config.project, config.viewSettings) {

    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> {
        if (project == null || project.isDisposed || project.isDefault)
            return Collections.emptyList()

        val children = mutableListOf<AbstractTreeNode<*>>()

        val baseDir = LocalFileSystem
            .getInstance()
            .findFileByPath(project.basePath ?: return children)
            ?: return children
        val psiManager = PsiManager.getInstance(project)
        val baseDirPsi = psiManager.findDirectory(baseDir)

        baseDirPsi?.let { baseDirectory ->
            listAndAddChildren(
                config = config,
                baseDirectory = baseDirectory,
                groupGradleAndOtherFiles = baseDirectory.hasAtLeastOneKmpOrCmpModule(),
                add = children::add
            )
        }

        return children
    }
}

