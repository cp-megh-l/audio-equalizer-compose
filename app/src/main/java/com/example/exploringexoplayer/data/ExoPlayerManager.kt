package com.example.exploringexoplayer.data

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer

object ExoPlayerManager {
    private var exoPlayer: ExoPlayer? = null

    fun getExoPlayer(context: Context): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
        return exoPlayer!!
    }

    fun releaseExoPlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }
}