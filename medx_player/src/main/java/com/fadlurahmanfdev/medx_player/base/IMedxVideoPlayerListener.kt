package com.fadlurahmanfdev.medx_player.base

import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import com.fadlurahmanfdev.medx_player.data.enums.MedxVideoPlayerState

interface IMedxVideoPlayerListener {
    /**
     * this function triggered when state of audio player changed.
     *
     * @param state the state of the playback (e.g., [MedxVideoPlayerState.IDLE], [MedxVideoPlayerState.BUFFERING], etc)
     * */
    fun onPlayerStateChanged(state: MedxVideoPlayerState) {}

    /**
     * Triggered when the duration of a media item is available or changed.
     *
     * @param duration the duration of a media item
     * */
    fun onDurationChanged(duration: Long) {}

    /**
     * Triggered when the position of a media item is available or changed.
     *
     * @param position the position of media item
     * */
    fun onPositionChanged(position: Long) {}

    /**
     * Triggered when the media meta data of a media item changed.
     *
     * @param mediaMetadata the content of meta data of a media item
     * */
    fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {}

    fun onPlayerError(error: PlaybackException) {}
}