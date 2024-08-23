package com.acutecoder.kmp.projectview.util

import com.intellij.ide.SelectInContext
import com.intellij.ide.SelectInManager
import com.intellij.ide.StandardTargetWeights
import com.intellij.ide.impl.ProjectViewSelectInTarget
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileSystemItem

class KmpSelectInTarget(
    project: Project,
    private val id: String = Constants.PANE_ID,
) : ProjectViewSelectInTarget(project), DumbAware {

    override fun toString(): String {
        return SelectInManager.getProject()
    }

    override fun isSubIdSelectable(subId: String, context: SelectInContext): Boolean {
        return canSelect(context)
    }

    override fun canSelect(file: PsiFileSystemItem?): Boolean {
        return super.canSelect(file)
    }

    override fun getMinorViewId(): String {
        return id
    }

    override fun getWeight(): Float {
        return StandardTargetWeights.PROJECT_WEIGHT
    }
}
