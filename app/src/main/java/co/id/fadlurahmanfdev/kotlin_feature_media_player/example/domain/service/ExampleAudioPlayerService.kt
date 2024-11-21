package co.id.fadlurahmanfdev.kotlin_feature_media_player.example.domain.service

import android.app.Notification
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.dto.model.MediaStateModel
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.enums.AudioPlayerState
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.service.FeatureAudioPlayerService
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.data.repository.ExampleAudioNotificationRepositoryImpl
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.data.repository.ExampleMediaNotificationRepository

@OptIn(UnstableApi::class)
class ExampleAudioPlayerService : FeatureAudioPlayerService() {
    private lateinit var exampleMediaNotificationRepository: ExampleMediaNotificationRepository


    override fun onInitAndCreateMediaNotificationChannel() {
        exampleMediaNotificationRepository = ExampleAudioNotificationRepositoryImpl(applicationContext)
        exampleMediaNotificationRepository.createMediaNotificationChannel()
    }

    override fun onIdleAudioNotification(
        notificationId: Int,
        mediaItemCurrentlyPlaying: MediaItem,
        mediaSession: MediaSessionCompat,
    ): Notification {
        return exampleMediaNotificationRepository.getMediaNotification(
            context = applicationContext,
            notificationId = notificationId,
            playbackStateCompat = PlaybackStateCompat.STATE_NONE,
            title = mediaItemCurrentlyPlaying.mediaMetadata.title?.toString() ?: "-",
            artist = mediaItemCurrentlyPlaying.mediaMetadata.artist?.toString() ?: "-",
            position = 0,
            duration = 0,
            mediaSession = mediaSession
        )
    }

    override fun onUpdateAudioNotification(
        notificationId: Int,
        metadata: MediaMetadata,
        mediaState: MediaStateModel,
    ) {
        if (mediaSession != null) {
            val playbackStateCompat = when(mediaState.state){
                AudioPlayerState.IDLE, AudioPlayerState.BUFFERING, AudioPlayerState.READY, AudioPlayerState.PLAYING -> PlaybackStateCompat.STATE_PLAYING
                AudioPlayerState.PAUSED -> PlaybackStateCompat.STATE_PAUSED
                AudioPlayerState.STOPPED -> PlaybackStateCompat.STATE_STOPPED
                AudioPlayerState.ENDED -> PlaybackStateCompat.STATE_STOPPED
            }
            exampleMediaNotificationRepository.updateMediaNotification(
                applicationContext,
                notificationId = notificationId,
                playbackStateCompat = playbackStateCompat,
                title = metadata.title?.toString() ?: "-",
                artist = metadata.artist?.toString() ?: "-",
                position = mediaState.position,
                duration = mediaState.duration,
                mediaSession = mediaSession!!
            )
        }
    }
}