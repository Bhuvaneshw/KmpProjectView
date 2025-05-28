package com.acutecoder.kmp.preference

import com.acutecoder.kmp.projectview.util.Constants
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationManagerEx
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.FormBuilder
import java.awt.event.ItemEvent
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
    private lateinit var ktorKeywordsField: JTextField
    private lateinit var commonMainKeywordsField: JTextField
    private lateinit var folderIgnoreField: JTextField
    private lateinit var fileIgnoreField: JTextField
    private lateinit var regenerateResClassCheckBox: JCheckBox
    private lateinit var autoRegenerateResClassCheckBox: JCheckBox
    private lateinit var composeVectorConverterCheckBox: JCheckBox
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
        ktorKeywordsField = JTextField()
        commonMainKeywordsField = JTextField()
        folderIgnoreField = JTextField()
        fileIgnoreField = JTextField()

        regenerateResClassCheckBox = JCheckBox("Enable regenerateResClass feature")
        autoRegenerateResClassCheckBox =
            JCheckBox("Enable auto regenerateResClass feature")
        composeVectorConverterCheckBox = JCheckBox("Enable composeVectorConverter feature")
        regenerateResClassCheckBox.addItemListener {
            autoRegenerateResClassCheckBox.isEnabled = it.stateChange == ItemEvent.SELECTED
        }
        autoRegenerateResClassCheckBox.isEnabled = PluginPreference.getInstance().state.regenerateResClassFeatureEnabled

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
            addLabeledComponent(JLabel("Ktor Identifiers"), ktorKeywordsField, true)
            addLabeledComponent(JLabel("Folder ignore pattern"), folderIgnoreField, true)
            addLabeledComponent(JLabel("File ignore pattern"), fileIgnoreField, true)

            addComponent(JLabel("Hint: Add multiple identifiers by separating them with commas."), gap)
            addComponent(JLabel("If a KMP/CMP identifier matches the build.gradle file,"))
            addComponent(JLabel("the corresponding module will be selected."))
            addComponent(JLabel("If changes don’t take effect, restart the IDE."))

            addComponent(JLabel("Other Tools"), gap)
            addComponent(regenerateResClassCheckBox)
            addComponent(autoRegenerateResClassCheckBox)
            addComponent(JLabel("Detects changes in resource directory and triggers generateResClass automatically."))

            addComponent(JLabel("Experimental"), gap)
            addComponent(composeVectorConverterCheckBox)

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
                ktorKeywordsField.text != settings.ktorKeywords ||
                commonMainKeywordsField.text != settings.commonMainKeywords ||
                folderIgnoreField.text != settings.folderIgnoreKeywords ||
                fileIgnoreField.text != settings.fileIgnoreKeywords ||
                regenerateResClassCheckBox.isSelected != settings.regenerateResClassFeatureEnabled ||
                autoRegenerateResClassCheckBox.isSelected != settings.autoRegenerateResClassFeatureEnabled ||
                composeVectorConverterCheckBox.isSelected != settings.composeVectorConverterFeatureEnabled
    }

    override fun apply() {
        val settings = PluginPreference.getInstance()
        val requiresRestart = settings.state.run {
            regenerateResClassFeatureEnabled != regenerateResClassCheckBox.isSelected
                    || autoRegenerateResClassFeatureEnabled != autoRegenerateResClassCheckBox.isSelected
        }

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
                ktorKeywords = ktorKeywordsField.text
                commonMainKeywords = commonMainKeywordsField.text
                folderIgnoreKeywords = folderIgnoreField.text
                fileIgnoreKeywords = fileIgnoreField.text
                regenerateResClassFeatureEnabled = regenerateResClassCheckBox.isSelected
                autoRegenerateResClassFeatureEnabled = autoRegenerateResClassCheckBox.isSelected
                composeVectorConverterFeatureEnabled = composeVectorConverterCheckBox.isSelected
            }
        )

        PreferenceObserver.emit()

        if (requiresRestart) {
            ApplicationManager.getApplication().invokeLater {
                val result = Messages.showYesNoDialog(
                    "Changes will take effect after restarting the IDE. Do you want to restart now?",
                    "Restart Required",
                    "Restart",
                    "Cancel",
                    null
                )
                if (result == Messages.YES) {
                    ApplicationManagerEx.getApplicationEx().restart(true)
                }
            }
        }
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
        ktorKeywordsField.text = settings.ktorKeywords
        commonMainKeywordsField.text = settings.commonMainKeywords
        folderIgnoreField.text = settings.folderIgnoreKeywords
        fileIgnoreField.text = settings.fileIgnoreKeywords
        regenerateResClassCheckBox.isSelected = settings.regenerateResClassFeatureEnabled
        autoRegenerateResClassCheckBox.isSelected = settings.autoRegenerateResClassFeatureEnabled
        composeVectorConverterCheckBox.isSelected = settings.composeVectorConverterFeatureEnabled
    }

}
