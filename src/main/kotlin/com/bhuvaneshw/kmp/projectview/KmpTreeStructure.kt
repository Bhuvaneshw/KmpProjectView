package com.bhuvaneshw.kmp.projectview

import com.bhuvaneshw.kmp.preference.PluginPreference
import com.bhuvaneshw.kmp.projectview.nodes.BaseProjectViewNode
import com.bhuvaneshw.kmp.projectview.util.Config
import com.bhuvaneshw.kmp.projectview.util.Constants
import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.ProjectTreeStructure
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project

class KmpTreeStructure(
    project: Project,
    private val id: String = Constants.Common.PANE_ID,
) : ProjectTreeStructure(project, id) {

    override fun createRoot(project: Project, settings: ViewSettings): AbstractTreeNode<*> {
        return BaseProjectViewNode(Config(project, settings) {
            PluginPreference.getInstance().state
        })
    }

    override fun getChildElements(element: Any): Array<Any> {
        if (element is AbstractTreeNode<*> && element.value == Constants.Node.GLOBAL_GRADLE_NODE_ID) {
            return element.children.toTypedArray()
        }
        return super.getChildElements(element)
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
        return ProjectView.getInstance(myProject).isShowScratchesAndConsoles(id)
    }

}
