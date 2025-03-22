package com.fadlurahmanfdev.example.data.repository

import android.app.Notification
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.fadlurahmanfdev.example.R
import com.fadlurahmanfdev.example.domain.receiver.AppAudioPlayerReceiverV2
import com.fadlurahmanfdev.medx.MedxAudioPlayerManager
import com.fadlurahmanfdev.medx.data.dto.model.MediaNotificationActionModel
import com.fadlurahmanfdev.medx.data.repository.BaseAudioNotificationRepository

class ExampleAudioNotificationRepositoryImpl(context: Context) :
    BaseAudioNotificationRepository(context),
    ExampleMediaNotificationRepository {
    override fun createMediaNotificationChannel() {
        super.createMediaNotificationChannel(
            channelId = "MEDIA",
            channelName = "Media",
            channelDescription = "Notifikasi Media",
        )
    }

    override fun getMediaNotification(
        context: Context,
        notificationId: Int,
        playbackStateCompat: Int,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
        mediaSession: MediaSessionCompat,
    ): Notification {
        val actions = arrayListOf<MediaNotificationActionModel>().apply {
            if (playbackStateCompat == PlaybackStateCompat.STATE_PLAYING) {
                add(
                    MediaNotificationActionModel(
                        icon = R.drawable.baseline_skip_previous_24,
                        title = "Previous",
                        pendingIntent = MedxAudioPlayerManager.getSkipToPreviousAudioPendingIntent(
                            context,
                            1,
                            AppAudioPlayerReceiverV2::class.java
                        )
                    )
                )
            } else {
                add(
                    MediaNotificationActionModel(
                        icon = R.drawable.baseline_skip_previous_24,
                        title = "Previous",
                        pendingIntent = MedxAudioPlayerManager.getNonePendingIntent(
                            context,
                            AppAudioPlayerReceiverV2::class.java
                        )
                    )
                )
            }

            if (playbackStateCompat == PlaybackStateCompat.STATE_PLAYING) {
                add(
                    MediaNotificationActionModel(
                        icon = R.drawable.baseline_pause_24,
                        title = "Pause",
                        pendingIntent = MedxAudioPlayerManager.getPauseAudioPendingIntent(
                            context,
                            2,
                            AppAudioPlayerReceiverV2::class.java
                        )
                    )
                )
            } else if (playbackStateCompat == PlaybackStateCompat.STATE_PAUSED) {
                add(
                    MediaNotificationActionModel(
                        icon = R.drawable.baseline_play_arrow_24,
                        title = "Resume",
                        pendingIntent = MedxAudioPlayerManager.getResumePendingIntent(
                            context,
                            requestCode = 3,
                            notificationId = 1,
                            AppAudioPlayerReceiverV2::class.java
                        )
                    )
                )
            } else if (playbackStateCompat == PlaybackStateCompat.STATE_STOPPED) {
                add(
                    MediaNotificationActionModel(
                        icon = R.drawable.baseline_play_arrow_24,
                        title = "Replay",
                        pendingIntent = MedxAudioPlayerManager.getResumePendingIntent(
                            context,
                            requestCode = 3,
                            notificationId = 1,
                            AppAudioPlayerReceiverV2::class.java
                        )
                    )
                )
            } else {
                add(
                    MediaNotificationActionModel(
                        icon = R.drawable.baseline_play_arrow_24,
                        title = "Play",
                        pendingIntent = MedxAudioPlayerManager.getNonePendingIntent(
                            context,
                            AppAudioPlayerReceiverV2::class.java
                        )
                    )
                )
            }

            if (playbackStateCompat == PlaybackStateCompat.STATE_PLAYING) {
                add(
                    MediaNotificationActionModel(
                        icon = R.drawable.baseline_skip_next_24,
                        title = "Next",
                        pendingIntent = MedxAudioPlayerManager.getSkipToNextAudioPendingIntent(
                            context,
                            requestCode = 4,
                            AppAudioPlayerReceiverV2::class.java
                        )
                    )
                )
            } else {
                add(
                    MediaNotificationActionModel(
                        icon = R.drawable.baseline_skip_next_24,
                        title = "Next",
                        pendingIntent = MedxAudioPlayerManager.getNonePendingIntent(
                            context,
                            AppAudioPlayerReceiverV2::class.java
                        )
                    )
                )
            }
        }
        return getBaseMediaNotification(
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
            context,
            notificationId = notificationId,
            playbackStateCompat = playbackStateCompat,
            title = title,
            artist = artist,
            position = position,
            duration = duration,
            mediaSession = mediaSession
        )
        showNotification(
            notificationId = notificationId,
            notification = notification,
        )
    }
}