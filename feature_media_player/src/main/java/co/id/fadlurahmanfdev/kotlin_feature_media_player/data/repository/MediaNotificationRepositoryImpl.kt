package co.id.fadlurahmanfdev.kotlin_feature_media_player.data.repository

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.MediaNotificationActionModel
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.state.AudioNotificationState

class MediaNotificationRepositoryImpl(val context: Context) :
    MediaNotificationRepository {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var mediaSession: MediaSessionCompat? = null

    override fun isNotificationChannelExist(channelId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val allChannels = notificationManager.notificationChannels
            var knownChannel: NotificationChannel? = null
            for (element in allChannels) {
                if (element.id == channelId) {
                    knownChannel = element
                    break
                }
            }
            return knownChannel != null
        }
        return false
    }

    /**
     * what ever channel created using this func, will get no sound
     * */
    override fun createChannel(channelId: String, channelName: String, channelDescription: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isNotificationChannelExist(channelId = channelId)) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = channelDescription
                    setSound(null, null)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    override fun isNotificationPermissionGranted(): Boolean {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> {
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            }

            else -> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission_group.NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    private fun getMediaNotificationBuilder(
        @DrawableRes smallIcon: Int,
        channelId: String,
        currentAudioState: AudioNotificationState,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
    ): NotificationCompat.Builder {
        val currentAudioNotificationState = when (currentAudioState) {
            AudioNotificationState.PLAYING -> {
                PlaybackStateCompat.STATE_PLAYING
            }

            AudioNotificationState.PAUSED -> {
                PlaybackStateCompat.STATE_PAUSED
            }

            AudioNotificationState.ENDED -> {
                PlaybackStateCompat.STATE_STOPPED
            }
        }
        if(mediaSession == null){
            mediaSession = MediaSessionCompat(context, "MusicPlayerService")
        }
        mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(currentAudioNotificationState, position, 1f)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build()
        )
        val mediaMetaData = MediaMetadataCompat.Builder().apply {
            putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
        }.build()
        mediaSession?.setMetadata(mediaMetaData)
        mediaSession?.setCallback(object : MediaSessionCompat.Callback() {
            override fun onSeekTo(pos: Long) {
                super.onSeekTo(pos)
                Log.d(
                    MediaNotificationRepositoryImpl::class.java.simpleName,
                    "audio seek to: $pos"
                )
            }
        })
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
        if (mediaSession != null) {
            mediaStyle.setMediaSession(mediaSession!!.sessionToken)
        }
        mediaStyle.setShowActionsInCompactView(0, 1, 2)
        return NotificationCompat.Builder(context, channelId).setSmallIcon(smallIcon)
            .setStyle(mediaStyle)
    }

    override fun getNotification(
        @DrawableRes smallIcon: Int,
        channelId: String,
        currentAudioState: AudioNotificationState,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
        actions: List<MediaNotificationActionModel>,
    ): Notification {
        val notification = getMediaNotificationBuilder(
            smallIcon = smallIcon,
            channelId = channelId,
            currentAudioState = currentAudioState,
            title = title,
            artist = artist,
            position = position,
            duration = duration,
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
        return notificationManager.notify(
            notificationId,
            notification,
        )
    }

}