package com.acutecoder.kmp.projectview

import com.acutecoder.kmp.preference.PluginPreference
import com.acutecoder.kmp.projectview.nodes.BaseProjectViewNode
import com.acutecoder.kmp.projectview.util.Config
import com.acutecoder.kmp.projectview.util.Constants
import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.ProjectTreeStructure
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project

class KmpTreeStructure(
    project: Project,
    private val id: String = Constants.PANE_ID,
) : ProjectTreeStructure(project, id) {

    override fun createRoot(project: Project, settings: ViewSettings): AbstractTreeNode<*> {
        return BaseProjectViewNode(Config(project, settings) {
            PluginPreference.getInstance().state
        })
    }

    override fun isShowExcludedFiles(): Boolean {
        return ProjectView.getInstance(myProject).isShowExcludedFiles(id)
    }

    override fun isShowLibraryContents(): Boolean {
        return false
    }

    override fun isUseFileNestingRules(): Boolean {
        return ProjectView.getInstance(myProject).isUseFileNestingRules(id)
    }

    override fun isShowScratchesAndConsoles(): Boolean {
        return false
    }

}
