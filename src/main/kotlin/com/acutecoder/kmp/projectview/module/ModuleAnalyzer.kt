package com.acutecoder.kmp.projectview.module

import com.acutecoder.kmp.preference.PluginPreference
import com.acutecoder.kmp.preference.PreferenceState
import com.acutecoder.kmp.projectview.nodes.*
import com.acutecoder.kmp.projectview.util.*
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

private inline val preference: PreferenceState
    get() = PluginPreference.getInstance().state

fun PsiElement.moduleType(): ModuleType {
    if (this !is PsiDirectory)
        return ModuleType.Unknown

    return this.children.analyzeModuleType()
}

fun Array<PsiElement>.analyzeModuleType(): ModuleType {
    var src = false
    var kmp = false
    var cmp = false
    var ktor = false

    this.forEach { file ->
        if (file is PsiDirectory && file.virtualFile.name == "src")
            src = true
        if (file is PsiFile && (file.virtualFile.name == "build.gradle.kts" || file.virtualFile.name == "build.gradle")) {
            kmp = preference.kmpKeywordList.any { file.text.contains(it.trim()) }
            cmp = preference.cmpKeywordList.any { file.text.contains(it.trim()) }
            ktor = preference.ktorKeywordList.any { file.text.contains(it.trim()) }
        }
    }

    return if (src) {
        when {
            kmp && cmp -> ModuleType.CMP
            kmp -> ModuleType.KMP
            ktor -> ModuleType.Server
            else -> ModuleType.Unknown
        }
    } else ModuleType.Unknown
}

fun PsiDirectory.hasAtLeastOneKmpOrCmpModule() = children.any { it.moduleType() != ModuleType.Unknown }

