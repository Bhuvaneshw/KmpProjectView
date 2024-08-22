package com.acutecoder.kmp.projectview

import com.acutecoder.kmp.projectview.nodes.KmpProjectViewNode
import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.ProjectTreeStructure
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry

class KmpTreeStructure(
    project: Project,
    private val id: String = Constants.PANE_ID,
) : ProjectTreeStructure(project, id) {

    override fun createRoot(project: Project, settings: ViewSettings): AbstractTreeNode<*> {
        return KmpProjectViewNode(project, settings)
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

    override fun isToBuildChildrenInBackground(element: Any): Boolean {
        return Registry.`is`("ide.projectView.ProjectViewPaneTreeStructure.BuildChildrenInBackground")
    }
}
