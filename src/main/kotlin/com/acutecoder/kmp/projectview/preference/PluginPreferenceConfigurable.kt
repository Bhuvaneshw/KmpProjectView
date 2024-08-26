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
    private lateinit var groupOtherMainCheckBox: JCheckBox
    private lateinit var kmpKeywordsField: JTextField
    private lateinit var cmpKeywordsField: JTextField
    private lateinit var commonMainKeywordsField: JTextField

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
        showModuleNameOnlyCheckBox = JCheckBox("Hide extra info for source set")
        isTooltipEnabledCheckBox = JCheckBox("Enable Tooltips")
        groupOtherMainCheckBox = JCheckBox("Group everything except commonMain")
        kmpKeywordsField = JTextField()
        cmpKeywordsField = JTextField()
        commonMainKeywordsField = JTextField()
        val kmpScrollPane = ScrollPane(kmpKeywordsField)
        val cmpScrollPane = ScrollPane(cmpKeywordsField)
        val commonScrollPane = ScrollPane(commonMainKeywordsField)

        settingsPanel.add(showKmpSideTextCheckBox)
        settingsPanel.add(showCommonMainOnTopCheckBox)
        settingsPanel.add(differentiateCommonMainCheckBox)
        settingsPanel.add(showModuleNameOnlyCheckBox)
        settingsPanel.add(isTooltipEnabledCheckBox)
        settingsPanel.add(groupOtherMainCheckBox)
        settingsPanel.add(Box.createVerticalStrut(20))

        settingsPanel.add(JLabel("commonMain Identifiers"))
        settingsPanel.add(commonScrollPane)
        settingsPanel.add(Box.createVerticalStrut(10))

        settingsPanel.add(JLabel("KMP Identifiers"))
        settingsPanel.add(kmpScrollPane)
        settingsPanel.add(Box.createVerticalStrut(10))

        settingsPanel.add(JLabel("CMP Identifiers"))
        settingsPanel.add(cmpScrollPane)
        settingsPanel.add(Box.createVerticalStrut(10))

        settingsPanel.add(JLabel("Hint: You can add multiple identifiers by separating them with commas."))
        settingsPanel.add(JLabel("If any KMP/CMP identifier matches with the build.gradle file,"))
        settingsPanel.add(JLabel("the corresponding module will be determined."))

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
                groupOtherMainCheckBox.isSelected != settings.groupOtherMain ||
                kmpKeywordsField.text != settings.kmpKeywords ||
                cmpKeywordsField.text != settings.cmpKeywords ||
                commonMainKeywordsField.text != settings.commonMainKeywords
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
                groupOtherMain = groupOtherMainCheckBox.isSelected
                kmpKeywords = kmpKeywordsField.text
                cmpKeywords = cmpKeywordsField.text
                commonMainKeywords = commonMainKeywordsField.text
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
        groupOtherMainCheckBox.isSelected = settings.groupOtherMain
        kmpKeywordsField.text = settings.kmpKeywords
        cmpKeywordsField.text = settings.cmpKeywords
        commonMainKeywordsField.text = settings.commonMainKeywords
    }

//    override fun disposeUIResources() {
//        settingsPanel = null
//    }

}
