import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    // https://plugins.gradle.org/plugin/org.jetbrains.kotlin.jvm
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "com.acutecoder.kmp.projectview"
version = "1.0.6-beta02"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // https://plugins.jetbrains.com/plugin/22989-android/versions, https://plugins.jetbrains.com/plugin/25442-kmp-project-view/edit/versions/
        intellijIdeaCommunity("252.16512.17") // v2025.1-EAP-4 https://www.jetbrains.com/idea/nextversion/ https://www.jetbrains.com/idea/download/other.html

        bundledPlugins("org.jetbrains.plugins.gradle")
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("252.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
