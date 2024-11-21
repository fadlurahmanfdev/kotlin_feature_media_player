package co.id.fadlurahmanfdev.kotlin_feature_media_player.example.data.repository

import android.app.Notification
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat

interface ExampleMediaNotificationRepository {
    fun createMediaNotificationChannel()
    fun getMediaNotification(
        context: Context,
        notificationId: Int,
        playbackStateCompat: Int,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
        mediaSession: MediaSessionCompat,
    ): Notification

    fun updateMediaNotification(
        context: Context,
        notificationId: Int,
        playbackStateCompat: Int,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
        mediaSession: MediaSessionCompat,
    )
}