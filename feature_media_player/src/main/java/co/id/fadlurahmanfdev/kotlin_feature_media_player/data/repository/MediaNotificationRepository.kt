package co.id.fadlurahmanfdev.kotlin_feature_media_player.data.repository

import android.app.Notification
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.DrawableRes
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.MediaNotificationActionModel
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.state.AudioNotificationState

interface MediaNotificationRepository {
    fun isNotificationChannelExist(channelId: String): Boolean
    fun createMediaChannel(channelId: String, channelName: String, channelDescription: String)
    fun isNotificationPermissionEnabledAndGranted(): Boolean
    fun getMediaNotification(
        @DrawableRes smallIcon: Int,
        channelId: String,
        currentAudioState: AudioNotificationState,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
        onSeekToPosition: (Long) -> Unit,
        actions: List<MediaNotificationActionModel>,
        mediaSession: MediaSessionCompat
    ): Notification

    fun showNotification(notificationId: Int, notification: Notification)
}