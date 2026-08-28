package com.dino.sufara.feature.lesson.presentation.viewer.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer

/** A single-use-at-a-time MediaPlayer wrapper for recordings packaged in the app assets. */
internal class AssetAudioPlayer(context: Context) {
    private val assets = context.applicationContext.assets
    private var currentPlayer: MediaPlayer? = null

    fun play(
        assetPath: String,
        onPlaybackStateChanged: (Boolean) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        release()
        onPlaybackStateChanged(true)

        val player = MediaPlayer()
        currentPlayer = player
        try {
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            assets.openFd(assetPath).use { descriptor ->
                player.setDataSource(
                    descriptor.fileDescriptor,
                    descriptor.startOffset,
                    descriptor.length
                )
            }
            player.setOnPreparedListener { prepared ->
                if (currentPlayer === prepared) prepared.start()
            }
            player.setOnCompletionListener { completed ->
                if (currentPlayer === completed) {
                    releaseCurrent(completed)
                    onPlaybackStateChanged(false)
                }
            }
            player.setOnErrorListener { failed, what, extra ->
                if (currentPlayer === failed) {
                    releaseCurrent(failed)
                    onPlaybackStateChanged(false)
                    onError(IllegalStateException("MediaPlayer error $what/$extra for $assetPath"))
                }
                true
            }
            player.prepareAsync()
        } catch (error: Exception) {
            if (currentPlayer === player) releaseCurrent(player) else player.release()
            onPlaybackStateChanged(false)
            onError(error)
        }
    }

    fun release() {
        currentPlayer?.let(::releaseCurrent)
    }

    private fun releaseCurrent(player: MediaPlayer) {
        if (currentPlayer === player) currentPlayer = null
        player.setOnPreparedListener(null)
        player.setOnCompletionListener(null)
        player.setOnErrorListener(null)
        runCatching { player.release() }
    }
}
