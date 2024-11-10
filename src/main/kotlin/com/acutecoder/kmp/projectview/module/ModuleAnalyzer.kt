package com.acutecoder.kmp.projectview.module

import com.acutecoder.kmp.projectview.preference.PluginPreference
import com.acutecoder.kmp.projectview.preference.PreferenceState
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

private inline val preference: PreferenceState
    get() = PluginPreference.getInstance().state

fun PsiElement.moduleType(): ModuleType {
    if (this !is PsiDirectory)
        return ModuleType.Unknown

    return this.children.analyzeModuleType()
}

fun Array<PsiElement>.analyzeModuleType(): ModuleType {
    var src = false
    var kmp = false
    var cmp = false

    this.forEach { file ->
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
    } else ModuleType.Unknown
}

fun PsiDirectory.hasAtLeastOneKmpOrCmpModule() = children.any { it.moduleType().isKmpOrCmp() }
