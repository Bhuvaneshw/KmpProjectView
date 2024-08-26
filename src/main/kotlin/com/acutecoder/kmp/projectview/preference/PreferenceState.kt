package com.acutecoder.kmp.projectview.preference

import com.intellij.openapi.components.BaseState

class PreferenceState : BaseState() {
    var showKmpModuleSideText by property(true)
    var showCommonMainOnTop by property(true)
    var differentiateCommonMain by property(true)
    var showModuleNameOnly by property(true)
    var isTooltipEnabled by property(true)
    var groupOtherMain by property(false)
    var kmpKeywords by string("libs.plugins.kotlinMultiplatform,libs.plugins.multiplatform")
    var cmpKeywords by string("libs.plugins.jetbrainsCompose,libs.plugins.compose")
    var commonMainKeywords by string("commonMain")

    val kmpKeywordList: List<String> get() = kmpKeywords?.split(",") ?: emptyList()
    val cmpKeywordList: List<String> get() = cmpKeywords?.split(",") ?: emptyList()
    val commonMainKeywordList: List<String> get() = commonMainKeywords?.split(",") ?: emptyList()
}
