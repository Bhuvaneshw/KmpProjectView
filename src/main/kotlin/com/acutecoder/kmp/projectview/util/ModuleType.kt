package com.acutecoder.kmp.projectview.util

enum class ModuleType(val displayName: String) {
    KMP("Kotlin Multiplatform Project"),
    CMP("Compose Multiplatform Project"),
    Unknown("Unknown"),
    NotAModule("Not a"),
}
