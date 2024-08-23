package com.acutecoder.kmp.projectview

import com.acutecoder.kmp.projectview.util.Constants
import com.acutecoder.kmp.projectview.util.KmpSelectInTarget
import com.intellij.ide.SelectInTarget
import com.intellij.ide.projectView.impl.ProjectAbstractTreeStructureBase
import com.intellij.ide.projectView.impl.ProjectViewPane
import com.intellij.openapi.project.Project

class KmpProjectPane(private val project: Project) : ProjectViewPane(project) {

    override fun getId(): String {
        return Constants.PANE_ID
    }

    override fun getTitle(): String {
        return Constants.PANE_NAME
    }

    override fun createStructure(): ProjectAbstractTreeStructureBase {
        return KmpTreeStructure(project)
    }

    override fun createSelectInTarget(): SelectInTarget {
        return KmpSelectInTarget(project)
    }

    override fun getWeight(): Int {
        return Constants.PANE_WEIGHT
    }

}
