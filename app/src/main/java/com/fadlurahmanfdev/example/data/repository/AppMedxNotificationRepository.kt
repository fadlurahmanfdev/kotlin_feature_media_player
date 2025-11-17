package com.fadlurahmanfdev.example.data.repository

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import androidx.media3.session.MediaSession

interface AppMedxNotificationRepository {
    fun createMediaNotificationChannel()

    fun getMediaNotification(
        albumArtBitmap: Bitmap?,
        context: Context,
        notificationId: Int,
        mediaSession: MediaSession
    ): Notification

    fun updateMediaNotification(
        albumArtBitmap: Bitmap?,
        context: Context,
        notificationId: Int,
        mediaSession: MediaSession
    )
}