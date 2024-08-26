package com.acutecoder.kmp.projectview.util

import com.acutecoder.kmp.projectview.preference.PluginPreference
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.util.maximumHeight
import com.intellij.ui.util.maximumWidth
import com.jetbrains.rd.util.LogLevel
import com.jetbrains.rd.util.Logger
import javax.swing.JScrollPane
import javax.swing.JTextField

fun log(message: Any?) {
    Logger.root.log(LogLevel.Warn, "$message", null)
}

fun logError(message: Any?) {
    Logger.root.log(LogLevel.Error, "$message", null)
}

fun PsiDirectory.canBeSkipped(): Boolean {
    return name.startsWith(".") || name == "build" || name == "projectFilesBackup"
}

fun PsiFile.isGradleFile(): Boolean {
    return name.lowercase() in listOf(
        "build.gradle.kts",
        "build.gradle",
        "gradlew",
        "gradlew.bat",
        "settings.gradle.kts",
        "settings.gradle",
        "gradle.properties",
    )
}

fun PsiElement.moduleType(): ModuleType {
    if (this !is PsiDirectory) return ModuleType.Unknown

    val preference = PluginPreference.getInstance().state
    var src = false
    var kmp = false
    var cmp = false

    this.children.forEach { file ->
        if (file is PsiDirectory && file.virtualFile.name == "src")
            src = true
        if (file is PsiFile && (file.virtualFile.name == "build.gradle.kts" || file.virtualFile.name == "build.gradle")) {
            kmp = preference.kmpKeywordList.any { file.text.contains(it.trim()) }
            cmp = preference.cmpKeywordList.any { file.text.contains(it.trim()) }
        }
    }

    return if (src) {
        when {
            kmp && cmp -> ModuleType.CMP
            kmp -> ModuleType.KMP
            else -> ModuleType.Unknown
        }
    } else ModuleType.NotAModule
}

@Suppress("nothing_to_inline", "functionName")
inline fun ScrollPane(jTextField: JTextField) = JBScrollPane(jTextField).apply {
    horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
    verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_NEVER
    maximumHeight = 40
    maximumWidth = 800
}

fun PsiDirectory.findSrcDirectory(): PsiDirectory? {
    return children.find { it is PsiDirectory && it.name == "src" }.let {
        if (it is PsiDirectory) it
        else null
    }
}

fun VirtualFile.isAncestorOf(file: VirtualFile?): Boolean {
    var currentFile = file
    while (currentFile != null) {
        if (currentFile == this) {
            return true
        }
        currentFile = currentFile.parent
    }
    return false
}

infix fun PsiFileSystemItem.matches(identifiers: List<String>): Boolean {
    return name in identifiers
}
