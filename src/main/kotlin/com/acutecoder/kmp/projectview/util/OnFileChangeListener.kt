package com.acutecoder.kmp.projectview.util

import com.intellij.psi.*

class OnFileChangeListener(private val directory: PsiDirectory, val callback: () -> Unit) : PsiTreeChangeListener {
    override fun beforeChildAddition(event: PsiTreeChangeEvent) {}
    override fun beforeChildRemoval(event: PsiTreeChangeEvent) {}
    override fun beforeChildReplacement(event: PsiTreeChangeEvent) {}
    override fun beforeChildMovement(p0: PsiTreeChangeEvent) {}
    override fun beforeChildrenChange(p0: PsiTreeChangeEvent) {}
    override fun beforePropertyChange(p0: PsiTreeChangeEvent) {}

    override fun childAdded(event: PsiTreeChangeEvent) = checkAndCall(event.child)
    override fun childRemoved(event: PsiTreeChangeEvent) = checkAndCall(event.child)
    override fun childReplaced(event: PsiTreeChangeEvent) = checkAndCall(event.child)
    override fun childrenChanged(event: PsiTreeChangeEvent) = checkAndCall(event.child)
    override fun childMoved(event: PsiTreeChangeEvent) = checkAndCall(event.child)

    override fun propertyChanged(event: PsiTreeChangeEvent) {
        if (event.propertyName == PsiTreeChangeEvent.PROP_FILE_NAME)
            checkAndCall(event.element)
    }

    @Suppress("nothing_to_inline")
    private inline fun checkAndCall(file: PsiElement?) {
        if (file is PsiFile && directory.virtualFile.isAncestorOf(file.virtualFile)) callback()
    }
}
