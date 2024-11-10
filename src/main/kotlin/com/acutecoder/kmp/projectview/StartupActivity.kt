package com.acutecoder.kmp.projectview

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StartupActivity : ProjectActivity, Disposable {

    override suspend fun execute(project: Project) {
        VirtualFileManager.getInstance().addAsyncFileListener({
            object : AsyncFileListener.ChangeApplier {
                override fun afterVfsChange() {
                    CoroutineScope(Dispatchers.Default).launch {
                        refreshProjectView(project)
                    }
                }
            }
        }, this)
    }

    private fun refreshProjectView(project: Project) {
        ProjectView.getInstance(project).refresh()
    }

    override fun dispose() {}

}

