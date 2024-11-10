package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.module.hasAtLeastOneKmpOrCmpModule
import com.acutecoder.kmp.projectview.util.Config
import com.acutecoder.kmp.projectview.util.canBeSkipped
import com.acutecoder.kmp.projectview.util.isGradleFile
import com.intellij.ide.projectView.impl.nodes.ProjectViewProjectNode
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
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

fun listAndAddChildren(
    config: Config,
    baseDirectory: PsiDirectory,
    groupGradleAndOtherFiles: Boolean,
    add: (AbstractTreeNode<*>) -> Unit,
) {
    if (groupGradleAndOtherFiles && config.preference().splitGradleAndOther < 2) {
        val otherFiles = OtherGroupNode(config, baseDirectory)
        val gradleFiles = GradleGroupNode(config)

        for (child in baseDirectory.children) {
            if (child is PsiDirectory) {
                if (child.name == "gradle") {
                    for (file in child.children) {
                        if (file is PsiFile)
                            gradleFiles.children.add(PsiFileNode(config.project, file, config.viewSettings))
                    }
                } else if (child.name == "kotlin-js-store") {
                    for (file in child.children) {
                        if (file is PsiFile)
                            otherFiles.children.add(PsiFileNode(config.project, file, config.viewSettings))
                    }
                } else if (!child.canBeSkipped(config))
                    add(FolderNode(config, child))
            } else if (child is PsiFile && !child.canBeSkipped(config)) {
                if (child.canBeSkipped(config)) continue

                if (child.isGradleFile()) {
                    gradleFiles.children.add(PsiFileNode(config.project, child, config.viewSettings))
                } else {
                    otherFiles.children.add(PsiFileNode(config.project, child, config.viewSettings))
                }
            }
        }

        if (gradleFiles.children.isNotEmpty())
            add(gradleFiles)
        if (otherFiles.children.isNotEmpty())
            add(otherFiles)
    } else {
        for (child in baseDirectory.children) {
            if (child is PsiDirectory) {
                if (child.canBeSkipped(config)) continue

                add(FolderNode(config, child))
            } else if (child is PsiFile && !child.canBeSkipped(config)) {
                if (child.canBeSkipped(config)) continue

                add(PsiFileNode(config.project, child, config.viewSettings))
            }
        }
    }
}
