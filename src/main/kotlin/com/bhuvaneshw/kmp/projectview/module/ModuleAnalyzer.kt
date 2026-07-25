package com.bhuvaneshw.kmp.projectview.module

import com.bhuvaneshw.kmp.preference.PluginPreference
import com.bhuvaneshw.kmp.projectview.nodes.FolderNode
import com.bhuvaneshw.kmp.projectview.nodes.GradleGroupNode
import com.bhuvaneshw.kmp.projectview.nodes.HintedPsiFileNode
import com.bhuvaneshw.kmp.projectview.nodes.ImportantFolderNode
import com.bhuvaneshw.kmp.projectview.nodes.OtherGroupNode
import com.bhuvaneshw.kmp.projectview.nodes.OtherSourceSetGroup
import com.bhuvaneshw.kmp.projectview.nodes.VirtualGroupNode
import com.bhuvaneshw.kmp.projectview.util.Config
import com.bhuvaneshw.kmp.projectview.util.canBeSkipped
import com.bhuvaneshw.kmp.projectview.util.isGradleFile
import com.bhuvaneshw.kmp.projectview.util.isSourceSet
import com.bhuvaneshw.kmp.projectview.util.matches
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

fun PsiElement.moduleType(): ModuleType {
    val directory = this as? PsiDirectory ?: return ModuleType.Unknown

    val module = ModuleUtilCore.findModuleForPsiElement(directory)
    if (module != null) {
        val projectPath =
            ExternalSystemApiUtil.getExternalProjectPath(module)
        if (projectPath != null && FileUtil.pathsEqual(directory.virtualFile.path, projectPath)) {
            val moduleType = GradleModuleHelper.getModuleType(module)
            if (moduleType != ModuleType.Unknown) return moduleType
        }
    }

    val preference = PluginPreference.getInstance().state
    if (GradleModuleHelper.hasFileMarkers(directory, preference.iosFileMarkerList)) {
        return ModuleType.IOS
    }

    return ModuleType.Unknown
}

fun PsiDirectory.isBuildRoot(): Boolean {
    val dirPath = virtualFile.path
    return GradleModuleHelper.getAllBuildRoots(project).any { FileUtil.pathsEqual(it, dirPath) }
}

fun PsiDirectory.isSharedModule(config: Config): Boolean {
    val module = ModuleUtilCore.findModuleForPsiElement(this) ?: return false
    val projectPath = ExternalSystemApiUtil.getExternalProjectPath(module) ?: return false
    if (!FileUtil.pathsEqual(virtualFile.path, projectPath)) return false

    val moduleName = GradleModuleHelper.getModuleName(module)
    return config.preference().sharedModuleKeywordList.any { moduleName.contains(it, ignoreCase = true) }
}

fun PsiDirectory.shouldGroupFiles(config: Config): Boolean {
    val splitMode = config.preference().splitGradleAndOther
    return when (splitMode) {
        0 -> isBuildRoot()
        1 -> true
        else -> false
    }
}

