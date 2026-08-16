package com.bhuvaneshw.kmp.projectview.util

object Constants {

    object Common {
        const val PANE_NAME = "KMP Project"
        const val PANE_ID = "KMPProjectPane"
        const val PANE_WEIGHT = 143
        const val SETTINGS_TAB_NAME = "KMP Project View"
    }

    object Weight {
        const val DEFAULT_WEIGHT = 10
        const val GLOBAL_GRADLE_WEIGHT = 100
    }

    object Node {
        const val GRADLE_SCRIPTS_NODE_NAME = "Gradle Scripts"
        const val GLOBAL_GRADLE_NODE_ID = "Global-Gradle"
        const val GRADLE_FILES = "Gradle Files"
        const val OTHER_FILES = "Other Files"
        const val OTHER_SOURCE_SET = "Other Source Set"
    }

    object File {
        const val BUILD_GRADLE_KTS = "build.gradle.kts"
        const val BUILD_GRADLE = "build.gradle"
        const val GRADLEW = "gradlew"
        const val GRADLEW_BAT = "gradlew.bat"
        const val SETTINGS_GRADLE_KTS = "settings.gradle.kts"
        const val SETTINGS_GRADLE = "settings.gradle"
        const val GRADLE_PROPERTIES = "gradle.properties"
        const val LOCAL_PROPERTIES = "local.properties"
        const val GRADLE_WRAPPER_PROPERTIES = "gradle-wrapper.properties"
    }

    object Folder {
        const val GRADLE = "gradle"
        const val WRAPPER = "wrapper"
        const val SRC = "src"
        const val KOTLIN_JS_STORE = "kotlin-js-store"
        const val KOTLIN = "kotlin"
        const val RESOURCE = "resource"
        const val RES = "res"
    }

    object Suffix {
        const val VERSIONS_TOML = ".versions.toml"
        const val TOML = ".toml"
        const val PRO = ".pro"
        const val JAR = ".jar"
    }

    object Hint {
        const val PROJECT = "Project: %s"
        const val INCLUDED_BUILD = "Included build: %s"
        const val MODULE = "Module %s"
        const val PROJECT_SETTINGS = "Project Settings"
        const val PROJECT_PROPERTIES = "Project Properties"
        const val SDK_LOCATION = "SDK Location"
        const val GRADLE_VERSION = "Gradle Version"
        const val VERSION_CATALOG = "Version Catalog \"%s\""
        const val PROGUARD_RULES = "ProGuard Rules for \"%s\""
        const val GRADLE_WRAPPER = "Gradle Wrapper"
        const val GRADLE_SCRIPT = "Gradle Script"
        const val SRC = " (src)"
    }
}
