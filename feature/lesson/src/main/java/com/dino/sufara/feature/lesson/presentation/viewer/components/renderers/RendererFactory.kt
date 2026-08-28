package com.dino.sufara.feature.lesson.presentation.viewer.components.renderers

object RendererFactory {
    /**
     * Multicolour spans split Arabic shaping in the bundled fonts. Keep the
     * renderer deliberately single-colour until shaping can be preserved.
     */
    @Suppress("UNUSED_PARAMETER")
    fun getRenderer(fontName: String): ArabicExampleRenderer {
        return EmptyExampleRenderer()
    }
}
