package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.module.hasAtLeastOneKmpOrCmpModule
import com.acutecoder.kmp.projectview.module.moduleType
import com.acutecoder.kmp.projectview.preference.PluginPreference
import com.acutecoder.kmp.projectview.util.*
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.ui.SimpleTextAttributes

class FolderNode(
    private val config: Config,
    private val folder: PsiDirectory,
) : ProjectViewNode<PsiDirectory>(
    config.project,
    folder.moduleType().let { if (it.isKmpOrCmp()) folder.findSrcDirectory() ?: folder else folder },
    config.viewSettings
) {

    private val preferences = PluginPreference.getInstance().state

    override fun update(presentation: PresentationData) {
        val moduleName = folder.name
        val moduleType = folder.moduleType()
        val icon = if (moduleType.isKmpOrCmp()) AllIcons.Nodes.Module
        else if (moduleName.equals("kotlin", true)) AllIcons.Modules.SourceRoot
        else if (moduleName.contains("resource", true) || moduleName.equals("res", true)) AllIcons.Modules.ResourcesRoot
        else AllIcons.Nodes.Folder

        presentation.setIcon(
            if (moduleType.isKmpOrCmp() && preferences.isTooltipEnabled)
                icon.withTooltip("${moduleType.displayName} module")
            else icon.withoutTooltip()
        )

        presentation.addText(moduleName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        if (moduleType.isKmpOrCmp() && preferences.showKmpModuleSideText)
            presentation.addText(" ($moduleType)", SimpleTextAttributes.GRAY_ATTRIBUTES)
    }

    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> {
        val children = mutableListOf<AbstractTreeNode<*>>()

        if (folder.moduleType().isKmpOrCmp())
            listAndAddChildrenAsModule(
                config = config,
                baseDirectory = folder,
                add = children::add
            )
        else listAndAddChildren(
            config = config,
            baseDirectory = folder,
            groupGradleAndOtherFiles = folder.hasAtLeastOneKmpOrCmpModule(),
            add = children::add
        )

        return children
    }

    override fun getWeight(): Int = Constants.DEFAULT_WEIGHT

    override fun contains(file: VirtualFile): Boolean {
        return folder.virtualFile == file || folder.virtualFile.isAncestorOf(file)
    }
}

fun listAndAddChildrenAsModule(
    config: Config,
    baseDirectory: PsiDirectory,
    add: (AbstractTreeNode<*>) -> Unit,
) {
    val hasAtLeastOnKmpOrCmpModule = baseDirectory.hasAtLeastOneKmpOrCmpModule() || config.preference().splitGradleAndOther == 1
    val otherFiles = OtherGroupNode(config, baseDirectory)
    val gradleFiles = GradleGroupNode(config)

    for (child in baseDirectory.children) {
        if (child is PsiDirectory) {
            if (child.name == "src") {
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
                                if (file is PsiFile) {
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
            } else if (child.name == "gradle") {
                for (file in child.children) {
                    if (file is PsiFile) {
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
