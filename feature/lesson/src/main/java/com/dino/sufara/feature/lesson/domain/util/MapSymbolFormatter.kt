package com.dino.sufara.feature.lesson.domain.util

/** Replaces font-dependent tatweel constructions with a standard dotted-circle carrier. */
fun mapDisplaySymbol(rawSymbol: String): String {
    val marks = rawSymbol.filter { it in '\u064B'..'\u0652' || it == '\u0670' }
    return if ('ـ' in rawSymbol && marks.isNotEmpty()) "◌$marks" else rawSymbol
}
