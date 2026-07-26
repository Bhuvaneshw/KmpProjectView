package com.bhuvaneshw.kmp.preference

import com.bhuvaneshw.kmp.projectview.util.Constants
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationManagerEx
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.FormBuilder
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JTextField

class PluginPreferenceConfigurable : Configurable {

    private lateinit var isTooltipEnabledCheckBox: JCheckBox
    private lateinit var showCommonMainOnTopCheckBox: JCheckBox
    private lateinit var differentiateCommonMainCheckBox: JCheckBox
    private lateinit var showSharedModuleOnTopCheckBox: JCheckBox
    private lateinit var showModuleNameOnlyCheckBox: JCheckBox
    private lateinit var showKmpSideTextCheckBox: JCheckBox
    private lateinit var groupOtherMainCheckBox: JCheckBox
    private lateinit var unGroupCommonMainCheckBox: JCheckBox
    private lateinit var separateBuildsCheckBox: JCheckBox
    private lateinit var useGradleProjectNameCheckBox: JCheckBox
    private lateinit var splitGradleAndOtherComboBox: ComboBox<String>
    private lateinit var kmpKeywordsField: JTextField
    private lateinit var cmpKeywordsField: JTextField
    private lateinit var ktorKeywordsField: JTextField
    private lateinit var androidKeywordsField: JTextField
    private lateinit var iosFileMarkersField: JTextField
    private lateinit var desktopKeywordsField: JTextField
    private lateinit var webKeywordsField: JTextField
    private lateinit var sharedModuleKeywordsField: JTextField
    private lateinit var commonMainKeywordsField: JTextField
    private lateinit var folderIgnoreField: JTextField
    private lateinit var fileIgnoreField: JTextField
    private lateinit var regenerateResClassCheckBox: JCheckBox
    private lateinit var composeVectorConverterCheckBox: JCheckBox
    private lateinit var composeVectorAssetCheckBox: JCheckBox
    private val gap = 20

    override fun getDisplayName(): String {
        return Constants.SETTINGS_TAB_NAME
    }

