package com.fadlurahmanfdev.example.data.repository

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.fadlurahmanfdev.example.R
import com.fadlurahmanfdev.example.domain.receiver.AppMedxAudioPlayerReceiver
import com.fadlurahmanfdev.medx_player.MedxPlayerManager
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
        playbackStateCompat: Int,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
        mediaSession: MediaSessionCompat,
    ): Notification {
        val actions = arrayListOf<NotificationCompat.Action>().apply {
            if (playbackStateCompat == PlaybackStateCompat.STATE_PLAYING) {
                add(
                    NotificationCompat.Action(
                        IconCompat.createWithResource(
                            context,
                            R.drawable.baseline_skip_previous_24
                        ),
                        "Previous",
                        MedxPlayerManager.getSkipToPreviousAudioPendingIntent(
                            context,
                            1,
                            AppMedxAudioPlayerReceiver::class.java
                        )
                    )
                )
            } else {
                add(
                    NotificationCompat.Action(
                        IconCompat.createWithResource(
                            context,
                            R.drawable.baseline_skip_previous_24
                        ),
                        "Previous",
                        MedxPlayerManager.getNonePendingIntent(
                            context,
                            AppMedxAudioPlayerReceiver::class.java
                        )
                    )
                )
            }

            if (playbackStateCompat == PlaybackStateCompat.STATE_PLAYING) {
                add(
                    NotificationCompat.Action(
                        IconCompat.createWithResource(context, R.drawable.baseline_pause_24),
                        "Pause",
                        MedxPlayerManager.getPauseAudioPendingIntent(
                            context,
                            2,
                            AppMedxAudioPlayerReceiver::class.java
                        )
                    )
                )
            } else if (playbackStateCompat == PlaybackStateCompat.STATE_PAUSED) {
                add(
                    NotificationCompat.Action(
                        IconCompat.createWithResource(context, R.drawable.baseline_play_arrow_24),
                        "Resume",
                        MedxPlayerManager.getResumePendingIntent(
                            context,
                            requestCode = 3,
                            notificationId = 1,
                            AppMedxAudioPlayerReceiver::class.java
                        )
                    )
                )
            } else if (playbackStateCompat == PlaybackStateCompat.STATE_STOPPED) {
                add(
                    NotificationCompat.Action(
                        IconCompat.createWithResource(context, R.drawable.baseline_play_arrow_24),
                        "Replay",
                        MedxPlayerManager.getResumePendingIntent(
                            context,
                            requestCode = 3,
                            notificationId = 1,
                            AppMedxAudioPlayerReceiver::class.java
                        )
                    )
                )
            } else {
                add(
                    NotificationCompat.Action(
                        IconCompat.createWithResource(context, R.drawable.baseline_play_arrow_24),
                        "Play",
                        MedxPlayerManager.getNonePendingIntent(
                            context,
                            AppMedxAudioPlayerReceiver::class.java
                        )
                    )
                )
            }

            if (playbackStateCompat == PlaybackStateCompat.STATE_PLAYING) {
                add(
                    NotificationCompat.Action(
                        IconCompat.createWithResource(context, R.drawable.baseline_skip_next_24),
                        "Next",
                        MedxPlayerManager.getSkipToNextAudioPendingIntent(
                            context,
                            requestCode = 4,
                            AppMedxAudioPlayerReceiver::class.java
                        )
                    )
                )
            } else {
                add(
                    NotificationCompat.Action(
                        IconCompat.createWithResource(context, R.drawable.baseline_skip_next_24),
                        "Next",
                        MedxPlayerManager.getNonePendingIntent(
                            context,
                            AppMedxAudioPlayerReceiver::class.java
                        )
                    )
                )
            }
        }
        return getBaseMediaNotification(
            albumArtBitmap = albumArtBitmap,
            smallIcon = R.drawable.ic_launcher_foreground,
            channelId = "MEDIA",
            playbackStateCompat = playbackStateCompat,
            artist = artist,
            title = title,
            position = position,
            duration = duration,
            actions = actions,
            onSeekToPosition = { positionSeekTo ->
                MedxPlayerManager.sendBroadcastSeekToPosition(
                    context,
                    position = positionSeekTo,
                    notificationId = notificationId,
                    clazz = AppMedxAudioPlayerReceiver::class.java,
                )
            },
            mediaSession = mediaSession,
        )
    }

    override fun updateMediaNotification(
        albumArtBitmap: Bitmap?,
        context: Context,
        notificationId: Int,
        playbackStateCompat: Int,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
        mediaSession: MediaSessionCompat
    ) {
        val notification = getMediaNotification(
            albumArtBitmap = albumArtBitmap,
            context = context,
            notificationId = notificationId,
            playbackStateCompat = playbackStateCompat,
            title = title,
            artist = artist,
            position = position,
            duration = duration,
            mediaSession = mediaSession
        )
        appPushlyNotification.showNotification(
            notificationId = notificationId,
            notification = notification
        )
    }
}