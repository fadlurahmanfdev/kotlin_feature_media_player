package com.fadlurahmanfdev.medx.notification

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat

abstract class BaseMedxNotification(private val context: Context) {
    private fun getMediaNotificationBuilder(
        albumArtBitmap: Bitmap?,
        @DrawableRes smallIcon: Int,
        channelId: String,
        playbackStateCompat: Int,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
        onSeekToPosition: (Long) -> Unit,
        mediaSession: MediaSessionCompat,
    ): NotificationCompat.Builder {
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(playbackStateCompat, position, 1f)
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SEEK_TO)
                .build()
        )
        val mediaMetaData = MediaMetadataCompat.Builder().apply {
            putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
        }.build()
        mediaSession.setMetadata(mediaMetaData)
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onSeekTo(pos: Long) {
                super.onSeekTo(pos)
                onSeekToPosition(pos)
            }
        })
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
        mediaStyle.setMediaSession(mediaSession.sessionToken)
        mediaStyle.setShowActionsInCompactView(0, 1, 2)
        return NotificationCompat.Builder(context, channelId).setSmallIcon(smallIcon)
            .setStyle(mediaStyle)
            .apply {
                if (albumArtBitmap != null) {
                    setLargeIcon(albumArtBitmap)
                }
            }

    }

    open fun getBaseMediaNotification(
        albumArtBitmap: Bitmap?,
        @DrawableRes smallIcon: Int,
        channelId: String,
        playbackStateCompat: Int,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
        onSeekToPosition: (Long) -> Unit,
        actions: List<NotificationCompat.Action>,
        mediaSession: MediaSessionCompat,
    ): Notification {
        val notification = getMediaNotificationBuilder(
            albumArtBitmap = albumArtBitmap,
            smallIcon = smallIcon,
            channelId = channelId,
            playbackStateCompat = playbackStateCompat,
            title = title,
            artist = artist,
            position = position,
            duration = duration,
            onSeekToPosition = onSeekToPosition,
            mediaSession = mediaSession
        ).apply {
            repeat(actions.size) { index ->
                addAction(actions[index])
            }
        }
        return notification.apply {
            if (PlaybackStateCompat.STATE_PLAYING == playbackStateCompat) {
                setOngoing(true)
            }
        }.build()
    }
}