package com.acutecoder.kmp.projectview.nodes

import com.intellij.ide.projectView.ViewSettings
import com.intellij.openapi.project.Project
import javax.swing.Icon

class LessImportantVirtualFolderNode(
    project: Project,
    folderName: String,
    icon: Icon,
    viewSettings: ViewSettings,
) : VirtualFolderNode(project, folderName, icon, viewSettings) {

    override fun getWeight(): Int {
        return 100
    }

}
