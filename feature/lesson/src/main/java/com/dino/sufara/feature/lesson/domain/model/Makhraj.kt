package com.dino.sufara.feature.lesson.domain.model

/** Stable IDs for the 17 detailed articulation points used by the app. */
enum class MakhrajId {
    ORAL_CAVITY_MADD,
    THROAT_DEEPEST,
    THROAT_MIDDLE,
    THROAT_UPPER,
    TONGUE_REARMOST_QAF,
    TONGUE_REAR_KAF,
    TONGUE_MIDDLE,
    TONGUE_SIDE_DAD,
    TONGUE_SIDE_TO_TIP_LAM,
    TONGUE_TIP_NUN,
    TONGUE_TIP_RA,
    TONGUE_TIP_UPPER_INCISOR_ROOTS,
    TONGUE_TIP_LOWER_INCISORS,
    TONGUE_TIP_UPPER_INCISOR_EDGES,
    LOWER_LIP_UPPER_INCISORS,
    BETWEEN_LIPS,
    NASAL_CAVITY_GHUNNAH
}

data class MakhrajInfo(
    val number: Int,
    val id: MakhrajId,
    val arabicName: String,
    val description: String,
    val letters: String,
    val imagePath: String? = null
)

/** Traditional 17-point map; images are independent CC0 anatomical references. */
object MakharijCatalog {
    private const val ASSET_ROOT = "file:///android_asset/makharij/wright-mccloy"

    val all: List<MakhrajInfo> = listOf(
        MakhrajInfo(1, MakhrajId.ORAL_CAVITY_MADD, "الجوف", "Шупљина уста и грла; за харфове медда.", "ا و ي"),
        MakhrajInfo(2, MakhrajId.THROAT_DEEPEST, "أقصى الحلق", "Најдубљи део грла.", "ء ه"),
        MakhrajInfo(3, MakhrajId.THROAT_MIDDLE, "وسط الحلق", "Средина грла.", "ع ح"),
        MakhrajInfo(4, MakhrajId.THROAT_UPPER, "أدنى الحلق", "Горњи део грла, најближи устима.", "غ خ"),
        MakhrajInfo(5, MakhrajId.TONGUE_REARMOST_QAF, "أقصى اللسان", "Најзадњи део језика према горњем непцу.", "ق"),
        MakhrajInfo(6, MakhrajId.TONGUE_REAR_KAF, "أقصى اللسان أسفل من القاف", "Задњи део језика, мало испред исходишта кафа.", "ك"),
        MakhrajInfo(7, MakhrajId.TONGUE_MIDDLE, "وسط اللسان", "Средина језика према горњем непцу.", "ج ش ي"),
        MakhrajInfo(8, MakhrajId.TONGUE_SIDE_DAD, "حافة اللسان مع الأضراس", "Бочна страна језика уз горње кутњаке.", "ض"),
        MakhrajInfo(9, MakhrajId.TONGUE_SIDE_TO_TIP_LAM, "حافة اللسان إلى طرفه", "Предњи део бочне ивице језика до врха.", "ل"),
        MakhrajInfo(10, MakhrajId.TONGUE_TIP_NUN, "طرف اللسان تحت مخرج اللام", "Врх језика непосредно испод исходишта лама.", "ن"),
        MakhrajInfo(11, MakhrajId.TONGUE_TIP_RA, "طرف اللسان قريباً من مخرج النون", "Врх језика близу исходишта нуна.", "ر"),
        MakhrajInfo(12, MakhrajId.TONGUE_TIP_UPPER_INCISOR_ROOTS, "طرف اللسان مع أصول الثنايا العليا", "Врх језика са кореновима горњих секутића.", "ط د ت"),
        MakhrajInfo(13, MakhrajId.TONGUE_TIP_LOWER_INCISORS, "طرف اللسان مع فويق الثنايا السفلى", "Врх језика близу унутрашње стране доњих секутића.", "ص ز س"),
        MakhrajInfo(14, MakhrajId.TONGUE_TIP_UPPER_INCISOR_EDGES, "طرف اللسان مع أطراف الثنايا العليا", "Врх језика са крајевима горњих секутића.", "ظ ذ ث"),
        MakhrajInfo(15, MakhrajId.LOWER_LIP_UPPER_INCISORS, "بطن الشفة السفلى", "Унутрашња страна доње усне са горњим секутићима.", "ف"),
        MakhrajInfo(16, MakhrajId.BETWEEN_LIPS, "بين الشفتين", "Између две усне.", "و ب م"),
        MakhrajInfo(17, MakhrajId.NASAL_CAVITY_GHUNNAH, "الخيشوم", "Носна шупљина за гунну нуна и мима.", "ن م")
    )

