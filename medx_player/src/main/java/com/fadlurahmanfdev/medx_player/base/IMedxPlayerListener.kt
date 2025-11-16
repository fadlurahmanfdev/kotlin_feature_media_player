package com.fadlurahmanfdev.medx_player.base

import androidx.media3.common.MediaMetadata
import com.fadlurahmanfdev.medx_player.data.enums.MedxPlayerState

interface IMedxPlayerListener {
    /**
     * this function triggered when state of audio player changed.
     *
     * @param state the state of the playback (e.g., [MedxPlayerState.IDLE], [MedxPlayerState.BUFFERING], etc)
     * */
    fun onPlayerStateChanged(state: MedxPlayerState) {}
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

    /**
     * Triggered when the is playing changed.
     *
     * @param isPlaying whether the media is playing.
     * */
    fun onIsPlayingChanged(isPlaying: Boolean) {}
}