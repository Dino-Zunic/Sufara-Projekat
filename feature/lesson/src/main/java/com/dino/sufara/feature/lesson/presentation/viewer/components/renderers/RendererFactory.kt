package com.dino.sufara.feature.lesson.presentation.viewer.components.renderers

object RendererFactory {
    fun getRenderer(fontName: String): ArabicExampleRenderer {
        return if (fontName == "Amiri") {
            ShaderBrushExampleRenderer()
        } else {
            SpanStyleExampleRenderer()
        }
    }
}