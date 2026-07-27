package com.bhuvaneshw.kmp.projectview.module

import com.bhuvaneshw.kmp.preference.PluginPreference
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import org.jetbrains.plugins.gradle.model.ExternalProject
import org.jetbrains.plugins.gradle.service.project.data.ExternalProjectDataCache
import org.jetbrains.plugins.gradle.settings.GradleExtensionsSettings
import org.jetbrains.plugins.gradle.settings.GradleSettings
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.io.File
import java.util.regex.Pattern

object GradleModuleHelper {

    fun getModuleType(module: Module): ModuleType {
        val preference = PluginPreference.getInstance().state
        val extensionsData =
            GradleExtensionsSettings.getInstance(module.project).getExtensionsFor(module)

        val extensionNames = extensionsData?.extensions?.keys?.toList() ?: emptyList()
        val extensionTypes = extensionsData?.extensions?.values?.map { it.typeFqn } ?: emptyList()
        val sourceSetNames = getModuleSourceSets(module)

        if (extensionNames.isEmpty() && extensionTypes.isEmpty() && sourceSetNames.isEmpty())
            return ModuleType.Unknown

        fun isMatch(keywords: List<String>): Boolean {
            val typeKeywords = mutableListOf<String>()
            val nameKeywords = mutableListOf<String>()

            keywords.forEach { keyword ->
                when {
                    keyword.startsWith("[") && keyword.endsWith("]") ->
                        typeKeywords.add(keyword.substring(1, keyword.length - 1))

                    keyword.startsWith("(") && keyword.endsWith(")") ->
                        nameKeywords.add(keyword.substring(1, keyword.length - 1))

                    else -> {
                        typeKeywords.add(keyword)
                        nameKeywords.add(keyword)
                    }
                }
            }

            val typeMatch = typeKeywords.any { kw ->
                extensionTypes.any { it.contains(kw, ignoreCase = true) }
            }
            val nameMatch = nameKeywords.any { kw ->
                extensionNames.any { it.contains(kw, ignoreCase = true) }
            }

            return if (typeKeywords.isNotEmpty() && nameKeywords.isNotEmpty()) {
                (typeMatch && nameMatch)
            } else {
                typeMatch || nameMatch
            }
        }

        val hasAndroid = isMatch(preference.androidKeywordList)
        val hasWeb = isMatch(preference.webKeywordList)
        val hasDesktop = isMatch(preference.desktopKeywordList)
        val isKtor = isMatch(preference.ktorKeywordList)
        val isCMP = isMatch(preference.cmpKeywordList)
        val isKMP = isMatch(preference.kmpKeywordList)

        val targetedPlatforms = mutableSetOf<String>()
        if (hasAndroid) targetedPlatforms.add("Android")
        if (hasWeb) targetedPlatforms.add("Web")
        if (hasDesktop) targetedPlatforms.add("Desktop")

        val platformCount = targetedPlatforms.size

        return when {
            platformCount == 1 -> {
                when {
                    hasAndroid -> when {
                        isCMP -> ModuleType.CMP
                        isKMP -> ModuleType.KMP
                        else -> ModuleType.Android
                    }

                    hasWeb -> ModuleType.Web
                    else -> ModuleType.Desktop
                }
            }

            platformCount > 1 -> if (isCMP) ModuleType.CMP else ModuleType.KMP
            isKtor -> ModuleType.Ktor
            isCMP -> ModuleType.CMP
            isKMP -> ModuleType.KMP
            else -> ModuleType.Unknown
        }
    }

    private fun getModuleSourceSets(module: Module): Set<String> {
        val projectPath = ExternalSystemApiUtil.getExternalProjectPath(module) ?: return emptySet()
        val rootPath = ExternalSystemApiUtil.getExternalRootProjectPath(module) ?: return emptySet()
        val rootExternalProject =
            ExternalProjectDataCache.getInstance(module.project).getRootExternalProject(rootPath)
                ?: return emptySet()

        val targetProject = findExternalProjectByPath(rootExternalProject, projectPath)
        return targetProject?.sourceSets?.keys ?: emptySet()
    }

    private fun findExternalProjectByPath(parent: ExternalProject, path: String): ExternalProject? {
        if (FileUtil.pathsEqual(parent.projectDir.path, path)) return parent
        for (child in parent.childProjects.values) {
            val found = findExternalProjectByPath(child, path)
            if (found != null) return found
        }
        return null
    }

    fun hasFileMarkers(directory: PsiDirectory, markers: List<String>): Boolean {
        if (markers.isEmpty()) return false
        val children = directory.children
        return markers.any { marker ->
            val pattern =
                Pattern.compile(FileUtil.convertAntToRegexp(marker), Pattern.CASE_INSENSITIVE)
            children.any { child ->
                (child is PsiFile || child is PsiDirectory) && pattern.matcher(child.name).matches()
            }
        }
    }

    fun getProjectName(project: Project, rootPath: String): String {
        val useGradleName =
            PluginPreference.getInstance().state.useGradleProjectNameForSubstitutedProject
        val folderName = File(rootPath).name

        if (!useGradleName) return folderName

        val linkedProjects = GradleSettings.getInstance(project).linkedProjectsSettings
        for (setting in linkedProjects) {
            if (FileUtil.pathsEqual(setting.externalProjectPath, rootPath)) {
                val externalProjectData = ProjectDataManager.getInstance()
                    .getExternalProjectData(project, GradleConstants.SYSTEM_ID, rootPath)
                val name = externalProjectData?.externalProjectStructure?.data?.externalName
                if (name != null) return name
            }

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
            .maxByOrNull { it.length }
    }
}
