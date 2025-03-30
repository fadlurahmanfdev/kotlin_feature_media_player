package com.fadlurahmanfdev.medx.base

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.fadlurahmanfdev.medx.data.enums.MedxAudioPlayerState

interface IMedxAudioPlayer {
    val medxAudioResourceManager: IMedxAudioResourceManager

    var exoPlayer: ExoPlayer
    var mediaSession: MediaSession
    var _duration: Long
    val duration: Long
        get() = _duration
    var _position: Long
    val position: Long
        get() = _position

    var _medxAudioPlayerState: MedxAudioPlayerState
    val medxAudioPlayerState: MedxAudioPlayerState
        get() = _medxAudioPlayerState

    var listener: IMedxAudioPlayerListener?


    fun initialize()
    fun addListener(listener: IMedxAudioPlayerListener)

    fun removeListener()

    fun playAudio(mediaItems: List<MediaItem>)
    fun pause()
    fun resume()
    fun stop()
    fun skipToPreviousMediaItem()
    fun skipToNextMediaItem()
    fun seekToPosition(position: Long)
    fun release()

    fun onAudioPlayerStateChanged(state: MedxAudioPlayerState)
}