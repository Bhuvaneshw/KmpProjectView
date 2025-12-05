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
inline fun GradleGroupNode(config: Config) =
    VirtualGroupNode(
        config = config,
        folderName = "Gradle Files",
        element = "Gradle Files",
        icon = GradleIcons.Gradle,
    )

@Suppress("NOTHING_TO_INLINE", "functionName")
inline fun OtherGroupNode(
    config: Config,
    baseDirectory: PsiDirectory,
) =
    VirtualGroupNode(
        config = config,
        folderName = "Other Files",
        element = baseDirectory,
        icon = AllIcons.FileTypes.Any_type,
    )

@Suppress("NOTHING_TO_INLINE", "functionName")
inline fun OtherSourceSetGroup(config: Config) = VirtualGroupNode(
    config = config,
    folderName = "Other Source Set",
    element = "Other Source Set",
    tooltip = "Source Set group",
    icon = AllIcons.Nodes.ModuleGroup,
    weight = 10,
)

class VirtualGroupNode<T : Any>(
    config: Config,
    private val folderName: String,
    element: T,
    private val icon: Icon,
    private val tooltip: String = folderName,
    private val weight: Int = 100,
    private val isTooltipEnabled: Boolean = config.preference().isTooltipEnabled,
) : ProjectViewNode<T>(config.project, element, config.viewSettings) {

    val children = mutableListOf<AbstractTreeNode<*>>()

    override fun update(presentation: PresentationData) {
        presentation.setIcon(
            if (isTooltipEnabled)
                icon.withTooltip(tooltip)
            else icon.withoutTooltip()
        )

        presentation.addText(folderName, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
    }

    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> {
        return children
    }

    override fun getWeight(): Int {
        return weight
    }

    override fun contains(file: VirtualFile): Boolean {
        return false
    }
}
