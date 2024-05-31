package co.id.fadlurahmanfdev.kotlin_feature_media_player.example.domain.service

import android.app.Notification
import android.support.v4.media.session.MediaSessionCompat
import androidx.media3.common.util.UnstableApi
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.state.AudioNotificationState
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.state.MusicPlayerState
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.service.FeatureMusicPlayerService
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.data.repository.ExampleMediaNotificationRepository
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.data.repository.ExampleMediaNotificationRepositoryImpl

class ExampleMusicPlayerService : FeatureMusicPlayerService() {
    private lateinit var exampleMediaNotificationRepository: ExampleMediaNotificationRepository

    @UnstableApi
    override fun onCreate() {
        super.onCreate()
        exampleMediaNotificationRepository = ExampleMediaNotificationRepositoryImpl(applicationContext)
    }

    override fun onIdleAudioNotification(
        notificationId: Int,
        title: String,
        artist: String,
        mediaSession: MediaSessionCompat,
    ): Notification {
        return exampleMediaNotificationRepository.getMediaNotification(
            context = applicationContext,
            notificationId = notificationId,
            currentAudioState = AudioNotificationState.IDLE,
            title = title,
            artist = artist,
            position = 0,
            duration = 0,
            mediaSession = mediaSession
        )
    }

    override fun onUpdateAudioStateNotification(
        notificationId: Int,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
        musicPlayerState: MusicPlayerState
    ) {
        if (mediaSession != null) {
            when (musicPlayerState) {
                MusicPlayerState.PLAYING, MusicPlayerState.RESUME -> {
                    exampleMediaNotificationRepository.updateMediaNotification(
                        applicationContext,
                        notificationId = notificationId,
                        currentAudioState = AudioNotificationState.PLAYING,
                        title = title,
                        artist = artist,
                        position = position,
                        duration = duration,
                        mediaSession = mediaSession!!
                    )
                }

                MusicPlayerState.PAUSED -> {
                    exampleMediaNotificationRepository.updateMediaNotification(
                        applicationContext,
                        notificationId = notificationId,
                        currentAudioState = AudioNotificationState.PAUSED,
                        title = title,
                        artist = artist,
                        position = position,
                        duration = duration,
                        mediaSession = mediaSession!!
                    )
                }

                MusicPlayerState.ENDED -> {
                    exampleMediaNotificationRepository.updateMediaNotification(
                        applicationContext,
                        notificationId = notificationId,
                        currentAudioState = AudioNotificationState.ENDED,
                        title = title,
                        artist = artist,
                        position = position,
                        duration = duration,
                        mediaSession = mediaSession!!
                    )
                }

                else -> {

                }
            }
        }
    }
}