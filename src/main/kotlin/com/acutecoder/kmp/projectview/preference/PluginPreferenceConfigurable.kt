package com.acutecoder.kmp.projectview.preference

import com.acutecoder.kmp.projectview.util.Constants
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.util.ui.FormBuilder
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JTextField

class PluginPreferenceConfigurable : Configurable {

    private lateinit var isTooltipEnabledCheckBox: JCheckBox
    private lateinit var showCommonMainOnTopCheckBox: JCheckBox
    private lateinit var differentiateCommonMainCheckBox: JCheckBox
    private lateinit var showModuleNameOnlyCheckBox: JCheckBox
    private lateinit var showKmpSideTextCheckBox: JCheckBox
    private lateinit var groupOtherMainCheckBox: JCheckBox
    private lateinit var unGroupCommonMainCheckBox: JCheckBox
    private lateinit var splitGradleAndOtherComboBox: ComboBox<String>
    private lateinit var kmpKeywordsField: JTextField
    private lateinit var cmpKeywordsField: JTextField
    private lateinit var commonMainKeywordsField: JTextField
    private lateinit var folderIgnoreField: JTextField
    private lateinit var fileIgnoreField: JTextField
    private val gap = 20

    override fun getDisplayName(): String {
        return Constants.SETTINGS_TAB_NAME
    }

    override fun createComponent(): JComponent {
        showKmpSideTextCheckBox = JCheckBox("Show module type (KMP/CMP)")
        showCommonMainOnTopCheckBox = JCheckBox("Show commonMain on top")
        differentiateCommonMainCheckBox = JCheckBox("Highlight commonMain")
        showModuleNameOnlyCheckBox = JCheckBox("Hide extra info for source set")
        isTooltipEnabledCheckBox = JCheckBox("Show Tooltip")
        groupOtherMainCheckBox = JCheckBox("Group everything except commonMain")
        unGroupCommonMainCheckBox = JCheckBox("Ungroup commonMain")

        splitGradleAndOtherComboBox = ComboBox(arrayOf("Module Level", "All Level", "None"))

        kmpKeywordsField = JTextField()
        cmpKeywordsField = JTextField()
        commonMainKeywordsField = JTextField()
        folderIgnoreField = JTextField()
        fileIgnoreField = JTextField()

        return FormBuilder.createFormBuilder().apply {
            addComponent(showKmpSideTextCheckBox)
            addComponent(showCommonMainOnTopCheckBox)
            addComponent(differentiateCommonMainCheckBox)
            addComponent(showModuleNameOnlyCheckBox)
            addComponent(isTooltipEnabledCheckBox)
            addComponent(groupOtherMainCheckBox)
            addComponent(unGroupCommonMainCheckBox)

            addLabeledComponent(JLabel("Split Gradle and Other files in"), splitGradleAndOtherComboBox, gap, false)

            addLabeledComponent(JLabel("commonMain Identifiers"), commonMainKeywordsField, gap, true)
            addLabeledComponent(JLabel("KMP Identifiers"), kmpKeywordsField, true)
            addLabeledComponent(JLabel("CMP Identifiers"), cmpKeywordsField, true)
            addLabeledComponent(JLabel("Folder ignore pattern"), folderIgnoreField, true)
            addLabeledComponent(JLabel("File ignore pattern"), fileIgnoreField, true)

            addComponent(JLabel("Hint: You can add multiple identifiers by separating them with commas."), gap)
            addComponent(JLabel("If any KMP/CMP identifier matches with the build.gradle file,"))
            addComponent(JLabel("the corresponding module will be determined."))
            addComponent(JLabel("If changes are not applied, try restarting the IDE."))
            addComponent(JLabel("Enable both \"Group everything except commonMain\" and \"Ungroup commonMain\""))
            addComponent(JLabel("for better appearance."))

            reset()
        }.panel
    }

    override fun isModified(): Boolean {
        val settings = PluginPreference.getInstance().state

        return showKmpSideTextCheckBox.isSelected != settings.showKmpModuleSideText ||
                showCommonMainOnTopCheckBox.isSelected != settings.showCommonMainOnTop ||
                differentiateCommonMainCheckBox.isSelected != settings.differentiateCommonMain ||
                showModuleNameOnlyCheckBox.isSelected != settings.showModuleNameOnly ||
                isTooltipEnabledCheckBox.isSelected != settings.isTooltipEnabled ||
                groupOtherMainCheckBox.isSelected != settings.groupOtherMain ||
                unGroupCommonMainCheckBox.isSelected != settings.unGroupCommonMain ||
                splitGradleAndOtherComboBox.selectedIndex != settings.splitGradleAndOther ||
                kmpKeywordsField.text != settings.kmpKeywords ||
                cmpKeywordsField.text != settings.cmpKeywords ||
                commonMainKeywordsField.text != settings.commonMainKeywords ||
                folderIgnoreField.text != settings.folderIgnoreKeywords ||
                fileIgnoreField.text != settings.fileIgnoreKeywords
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
                unGroupCommonMain = unGroupCommonMainCheckBox.isSelected
                splitGradleAndOther = splitGradleAndOtherComboBox.selectedIndex
                kmpKeywords = kmpKeywordsField.text
                cmpKeywords = cmpKeywordsField.text
                commonMainKeywords = commonMainKeywordsField.text
                folderIgnoreKeywords = folderIgnoreField.text
                fileIgnoreKeywords = fileIgnoreField.text
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
        unGroupCommonMainCheckBox.isSelected = settings.unGroupCommonMain
        splitGradleAndOtherComboBox.selectedIndex = settings.splitGradleAndOther
        kmpKeywordsField.text = settings.kmpKeywords
        cmpKeywordsField.text = settings.cmpKeywords
        commonMainKeywordsField.text = settings.commonMainKeywords
        folderIgnoreField.text = settings.folderIgnoreKeywords
        fileIgnoreField.text = settings.fileIgnoreKeywords
    }

}
