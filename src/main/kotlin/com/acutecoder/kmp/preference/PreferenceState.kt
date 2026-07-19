package com.acutecoder.kmp.preference

import com.intellij.openapi.components.BaseState

class PreferenceState : BaseState() {
    var showKmpModuleSideText by property(true)
    var showCommonMainOnTop by property(true)
    var differentiateCommonMain by property(true)
    var showModuleNameOnly by property(true)
    var isTooltipEnabled by property(true)
    var groupOtherMain by property(false)
    var unGroupCommonMain by property(false)
    var separateNodeForSubstitutedProject by property(true)
    var useGradleProjectNameForSubstitutedProject by property(false)
    var splitGradleAndOther by property(0)
    var kmpKeywords by string("KotlinMultiplatformExtension")
    var cmpKeywords by string("ComposeExtension,compose")
    var ktorKeywords by string("KtorExtension,ktor")
    var commonMainKeywords by string("commonMain")
    var folderIgnoreKeywords by string("\\..*,build,projectFilesBackup")
    var fileIgnoreKeywords by string("")

    val kmpKeywordList: List<String> get() = kmpKeywords.splitKeyword()
    val cmpKeywordList: List<String> get() = cmpKeywords.splitKeyword()
    val ktorKeywordList: List<String> get() = ktorKeywords.splitKeyword()
    val commonMainKeywordList: List<String> get() = commonMainKeywords.splitKeyword()

    var regenerateResClassFeatureEnabled by property(true)
    var autoRegenerateResClassFeatureEnabled by property(true)
    var composeVectorConverterFeatureEnabled by property(false)
}

@Suppress("NOTHING_TO_INLINE")
private inline fun String?.splitKeyword() = this
    ?.split(",")
    ?.map(String::trim)
    ?.filter(String::isNotEmpty)
    ?: emptyList()
