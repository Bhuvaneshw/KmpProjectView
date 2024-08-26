package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.preference.PluginPreference
import com.acutecoder.kmp.projectview.util.withoutTooltip
import com.acutecoder.kmp.projectview.util.withTooltip
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.ui.SimpleTextAttributes
import javax.swing.Icon

@Suppress("NOTHING_TO_INLINE", "functionName")
inline fun GradleGroupNode(project: Project, settings: ViewSettings) =
    VirtualGroupNode(
        project = project,
        folderName = "Gradle Files",
        element = "Gradle Files",
        icon = AllIcons.Nodes.ConfigFolder,
        viewSettings = settings,
        isTooltipEnabled = PluginPreference.getInstance().state.isTooltipEnabled,
    )

@Suppress("NOTHING_TO_INLINE", "functionName")
inline fun OtherGroupNode(project: Project, settings: ViewSettings, baseDirectory: PsiDirectory) =
    VirtualGroupNode(
        project = project,
        folderName = "Other Files",
        element = baseDirectory,
        icon = AllIcons.Nodes.Folder,
        viewSettings = settings,
        isTooltipEnabled = PluginPreference.getInstance().state.isTooltipEnabled,
    )

@Suppress("NOTHING_TO_INLINE", "functionName")
inline fun OtherSourceSetGroup(
    project: Project,
    settings: ViewSettings,
) = VirtualGroupNode(
    project = project,
    folderName = "Other Source Set",
    element = "Other Source Set",
    tooltip = "Source Set group",
    icon = AllIcons.Nodes.ModuleGroup,
    viewSettings = settings,
    isTooltipEnabled = PluginPreference.getInstance().state.isTooltipEnabled,
    weight = 10,
)

class VirtualGroupNode<T : Any>(
    project: Project,
    private val folderName: String,
    element: T,
    private val icon: Icon,
    viewSettings: ViewSettings,
    private val isTooltipEnabled: Boolean,
    private val tooltip: String = folderName,
    private val weight: Int = 100,
) : ProjectViewNode<T>(project, element, viewSettings) {

    val children = mutableListOf<AbstractTreeNode<*>>()

    override fun update(presentation: PresentationData) {
        presentation.setIcon(
            if (isTooltipEnabled)
                icon.withTooltip(tooltip)
            else icon.withoutTooltip()
        )

        presentation.addText(folderName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
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
