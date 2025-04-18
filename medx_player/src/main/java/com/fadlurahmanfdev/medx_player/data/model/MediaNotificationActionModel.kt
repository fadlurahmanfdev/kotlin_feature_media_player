package com.fadlurahmanfdev.medx_player.data.model

import android.app.PendingIntent
import androidx.annotation.DrawableRes

data class MediaNotificationActionModel(
    @DrawableRes val icon: Int,
    val title: String,
    val pendingIntent: PendingIntent,
)