    override fun createComponent(): JComponent {
        showKmpSideTextCheckBox = JCheckBox("Show module type (KMP/CMP/Ktor/...)")
        showCommonMainOnTopCheckBox = JCheckBox("Show commonMain on top")
        differentiateCommonMainCheckBox = JCheckBox("Highlight commonMain")
        showSharedModuleOnTopCheckBox = JCheckBox("Show shared module on top")
        showModuleNameOnlyCheckBox = JCheckBox("Hide extra info for source set")
        isTooltipEnabledCheckBox = JCheckBox("Show Tooltip")
        groupOtherMainCheckBox = JCheckBox("Group everything except commonMain")
        unGroupCommonMainCheckBox = JCheckBox("Ungroup commonMain")
        separateBuildsCheckBox = JCheckBox("Show included builds as separate folders. [1]")
        useGradleProjectNameCheckBox =
            JCheckBox("Use Gradle root project name instead of folder name. [1]")

        splitGradleAndOtherComboBox = ComboBox(arrayOf("Project Level", "All Level", "None"))

        kmpKeywordsField = JTextField()
        cmpKeywordsField = JTextField()
        ktorKeywordsField = JTextField()
        androidKeywordsField = JTextField()
        iosFileMarkersField = JTextField()
        desktopKeywordsField = JTextField()
        webKeywordsField = JTextField()
        sharedModuleKeywordsField = JTextField()
        commonMainKeywordsField = JTextField()
        folderIgnoreField = JTextField()
        fileIgnoreField = JTextField()

        regenerateResClassCheckBox = JCheckBox("Enable Regenerate Res Class feature. (Right Click composeResource -> Regenerate Res Class)")
        composeVectorConverterCheckBox = JCheckBox("Enable Compose Vector Converter feature. (Right Click <drawable>.xml -> Convert To Compose Vector).")
        composeVectorAssetCheckBox = JCheckBox("Enable Compose Vector Asset feature. (Right Click composeResource -> New -> Compose Vector Asset)")

        composeVectorConverterCheckBox.addItemListener {
            composeVectorAssetCheckBox.isEnabled = it.stateChange == java.awt.event.ItemEvent.SELECTED
        }

        return FormBuilder.createFormBuilder().apply {
            addComponent(showKmpSideTextCheckBox)
            addComponent(showCommonMainOnTopCheckBox)
            addComponent(differentiateCommonMainCheckBox)
            addComponent(showSharedModuleOnTopCheckBox)
            addComponent(showModuleNameOnlyCheckBox)
            addComponent(isTooltipEnabledCheckBox)
            addComponent(groupOtherMainCheckBox)
            addComponent(unGroupCommonMainCheckBox)
            addComponent(separateBuildsCheckBox)
            addComponent(useGradleProjectNameCheckBox)
            addComponent(JLabel("Note [1]: Only applicable for composite builds with included projects."))

            addLabeledComponent(
                JLabel("Split Gradle and Other files in"),
                splitGradleAndOtherComboBox,
                gap,
                false
            )

            addLabeledComponent(JLabel("KMP Identifiers"), kmpKeywordsField, gap, true)
            addLabeledComponent(JLabel("CMP Identifiers"), cmpKeywordsField, true)
            addLabeledComponent(JLabel("Ktor Identifiers"), ktorKeywordsField, true)
            addLabeledComponent(JLabel("Android Identifiers"), androidKeywordsField, true)
            addLabeledComponent(JLabel("Desktop Identifiers"), desktopKeywordsField, true)
            addLabeledComponent(JLabel("Web Identifiers"), webKeywordsField, true)
            addLabeledComponent(JLabel("iOS File Markers"), iosFileMarkersField, true)
            addLabeledComponent(
                JLabel("Shared Module Identifiers"),
                sharedModuleKeywordsField,
                true
            )
            addLabeledComponent(JLabel("commonMain Identifiers"), commonMainKeywordsField, true)
            addLabeledComponent(JLabel("Folder ignore pattern"), folderIgnoreField, true)
            addLabeledComponent(JLabel("File ignore pattern"), fileIgnoreField, true)

            addComponent(
                JLabel(
                    "<html>Hint: <ul>" +
                            "<li>Add multiple identifiers by separating them with commas.</li>" +
                            "<li>[Type] matches Extension Types (FQNs)</li>" +
                            "<li>(Name) matches Extension Names,</li>" +
                            "<li>If both [] and () are used in a single field, both(at-least one in each) must match (AND logic).</li>" +
                            "<li>If changes don’t take effect, restart the IDE.</li>" +
                            "</ul> </html>"
                ),
                gap
            )

            addComponent(JLabel("Other Tools"), gap)
            addComponent(regenerateResClassCheckBox)
            addComponent(composeVectorConverterCheckBox)
            addComponent(JLabel("Removes android related attributes."))
            addComponent(composeVectorAssetCheckBox)

            addComponent(JButton("Restore Defaults").apply {
                addActionListener {
                    reset(PreferenceState())
                }
            }, gap)

            reset()
        }.panel
    }

    override fun isModified(): Boolean {
        val settings = PluginPreference.getInstance().state

        return showKmpSideTextCheckBox.isSelected != settings.showKmpModuleSideText ||
                showCommonMainOnTopCheckBox.isSelected != settings.showCommonMainOnTop ||
                differentiateCommonMainCheckBox.isSelected != settings.differentiateCommonMain ||
                showSharedModuleOnTopCheckBox.isSelected != settings.showSharedModuleOnTop ||
                showModuleNameOnlyCheckBox.isSelected != settings.showModuleNameOnly ||
                isTooltipEnabledCheckBox.isSelected != settings.isTooltipEnabled ||
                groupOtherMainCheckBox.isSelected != settings.groupOtherMain ||
                unGroupCommonMainCheckBox.isSelected != settings.unGroupCommonMain ||
                separateBuildsCheckBox.isSelected != settings.separateNodeForSubstitutedProject ||
                useGradleProjectNameCheckBox.isSelected != settings.useGradleProjectNameForSubstitutedProject ||
                splitGradleAndOtherComboBox.selectedIndex != settings.splitGradleAndOther ||
                kmpKeywordsField.text != settings.kmpKeywords ||
                cmpKeywordsField.text != settings.cmpKeywords ||
                ktorKeywordsField.text != settings.ktorKeywords ||
                androidKeywordsField.text != settings.androidKeywords ||
                iosFileMarkersField.text != settings.iosFileMarkers ||
                desktopKeywordsField.text != settings.desktopKeywords ||
                webKeywordsField.text != settings.webKeywords ||
                sharedModuleKeywordsField.text != settings.sharedModuleKeywords ||
                commonMainKeywordsField.text != settings.commonMainKeywords ||
                folderIgnoreField.text != settings.folderIgnoreKeywords ||
                fileIgnoreField.text != settings.fileIgnoreKeywords ||
                regenerateResClassCheckBox.isSelected != settings.regenerateResClassFeatureEnabled ||
                composeVectorConverterCheckBox.isSelected != settings.composeVectorConverterFeatureEnabled ||
                composeVectorAssetCheckBox.isSelected != settings.composeVectorAssetFeatureEnabled
    }

