package com.acutecoder.kmp.projectview.util

import javax.swing.Icon

class IconWithoutTooltip(icon: Icon) : Icon by icon

@Suppress("nothing_to_inline")
inline fun Icon.withoutTooltip(): Icon = IconWithoutTooltip(this)
