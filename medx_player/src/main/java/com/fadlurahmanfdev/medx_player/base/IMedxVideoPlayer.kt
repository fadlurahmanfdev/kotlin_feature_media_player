package com.fadlurahmanfdev.medx_player.base

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.fadlurahmanfdev.medx_player.data.enums.MedxVideoPlayerState

interface IMedxVideoPlayer {
    val medxVideoResourceManager: IMedxVideoResourceManager

    var exoPlayer: ExoPlayer
    var mediaSession: MediaSession
    var _duration: Long
    val duration: Long
        get() = _duration
    var _position: Long
    val position: Long
        get() = _position

    var _medxVideoPlayerState: MedxVideoPlayerState
    val medxVideoPlayerState: MedxVideoPlayerState
        get() = _medxVideoPlayerState

    var listener: IMedxVideoPlayerListener?


    fun initialize()
    fun addListener(listener: IMedxVideoPlayerListener)

    fun removeListener()

    fun playVideo(mediaItems: List<MediaItem>)
    fun pause()
    fun resume()
    fun stop()
    fun hasPreviousMediaItem(): Boolean
    fun skipToPreviousMediaItem()
    fun hasNextMediaItem(): Boolean
    fun skipToNextMediaItem()
    fun seekToPosition(position: Long)
    fun release()

    fun onVideoPlayerStateChanged(state: MedxVideoPlayerState)
}