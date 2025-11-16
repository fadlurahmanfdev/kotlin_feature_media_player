package com.fadlurahmanfdev.medx_player.notification

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper

abstract class BaseMedxNotification(private val context: Context) {
    @UnstableApi
    private fun getMediaNotificationBuilder(
        albumArtBitmap: Bitmap?,
        @DrawableRes smallIcon: Int,
        channelId: String,
        mediaSession: MediaSession,
    ): NotificationCompat.Builder {
        val mediaStyle = MediaStyleNotificationHelper.MediaStyle(mediaSession)
        return NotificationCompat.Builder(context, channelId).setSmallIcon(smallIcon)
            .setStyle(mediaStyle)
            .apply {
                if (albumArtBitmap != null) {
                    setLargeIcon(albumArtBitmap)
                }
            }
    }

    @OptIn(UnstableApi::class)
    open fun getBaseMediaNotificationV3(
        albumArtBitmap: Bitmap?,
        @DrawableRes smallIcon: Int,
        channelId: String,
        mediaSession: MediaSession,
    ): Notification {
        return getMediaNotificationBuilder(
            albumArtBitmap = albumArtBitmap,
            smallIcon = smallIcon,
            channelId = channelId,
            mediaSession = mediaSession
        ).apply {
            setOngoing(mediaSession.player.isPlaying)
        }.build()
    }
}