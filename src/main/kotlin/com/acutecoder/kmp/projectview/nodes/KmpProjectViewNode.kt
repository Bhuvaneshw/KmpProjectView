package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.preference.PluginPreference
import com.acutecoder.kmp.projectview.util.*
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.ProjectViewProjectNode
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
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
            val gradleFiles = GradleGroupNode(project = project, settings = settings)
            val otherFiles = OtherGroupNode(project = project, settings = settings, baseDirectory = baseDirectory)

            for (child in baseDirectory.children) {
                if (child is PsiDirectory && !child.canBeSkipped()) {
                    val moduleType = child.moduleType()

                    if (moduleType == ModuleType.KMP || moduleType == ModuleType.CMP) {
                        val moduleNode =
                            VirtualFolderNode(
                                project = project,
                                folder = child.findSrcDirectory() ?: child,
                                moduleName = child.name,
                                icon = AllIcons.Nodes.Module,
                                viewSettings = settings,
                                moduleType = moduleType,
                                showOnTop = true,
                            )

                        for (subModuleFile in child.children) {
                            appendKmpModule(project, settings, subModuleFile, moduleNode)
                        }

                        children.add(moduleNode)
                    } else if (child.name == "gradle") {
                        for (file in child.children) {
                            if (file is PsiFile)
                                gradleFiles.children.add(PsiFileNode(project, file, settings))
                        }
                    } else if (child.name == "kotlin-js-store") {
                        for (file in child.children) {
                            if (file is PsiFile)
                                otherFiles.children.add(PsiFileNode(project, file, settings))
                        }
                    } else {
                        val virtualFolderNode = VirtualFolderNode(
                            project = project,
                            folder = child,
                            moduleName = child.name,
                            icon = if (moduleType == ModuleType.Unknown) AllIcons.Nodes.Module else AllIcons.Nodes.Folder,
                            viewSettings = settings
                        )

                        for (subModuleFile in child.children)
                            appendModule(project, settings, subModuleFile, virtualFolderNode)

                        children.add(virtualFolderNode)
                    }
                } else if (child is PsiFile) {
                    if (child.isGradleFile())
                        gradleFiles.children.add(PsiFileNode(project, child, settings))
                    else otherFiles.children.add(PsiFileNode(project, child, settings))
                }
            }

            children.add(gradleFiles)
            children.add(otherFiles)
        }

        return children
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
            appendSubKmpModule(project, settings, subModuleFile, virtualFolderNode)
        } else
            virtualFolderNode.children.add(PsiDirectoryNode(project, subModuleFile, settings))
    } else if (subModuleFile is PsiFile) {
        virtualFolderNode.children.add(PsiFileNode(project, subModuleFile, settings))
    }
}

private fun appendSubKmpModule(
    project: Project,
    settings: ViewSettings,
    subModuleFile: PsiDirectory,
    virtualFolderNode: VirtualFolderNode,
) {
    val preferences = PluginPreference.getInstance().state

    if (preferences.groupOtherMain) {
        val otherGroup = OtherSourceSetGroup(project, settings)

        for (srcFile in subModuleFile.children) {
            if (srcFile is PsiDirectory && !srcFile.canBeSkipped() && srcFile.name != "gradle") {
                if (srcFile matches preferences.commonMainKeywordList)
                    virtualFolderNode.children.add(CommonMainNode(project, srcFile, settings, preferences))
                else otherGroup.children.add(OtherMainNode(project, srcFile, settings, preferences))
            } else if (srcFile is PsiFile)
                otherGroup.children.add(PsiFileNode(project, srcFile, settings))
        }

        virtualFolderNode.children.add(otherGroup)
    } else for (srcFile in subModuleFile.children) {
        if (srcFile is PsiDirectory && !srcFile.canBeSkipped() && srcFile.name != "gradle") {
            if (srcFile matches preferences.commonMainKeywordList)
                virtualFolderNode.children.add(CommonMainNode(project, srcFile, settings, preferences))
            else virtualFolderNode.children.add(OtherMainNode(project, srcFile, settings, preferences))
        } else if (srcFile is PsiFile)
            virtualFolderNode.children.add(PsiFileNode(project, srcFile, settings))
    }
}

private fun appendModule(
    project: Project,
    settings: ViewSettings,
    subModuleFile: PsiElement,
    virtualFolderNode: VirtualFolderNode,
) {
    if (subModuleFile is PsiDirectory && !subModuleFile.canBeSkipped()) {
        virtualFolderNode.children.add(PsiDirectoryNode(project, subModuleFile, settings))
    } else if (subModuleFile is PsiFile) {
        virtualFolderNode.children.add(PsiFileNode(project, subModuleFile, settings))
    }
}
