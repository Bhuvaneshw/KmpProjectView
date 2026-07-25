package com.acutecoder.kmp.projectview.module

enum class ModuleType(val description: String) {
    KMP("Kotlin Multiplatform Project"),
    CMP("Compose Multiplatform Project"),
    Ktor("Ktor Server Project"),
    Android("Android Project"),
    IOS("iOS Project"),
    Desktop("Desktop Project"),
    Web("Web Project"),
    Unknown("Unknown");

    fun isKmpOrCmp() = this == KMP || this == CMP
    fun isGradleModule() = this != Unknown && this != IOS
    override fun toString(): String = if (this == IOS) "iOS" else super.toString()
}
