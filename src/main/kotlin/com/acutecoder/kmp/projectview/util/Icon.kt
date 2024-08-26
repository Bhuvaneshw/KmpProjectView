package com.acutecoder.kmp.projectview.util

import com.intellij.ui.icons.IconWrapperWithToolTip
import javax.swing.Icon

class IconWithoutTooltip(icon: Icon) : Icon by icon

@Suppress("nothing_to_inline")
inline fun Icon.withoutTooltip(): Icon = IconWithoutTooltip(this)

@Suppress("nothing_to_inline")
inline fun Icon.withTooltip(tooltip: String): Icon = IconWrapperWithToolTip(this, { tooltip })
