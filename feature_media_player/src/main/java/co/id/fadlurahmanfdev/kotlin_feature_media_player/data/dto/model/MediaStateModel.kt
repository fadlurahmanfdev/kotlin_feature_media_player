package co.id.fadlurahmanfdev.kotlin_feature_media_player.data.dto.model

import android.os.Parcelable
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.enums.AudioPlayerState
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaStateModel(
    val position: Long,
    val duration: Long,
    val state: AudioPlayerState = AudioPlayerState.IDLE,
) : Parcelable
