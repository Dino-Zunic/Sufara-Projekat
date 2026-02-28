package com.dino.sufara.feature.lesson.domain.util

import android.util.Log

object SufaraLogger {
    private const val TAG = "SufaraDebug"

    fun log(message: String) {
        // Ово шаље поруку директно у твој Android Studio Logcat
        Log.d(TAG, message)
    }
}