package co.id.fadlurahmanfdev.kotlin_feature_media_player.example.data.repository

import android.app.Notification
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.dto.model.MediaNotificationActionModel
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.repository.BaseAudioNotificationRepository
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.manager.FeatureAudioPlayerManager
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.R
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.domain.receiver.ExampleAudioPlayerReceiver

class ExampleAudioNotificationRepositoryImpl(context: Context) :
    BaseAudioNotificationRepository(context), ExampleMediaNotificationRepository {
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
                        icon = co.id.fadlurahmanfdev.kotlin_feature_media_player.R.drawable.round_skip_previous_24,
                        title = "Previous",
                        pendingIntent = FeatureAudioPlayerManager.getPreviousPendingIntent(
                            context,
                            notificationId = notificationId,
                            ExampleAudioPlayerReceiver::class.java
                        )
                    )
                )
            } else {
                add(
                    MediaNotificationActionModel(
                        icon = co.id.fadlurahmanfdev.kotlin_feature_media_player.R.drawable.round_skip_previous_24,
                        title = "Previous",
                        pendingIntent = FeatureAudioPlayerManager.getNonePendingIntent(
                            context,
                            ExampleAudioPlayerReceiver::class.java
                        )
                    )
                )
            }

            if (playbackStateCompat == PlaybackStateCompat.STATE_PLAYING) {
                add(
                    MediaNotificationActionModel(
                        icon = co.id.fadlurahmanfdev.kotlin_feature_media_player.R.drawable.round_pause_24,
                        title = "Pause",
                        pendingIntent = FeatureAudioPlayerManager.getPausePendingIntent(
                            context,
                            notificationId = notificationId,
                            ExampleAudioPlayerReceiver::class.java
                        )
                    )
                )
            } else if (playbackStateCompat == PlaybackStateCompat.STATE_PAUSED) {
                add(
                    MediaNotificationActionModel(
                        icon = co.id.fadlurahmanfdev.kotlin_feature_media_player.R.drawable.round_play_arrow_24,
                        title = "Resume",
                        pendingIntent = FeatureAudioPlayerManager.getResumePendingIntent(
                            context,
                            notificationId = notificationId,
                            ExampleAudioPlayerReceiver::class.java
                        )
                    )
                )
            } else if (playbackStateCompat == PlaybackStateCompat.STATE_STOPPED) {
                add(
                    MediaNotificationActionModel(
                        icon = co.id.fadlurahmanfdev.kotlin_feature_media_player.R.drawable.round_play_arrow_24,
                        title = "Replay",
                        pendingIntent = FeatureAudioPlayerManager.getResumePendingIntent(
                            context,
                            notificationId = notificationId,
                            ExampleAudioPlayerReceiver::class.java
                        )
                    )
                )
            } else {
                add(
                    MediaNotificationActionModel(
                        icon = co.id.fadlurahmanfdev.kotlin_feature_media_player.R.drawable.round_play_arrow_24,
                        title = "Play",
                        pendingIntent = FeatureAudioPlayerManager.getNonePendingIntent(
                            context,
                            ExampleAudioPlayerReceiver::class.java
                        )
                    )
                )
            }

            if (playbackStateCompat == PlaybackStateCompat.STATE_PLAYING) {
                add(
                    MediaNotificationActionModel(
                        icon = co.id.fadlurahmanfdev.kotlin_feature_media_player.R.drawable.round_skip_next_24,
                        title = "Next",
                        pendingIntent = FeatureAudioPlayerManager.getNextPendingIntent(
                            context,
                            notificationId = notificationId,
                            ExampleAudioPlayerReceiver::class.java
                        )
                    )
                )
            } else {
                add(
                    MediaNotificationActionModel(
                        icon = co.id.fadlurahmanfdev.kotlin_feature_media_player.R.drawable.round_skip_next_24,
                        title = "Next",
                        pendingIntent = FeatureAudioPlayerManager.getNonePendingIntent(
                            context,
                            ExampleAudioPlayerReceiver::class.java
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
                FeatureAudioPlayerManager.sendBroadcastSeekToPosition(
                    context,
                    position = positionSeekTo,
                    notificationId = notificationId,
                    clazz = ExampleAudioPlayerReceiver::class.java,
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