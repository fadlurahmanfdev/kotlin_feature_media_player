package co.id.fadlurahmanfdev.kotlin_feature_media_player.example.domain.service

import android.app.Notification
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.state.AudioNotificationState
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.service.FeatureMusicPlayerService
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.data.repository.ExampleMediaNotificationRepository
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.data.repository.ExampleMediaNotificationRepositoryImpl

class ExampleMusicPlayerService : FeatureMusicPlayerService() {
    private lateinit var exampleMediaNotificationRepository: ExampleMediaNotificationRepository

    @UnstableApi
    override fun onCreate() {
        super.onCreate()
        Log.d(
            ExampleMusicPlayerService::class.java.simpleName,
            "on create ${ExampleMusicPlayerService::class.java.simpleName}"
        )
        exampleMediaNotificationRepository = ExampleMediaNotificationRepositoryImpl(
            mediaNotificationRepository = mediaNotificationRepository,
        )
        Log.d(
            ExampleMusicPlayerService::class.java.simpleName,
            "successfully on create ${ExampleMusicPlayerService::class.java.simpleName}"
        )
    }

    override fun onIdleAudioNotification(
        notificationId: Int,
        title: String,
        artist: String
    ): Notification {
        return exampleMediaNotificationRepository.getMediaNotification(
            context = applicationContext,
            notificationId = notificationId,
            currentAudioState = AudioNotificationState.IDLE,
            title = title,
            artist = artist,
            position = 0,
            duration = 0,
        )
    }

    override fun onUpdatePlayingAudioNotification(
        notificationId: Int,
        title: String,
        artist: String,
        position: Long,
        duration: Long
    ) {
        exampleMediaNotificationRepository.updateMediaNotification(
            applicationContext,
            notificationId = notificationId,
            title = title,
            artist = artist,
            position = position,
            duration = duration,
            currentAudioState = AudioNotificationState.PLAYING,
        )
    }

    override fun onUpdatePauseAudioNotification(
        notificationId: Int,
        title: String,
        artist: String,
        position: Long,
        duration: Long
    ) {
        exampleMediaNotificationRepository.updateMediaNotification(
            applicationContext,
            notificationId = notificationId,
            title = title,
            artist = artist,
            position = position,
            duration = duration,
            currentAudioState = AudioNotificationState.PAUSED,
        )
    }

    override fun onEndedAudioNotification(
        notificationId: Int,
        title: String,
        artist: String,
        position: Long,
        duration: Long
    ) {
        exampleMediaNotificationRepository.updateMediaNotification(
            applicationContext,
            notificationId = notificationId,
            title = title,
            artist = artist,
            position = position,
            duration = duration,
            currentAudioState = AudioNotificationState.ENDED,
        )
    }
}