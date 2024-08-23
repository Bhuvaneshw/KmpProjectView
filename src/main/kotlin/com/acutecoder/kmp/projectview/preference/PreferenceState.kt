package com.acutecoder.kmp.projectview.preference

import com.intellij.openapi.components.BaseState

class PreferenceState : BaseState() {
    var showKmpModuleSideText by property(true)
    var showCommonMainOnTop by property(true)
    var differentiateCommonMain by property(true)
    var showModuleNameOnly by property(true)
    var isTooltipEnabled by property(true)
}
