package com.fadlurahmanfdev.example.data.repository

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.support.v4.media.session.MediaSessionCompat

interface AppMedxNotificationRepository {
    fun createMediaNotificationChannel()
    fun getMediaNotification(
        albumArtBitmap: Bitmap?,
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
        albumArtBitmap: Bitmap?,
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