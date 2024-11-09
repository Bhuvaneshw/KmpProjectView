package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.preference.PluginPreference
import com.acutecoder.kmp.projectview.preference.PreferenceState
import com.acutecoder.kmp.projectview.util.*
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.ProjectViewProjectNode
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import java.util.*

class KmpProjectViewNode(
    project: Project,
    viewSettings: ViewSettings,
) : ProjectViewProjectNode(project, viewSettings) {

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
            listAndAddChildren(baseDirectory, project, settings, true) {
                children.add(it)
            }
        }

        return children
    }
}

fun listAndAddChildren(
    baseDirectory: PsiDirectory,
    project: Project,
    settings: ViewSettings,
    splitFiles: Boolean,
    add: (AbstractTreeNode<*>) -> Unit,
) {
    val preference = PluginPreference.getInstance().state
    val gradleFiles = GradleGroupNode(project, settings, preference.isTooltipEnabled)
    val otherFiles = OtherGroupNode(project, settings, baseDirectory, preference.isTooltipEnabled)

    for (child in baseDirectory.children) {
        if (child is PsiDirectory && !child.canBeSkipped()) {
            findModuleTypeAndAdd(child, project, settings, gradleFiles, otherFiles, add)
        } else if (child is PsiFile) {
            if (splitFiles) {
                if (child.isGradleFile())
                    gradleFiles.children.add(PsiFileNode(project, child, settings))
                else otherFiles.children.add(PsiFileNode(project, child, settings))
            } else add(PsiFileNode(project, child, settings))
        }
    }

    if (splitFiles) {
        add(gradleFiles)
        add(otherFiles)
    }
}

private fun findModuleTypeAndAdd(
    directory: PsiDirectory,
    project: Project,
    settings: ViewSettings,
    gradleFiles: VirtualGroupNode<String>,
    otherFiles: VirtualGroupNode<PsiDirectory>,
    add: (AbstractTreeNode<*>) -> Unit,
) {
    val moduleType = directory.moduleType()

    if (moduleType == ModuleType.KMP || moduleType == ModuleType.CMP) {
        val moduleNode =
            VirtualFolderNode(
                project = project,
                folder = directory.findSrcDirectory() ?: directory,
                moduleName = directory.name,
                icon = AllIcons.Nodes.Module,
                viewSettings = settings,
                moduleType = moduleType,
                showOnTop = true,
            )

        for (subModuleFile in directory.children) {
            appendKmpModule(project, settings, subModuleFile, moduleNode)
        }

        add(moduleNode)
    } else if (directory.name == "gradle") {
        for (file in directory.children) {
            if (file is PsiFile)
                gradleFiles.children.add(PsiFileNode(project, file, settings))
        }
    } else if (directory.name == "kotlin-js-store") {
        for (file in directory.children) {
            if (file is PsiFile)
                otherFiles.children.add(PsiFileNode(project, file, settings))
        }
    } else {
        val virtualFolderNode = VirtualFolderNode(
            project = project,
            folder = directory,
            moduleName = directory.name,
            icon = if (moduleType == ModuleType.Unknown) AllIcons.Nodes.Module else AllIcons.Nodes.Folder,
            viewSettings = settings
        )

        val hasAtLeastOneKmpModule = directory.children.any {
            it.moduleType().let { type -> type == ModuleType.KMP || type == ModuleType.CMP }
        }

        if (hasAtLeastOneKmpModule) {
            listAndAddChildren(directory, project, settings, true) {
                virtualFolderNode.children.add(it)
            }
        } else {
            val preference = PluginPreference.getInstance().state
            val gradleFiles2 = GradleGroupNode(project, settings, preference.isTooltipEnabled)
            val otherFiles2 = OtherGroupNode(project, settings, directory, preference.isTooltipEnabled)

            for (subModuleFile in directory.children) {
                val hasAtLeastOneKmpModule2 = subModuleFile is PsiDirectory && subModuleFile.children.any {
                    it.moduleType().let { type -> type == ModuleType.KMP || type == ModuleType.CMP }
                }

                appendModule(
                    project = project,
                    settings = settings,
                    subModuleFile = subModuleFile,
                    virtualFolderNode = virtualFolderNode,
                    gradleFiles = gradleFiles2,
                    otherFiles = otherFiles2,
                    hasAtLeastOneKmpModule = hasAtLeastOneKmpModule2
                )
            }

            if (gradleFiles2.children.isNotEmpty())
                virtualFolderNode.children.add(gradleFiles2)
            if (otherFiles2.children.isNotEmpty())
                virtualFolderNode.children.add(otherFiles2)
        }

        add(virtualFolderNode)
    }
}

private fun appendKmpModule(
    project: Project,
    settings: ViewSettings,
    subModuleFile: PsiElement,
    virtualFolderNode: VirtualFolderNode,
) {
    if (subModuleFile is PsiDirectory && !subModuleFile.canBeSkipped() && subModuleFile.name != "gradle") {
        if (subModuleFile.name == "src") {
            appendKmpSourceModule(project, settings, subModuleFile, virtualFolderNode)
        } else {
            val moduleType = subModuleFile.moduleType()
            if (moduleType == ModuleType.KMP || moduleType == ModuleType.CMP) {
                val preference = PluginPreference.getInstance().state
                val gradleFiles = GradleGroupNode(project, settings, preference.isTooltipEnabled)
                val otherFiles = OtherGroupNode(project, settings, subModuleFile, preference.isTooltipEnabled)

                findModuleTypeAndAdd(subModuleFile, project, settings, gradleFiles, otherFiles) {
                    virtualFolderNode.children.add(it)
                }
            } else virtualFolderNode.children.add(FolderNode(project, subModuleFile, settings))
        }
    } else if (subModuleFile is PsiFile) {
        virtualFolderNode.children.add(PsiFileNode(project, subModuleFile, settings))
    }
}

