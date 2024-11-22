package com.acutecoder.kmp.projectview

import com.acutecoder.kmp.preference.PreferenceObserver
import com.acutecoder.kmp.projectview.util.Constants
import com.acutecoder.kmp.projectview.util.KmpSelectInTarget
import com.intellij.ide.SelectInTarget
import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.projectView.impl.ProjectAbstractTreeStructureBase
import com.intellij.ide.projectView.impl.ProjectViewPane
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.gradle.settings.GradleSettings

class KmpProjectPane(private val project: Project) : ProjectViewPane(project) {

    init {
        PreferenceObserver.observe {
            ProjectView.getInstance(project).refresh()
        }
    }

    override fun getId(): String = Constants.PANE_ID
    override fun getTitle(): String = Constants.PANE_NAME
    override fun createStructure(): ProjectAbstractTreeStructureBase = KmpTreeStructure(project)
    override fun createSelectInTarget(): SelectInTarget = KmpSelectInTarget(project)
    override fun getWeight(): Int = Constants.PANE_WEIGHT

    override fun isInitiallyVisible(): Boolean {
        try {
            val gradleSettings = GradleSettings.getInstance(project)
            val linkedProjects = gradleSettings.linkedProjectsSettings
            return linkedProjects.isNotEmpty()
        } catch (e: Exception) {
            return true
        }
    }

}
