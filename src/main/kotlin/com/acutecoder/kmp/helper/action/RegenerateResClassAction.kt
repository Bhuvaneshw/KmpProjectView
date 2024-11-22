package com.acutecoder.kmp.helper.action

import com.acutecoder.kmp.helper.executor.RegenerateResClassExecutor
import com.acutecoder.kmp.helper.util.Constants
import com.acutecoder.kmp.preference.PluginPreference
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project

class RegenerateResClassAction : AnAction() {

    private val regenerateResClassExecutor = RegenerateResClassExecutor()
    private val notificationGroup = NotificationGroupManager.getInstance()
        .getNotificationGroup(Constants.REGENERATE_RES_CLASS)

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        ProgressManager.getInstance().run(object : Task.Modal(project, Constants.REGENERATE_RES_CLASS, false) {

            override fun run(indicator: ProgressIndicator) {
                regenerateResClassExecutor.execute(project)
            }

            override fun onThrowable(error: Throwable) {
                project.showNotification(
                    title = title,
                    message = "Error while generating Res class: ${error.message}",
                    type = NotificationType.ERROR
                )
            }
        })
    }

    override fun update(event: AnActionEvent) {
        if (!PluginPreference.getInstance().state.regenerateResClassFeatureEnabled) {
            event.presentation.isEnabledAndVisible = false
            return
        }

        event.presentation.isEnabledAndVisible = isAvailable(event.dataContext)
    }

    private fun isAvailable(dataContext: DataContext): Boolean {
        val location = CommonDataKeys.VIRTUAL_FILE.getData(dataContext)

        return location != null && (location.name.contains("resource", true) || location.name.equals("res", true))
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    private fun Project.showNotification(
        title: String,
        message: String,
        type: NotificationType = NotificationType.INFORMATION,
    ) {
        notificationGroup.createNotification(title, message, type).notify(this)
    }
}
