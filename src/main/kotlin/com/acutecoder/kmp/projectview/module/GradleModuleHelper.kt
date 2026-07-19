package com.acutecoder.kmp.projectview.module

import com.acutecoder.kmp.preference.PluginPreference
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.plugins.gradle.settings.GradleExtensionsSettings
import org.jetbrains.plugins.gradle.settings.GradleSettings
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.io.File

object GradleModuleHelper {

    fun getModuleType(module: Module): ModuleType {
        val extensionsData =
            GradleExtensionsSettings.getInstance(module.project).getExtensionsFor(module)
                ?: return ModuleType.Unknown

        val extensionTypes = extensionsData.extensions.values.map { it.typeFqn }
        val preference = PluginPreference.getInstance().state

        return when {
            extensionTypes.any { type -> preference.cmpKeywordList.any { type.contains(it.trim()) } } -> ModuleType.CMP
            extensionTypes.any { type -> preference.kmpKeywordList.any { type.contains(it.trim()) } } -> ModuleType.KMP
            extensionTypes.any { type -> preference.ktorKeywordList.any { type.contains(it.trim()) } } -> ModuleType.Ktor
            else -> ModuleType.Unknown
        }
    }

    fun getProjectName(project: Project, rootPath: String): String {
        val useGradleName =
            PluginPreference.getInstance().state.useGradleProjectNameForSubstitutedProject
        val folderName = File(rootPath).name

        if (!useGradleName) return folderName

        val linkedProjects = GradleSettings.getInstance(project).linkedProjectsSettings
        for (setting in linkedProjects) {
            // Check if it's the linked project root
            if (FileUtil.pathsEqual(setting.externalProjectPath, rootPath)) {
                val externalProjectData = ProjectDataManager.getInstance()
                    .getExternalProjectData(project, GradleConstants.SYSTEM_ID, rootPath)
                val name = externalProjectData?.externalProjectStructure?.data?.externalName
                if (name != null) return name
            }

            // Check composite participants (included builds)
            val participant = setting.compositeBuild?.compositeParticipants?.find {
                FileUtil.pathsEqual(it.rootPath, rootPath)
            }
            if (participant != null) {
                return participant.rootProjectName ?: folderName
            }
        }

        return folderName
    }

    fun getModuleName(module: Module): String {
        val projectPath = ExternalSystemApiUtil.getExternalProjectPath(module) ?: return module.name
        val moduleNode = ExternalSystemApiUtil.findModuleNode(
            module.project,
            GradleConstants.SYSTEM_ID,
            projectPath
        )
        return moduleNode?.data?.externalName ?: module.name
    }

    fun getAllBuildRoots(project: Project): Set<String> {
        val roots = mutableSetOf<String>()
        GradleSettings.getInstance(project).linkedProjectsSettings.forEach {
            roots.add(it.externalProjectPath)
            it.compositeBuild?.compositeParticipants?.forEach { participant ->
                roots.add(participant.rootPath)
            }
        }
        project.basePath?.let { roots.add(it) }
        return roots
    }

    fun getBuildRootPath(project: Project, path: String): String? {
        val allRoots = getAllBuildRoots(project)
        return allRoots.filter { FileUtil.isAncestor(it, path, false) }
            .maxByOrNull { it.length } // Return the most specific (longest) root path
    }
}