    private val byId = all.associateBy(MakhrajInfo::id)

    fun forLesson(lessonId: String, symbol: String): List<MakhrajInfo> {
        // These course entries explicitly teach madd, so و/ي belong to the
        // cavity here rather than to their non-madd articulation points.
        if (lessonId in setOf("023", "024", "025", "026")) {
            return listOf(info(MakhrajId.ORAL_CAVITY_MADD))
        }

        return when (symbol.firstOrNull()) {
            'ء', 'أ', 'إ', 'ؤ', 'ئ' -> listOf(info(MakhrajId.THROAT_DEEPEST))
            'ه' -> listOf(info(MakhrajId.THROAT_DEEPEST))
            'ع' -> listOf(info(MakhrajId.THROAT_MIDDLE, "ain.svg"))
            'ح' -> listOf(info(MakhrajId.THROAT_MIDDLE, "haa.svg"))
            'غ' -> listOf(info(MakhrajId.THROAT_UPPER, "ghain.svg"))
            'خ' -> listOf(info(MakhrajId.THROAT_UPPER, "khaa.svg"))
            'ق' -> listOf(info(MakhrajId.TONGUE_REARMOST_QAF, "q.svg"))
            'ك' -> listOf(info(MakhrajId.TONGUE_REAR_KAF, "k.svg"))
            'ج' -> listOf(info(MakhrajId.TONGUE_MIDDLE, "jeem.svg"))
            'ش' -> listOf(info(MakhrajId.TONGUE_MIDDLE, "sheen.svg"))
            'ي' -> listOf(info(MakhrajId.TONGUE_MIDDLE))
            'ض' -> listOf(info(MakhrajId.TONGUE_SIDE_DAD))
            'ل' -> listOf(info(MakhrajId.TONGUE_SIDE_TO_TIP_LAM))
            'ن' -> listOf(info(MakhrajId.TONGUE_TIP_NUN, "n.svg"), info(MakhrajId.NASAL_CAVITY_GHUNNAH))
            'ر' -> listOf(info(MakhrajId.TONGUE_TIP_RA))
            'ط' -> listOf(info(MakhrajId.TONGUE_TIP_UPPER_INCISOR_ROOTS, "t.svg"))
            'د' -> listOf(info(MakhrajId.TONGUE_TIP_UPPER_INCISOR_ROOTS, "d.svg"))
            'ت', 'ة' -> listOf(info(MakhrajId.TONGUE_TIP_UPPER_INCISOR_ROOTS, "t.svg"))
            'ص', 'س' -> listOf(info(MakhrajId.TONGUE_TIP_LOWER_INCISORS, "s_laminal.svg"))
            'ز' -> listOf(info(MakhrajId.TONGUE_TIP_LOWER_INCISORS, "z_laminal.svg"))
            'ظ', 'ذ' -> listOf(info(MakhrajId.TONGUE_TIP_UPPER_INCISOR_EDGES, "eth.svg"))
            'ث' -> listOf(info(MakhrajId.TONGUE_TIP_UPPER_INCISOR_EDGES, "theta.svg"))
            'ف' -> listOf(info(MakhrajId.LOWER_LIP_UPPER_INCISORS, "f.svg"))
            'و' -> listOf(info(MakhrajId.BETWEEN_LIPS))
            'ب' -> listOf(info(MakhrajId.BETWEEN_LIPS, "b.svg"))
            'م' -> listOf(info(MakhrajId.BETWEEN_LIPS, "m.svg"), info(MakhrajId.NASAL_CAVITY_GHUNNAH))
            else -> emptyList()
        }
    }

    private fun info(id: MakhrajId, assetName: String? = null): MakhrajInfo {
        val definition = checkNotNull(byId[id])
        return definition.copy(imagePath = assetName?.let { "$ASSET_ROOT/$it" })
    }
}
