package co.id.fadlurahmanfdev.kotlin_feature_media_player.example.data.repository

import android.app.Notification
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.MediaNotificationActionModel
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.state.AudioNotificationState
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.common.BaseMediaNotificationService
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.manager.FeatureMusicPlayerManager
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.R
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.domain.receiver.ExampleMusicPlayerReceiver

class ExampleMediaNotificationRepositoryImpl(context: Context) :
    BaseMediaNotificationService(context), ExampleMediaNotificationRepository {
    override fun createMediaNotificationChannel() {
        super.createMediaChannel(
            channelId = "MEDIA",
            channelName = "Media",
            channelDescription = "Notifikasi Media",
        )
    }

    override fun getMediaNotification(
        context: Context,
        notificationId: Int,
        currentAudioState: AudioNotificationState,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
        mediaSession: MediaSessionCompat,
    ): Notification {
        val actions = arrayListOf<MediaNotificationActionModel>().apply {
            if (currentAudioState != AudioNotificationState.IDLE) {
                add(
                    MediaNotificationActionModel(
                        icon = co.id.fadlurahmanfdev.kotlin_feature_media_player.R.drawable.round_skip_previous_24,
                        title = "Previous",
                        pendingIntent = FeatureMusicPlayerManager.getPreviousPendingIntent(
                            context,
                            notificationId = notificationId,
                            ExampleMusicPlayerReceiver::class.java
                        )
                    )
                )
            }

            if (currentAudioState == AudioNotificationState.PLAYING) {
                add(
                    MediaNotificationActionModel(
                        icon = co.id.fadlurahmanfdev.kotlin_feature_media_player.R.drawable.round_pause_24,
                        title = "Pause",
                        pendingIntent = FeatureMusicPlayerManager.getPausePendingIntent(
                            context,
                            notificationId = notificationId,
                            ExampleMusicPlayerReceiver::class.java
                        )
                    )
                )
            } else if (currentAudioState == AudioNotificationState.PAUSED) {
                add(
                    MediaNotificationActionModel(
                        icon = co.id.fadlurahmanfdev.kotlin_feature_media_player.R.drawable.round_play_arrow_24,
                        title = "Resume",
                        pendingIntent = FeatureMusicPlayerManager.getResumePendingIntent(
                            context,
                            notificationId = notificationId,
                            ExampleMusicPlayerReceiver::class.java
                        )
                    )
                )
            } else if (currentAudioState == AudioNotificationState.ENDED) {
                add(
                    MediaNotificationActionModel(
                        icon = co.id.fadlurahmanfdev.kotlin_feature_media_player.R.drawable.round_play_arrow_24,
                        title = "Replay",
                        pendingIntent = FeatureMusicPlayerManager.getResumePendingIntent(
                            context,
                            notificationId = notificationId,
                            ExampleMusicPlayerReceiver::class.java
                        )
                    )
                )
            }

            if (currentAudioState != AudioNotificationState.IDLE) {
                add(
                    MediaNotificationActionModel(
                        icon = co.id.fadlurahmanfdev.kotlin_feature_media_player.R.drawable.round_skip_next_24,
                        title = "Next",
                        pendingIntent = FeatureMusicPlayerManager.getNextPendingIntent(
                            context,
                            notificationId = notificationId,
                            ExampleMusicPlayerReceiver::class.java
                        )
                    )
                )
            }
        }
        return getMediaNotification(
            smallIcon = R.drawable.il_logo_bankmas,
            channelId = "MEDIA",
            currentAudioState = currentAudioState,
            artist = artist,
            title = title,
            position = position,
            duration = duration,
            actions = actions,
            onSeekToPosition = { positionSeekTo ->
                FeatureMusicPlayerManager.sendBroadcastSeekToPosition(
                    context,
                    position = positionSeekTo,
                    clazz = ExampleMusicPlayerReceiver::class.java,
                )
            },
            mediaSession = mediaSession,
        )
    }

    override fun updateMediaNotification(
        context: Context,
        notificationId: Int,
        currentAudioState: AudioNotificationState,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
        mediaSession: MediaSessionCompat
    ) {
        val notification = getMediaNotification(
            context,
            notificationId = notificationId,
            currentAudioState = currentAudioState,
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