private fun appendKmpSourceModule(
    project: Project,
    settings: ViewSettings,
    subModuleFile: PsiDirectory,
    virtualFolderNode: VirtualFolderNode,
) {
    val preferences = PluginPreference.getInstance().state

    if (preferences.groupOtherMain) {
        val otherGroup = OtherSourceSetGroup(project, settings, preferences.isTooltipEnabled)

        for (srcFile in subModuleFile.children) {
            if (srcFile is PsiDirectory && !srcFile.canBeSkipped() && srcFile.name != "gradle") {
                if (srcFile matches preferences.commonMainKeywordList) {
                    appendCommonMain(preferences, srcFile, virtualFolderNode, project, settings)
                } else {
                    val moduleType = srcFile.moduleType()
                    if (moduleType == ModuleType.KMP || moduleType == ModuleType.CMP) {
                        val preference = PluginPreference.getInstance().state
                        val gradleFiles = GradleGroupNode(project, settings, preference.isTooltipEnabled)
                        val otherFiles = OtherGroupNode(project, settings, srcFile, preference.isTooltipEnabled)

                        findModuleTypeAndAdd(srcFile, project, settings, gradleFiles, otherFiles) {
                            virtualFolderNode.children.add(it)
                        }
                    } else otherGroup.children.add(OtherMainNode(project, srcFile, settings, preferences))
                }
            } else if (srcFile is PsiFile)
                otherGroup.children.add(PsiFileNode(project, srcFile, settings))
        }

        virtualFolderNode.children.add(otherGroup)
    } else for (srcFile in subModuleFile.children) {
        if (srcFile is PsiDirectory && !srcFile.canBeSkipped() && srcFile.name != "gradle") {
            if (srcFile matches preferences.commonMainKeywordList) {
                appendCommonMain(preferences, srcFile, virtualFolderNode, project, settings)
            } else {
                val moduleType = srcFile.moduleType()
                if (moduleType == ModuleType.KMP || moduleType == ModuleType.CMP) {
                    val preference = PluginPreference.getInstance().state
                    val gradleFiles = GradleGroupNode(project, settings, preference.isTooltipEnabled)
                    val otherFiles = OtherGroupNode(project, settings, srcFile, preference.isTooltipEnabled)

                    findModuleTypeAndAdd(srcFile, project, settings, gradleFiles, otherFiles) {
                        virtualFolderNode.children.add(it)
                    }
                } else virtualFolderNode.children.add(OtherMainNode(project, srcFile, settings, preferences))
            }
        } else if (srcFile is PsiFile)
            virtualFolderNode.children.add(PsiFileNode(project, srcFile, settings))
    }
}

private fun appendCommonMain(
    preferences: PreferenceState,
    srcFile: PsiDirectory,
    virtualFolderNode: VirtualFolderNode,
    project: Project,
    settings: ViewSettings,
) {
    if (preferences.unGroupCommonMain) {
        for (commonMainChildren in srcFile.children) {
            if (commonMainChildren is PsiFile)
                virtualFolderNode.children.add(PsiFileNode(project, commonMainChildren, settings))
            else if (commonMainChildren is PsiDirectory)
                virtualFolderNode.children.add(
                    ImportantVirtualFolderNode(
                        project = project,
                        directory = commonMainChildren,
                        settings = settings,
                        showOnTop = preferences.showCommonMainOnTop,
                        highlight = preferences.differentiateCommonMain,
                        showOnlyName = preferences.showModuleNameOnly,
                        tooltip = commonMainChildren.name + "(commonMain)"
                    )
                )
        }
    } else virtualFolderNode.children.add(CommonMainNode(project, srcFile, settings, preferences))
}

private fun appendModule(
    project: Project,
    settings: ViewSettings,
    subModuleFile: PsiElement,
    virtualFolderNode: VirtualFolderNode,
    gradleFiles: VirtualGroupNode<String>,
    otherFiles: VirtualGroupNode<PsiDirectory>,
    hasAtLeastOneKmpModule: Boolean,
) {
    if (subModuleFile is PsiDirectory && !subModuleFile.canBeSkipped()) {
        val moduleType = subModuleFile.moduleType()
        if (moduleType == ModuleType.KMP || moduleType == ModuleType.CMP) {
            findModuleTypeAndAdd(subModuleFile, project, settings, gradleFiles, otherFiles) {
                virtualFolderNode.children.add(it)
            }
        } else if (subModuleFile.name == "gradle") {
            for (file in subModuleFile.children) {
                if (file is PsiFile)
                    gradleFiles.children.add(PsiFileNode(project, file, settings))
            }
        } else if (subModuleFile.name == "kotlin-js-store") {
            for (file in subModuleFile.children) {
                if (file is PsiFile)
                    otherFiles.children.add(PsiFileNode(project, file, settings))
            }
        } else virtualFolderNode.children.add(FolderNode(project, subModuleFile, settings))
    } else if (subModuleFile is PsiFile) {
        if (hasAtLeastOneKmpModule) {
            if (subModuleFile.isGradleFile())
                gradleFiles.children.add(PsiFileNode(project, subModuleFile, settings))
            else otherFiles.children.add(PsiFileNode(project, subModuleFile, settings))
        } else virtualFolderNode.children.add(PsiFileNode(project, subModuleFile, settings))
    }
}
