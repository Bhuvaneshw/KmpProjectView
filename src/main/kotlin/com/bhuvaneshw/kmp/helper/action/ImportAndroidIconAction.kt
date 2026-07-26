package com.bhuvaneshw.kmp.helper.action

import com.android.tools.idea.npw.assetstudio.ui.IconPickerDialog
import com.bhuvaneshw.kmp.helper.executor.ComposeVectorConverterExecutor
import com.bhuvaneshw.kmp.preference.PluginPreference
import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

class ImportAndroidIconAction : AnAction() {

    private val composeVectorConverterExecutor = ComposeVectorConverterExecutor()

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val selectedFile = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        
        val targetComposeDir = findComposeDrawableDir(selectedFile) ?: selectedFile

        val picker = IconPickerDialog(null)
        if (picker.showAndGet()) {
            val selectedIcon = picker.selectedIcon ?: return
            val iconUrl = selectedIcon.url ?: return
            val defaultName = "ic_${selectedIcon.name.lowercase().replace(" ", "_")}"
            
            val fileName = Messages.showInputDialog(
                project,
                "Enter icon name:",
                "Compose Vector Asset",
                null,
                defaultName,
                null
            ) ?: return

            val finalFileName = if (fileName.endsWith(".xml")) fileName else "$fileName.xml"

            try {
                val xmlContent = iconUrl.readText()
                val convertedContent = composeVectorConverterExecutor.modify(xmlContent)

                runWriteAction {
                    var newFile = targetComposeDir.findChild(finalFileName)
                    if (newFile == null) {
                        newFile = targetComposeDir.createChildData(this, finalFileName)
                    }
                    VfsUtil.saveText(newFile, convertedContent)

                    val psiFile = PsiManager.getInstance(project).findFile(newFile)
                    if (psiFile != null) {
                        ReformatCodeProcessor(project, psiFile, null, false).run()
                    }
                }
            } catch (e: Exception) {
                Messages.showErrorDialog(project, "Failed to import icon: ${e.message}", "Error")
            }
        }
    }

    override fun update(event: AnActionEvent) {
        val state = PluginPreference.getInstance().state
        if (!state.composeVectorConverterFeatureEnabled || !state.composeVectorAssetFeatureEnabled) {
            event.presentation.isEnabledAndVisible = false
            return
        }

        val project = event.project
        val selectedFile = event.getData(CommonDataKeys.VIRTUAL_FILE)

        val isVisible = if (project != null && selectedFile != null && selectedFile.isDirectory) {
            val name = selectedFile.name
            val parentName = selectedFile.parent?.name
            name == "composeResources" || name == "drawable" ||
                    parentName == "composeResources" || parentName == "drawable"
        } else false

        event.presentation.isEnabledAndVisible = isVisible
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    private fun findComposeDrawableDir(file: VirtualFile): VirtualFile? {
        // Try searching downwards first if a folder is selected
        if (file.isDirectory) {
            val found = searchDown(file)
            if (found != null) return found
        }

        // Search upwards for module root, then look for commonMain
        var current: VirtualFile? = file
        while (current != null) {
            val src = current.findChild("src")
            if (src != null && src.isDirectory) {
                val commonMain = src.findChild("commonMain")
                if (commonMain != null && commonMain.isDirectory) {
                    val res = commonMain.findChild("composeResources")
                    if (res != null && res.isDirectory) {
                        return res.findChild("drawable") ?: res
                    }
                }
            }
            current = current.parent
        }
        return null
    }

    private fun searchDown(file: VirtualFile): VirtualFile? {
        if (file.name == "drawable" && file.parent?.name == "composeResources") return file
        if (file.name == "composeResources") return file.findChild("drawable") ?: file

        for (child in file.children) {
            if (child.isDirectory) {
                val found = searchDown(child)
                if (found != null) return found
            }
        }
        return null
    }
}
