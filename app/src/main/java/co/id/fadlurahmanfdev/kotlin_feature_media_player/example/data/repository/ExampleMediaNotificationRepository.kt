package co.id.fadlurahmanfdev.kotlin_feature_media_player.example.data.repository

import android.app.Notification
import android.content.Context
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.state.AudioNotificationState

interface ExampleMediaNotificationRepository {
    fun createMediaNotificationChannel()
    fun getMediaNotification(
        context: Context,
        notificationId: Int,
        currentAudioState: AudioNotificationState,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
    ): Notification

    fun updateMediaNotification(
        context: Context,
        notificationId: Int,
        currentAudioState: AudioNotificationState,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
    )
}