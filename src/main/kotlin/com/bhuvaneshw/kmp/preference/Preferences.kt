package com.bhuvaneshw.kmp.preference

enum class SplitMode(val displayName: String) {
    PROJECT_LEVEL("Project Level"),
    ALL_LEVEL("All Level"),
    NONE("None");

    override fun toString(): String = displayName
}

enum class ModuleSortingOrder(val displayName: String) {
    ALPHABETICAL("Alphabetical"),
    PRIORITIZE_MODULES("Prioritize Modules"),
    PRIORITIZE_AND_CATEGORIZE("Prioritize and Categorize");

    override fun toString(): String = displayName
}
