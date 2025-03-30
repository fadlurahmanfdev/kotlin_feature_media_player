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
import com.fadlurahmanfdev.example.domain.receiver.AppAudioPlayerReceiverV2
import com.fadlurahmanfdev.medx.MedxAudioPlayerManager
import com.fadlurahmanfdev.medx.notification.BaseMedxNotification

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
                        MedxAudioPlayerManager.getSkipToPreviousAudioPendingIntent(
                            context,
                            1,
                            AppAudioPlayerReceiverV2::class.java
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
                        MedxAudioPlayerManager.getNonePendingIntent(
                            context,
                            AppAudioPlayerReceiverV2::class.java
                        )
                    )
                )
            }

            if (playbackStateCompat == PlaybackStateCompat.STATE_PLAYING) {
                add(
                    NotificationCompat.Action(
                        IconCompat.createWithResource(context, R.drawable.baseline_pause_24),
                        "Pause",
                        MedxAudioPlayerManager.getPauseAudioPendingIntent(
                            context,
                            2,
                            AppAudioPlayerReceiverV2::class.java
                        )
                    )
                )
            } else if (playbackStateCompat == PlaybackStateCompat.STATE_PAUSED) {
                add(
                    NotificationCompat.Action(
                        IconCompat.createWithResource(context, R.drawable.baseline_play_arrow_24),
                        "Resume",
                        MedxAudioPlayerManager.getResumePendingIntent(
                            context,
                            requestCode = 3,
                            notificationId = 1,
                            AppAudioPlayerReceiverV2::class.java
                        )
                    )
                )
            } else if (playbackStateCompat == PlaybackStateCompat.STATE_STOPPED) {
                add(
                    NotificationCompat.Action(
                        IconCompat.createWithResource(context, R.drawable.baseline_play_arrow_24),
                        "Replay",
                        MedxAudioPlayerManager.getResumePendingIntent(
                            context,
                            requestCode = 3,
                            notificationId = 1,
                            AppAudioPlayerReceiverV2::class.java
                        )
                    )
                )
            } else {
                add(
                    NotificationCompat.Action(
                        IconCompat.createWithResource(context, R.drawable.baseline_play_arrow_24),
                        "Play",
                        MedxAudioPlayerManager.getNonePendingIntent(
                            context,
                            AppAudioPlayerReceiverV2::class.java
                        )
                    )
                )
            }

            if (playbackStateCompat == PlaybackStateCompat.STATE_PLAYING) {
                add(
                    NotificationCompat.Action(
                        IconCompat.createWithResource(context, R.drawable.baseline_skip_next_24),
                        "Next",
                        MedxAudioPlayerManager.getSkipToNextAudioPendingIntent(
                            context,
                            requestCode = 4,
                            AppAudioPlayerReceiverV2::class.java
                        )
                    )
                )
            } else {
                add(
                    NotificationCompat.Action(
                        IconCompat.createWithResource(context, R.drawable.baseline_skip_next_24),
                        "Next",
                        MedxAudioPlayerManager.getNonePendingIntent(
                            context,
                            AppAudioPlayerReceiverV2::class.java
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
                MedxAudioPlayerManager.sendBroadcastSeekToPosition(
                    context,
                    position = positionSeekTo,
                    notificationId = notificationId,
                    clazz = AppAudioPlayerReceiverV2::class.java,
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