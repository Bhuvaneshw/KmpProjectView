package com.acutecoder.kmp.projectview.nodes

import com.acutecoder.kmp.projectview.util.Config
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.psi.PsiFile
import com.intellij.ui.SimpleTextAttributes

class HintedPsiFileNode(
    config: Config,
    file: PsiFile,
    private val sideText: String,
) : PsiFileNode(config.project, file, config.viewSettings) {

    override fun update(presentation: PresentationData) {
        super.update(presentation)

        presentation.addText(presentation.presentableText, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        presentation.addText(sideText, SimpleTextAttributes.GRAY_ATTRIBUTES)
    }
}
