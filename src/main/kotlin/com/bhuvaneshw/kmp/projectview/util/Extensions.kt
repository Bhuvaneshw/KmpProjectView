package com.bhuvaneshw.kmp.projectview.util

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.jetbrains.rd.util.LogLevel
import com.jetbrains.rd.util.Logger
import java.util.regex.Pattern

fun log(message: Any?) {
    Logger.root.log(LogLevel.Warn, "$message", null)
}

fun logError(message: Any?) {
    Logger.root.log(LogLevel.Error, "$message", null)
}

fun PsiDirectory.canBeSkipped(config: Config): Boolean {
    return config.preference().folderIgnoreKeywords
        ?.split(",")
        ?.filter { it.isNotBlank() }
        ?.any { Pattern.matches(it, name) } ?: false
}

fun PsiFile.canBeSkipped(config: Config): Boolean {
    return config.preference().fileIgnoreKeywords
        ?.split(",")
        ?.filter { it.isNotBlank() }
        ?.any { Pattern.matches(it, name) } ?: false
}

fun PsiFile.isGradleFile(): Boolean {
    val name = name.lowercase()
    return name in listOf(
        Constants.File.BUILD_GRADLE_KTS,
        Constants.File.BUILD_GRADLE,
        Constants.File.GRADLEW,
        Constants.File.GRADLEW_BAT,
        Constants.File.SETTINGS_GRADLE_KTS,
        Constants.File.SETTINGS_GRADLE,
        Constants.File.GRADLE_PROPERTIES,
        Constants.File.LOCAL_PROPERTIES,
        Constants.File.GRADLE_WRAPPER_PROPERTIES,
    ) || name.endsWith(Constants.Suffix.VERSIONS_TOML) || name.endsWith(Constants.Suffix.PRO)
}

fun PsiDirectory.findSrcDirectory(): PsiDirectory? {
    return children.find { it is PsiDirectory && it.name == Constants.Folder.SRC }.let {
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

fun PsiDirectory.isSourceSet(): Boolean {
    return children.any { it is PsiDirectory && it.name == Constants.Folder.KOTLIN }
}
