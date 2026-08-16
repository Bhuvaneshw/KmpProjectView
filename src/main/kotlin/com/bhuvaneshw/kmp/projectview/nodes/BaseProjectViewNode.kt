package com.bhuvaneshw.kmp.projectview.nodes

import com.bhuvaneshw.kmp.preference.PluginPreference
import com.bhuvaneshw.kmp.projectview.module.GradleModuleHelper
import com.bhuvaneshw.kmp.projectview.module.listAndAddChildren
import com.bhuvaneshw.kmp.projectview.util.Config
import com.bhuvaneshw.kmp.projectview.util.isGradleFile
import com.intellij.ide.projectView.impl.nodes.ProjectViewProjectNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
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
                    add = children::add,
                    weightOffset = weightOffset,
                    isLabelEnabled = isComposite
                )
            }
        }

        if (preferences.globalGradleFiles) {
            val globalGradleFiles = collectGlobalGradleFiles(config)
            if (globalGradleFiles.isNotEmpty()) {
                val globalGradleNode = GlobalGradleGroupNode(config)
                globalGradleNode.children.addAll(globalGradleFiles)
                children.add(0, globalGradleNode)
            }
        }

        return children
    }

    private fun collectGlobalGradleFiles(
        config: Config
    ): List<AbstractTreeNode<*>> {
        val project = config.project
        val gradleFiles = mutableListOf<AbstractTreeNode<*>>()
        val psiManager = PsiManager.getInstance(project)
        val fileSystem = LocalFileSystem.getInstance()
        val visitedFiles = mutableSetOf<String>()

        val linkedProjects = GradleSettings.getInstance(project).linkedProjectsSettings
        val rootPaths = linkedProjects.map { it.externalProjectPath }.toSet()
        val compositePaths = linkedProjects.flatMap {
            it.compositeBuild?.compositeParticipants?.map { p -> p.rootPath } ?: emptyList()
        }.toSet()

        fun addFile(file: PsiFile, hint: String) {
            if (!visitedFiles.add(file.virtualFile.path)) return
            gradleFiles.add(HintedPsiFileNode(config, file, " ($hint)"))
        }

        val allProjects = GradleModuleHelper.getAllGradleProjects(project)

        allProjects.forEach { gradleProject ->
            val path = gradleProject.projectDir.path
            val dir = fileSystem.findFileByPath(path) ?: return@forEach
            val dirPsi = psiManager.findDirectory(dir) ?: return@forEach

            val isRoot = rootPaths.any { FileUtil.pathsEqual(it, path) }
            val isComposite = compositePaths.any { FileUtil.pathsEqual(it, path) }

            val buildLabel = when {
                isComposite -> "Included build: ${gradleProject.name}"
                isRoot -> "Project: ${gradleProject.name}"
                else -> "Module ${gradleProject.id}"
            }

            dirPsi.children.forEach { child ->
                when (child) {
                    is PsiFile -> {
                        val name = child.name.lowercase()
                        when {
                            name.startsWith("build.gradle") -> addFile(child, buildLabel)
                            name.startsWith("settings.gradle") -> addFile(child, "Project Settings")
                            name == "gradle.properties" -> addFile(child, "Project Properties")
                            name == "local.properties" -> addFile(child, "SDK Location")
                            name == "gradle-wrapper.properties" -> addFile(child, "Gradle Version")
                            name.endsWith(".versions.toml") -> addFile(
                                child,
                                "Version Catalog \"${
                                    child.name.removeSuffix(".versions.toml").removeSuffix(".toml")
                                }\""
                            )

                            name.endsWith(".pro") -> addFile(
                                child,
                                "ProGuard Rules for \"${gradleProject.id}\""
                            )

                            child.isGradleFile() -> addFile(child, buildLabel)
                        }
                    }

                    is PsiDirectory if child.name == "gradle" -> {
                        child.findSubdirectory("wrapper")?.children?.filterIsInstance<PsiFile>()
                            ?.forEach { file ->
                                if (file.name == "gradle-wrapper.properties") addFile(
                                    file,
                                    "Gradle Version"
                                )
                                else if (file.isGradleFile()) addFile(file, "Gradle Wrapper")
                            }

                        child.children.filterIsInstance<PsiFile>().forEach { file ->
                            val name = file.name.lowercase()
                            if (name.endsWith(".versions.toml")) addFile(
                                file,
                                "Version Catalog \"${
                                    file.name.removeSuffix(".versions.toml").removeSuffix(".toml")
                                }\""
                            )
                            else if (file.isGradleFile()) addFile(file, "Gradle Script")
                        }
                    }

                    is PsiDirectory if child.name == "src" -> {
                        child.children.filterIsInstance<PsiFile>().filter { it.isGradleFile() }
                            .forEach { file ->
                                addFile(file, "$buildLabel (src)")
                            }
                    }
                }
            }
        }

        return gradleFiles
    }
}