    override fun apply() {
        val settings = PluginPreference.getInstance()
        val requiresRestart = settings.state.run {
            regenerateResClassFeatureEnabled != regenerateResClassCheckBox.isSelected
        }

        settings.loadState(
            settings.state.apply {
                showKmpModuleSideText = showKmpSideTextCheckBox.isSelected
                showCommonMainOnTop = showCommonMainOnTopCheckBox.isSelected
                differentiateCommonMain = differentiateCommonMainCheckBox.isSelected
                showSharedModuleOnTop = showSharedModuleOnTopCheckBox.isSelected
                showModuleNameOnly = showModuleNameOnlyCheckBox.isSelected
                isTooltipEnabled = isTooltipEnabledCheckBox.isSelected
                groupOtherMain = groupOtherMainCheckBox.isSelected
                unGroupCommonMain = unGroupCommonMainCheckBox.isSelected
                separateNodeForSubstitutedProject = separateBuildsCheckBox.isSelected
                useGradleProjectNameForSubstitutedProject = useGradleProjectNameCheckBox.isSelected
                splitGradleAndOther = splitGradleAndOtherComboBox.selectedIndex
                kmpKeywords = kmpKeywordsField.text
                cmpKeywords = cmpKeywordsField.text
                ktorKeywords = ktorKeywordsField.text
                androidKeywords = androidKeywordsField.text
                iosFileMarkers = iosFileMarkersField.text
                desktopKeywords = desktopKeywordsField.text
                webKeywords = webKeywordsField.text
                sharedModuleKeywords = sharedModuleKeywordsField.text
                commonMainKeywords = commonMainKeywordsField.text
                folderIgnoreKeywords = folderIgnoreField.text
                fileIgnoreKeywords = fileIgnoreField.text
                regenerateResClassFeatureEnabled = regenerateResClassCheckBox.isSelected
                composeVectorConverterFeatureEnabled = composeVectorConverterCheckBox.isSelected
                composeVectorAssetFeatureEnabled = composeVectorAssetCheckBox.isSelected
            }
        )

        ApplicationManager.getApplication().messageBus.syncPublisher(KMP_PREFERENCE_CHANGE).onPreferenceChange()

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
        reset(PluginPreference.getInstance().state)
    }

    private fun reset(settings: PreferenceState) {
        showKmpSideTextCheckBox.isSelected = settings.showKmpModuleSideText
        showCommonMainOnTopCheckBox.isSelected = settings.showCommonMainOnTop
        differentiateCommonMainCheckBox.isSelected = settings.differentiateCommonMain
        showSharedModuleOnTopCheckBox.isSelected = settings.showSharedModuleOnTop
        showModuleNameOnlyCheckBox.isSelected = settings.showModuleNameOnly
        isTooltipEnabledCheckBox.isSelected = settings.isTooltipEnabled
        groupOtherMainCheckBox.isSelected = settings.groupOtherMain
        unGroupCommonMainCheckBox.isSelected = settings.unGroupCommonMain
        separateBuildsCheckBox.isSelected = settings.separateNodeForSubstitutedProject
        useGradleProjectNameCheckBox.isSelected = settings.useGradleProjectNameForSubstitutedProject
        splitGradleAndOtherComboBox.selectedIndex = settings.splitGradleAndOther
        kmpKeywordsField.text = settings.kmpKeywords
        cmpKeywordsField.text = settings.cmpKeywords
        ktorKeywordsField.text = settings.ktorKeywords
        androidKeywordsField.text = settings.androidKeywords
        iosFileMarkersField.text = settings.iosFileMarkers
        desktopKeywordsField.text = settings.desktopKeywords
        webKeywordsField.text = settings.webKeywords
        sharedModuleKeywordsField.text = settings.sharedModuleKeywords
        commonMainKeywordsField.text = settings.commonMainKeywords
        folderIgnoreField.text = settings.folderIgnoreKeywords
        fileIgnoreField.text = settings.fileIgnoreKeywords
        regenerateResClassCheckBox.isSelected = settings.regenerateResClassFeatureEnabled
        composeVectorConverterCheckBox.isSelected = settings.composeVectorConverterFeatureEnabled
        composeVectorAssetCheckBox.isSelected = settings.composeVectorAssetFeatureEnabled
        composeVectorAssetCheckBox.isEnabled = settings.composeVectorConverterFeatureEnabled
    }

}
