package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.util.Config
import com.acutecoder.kmp.projectview.util.withTooltip
import com.acutecoder.kmp.projectview.util.withoutTooltip
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.ui.SimpleTextAttributes
import icons.GradleIcons
import javax.swing.Icon

@Suppress("NOTHING_TO_INLINE", "functionName")
inline fun GradleGroupNode(
    config: Config,
    projectName: String? = null,
    rootPath: String = "",
    weightOffset: Int = 0
) =
    VirtualGroupNode(
        config = config,
        folderName = "Gradle Files",
        element = "Gradle-$rootPath",
        icon = GradleIcons.Gradle,
        projectName = projectName,
        weight = 100,
        additionalWeight = weightOffset
    )

@Suppress("NOTHING_TO_INLINE", "functionName")
inline fun OtherGroupNode(
    config: Config,
    baseDirectory: PsiDirectory,
    projectName: String? = null,
    weightOffset: Int = 0
) =
    VirtualGroupNode(
        config = config,
        folderName = "Other Files",
        element = baseDirectory,
        icon = AllIcons.FileTypes.Any_type,
        projectName = projectName,
        weight = 200,
        additionalWeight = weightOffset
    )

@Suppress("NOTHING_TO_INLINE", "functionName")
inline fun OtherSourceSetGroup(
    config: Config,
    projectName: String? = null,
    rootPath: String = "",
    weightOffset: Int = 0
) = VirtualGroupNode(
    config = config,
    folderName = "Other Source Set",
    element = "SourceSet-$rootPath",
    icon = AllIcons.Nodes.ModuleGroup,
    tooltip = "Source Set group",
    weight = 10,
    projectName = projectName,
    additionalWeight = weightOffset
)

class VirtualGroupNode<T : Any>(
    private val config: Config,
    private val folderName: String,
    element: T,
    private val icon: Icon,
    private val tooltip: String = folderName,
    private val weight: Int = 100,
    private val isTooltipEnabled: Boolean = config.preference().isTooltipEnabled,
    private val projectName: String? = null,
    private val additionalWeight: Int = 0,
) : ProjectViewNode<T>(config.project, element, config.viewSettings) {

    val children = mutableListOf<AbstractTreeNode<*>>()

    override fun update(presentation: PresentationData) {
        presentation.setIcon(
            if (isTooltipEnabled)
                icon.withTooltip(tooltip)
            else icon.withoutTooltip()
        )

        presentation.addText(folderName, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)

        if (!config.preference().separateNodeForSubstitutedProject) {
            projectName?.let {
                presentation.addText(" [$it]", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES)
            }
        }
    }

    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> {
        return children
    }

    override fun getWeight(): Int {
        return weight + additionalWeight
    }

    override fun contains(file: VirtualFile): Boolean {
        return false
    }
}
