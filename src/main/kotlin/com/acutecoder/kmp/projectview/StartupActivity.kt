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

    private var currentProject: Project? = null

    override suspend fun execute(project: Project) {
        currentProject = project

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

    override fun dispose() {
        currentProject = null
    }

}
