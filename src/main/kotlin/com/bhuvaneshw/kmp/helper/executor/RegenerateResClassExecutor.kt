package com.bhuvaneshw.kmp.helper.executor

import com.bhuvaneshw.kmp.helper.util.Constants
import com.bhuvaneshw.kmp.helper.util.runGradleTask
import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.project.Project

class RegenerateResClassExecutor {

    fun execute(project: Project, callback: TaskCallback? = null) {
        runGradleTask(project, Constants.REGENERATE_RES_CLASS_GRADLE_TASK, callback)
    }

}
