package com.acutecoder.kmp.helper.util

import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.util.*

fun runGradleTask(myProject: Project, taskName: String, callback: TaskCallback? = null) {
    val settings = ExternalSystemTaskExecutionSettings().apply {
        externalProjectPath = myProject.basePath
        taskNames = Collections.singletonList(taskName)
//    scriptParameters = java.lang.String.format("-filepath=\"%s\"", fileToProcess.getVirtualFile().getPath())
        vmOptions = ""
        externalSystemIdString = GradleConstants.SYSTEM_ID.id
    }

    ExternalSystemUtil.runTask(
        settings, DefaultRunExecutor.EXECUTOR_ID, myProject, GradleConstants.SYSTEM_ID,
        callback, ProgressExecutionMode.IN_BACKGROUND_ASYNC, false
    )
}
