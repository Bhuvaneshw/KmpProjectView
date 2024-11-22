package com.acutecoder.kmp.helper.executor

import com.acutecoder.kmp.helper.util.Constants
import com.acutecoder.kmp.helper.util.runGradleTask
import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.project.Project

class RegenerateResClassExecutor {

    fun execute(project: Project, callback: TaskCallback? = null) {
        runGradleTask(project, Constants.REGENERATE_RES_CLASS_GRADLE_TASK, callback)
    }

    companion object {
        private lateinit var executor: RegenerateResClassExecutor
        private var isRunning = false

        fun executeSafe(project: Project) {
            if (isRunning) return

            synchronized(this) {
                isRunning = true
                if (!this::executor.isInitialized) executor = RegenerateResClassExecutor()
            }

            synchronized(executor) {
                executor.execute(project, object : TaskCallback {
                    override fun onSuccess() {
                        isRunning = false
                    }

                    override fun onFailure() {
                        isRunning = false
                    }
                })
            }
        }
    }
}