fun listAndAddChildrenAsModule(
    config: Config,
    baseDirectory: PsiDirectory,
    add: (AbstractTreeNode<*>) -> Unit,
) {
    val hasAtLeastOnKmpOrCmpModule =
        baseDirectory.hasAtLeastOneKmpOrCmpModule() || config.preference().splitGradleAndOther == 1
    val otherFiles = OtherGroupNode(config, baseDirectory)
    val gradleFiles = GradleGroupNode(config)

    for (child in baseDirectory.children) {
        if (child is PsiDirectory) {
            val moduleType = baseDirectory.moduleType()
            if (child.name == "src" && moduleType.isKmpOrCmp()) {
                val otherSourceSet = OtherSourceSetGroup(config)

                for (srcChild in child.children) {
                    if (srcChild is PsiDirectory) {
                        if (srcChild.matches(config.preference().commonMainKeywordList)) {
                            if (config.preference().unGroupCommonMain) {
                                for (commonMainChild in srcChild.children) {
                                    if (commonMainChild is PsiDirectory && !commonMainChild.canBeSkipped(config))
                                        add(ImportantFolderNode(config, commonMainChild, true))
                                    else if (commonMainChild is PsiFile && !commonMainChild.canBeSkipped(config))
                                        add(PsiFileNode(config.project, commonMainChild, config.viewSettings))
                                }
                            } else add(
                                ImportantFolderNode(
                                    config = config,
                                    folder = srcChild,
                                    defaultIcon = if (config.preference().differentiateCommonMain)
                                        AllIcons.Modules.TestRoot else AllIcons.Modules.SourceRoot,
                                    showOnTop = config.preference().showCommonMainOnTop,
                                )
                            )
                        } else if (srcChild.isSourceSet()) {
                            val node = ImportantFolderNode(
                                config = config,
                                folder = srcChild,
                                defaultIcon = AllIcons.Modules.SourceRoot,
                                showOnTop = false,
                            )
                            if (config.preference().groupOtherMain)
                                otherSourceSet.children.add(node)
                            else add(node)
                        } else if (srcChild.name == "gradle") {
                            for (file in srcChild.children) {
                                if (file is PsiDirectory && file.name == "wrapper") {
                                    for (srcChild in file.children) {
                                        if (srcChild is PsiFile) {
                                            if (srcChild.name.lowercase().endsWith(".jar")) continue
                                            gradleFiles.children.add(PsiFileNode(config.project, srcChild, config.viewSettings))
                                        }
                                    }
                                } else if (file is PsiFile) {
                                    if (hasAtLeastOnKmpOrCmpModule)
                                        gradleFiles.children.add(PsiFileNode(config.project, file, config.viewSettings))
                                    else add(PsiFileNode(config.project, file, config.viewSettings))
                                }
                            }
                        } else if (srcChild.name == "kotlin-js-store") {
                            for (file in srcChild.children) {
                                if (file is PsiFile) {
                                    if (hasAtLeastOnKmpOrCmpModule)
                                        otherFiles.children.add(PsiFileNode(config.project, file, config.viewSettings))
                                    else add(PsiFileNode(config.project, file, config.viewSettings))
                                }
                            }
                        } else if (!srcChild.canBeSkipped(config))
                            add(FolderNode(config, srcChild))
                    } else if (srcChild is PsiFile) {
                        if (srcChild.canBeSkipped(config)) continue

                        if (srcChild.isGradleFile()) {
                            if (hasAtLeastOnKmpOrCmpModule)
                                gradleFiles.children.add(HintedPsiFileNode(config, srcChild, " (src)"))
                            else add(HintedPsiFileNode(config, srcChild, " (src)"))
                        } else {
                            if (hasAtLeastOnKmpOrCmpModule)
                                otherFiles.children.add(HintedPsiFileNode(config, srcChild, " (src)"))
                            else add(HintedPsiFileNode(config, srcChild, " (src)"))
                        }
                    }
                }

                if (config.preference().groupOtherMain)
                    add(otherSourceSet)
            } else if (child.name == "src" && moduleType == ModuleType.Server) {
                for (srcChild in child.children) {
                    if (srcChild is PsiDirectory) {
                        if (srcChild.isSourceSet()) {
                            val node = ImportantFolderNode(
                                config = config,
                                folder = srcChild,
                                defaultIcon = AllIcons.Modules.SourceRoot,
                                showOnTop = false,
                            )
                            add(node)
                        } else if (!srcChild.canBeSkipped(config))
                            add(FolderNode(config, srcChild))
                    } else if (srcChild is PsiFile) {
                        if (srcChild.canBeSkipped(config)) continue

                        if (srcChild.isGradleFile()) {
                            if (hasAtLeastOnKmpOrCmpModule)
                                gradleFiles.children.add(HintedPsiFileNode(config, srcChild, " (src)"))
                            else add(HintedPsiFileNode(config, srcChild, " (src)"))
                        } else {
                            if (hasAtLeastOnKmpOrCmpModule)
                                otherFiles.children.add(HintedPsiFileNode(config, srcChild, " (src)"))
                            else add(HintedPsiFileNode(config, srcChild, " (src)"))
                        }
                    }
                }
            } else if (child.name == "gradle") {
                for (file in child.children) {
                    if (file is PsiDirectory && file.name == "wrapper") {
                        for (srcChild in file.children) {
                            if (srcChild is PsiFile) {
                                if (srcChild.name.lowercase().endsWith(".jar")) continue
                                gradleFiles.children.add(PsiFileNode(config.project, srcChild, config.viewSettings))
                            }
                        }
                    } else if (file is PsiFile) {
                        if (hasAtLeastOnKmpOrCmpModule)
                            gradleFiles.children.add(PsiFileNode(config.project, file, config.viewSettings))
                        else add(PsiFileNode(config.project, file, config.viewSettings))
                    }
                }
            } else if (child.name == "kotlin-js-store") {
                for (file in child.children) {
                    if (file is PsiFile)
                        otherFiles.children.add(PsiFileNode(config.project, file, config.viewSettings))
                }
            } else if (!child.canBeSkipped(config))
                add(FolderNode(config, child))
        } else if (child is PsiFile && !child.canBeSkipped(config)) {
            if (child.isGradleFile()) {
                if (child.canBeSkipped(config)) continue

                if (hasAtLeastOnKmpOrCmpModule)
                    gradleFiles.children.add(PsiFileNode(config.project, child, config.viewSettings))
                else add(PsiFileNode(config.project, child, config.viewSettings))
            } else {
                if (child.canBeSkipped(config)) continue

                if (hasAtLeastOnKmpOrCmpModule)
                    otherFiles.children.add(PsiFileNode(config.project, child, config.viewSettings))
                else add(PsiFileNode(config.project, child, config.viewSettings))
            }
        }
    }

    if (hasAtLeastOnKmpOrCmpModule) {
        if (gradleFiles.children.isNotEmpty())
            add(gradleFiles)
        if (otherFiles.children.isNotEmpty())
            add(otherFiles)
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
                        if (file is PsiDirectory && file.name == "wrapper") {
                            for (srcChild in file.children) {
                                if (srcChild is PsiFile) {
                                    if (srcChild.name.lowercase().endsWith(".jar")) continue
                                    gradleFiles.children.add(PsiFileNode(config.project, srcChild, config.viewSettings))
                                }
                            }
                        } else if (file is PsiFile)
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
