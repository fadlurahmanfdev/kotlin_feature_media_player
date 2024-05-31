package co.id.fadlurahmanfdev.kotlin_feature_media_player.data.repository

import android.app.Notification
import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.MediaNotificationActionModel
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.state.AudioNotificationState
import com.github.fadlurahmanfdev.kotlin_core_notification.others.BaseNotificationService

class MediaNotificationRepositoryImpl(context: Context) : BaseNotificationService(context),
    MediaNotificationRepository {
    override fun isNotificationChannelExist(channelId: String): Boolean {
        return super.isNotificationChannelExist(channelId)
    }

    /**
     * create media channel with no sound
     * */
    override fun createMediaChannel(
        channelId: String,
        channelName: String,
        channelDescription: String
    ) {
        super.createNotificationChannel(
            channelId = channelId,
            channelName = channelName,
            channelDescription = channelDescription,
            sound = null,
        )
    }

    override fun isNotificationPermissionEnabledAndGranted(): Boolean {
        return super.isNotificationPermissionEnabledAndGranted()
    }

    private fun getMediaNotificationBuilder(
        @DrawableRes smallIcon: Int,
        channelId: String,
        currentAudioState: AudioNotificationState,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
        onSeekToPosition: (Long) -> Unit,
        mediaSession: MediaSessionCompat,
    ): NotificationCompat.Builder {
        val currentPlaybackState = when (currentAudioState) {
            AudioNotificationState.PLAYING, AudioNotificationState.IDLE -> {
                PlaybackStateCompat.STATE_PLAYING
            }

            AudioNotificationState.PAUSED -> {
                PlaybackStateCompat.STATE_PAUSED
            }

            AudioNotificationState.ENDED -> {
                PlaybackStateCompat.STATE_STOPPED
            }
        }
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(currentPlaybackState, position, 1f)
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
    }

    override fun getMediaNotification(
        @DrawableRes smallIcon: Int,
        channelId: String,
        currentAudioState: AudioNotificationState,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
        onSeekToPosition: (Long) -> Unit,
        actions: List<MediaNotificationActionModel>,
        mediaSession: MediaSessionCompat,
    ): Notification {
        val notification = getMediaNotificationBuilder(
            smallIcon = smallIcon,
            channelId = channelId,
            currentAudioState = currentAudioState,
            title = title,
            artist = artist,
            position = position,
            duration = duration,
            onSeekToPosition = onSeekToPosition,
            mediaSession = mediaSession
        ).apply {
            repeat(actions.size) { index ->
                addAction(
                    NotificationCompat.Action(
                        actions[index].icon,
                        actions[index].title,
                        actions[index].pendingIntent
                    )
                )
            }
        }
        return notification.apply {
            if (currentAudioState == AudioNotificationState.PLAYING) {
                setOngoing(true)
            }
        }.build()
    }

    override fun showNotification(notificationId: Int, notification: Notification) {
        return super.showNotification(notificationId, notification)
    }

}