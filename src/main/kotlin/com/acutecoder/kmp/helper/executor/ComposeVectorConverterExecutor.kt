package com.acutecoder.kmp.helper.executor

import com.intellij.openapi.vfs.VirtualFile

class ComposeVectorConverterExecutor {

    private val androidColorMap = mapOf(
        "black" to "#000000",
        "white" to "#FFFFFF",
        "red" to "#FF0000",
        "green" to "#00FF00",
        "blue" to "#0000FF",
        "yellow" to "#FFFF00",
        "cyan" to "#00FFFF",
        "magenta" to "#FF00FF",
    )

    private val tintRegex = """android:tint="(#[A-Fa-f0-9]{6})"""".toRegex()
    private val colorRegex = """@android:color/\w+""".toRegex()

    fun modify(fileContent: String): String {
        val tintMatch = tintRegex.find(fileContent)
        val tintColor = tintMatch?.groups?.get(1)?.value

        val modifiedContent = if (tintColor != null) {
            fileContent.replace(colorRegex, tintColor)
                .replace(tintRegex, "")
        } else {
            androidColorMap.entries.fold(fileContent) { acc, (colorName, hexCode) ->
                acc.replace("@android:color/$colorName", hexCode)
            }
        }

        return modifiedContent
    }

    fun isConvertableFile(file: VirtualFile?): Boolean {
        if (file == null || !file.name.endsWith(".xml")) return false

        return file.path.run {
            contains("src") && contains("composeResource") && contains("drawable")
        }
    }

}
