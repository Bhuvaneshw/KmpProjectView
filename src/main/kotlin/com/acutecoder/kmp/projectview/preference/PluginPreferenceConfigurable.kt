package com.acutecoder.kmp.projectview.preference

import com.acutecoder.kmp.projectview.util.Constants
import com.intellij.openapi.options.Configurable
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

class PluginPreferenceConfigurable : Configurable {

    private var settingsPanel: JPanel? = null
    private var isTooltipEnabledCheckBox: JCheckBox? = null
    private var showCommonMainOnTopCheckBox: JCheckBox? = null
    private var differentiateCommonMainCheckBox: JCheckBox? = null
    private var showKmpSideTextCheckBox: JCheckBox? = null

    override fun getDisplayName(): String {
        return Constants.SETTINGS_TAB_NAME
    }

    override fun createComponent(): JComponent? {
        settingsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        showKmpSideTextCheckBox = JCheckBox("Show module type (KMP/CMP)")
        showCommonMainOnTopCheckBox = JCheckBox("Show commonMain on top")
        differentiateCommonMainCheckBox = JCheckBox("Differentiate commonMain")
        isTooltipEnabledCheckBox = JCheckBox("Enable Tooltips")

        settingsPanel!!.add(showKmpSideTextCheckBox)
        settingsPanel!!.add(showCommonMainOnTopCheckBox)
        settingsPanel!!.add(differentiateCommonMainCheckBox)
        settingsPanel!!.add(isTooltipEnabledCheckBox)

        reset()

        return settingsPanel
    }

    override fun isModified(): Boolean {
        val settings = PluginPreference.getInstance().state

        return showKmpSideTextCheckBox?.isSelected != settings.showKmpModuleSideText ||
                showCommonMainOnTopCheckBox?.isSelected != settings.showCommonMainOnTop ||
                differentiateCommonMainCheckBox?.isSelected != settings.differentiateCommonMain ||
                isTooltipEnabledCheckBox?.isSelected != settings.isTooltipEnabled
    }

    override fun apply() {
        val settings = PluginPreference.getInstance()

        settings.loadState(
            settings.state.apply {
                showKmpModuleSideText = showKmpSideTextCheckBox?.isSelected ?: true
                showCommonMainOnTop = showCommonMainOnTopCheckBox?.isSelected ?: true
                differentiateCommonMain = differentiateCommonMainCheckBox?.isSelected ?: true
                isTooltipEnabled = isTooltipEnabledCheckBox?.isSelected ?: true
            }
        )

        PreferenceObserver.emit()
    }

    override fun reset() {
        val settings = PluginPreference.getInstance().state

        showKmpSideTextCheckBox?.isSelected = settings.showKmpModuleSideText
        showCommonMainOnTopCheckBox?.isSelected = settings.showCommonMainOnTop
        differentiateCommonMainCheckBox?.isSelected = settings.differentiateCommonMain
        isTooltipEnabledCheckBox?.isSelected = settings.isTooltipEnabled
    }

    override fun disposeUIResources() {
        settingsPanel = null
    }

}
