package com.fadlurahmanfdev.example.data.repository

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import androidx.core.app.NotificationManagerCompat
import androidx.media3.session.MediaSession
import com.fadlurahmanfdev.example.R
import com.fadlurahmanfdev.medx_player.notification.BaseMedxNotification

class AppMedxNotification(context: Context) : BaseMedxNotification(context),
    AppMedxNotificationRepository {
    val appPushlyNotification = AppPushlyNotification(context)

    override fun createMediaNotificationChannel() {
        if (appPushlyNotification.isSupportedNotificationChannel()) {
            appPushlyNotification.createNotificationChannel(
                channelId = "MEDIA",
                channelName = "Media",
                channelDescription = "Notifikasi Media",
                importance = NotificationManagerCompat.IMPORTANCE_DEFAULT,
                sound = null,
            )
        }
    }

    override fun getMediaNotification(
        albumArtBitmap: Bitmap?,
        context: Context,
        notificationId: Int,
        mediaSession: MediaSession
    ): Notification {
        return getBaseMediaNotificationV3(
            albumArtBitmap = albumArtBitmap,
            smallIcon = R.drawable.il_logo_fadlurahmanfdev,
            channelId = "MEDIA",
            mediaSession = mediaSession,
        )
    }

    override fun updateMediaNotification(
        albumArtBitmap: Bitmap?,
        context: Context,
        notificationId: Int,
        mediaSession: MediaSession
    ) {
        val notification = getMediaNotification(
            albumArtBitmap = albumArtBitmap,
            context = context,
            notificationId = notificationId,
            mediaSession = mediaSession
        )
        appPushlyNotification.showNotification(
            notificationId = notificationId,
            notification = notification
        )
    }
}