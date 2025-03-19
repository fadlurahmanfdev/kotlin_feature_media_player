package com.fadlurahmanfdev.example.domain.service

import android.app.Notification
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.fadlurahmanfdev.example.data.repository.ExampleAudioNotificationRepositoryImpl
import com.fadlurahmanfdev.example.data.repository.ExampleMediaNotificationRepository
import com.fadlurahmanfdev.medx.data.enums.AudioPlayerState
import com.fadlurahmanfdev.medx.domain.service.BaseMedxAudioPlayerService

@OptIn(UnstableApi::class)
class AppAudioPlayerServiceV2 : BaseMedxAudioPlayerService() {
    private lateinit var exampleMediaNotificationRepository: ExampleMediaNotificationRepository

    override fun onInitAndCreateMediaNotificationChannel() {
        exampleMediaNotificationRepository =
            ExampleAudioNotificationRepositoryImpl(applicationContext)
        exampleMediaNotificationRepository.createMediaNotificationChannel()
    }

    override fun idleAudioNotification(
        notificationId: Int,
        mediaItem: MediaItem,
        mediaSession: MediaSessionCompat,
    ): Notification {
        return exampleMediaNotificationRepository.getMediaNotification(
            context = applicationContext,
            notificationId = notificationId,
            playbackStateCompat = PlaybackStateCompat.STATE_NONE,
            title = mediaItem.mediaMetadata.title?.toString() ?: "-",
            artist = mediaItem.mediaMetadata.artist?.toString() ?: "-",
            position = 0,
            duration = 0,
            mediaSession = mediaSession
        )
    }

    override fun onDurationChanged(duration: Long) {
        super.onDurationChanged(duration)
        updateNotification()
    }

    override fun onPositionChanged(position: Long) {
        super.onPositionChanged(position)
        updateNotification()
    }


    private fun updateNotification(){
        if(mediaSession == null) return

        val playbackStateCompat = when (audioPlayerState) {
            AudioPlayerState.IDLE, AudioPlayerState.BUFFERING, AudioPlayerState.READY, AudioPlayerState.PLAYING -> PlaybackStateCompat.STATE_PLAYING
            AudioPlayerState.PAUSED -> PlaybackStateCompat.STATE_PAUSED
            AudioPlayerState.STOPPED -> PlaybackStateCompat.STATE_STOPPED
            AudioPlayerState.ENDED -> PlaybackStateCompat.STATE_STOPPED
        }

        exampleMediaNotificationRepository.updateMediaNotification(
            applicationContext,
            notificationId = notificationId,
            playbackStateCompat = playbackStateCompat,
            title = mediaItem.mediaMetadata.title?.toString() ?: "-",
            artist = mediaItem.mediaMetadata.artist?.toString() ?: "-",
            position = position,
            duration = duration,
            mediaSession = mediaSession!!
        )
    }
}