package com.fadlurahmanfdev.medx.data.dto.model

import android.os.Parcelable
import com.fadlurahmanfdev.medx.data.enums.AudioPlayerState
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaStateModel(
    val position: Long,
    val duration: Long,
    val state: AudioPlayerState = AudioPlayerState.IDLE,
) : Parcelable
