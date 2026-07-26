package com.acutecoder.kmp.projectview

import com.intellij.ide.BrowserUtil
import com.intellij.ide.projectView.ProjectView
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StartupActivity : ProjectActivity, Disposable {

    private var currentProject: Project? = null

    override suspend fun execute(project: Project) {
        currentProject = project

        showDeprecationNotification(project)

        VirtualFileManager.getInstance().addAsyncFileListener({
            object : AsyncFileListener.ChangeApplier {
                override fun afterVfsChange() {
                    currentProject?.let { currentProject ->
                        CoroutineScope(Dispatchers.Default).launch {
                            ProjectView.getInstance(currentProject).refresh()
                        }
                    }
                }
            }
        }, this)
    }

    private fun showDeprecationNotification(project: Project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Regenerate Res Class") // Using existing group or generic one
            .createNotification(
                "Plugin deprecated",
                "KMP Project View (com.acutecoder) is deprecated. Please migrate to the new version for latest updates.",
                NotificationType.WARNING
            )
            .addAction(NotificationAction.createSimple("Install new version") {
                BrowserUtil.browse("https://plugins.jetbrains.com/plugin/25442-kmp-project-view")
            })
            .notify(project)
    }

    override fun dispose() {
        currentProject = null
    }

}
