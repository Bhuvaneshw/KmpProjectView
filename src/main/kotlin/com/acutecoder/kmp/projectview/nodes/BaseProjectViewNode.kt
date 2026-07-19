package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.preference.PluginPreference
import com.acutecoder.kmp.projectview.module.hasAtLeastOneKmpOrCmpModule
import com.acutecoder.kmp.projectview.module.listAndAddChildren
import com.acutecoder.kmp.projectview.util.Config
import com.intellij.ide.projectView.impl.nodes.ProjectViewProjectNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import org.jetbrains.plugins.gradle.settings.GradleSettings
import java.util.Collections

class BaseProjectViewNode(private val config: Config) :
    ProjectViewProjectNode(config.project, config.viewSettings) {

    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> {
        val project = project
        if (project == null || project.isDisposed || project.isDefault)
            return Collections.emptyList()

        val preferences = PluginPreference.getInstance().state
        val separateBuilds = preferences.separateNodeForSubstitutedProject

        val children = mutableListOf<AbstractTreeNode<*>>()
        val psiManager = PsiManager.getInstance(project)
        val fileSystem = LocalFileSystem.getInstance()
        val visitedPaths = mutableSetOf<String>()

        val projectPaths = mutableSetOf<String>()

        GradleSettings.getInstance(project).linkedProjectsSettings.forEach {
            projectPaths.add(it.externalProjectPath)
            it.compositeBuild?.compositeParticipants?.forEach { participant ->
                projectPaths.add(participant.rootPath)
            }
        }

        ModuleManager.getInstance(project).modules.forEach { module ->
            ExternalSystemApiUtil.getExternalRootProjectPath(module)?.let { projectPaths.add(it) }
        }

        val basePath = project.basePath
        if (projectPaths.isEmpty() && basePath != null) {
            projectPaths.add(basePath)
        }

        val isComposite = projectPaths.size > 1

        // Sort paths to process the main project first
        val sortedPaths = projectPaths.sortedByDescending {
            basePath != null && FileUtil.pathsEqual(it, basePath)
        }

        for (path in sortedPaths) {
            val baseDir = fileSystem.findFileByPath(path) ?: continue
            if (!visitedPaths.add(baseDir.path)) continue

            val baseDirPsi = psiManager.findDirectory(baseDir) ?: continue
            val isMainProject = basePath != null && FileUtil.pathsEqual(path, basePath)
            val weightOffset = if (isMainProject) 0 else 20

            if (isComposite && separateBuilds) {
                children.add(BuildRootNode(config, baseDirPsi, weightOffset))
            } else {
                listAndAddChildren(
                    config = config,
                    baseDirectory = baseDirPsi,
                    groupGradleAndOtherFiles = baseDirPsi.hasAtLeastOneKmpOrCmpModule(),
                    add = children::add,
                    weightOffset = weightOffset,
                    isLabelEnabled = isComposite
                )
            }
        }

        return children
    }
}
