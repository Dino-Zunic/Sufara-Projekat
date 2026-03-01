package com.dino.sufara.feature.lesson.domain.util

import androidx.compose.runtime.Composable
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings

object ScriptConverter {
    val cyrillicToLatinMap = mapOf(
        'А' to "A", 'Б' to "B", 'В' to "V", 'Г' to "G", 'Д' to "D", 'Ђ' to "Đ", 'Е' to "E", 'Ж' to "Ž", 'З' to "Z", 'И' to "I", 'Ј' to "J", 'К' to "K", 'Л' to "L", 'Љ' to "Lj", 'М' to "M", 'Н' to "N", 'Њ' to "Nj", 'О' to "O", 'П' to "P", 'Р' to "R", 'С' to "S", 'Т' to "T", 'Ћ' to "Ć", 'У' to "U", 'Ф' to "F", 'Х' to "H", 'Ц' to "C", 'Ч' to "Č", 'Џ' to "Dž", 'Ш' to "Š",
        'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d", 'ђ' to "đ", 'е' to "e", 'ж' to "ž", 'з' to "z", 'и' to "i", 'ј' to "j", 'к' to "k", 'л' to "l", 'љ' to "lj", 'м' to "m", 'н' to "n", 'њ' to "nj", 'о' to "o", 'п' to "p", 'р' to "r", 'с' to "s", 'т' to "t", 'ћ' to "ć", 'у' to "u", 'ф' to "f", 'х' to "h", 'ц' to "c", 'ч' to "č", 'џ' to "dž", 'ш' to "š"
    )
}

@Composable
fun String.asScript(): String {
    val isCyrillic = LocalSufaraSettings.current.isCyrillic
    if (isCyrillic) return this
    
    val builder = StringBuilder()
    for (char in this) {
        builder.append(ScriptConverter.cyrillicToLatinMap[char] ?: char.toString())
    }
    return builder.toString()
}