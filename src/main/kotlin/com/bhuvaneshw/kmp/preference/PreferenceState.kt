package com.bhuvaneshw.kmp.preference

import com.intellij.openapi.components.BaseState

class PreferenceState : BaseState() {
    var showKmpModuleSideText by property(true)
    var showCommonMainOnTop by property(true)
    var differentiateCommonMain by property(true)
    var showSharedModuleOnTop by property(true)
    var showModuleNameOnly by property(true)
    var isTooltipEnabled by property(true)
    var groupOtherMain by property(false)
    var unGroupCommonMain by property(false)
    var separateNodeForSubstitutedProject by property(true)
    var useGradleProjectNameForSubstitutedProject by property(false)
    var splitGradleAndOther by property(0)

    var kmpKeywords by string("[KotlinMultiplatformExtension]")
    var cmpKeywords by string("[ComposeExtension],(compose)")
    var androidKeywords by string("[ApplicationExtension],[LibraryExtension],[AndroidComponentsExtension],(android)")
    var desktopKeywords by string("[KotlinJvmProjectExtension],[ComposeHotReloadExtension],(compose)")
    var webKeywords by string("[BinaryenExtension],(kotlinWasm),(kotlinNode)")
    var ktorKeywords by string("[KtorExtension],(ktor)")
    var iosFileMarkers by string("*.xcodeproj")

    var sharedModuleKeywords by string("shared")
    var commonMainKeywords by string("commonMain")
    var folderIgnoreKeywords by string("\\..*,build,projectFilesBackup")
    var fileIgnoreKeywords by string("")

    val kmpKeywordList: List<String> get() = kmpKeywords.splitKeywords()
    val cmpKeywordList: List<String> get() = cmpKeywords.splitKeywords()
    val ktorKeywordList: List<String> get() = ktorKeywords.splitKeywords()
    val androidKeywordList: List<String> get() = androidKeywords.splitKeywords()
    val iosFileMarkerList: List<String> get() = iosFileMarkers.splitKeywords()
    val desktopKeywordList: List<String> get() = desktopKeywords.splitKeywords()
    val webKeywordList: List<String> get() = webKeywords.splitKeywords()
    val sharedModuleKeywordList: List<String> get() = sharedModuleKeywords.splitKeywords()
    val commonMainKeywordList: List<String> get() = commonMainKeywords.splitKeywords()

    var regenerateResClassFeatureEnabled by property(true)
    var autoRegenerateResClassFeatureEnabled by property(true)
    var composeVectorConverterFeatureEnabled by property(false)
}

@Suppress("NOTHING_TO_INLINE")
private inline fun String?.splitKeywords() = this
    ?.split(",")
    ?.map(String::trim)
    ?.filter(String::isNotEmpty)
    ?: emptyList()