fun listAndAddChildrenAsModule(
    config: Config,
    baseDirectory: PsiDirectory,
    add: (AbstractTreeNode<*>) -> Unit,
    weightOffset: Int = 0,
    isLabelEnabled: Boolean = true
) {
    val moduleType = baseDirectory.moduleType()
    val shouldGroup = baseDirectory.shouldGroupFiles(config)

    val rootPath = baseDirectory.virtualFile.path
    val projectName =
        if (isLabelEnabled && !config.preference().separateNodeForSubstitutedProject) GradleModuleHelper.getProjectName(
            config.project,
            rootPath
        ) else null

    val otherFiles = OtherGroupNode(config, baseDirectory, projectName, weightOffset)
    val gradleFiles = GradleGroupNode(config, projectName, rootPath, weightOffset)

    for (child in baseDirectory.children) {
        if (child is PsiDirectory) {
            when {
                child.name == "src" && (moduleType.isGradleModule()) -> {
                    handleSourceDirectory(
                        srcDir = child,
                        config = config,
                        moduleType = moduleType,
                        add = add,
                        gradleFiles = gradleFiles,
                        otherFiles = otherFiles,
                        shouldGroup = shouldGroup,
                        projectName = projectName,
                        rootPath = rootPath,
                        weightOffset = weightOffset,
                        isLabelEnabled = isLabelEnabled
                    )
                }

                child.name == "gradle" -> {
                    handleGradleDirectory(
                        gradleDir = child,
                        config = config,
                        add = add,
                        gradleFiles = gradleFiles,
                        shouldGroup = shouldGroup
                    )
                }

                child.name == "kotlin-js-store" -> {
                    child.children.filterIsInstance<PsiFile>().forEach {
                        otherFiles.children.add(
                            PsiFileNode(config.project, it, config.viewSettings)
                        )
                    }
                }

                !child.canBeSkipped(config) -> {
                    add(FolderNode(config, child, weightOffset, isLabelEnabled))
                }
            }
        } else if (child is PsiFile && !child.canBeSkipped(config)) {
            val node = if (child.isGradleFile()) gradleFiles else otherFiles
            if (shouldGroup) {
                node.children.add(PsiFileNode(config.project, child, config.viewSettings))
            } else {
                add(PsiFileNode(config.project, child, config.viewSettings))
            }
        }
    }

    if (shouldGroup) {
        if (gradleFiles.children.isNotEmpty()) add(gradleFiles)
        if (otherFiles.children.isNotEmpty()) add(otherFiles)
    }
}

private fun handleSourceDirectory(
    srcDir: PsiDirectory,
    config: Config,
    moduleType: ModuleType,
    add: (AbstractTreeNode<*>) -> Unit,
    gradleFiles: VirtualGroupNode<*>,
    otherFiles: VirtualGroupNode<*>,
    shouldGroup: Boolean,
    projectName: String?,
    rootPath: String,
    weightOffset: Int,
    isLabelEnabled: Boolean
) {
    val otherSourceSet = OtherSourceSetGroup(config, projectName, rootPath, weightOffset)
    for (srcChild in srcDir.children) {
        if (srcChild is PsiDirectory) {
            when {
                srcChild.matches(config.preference().commonMainKeywordList) -> {
                    if (config.preference().unGroupCommonMain) {
                        srcChild.children.forEach {
                            if (it is PsiDirectory && !it.canBeSkipped(config))
                                add(
                                    ImportantFolderNode(
                                        config = config,
                                        folder = it,
                                        showOnTop = true,
                                        defaultIcon = null,
                                        additionalWeight = weightOffset,
                                        isLabelEnabled = isLabelEnabled
                                    )
                                )
                            else if (it is PsiFile && !it.canBeSkipped(config))
                                add(PsiFileNode(config.project, it, config.viewSettings))
                        }
                    } else {
                        add(
                            ImportantFolderNode(
                                config = config,
                                folder = srcChild,
                                showOnTop = config.preference().showCommonMainOnTop,
                                defaultIcon = if (config.preference().differentiateCommonMain) AllIcons.Modules.TestRoot else AllIcons.Modules.SourceRoot,
                                additionalWeight = weightOffset,
                                isLabelEnabled = isLabelEnabled
                            )
                        )
                    }
                }

                srcChild.isSourceSet() -> {
                    val node = ImportantFolderNode(
                        config = config,
                        folder = srcChild,
                        showOnTop = false,
                        defaultIcon = AllIcons.Modules.SourceRoot,
                        additionalWeight = weightOffset,
                        isLabelEnabled = isLabelEnabled
                    )
                    if (config.preference().groupOtherMain && moduleType.isKmpOrCmp())
                        otherSourceSet.children.add(node)
                    else add(node)
                }

                srcChild.name == "gradle" -> handleGradleDirectory(
                    gradleDir = srcChild,
                    config = config,
                    add = add,
                    gradleFiles = gradleFiles,
                    shouldGroup = shouldGroup
                )

                srcChild.name == "kotlin-js-store" -> {
                    srcChild.children.filterIsInstance<PsiFile>().forEach { file ->
                        if (shouldGroup) otherFiles.children.add(
                            PsiFileNode(config.project, file, config.viewSettings)
                        )
                        else add(PsiFileNode(config.project, file, config.viewSettings))
                    }
                }

                !srcChild.canBeSkipped(config) ->
                    add(
                        FolderNode(config, srcChild, weightOffset, isLabelEnabled)
                    )
            }
        } else if (srcChild is PsiFile && !srcChild.canBeSkipped(config)) {
            val hint = " (src)"
            if (srcChild.isGradleFile()) {
                if (shouldGroup) gradleFiles.children.add(
                    HintedPsiFileNode(config, srcChild, hint)
                )
                else add(HintedPsiFileNode(config, srcChild, hint))
            } else {
                if (shouldGroup) otherFiles.children.add(
                    HintedPsiFileNode(config, srcChild, hint)
                )
                else add(HintedPsiFileNode(config, srcChild, hint))
            }
        }
    }
    if (config.preference().groupOtherMain && otherSourceSet.children.isNotEmpty()) add(
        otherSourceSet
    )
}

