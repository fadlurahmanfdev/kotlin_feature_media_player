package com.fadlurahmanfdev.medx.data.model

import android.os.Parcelable
import com.fadlurahmanfdev.medx.data.enums.MedxAudioPlayerState
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaStateModel(
    val position: Long,
    val duration: Long,
    val state: MedxAudioPlayerState = MedxAudioPlayerState.IDLE,
) : Parcelable
