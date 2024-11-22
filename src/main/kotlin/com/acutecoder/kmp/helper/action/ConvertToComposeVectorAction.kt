package com.acutecoder.kmp.helper.action

import com.acutecoder.kmp.helper.executor.ComposeVectorConverterExecutor
import com.acutecoder.kmp.helper.util.Constants
import com.acutecoder.kmp.preference.PluginPreference
import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiManager

class ConvertToComposeVectorAction : AnAction() {

    private val notificationGroup = NotificationGroupManager.getInstance()
        .getNotificationGroup(Constants.COMPOSE_VECTOR_CONVERTER)
    private val composeVectorConverterExecutor = ComposeVectorConverterExecutor()

    override fun actionPerformed(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val project = event.project ?: return

        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, Constants.COMPOSE_VECTOR_CONVERTER, false) {

                override fun run(indicator: ProgressIndicator) {
                    indicator.text = "Converting ${file.name}..."
                    indicator.isIndeterminate = true

                    ApplicationManager.getApplication().invokeAndWait {
                        runReadAction {
                            val fileContent = VfsUtil.loadText(file)

                            runWriteAction {
                                val modifiedContent = composeVectorConverterExecutor.modify(fileContent)
                                VfsUtil.saveText(file, modifiedContent)
                            }
                        }

                        val reformatCodeProcessor = ReformatCodeProcessor(
                            project,
                            PsiManager.getInstance(project).findFile(file),
                            null,
                            false
                        )
                        reformatCodeProcessor.run()
                    }

                    indicator.text = "Conversion completed."
                }

                override fun onThrowable(error: Throwable) {
                    project.showNotification(title, "Error during conversion: ${error.message}", NotificationType.ERROR)
                }

                override fun onSuccess() {
                    super.onSuccess()
                    project.showNotification(title, "Converted successfully")
                }
            })
    }

    override fun update(event: AnActionEvent) {
        if (!PluginPreference.getInstance().state.composeVectorConverterFeatureEnabled) {
            event.presentation.isEnabledAndVisible = false
            return
        }

        val file = event.getData(PlatformDataKeys.VIRTUAL_FILE)
        val isEnabled = composeVectorConverterExecutor.isConvertableFile(file)
        event.presentation.isEnabledAndVisible = isEnabled
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