private fun handleGradleDirectory(
    gradleDir: PsiDirectory,
    config: Config,
    add: (AbstractTreeNode<*>) -> Unit,
    gradleFiles: VirtualGroupNode<*>,
    shouldGroup: Boolean
) {
    for (file in gradleDir.children) {
        if (file is PsiDirectory && file.name == "wrapper") {
            file.children.filterIsInstance<PsiFile>()
                .filter { !it.name.lowercase().endsWith(".jar") }
                .forEach {
                    gradleFiles.children.add(
                        PsiFileNode(config.project, it, config.viewSettings)
                    )
                }
        } else if (file is PsiFile) {
            if (shouldGroup)
                gradleFiles.children.add(PsiFileNode(config.project, file, config.viewSettings))
            else add(PsiFileNode(config.project, file, config.viewSettings))
        }
    }
}

fun listAndAddChildren(
    config: Config,
    baseDirectory: PsiDirectory,
    add: (AbstractTreeNode<*>) -> Unit,
    weightOffset: Int = 0,
    isLabelEnabled: Boolean = true
) {
    val shouldGroup = baseDirectory.shouldGroupFiles(config)
    if (shouldGroup) {
        val rootPath = baseDirectory.virtualFile.path
        val projectName =
            if (isLabelEnabled && !config.preference().separateNodeForSubstitutedProject) GradleModuleHelper.getProjectName(
                config.project,
                rootPath
            ) else null

        val otherFiles = OtherGroupNode(config, baseDirectory, projectName, weightOffset)
        val gradleFiles = GradleGroupNode(config, projectName, rootPath, weightOffset)

        for (child in baseDirectory.children) {
            if (child is PsiDirectory) {
                when (child.name) {
                    "gradle" -> handleGradleDirectory(child, config, add, gradleFiles, true)
                    "kotlin-js-store" -> child.children.filterIsInstance<PsiFile>().forEach {
                        otherFiles.children.add(
                            PsiFileNode(config.project, it, config.viewSettings)
                        )
                    }

                    else -> if (!child.canBeSkipped(config))
                        add(FolderNode(config, child, weightOffset, isLabelEnabled))
                }
            } else if (child is PsiFile && !child.canBeSkipped(config)) {
                if (child.isGradleFile())
                    gradleFiles.children.add(
                        PsiFileNode(config.project, child, config.viewSettings)
                    )
                else
                    otherFiles.children.add(PsiFileNode(config.project, child, config.viewSettings))
            }
        }

        if (gradleFiles.children.isNotEmpty()) add(gradleFiles)
        if (otherFiles.children.isNotEmpty()) add(otherFiles)
    } else {
        baseDirectory.children.forEach { child ->
            if (child is PsiDirectory && !child.canBeSkipped(config))
                add(FolderNode(config, child, weightOffset, isLabelEnabled))
            else if (child is PsiFile && !child.canBeSkipped(config))
                add(PsiFileNode(config.project, child, config.viewSettings))
        }
    }
}
