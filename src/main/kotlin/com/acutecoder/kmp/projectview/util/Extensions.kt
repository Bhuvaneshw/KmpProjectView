package com.acutecoder.kmp.projectview.util

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.jetbrains.rd.util.LogLevel
import com.jetbrains.rd.util.Logger

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

fun PsiElement.isKmpSubModule(): ModuleType {
    if (this !is PsiDirectory) return ModuleType.Unknown

    var src = false
    var kmp = false
    var cmp = false

    this.children.forEach {
        if (it is PsiFileSystemItem) log(it.virtualFile.name)
        if (it is PsiDirectory && it.virtualFile.name == "src")
            src = true
        if (it is PsiFile && (it.virtualFile.name == "build.gradle.kts" || it.virtualFile.name == "build.gradle")) {
            kmp = it.text.contains("libs.plugins.kotlinMultiplatform")
            cmp = it.text.contains("libs.plugins.jetbrainsCompose")
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
