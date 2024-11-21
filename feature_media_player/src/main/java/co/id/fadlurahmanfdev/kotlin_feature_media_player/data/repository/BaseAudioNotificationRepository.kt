package co.id.fadlurahmanfdev.kotlin_feature_media_player.data.repository

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.dto.model.MediaNotificationActionModel

abstract class BaseAudioNotificationRepository(private val context: Context) {
    private var notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isNotificationChannelExist(channelId: String): Boolean {
        val allChannels = notificationManager.notificationChannels
        var knownChannel: NotificationChannel? = null
        for (channel in allChannels) {
            if (channel.id == channelId) {
                knownChannel = channel
                break
            }
        }
        return knownChannel != null
    }

    /**
     * create media channel with no sound
     * */
    open fun createMediaNotificationChannel(
        channelId: String,
        channelName: String,
        channelDescription: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isNotificationChannelExist(channelId)){
                return
            }

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDescription
                setSound(sound, null)
            }

            notificationManager.createNotificationChannel(channel)

        } else {
            Log.w(
                this::class.java.simpleName,
                "${Build.VERSION.SDK_INT} is not supported to create notification channel"
            )
        }
    }

    private fun getMediaNotificationBuilder(
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
    }

    open fun getBaseMediaNotification(
        @DrawableRes smallIcon: Int,
        channelId: String,
        playbackStateCompat: Int,
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
            playbackStateCompat = playbackStateCompat,
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
            if (PlaybackStateCompat.STATE_PLAYING == playbackStateCompat) {
                setOngoing(true)
            }
        }.build()
    }

    fun showNotification(notificationId: Int, notification: Notification) {
        notificationManager.notify(notificationId, notification)
    }
}