package com.bhuvaneshw.kmp.projectview

import com.bhuvaneshw.kmp.projectview.util.Constants
import com.bhuvaneshw.kmp.preference.KMP_PREFERENCE_CHANGE
import com.bhuvaneshw.kmp.preference.PreferenceChangeListener
import com.bhuvaneshw.kmp.projectview.util.KmpSelectInTarget
import com.intellij.ide.SelectInTarget
import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.projectView.impl.ProjectAbstractTreeStructureBase
import com.intellij.ide.projectView.impl.ProjectViewPane
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.gradle.settings.GradleSettings

class KmpProjectPane(private val project: Project) : ProjectViewPane(project), PreferenceChangeListener {

    init {
        project.messageBus.connect(this).subscribe(KMP_PREFERENCE_CHANGE, this)
    }

    override fun getId(): String = Constants.Common.PANE_ID
    override fun getTitle(): String = Constants.Common.PANE_NAME
    override fun createStructure(): ProjectAbstractTreeStructureBase = KmpTreeStructure(project)
    override fun createSelectInTarget(): SelectInTarget = KmpSelectInTarget(project)
    override fun getWeight(): Int = Constants.Common.PANE_WEIGHT
    override fun onPreferenceChange() = ProjectView.getInstance(project).refresh()

    override fun isInitiallyVisible(): Boolean {
        try {
            val gradleSettings = GradleSettings.getInstance(project)
            val linkedProjects = gradleSettings.linkedProjectsSettings
            return linkedProjects.isNotEmpty()
        } catch (_: Exception) {
            return true
        }
    }
}
