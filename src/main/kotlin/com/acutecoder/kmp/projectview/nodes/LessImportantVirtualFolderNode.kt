package com.acutecoder.kmp.projectview.nodes

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ViewSettings
import com.intellij.openapi.project.Project
import javax.swing.Icon

class LessImportantVirtualFolderNode(
    project: Project,
    private val folderName: String,
    icon: Icon,
    viewSettings: ViewSettings,
    private val tooltip: String = folderName,
) : VirtualFolderNode(project, folderName, icon, viewSettings) {

    override fun getWeight(): Int {
        return 100
    }

    override fun update(presentation: PresentationData) {
        super.update(presentation)
        presentation.tooltip = tooltip
    }

}
