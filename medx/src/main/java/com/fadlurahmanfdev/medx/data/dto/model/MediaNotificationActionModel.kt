package com.fadlurahmanfdev.medx.data.dto.model

import android.app.PendingIntent
import androidx.annotation.DrawableRes

data class MediaNotificationActionModel(
    @DrawableRes val icon: Int,
    val title: String,
    val pendingIntent: PendingIntent,
)
