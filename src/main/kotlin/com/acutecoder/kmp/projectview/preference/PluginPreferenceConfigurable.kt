package com.acutecoder.kmp.projectview.preference

import com.acutecoder.kmp.projectview.util.Constants
import com.acutecoder.kmp.projectview.util.ScrollPane
import com.intellij.openapi.options.Configurable
import javax.swing.*

class PluginPreferenceConfigurable : Configurable {

    private lateinit var settingsPanel: JPanel
    private lateinit var isTooltipEnabledCheckBox: JCheckBox
    private lateinit var showCommonMainOnTopCheckBox: JCheckBox
    private lateinit var differentiateCommonMainCheckBox: JCheckBox
    private lateinit var showModuleNameOnlyCheckBox: JCheckBox
    private lateinit var showKmpSideTextCheckBox: JCheckBox
    private lateinit var kmpKeywordsField: JTextField
    private lateinit var cmpKeywordsField: JTextField

    override fun getDisplayName(): String {
        return Constants.SETTINGS_TAB_NAME
    }

    override fun createComponent(): JComponent {
        settingsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        showKmpSideTextCheckBox = JCheckBox("Show module type (KMP/CMP)")
        showCommonMainOnTopCheckBox = JCheckBox("Show commonMain on top")
        differentiateCommonMainCheckBox = JCheckBox("Differentiate commonMain")
        showModuleNameOnlyCheckBox = JCheckBox("Hide extra info for kmp/cmp source set (sub module)")
        isTooltipEnabledCheckBox = JCheckBox("Enable Tooltips")
        kmpKeywordsField = JTextField()
        cmpKeywordsField = JTextField()
        val kmpScrollPane = ScrollPane(kmpKeywordsField)
        val cmpScrollPane = ScrollPane(cmpKeywordsField)

        settingsPanel.add(showKmpSideTextCheckBox)
        settingsPanel.add(showCommonMainOnTopCheckBox)
        settingsPanel.add(differentiateCommonMainCheckBox)
        settingsPanel.add(showModuleNameOnlyCheckBox)
        settingsPanel.add(isTooltipEnabledCheckBox)
        settingsPanel.add(Box.createVerticalStrut(20))

        settingsPanel.add(JLabel("KMP Identifiers"))
        settingsPanel.add(kmpScrollPane)

        settingsPanel.add(JLabel("CMP Identifiers"))
        settingsPanel.add(cmpScrollPane)
        settingsPanel.add(JLabel("You can add multiple identifiers by separating them with commas."))
        settingsPanel.add(JLabel("If any identifier matches with the build.gradle file, the corresponding module will be determined."))

        reset()

        return settingsPanel
    }

    override fun isModified(): Boolean {
        val settings = PluginPreference.getInstance().state

        return showKmpSideTextCheckBox.isSelected != settings.showKmpModuleSideText ||
                showCommonMainOnTopCheckBox.isSelected != settings.showCommonMainOnTop ||
                differentiateCommonMainCheckBox.isSelected != settings.differentiateCommonMain ||
                showModuleNameOnlyCheckBox.isSelected != settings.showModuleNameOnly ||
                isTooltipEnabledCheckBox.isSelected != settings.isTooltipEnabled ||
                kmpKeywordsField.text != settings.kmpKeywords ||
                cmpKeywordsField.text != settings.cmpKeywords
    }

    override fun apply() {
        val settings = PluginPreference.getInstance()

        settings.loadState(
            settings.state.apply {
                showKmpModuleSideText = showKmpSideTextCheckBox.isSelected
                showCommonMainOnTop = showCommonMainOnTopCheckBox.isSelected
                differentiateCommonMain = differentiateCommonMainCheckBox.isSelected
                showModuleNameOnly = showModuleNameOnlyCheckBox.isSelected
                isTooltipEnabled = isTooltipEnabledCheckBox.isSelected
                kmpKeywords = kmpKeywordsField.text
                cmpKeywords = cmpKeywordsField.text
            }
        )

        PreferenceObserver.emit()
    }

    override fun reset() {
        val settings = PluginPreference.getInstance().state

        showKmpSideTextCheckBox.isSelected = settings.showKmpModuleSideText
        showCommonMainOnTopCheckBox.isSelected = settings.showCommonMainOnTop
        differentiateCommonMainCheckBox.isSelected = settings.differentiateCommonMain
        showModuleNameOnlyCheckBox.isSelected = settings.showModuleNameOnly
        isTooltipEnabledCheckBox.isSelected = settings.isTooltipEnabled
        kmpKeywordsField.text = settings.kmpKeywords
        cmpKeywordsField.text = settings.cmpKeywords
    }

//    override fun disposeUIResources() {
//        settingsPanel = null
//    }

}
