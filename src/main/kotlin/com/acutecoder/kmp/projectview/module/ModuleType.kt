package com.acutecoder.kmp.projectview.module

enum class ModuleType(val displayName: String) {
    KMP("Kotlin Multiplatform Project"),
    CMP("Compose Multiplatform Project"),
    Server("Ktor Server Project"),
    Unknown("Unknown");

    fun isKmpOrCmp() = this == KMP || this == CMP
}
