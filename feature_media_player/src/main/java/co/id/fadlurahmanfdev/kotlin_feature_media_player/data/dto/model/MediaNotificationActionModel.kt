package co.id.fadlurahmanfdev.kotlin_feature_media_player.data.dto.model

import android.app.PendingIntent
import androidx.annotation.DrawableRes

data class MediaNotificationActionModel(
    @DrawableRes val icon: Int,
    val title: String,
    val pendingIntent: PendingIntent,
)
