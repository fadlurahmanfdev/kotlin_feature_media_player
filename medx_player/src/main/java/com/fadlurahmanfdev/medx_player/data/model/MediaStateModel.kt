package com.fadlurahmanfdev.medx_player.data.model

import android.os.Parcelable
import com.fadlurahmanfdev.medx_player.data.enums.MedxPlayerState
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaStateModel(
    val position: Long,
    val duration: Long,
    val state: MedxPlayerState = MedxPlayerState.IDLE,
) : Parcelable
