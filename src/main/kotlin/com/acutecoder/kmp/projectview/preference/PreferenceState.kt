package com.acutecoder.kmp.projectview.preference

import com.intellij.openapi.components.BaseState

class PreferenceState : BaseState() {
    var isTooltipEnabled by property(true)
    var showCommonMainOnTop by property(true)
    var differentiateCommonMain by property(true)
    var showKmpModuleSideText by property(true)
}
