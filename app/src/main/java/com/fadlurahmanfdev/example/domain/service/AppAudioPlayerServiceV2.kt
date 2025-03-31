package com.fadlurahmanfdev.example.domain.service

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.fadlurahmanfdev.example.data.repository.AppMedxNotification
import com.fadlurahmanfdev.example.data.repository.AppMedxNotificationRepository
import com.fadlurahmanfdev.medx_player.data.enums.MedxAudioPlayerState
import com.fadlurahmanfdev.medx_player.service.BaseMedxAudioPlayerService

@OptIn(UnstableApi::class)
class AppAudioPlayerServiceV2 : BaseMedxAudioPlayerService() {
    private lateinit var appMedxNotificationRepository: AppMedxNotificationRepository

    var albumArtBitmap: Bitmap? = null

    override fun onInitAndCreateMediaNotificationChannel() {
        appMedxNotificationRepository =
            AppMedxNotification(applicationContext)
        appMedxNotificationRepository.createMediaNotificationChannel()
    }

    override fun idleAudioNotification(
        notificationId: Int,
        mediaItem: MediaItem,
        mediaSession: MediaSessionCompat,
        onReady: (notification: Notification) -> Unit
    ) {
        if (mediaItem.mediaMetadata.artworkUri != null) {
            Glide.with(applicationContext)
                .asBitmap()
                .load(mediaItem.mediaMetadata.artworkUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        albumArtBitmap = resource
                        val notification = appMedxNotificationRepository.getMediaNotification(
                            albumArtBitmap = albumArtBitmap,
                            context = applicationContext,
                            notificationId = notificationId,
                            playbackStateCompat = PlaybackStateCompat.STATE_NONE,
                            title = mediaItem.mediaMetadata.title?.toString() ?: "-",
                            artist = mediaItem.mediaMetadata.artist?.toString() ?: "-",
                            position = 0,
                            duration = 0,
                            mediaSession = mediaSession
                        )
                        onReady(notification)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        TODO("Not yet implemented")
                    }

                })
        } else {
            val notification = appMedxNotificationRepository.getMediaNotification(
                albumArtBitmap = null,
                context = applicationContext,
                notificationId = notificationId,
                playbackStateCompat = PlaybackStateCompat.STATE_NONE,
                title = mediaItem.mediaMetadata.title?.toString() ?: "-",
                artist = mediaItem.mediaMetadata.artist?.toString() ?: "-",
                position = 0,
                duration = 0,
                mediaSession = mediaSession
            )
            onReady(notification)
        }
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)
        if (mediaMetadata.artworkUri != null) {
            Glide.with(applicationContext)
                .asBitmap()
                .load(mediaMetadata.artworkUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        albumArtBitmap = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        TODO("Not yet implemented")
                    }

                })
        }
    }

    override fun onDurationChanged(duration: Long) {
        super.onDurationChanged(duration)
        updateNotification()
    }

    override fun onPositionChanged(position: Long) {
        super.onPositionChanged(position)
        updateNotification()
    }

    override fun onPlayerStateChanged(state: MedxAudioPlayerState) {
        super.onPlayerStateChanged(state)
        updateNotification()
    }


    private fun updateNotification() {
        if (mediaSession == null) return

        val playbackStateCompat = when (medxAudioPlayerState) {
            MedxAudioPlayerState.IDLE, MedxAudioPlayerState.BUFFERING, MedxAudioPlayerState.READY, MedxAudioPlayerState.PLAYING -> PlaybackStateCompat.STATE_PLAYING
            MedxAudioPlayerState.PAUSED -> PlaybackStateCompat.STATE_PAUSED
            MedxAudioPlayerState.STOPPED -> PlaybackStateCompat.STATE_STOPPED
            MedxAudioPlayerState.ENDED -> PlaybackStateCompat.STATE_STOPPED
        }

        appMedxNotificationRepository.updateMediaNotification(
            albumArtBitmap = albumArtBitmap,
            context = applicationContext,
            notificationId = notificationId,
            playbackStateCompat = playbackStateCompat,
            title = mediaMetadata.title?.toString() ?: "-",
            artist = mediaMetadata.artist?.toString() ?: "-",
            position = position,
            duration = duration,
            mediaSession = mediaSession!!
        )
    }
}