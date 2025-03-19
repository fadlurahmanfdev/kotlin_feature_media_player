package com.fadlurahmanfdev.medx.domain.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.fadlurahmanfdev.medx.MedxAudioPlayer
import com.fadlurahmanfdev.medx.constant.MedxConstant
import com.fadlurahmanfdev.medx.constant.MedxErrorConstant
import com.fadlurahmanfdev.medx.data.enums.AudioPlayerState
import com.fadlurahmanfdev.medx.domain.common.BaseMedxAudioPlayerV2

@UnstableApi
abstract class BaseMedxAudioPlayerService : Service(), BaseMedxAudioPlayerV2.Listener {
    private lateinit var audioPlayer: MedxAudioPlayer

    var notificationId: Int = -1
    private lateinit var mediaItems: List<MediaItem>
    lateinit var mediaItem: MediaItem

    private var _audioPlayerState: AudioPlayerState = AudioPlayerState.IDLE
    val audioPlayerState: AudioPlayerState
        get() = _audioPlayerState
    private var _duration: Long = 0L
    val duration: Long
        get() = _duration
    private var _position: Long = 0L
    val position: Long
        get() = _position

    var mediaSession: MediaSessionCompat? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @UnstableApi
    override fun onCreate() {
        super.onCreate()
        audioPlayer = MedxAudioPlayer(applicationContext)
        audioPlayer.addListener(this)
        audioPlayer.initialize()

        mediaSession = MediaSessionCompat(this, "BaseMedxAudioPlayerService")

        onInitAndCreateMediaNotificationChannel()
    }

    abstract fun onInitAndCreateMediaNotificationChannel()

    private fun getMediaItems(intent: Intent): List<MediaItem> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                (intent.getParcelableArrayListExtra(
                    MedxConstant.PARAM_MEDIA_ITEMS,
                    Bundle::class.java
                ) ?: listOf<Bundle>()).map { bundle ->
                    MediaItem.fromBundle(bundle)
                }
            }

            else -> {
                (intent.getParcelableArrayListExtra<Bundle>(MedxConstant.PARAM_MEDIA_ITEMS)
                    ?: listOf<Bundle>())
                    .map { bundle ->
                        MediaItem.fromBundle(bundle)
                    }
            }
        }
    }

    @UnstableApi
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(
            this::class.java.simpleName,
            "Medx-LOG %%% - MedxAudioPlayerService on start command ${intent?.action}"
        )
        when (intent?.action) {
            MedxConstant.ACTION_PLAY_REMOTE_AUDIO -> onStartCommandPlayRemoteAudio(intent)
        }
        return START_STICKY
    }

    private fun onStartCommandPlayRemoteAudio(intent: Intent) {
        val mediaItems: List<MediaItem> = getMediaItems(intent)

        // throw exception if the mediaItems empty
        if (mediaItems.isEmpty()) {
            Log.e(
                this::class.java.simpleName,
                "Medx-LOG %%% - cannot play remote audio, the list of  media item is missing"
            )
            throw MedxErrorConstant.MEDIA_ITEM_MISSING
        }

        notificationId =
            intent.getIntExtra(MedxConstant.PARAM_NOTIFICATION_ID, -1)

        // Check Notification ID, notification id should not be null or less than zero
        if (notificationId < 0) {
            throw MedxErrorConstant.NOTIFICATION_ID_MISSING
        }

        this.mediaItems = mediaItems

        // Set media item want to be played
        mediaItem = mediaItems.first()

        startForeground(
            notificationId,
            idleAudioNotification(
                notificationId = notificationId,
                mediaItem = mediaItem,
                mediaSession = mediaSession!!
            )
        )
        onPlayRemoteAudio(intent)
    }

    open fun onPlayRemoteAudio(intent: Intent) {
        audioPlayer.playRemoteAudio(mediaItems)
    }

    open fun onPauseAudio(intent: Intent) {}

    open fun onResumeAudio(intent: Intent) {}

    open fun onSkipToPreviousAudio(notificationId: Intent) {}

    open fun onSkipToNextAudio(notificationId: Intent) {}

    open fun onSeekToPosition(position: Long) {}

    abstract fun idleAudioNotification(
        notificationId: Int,
        mediaItem: MediaItem,
        mediaSession: MediaSessionCompat
    ): Notification

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
//        audioPlayer.destroy()
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    override fun onPositionChanged(position: Long) {
        super.onPositionChanged(position)
        Log.d(this::class.java.simpleName, "Medx-LOG %%% - on position changed $position")
        _position = position
    }

    override fun onPlayerStateChanged(state: AudioPlayerState) {
        super.onPlayerStateChanged(state)
        Log.d(this::class.java.simpleName, "Medx-LOG %%% - on audio player state $state")
        _audioPlayerState = state
    }

    override fun onDurationChanged(duration: Long) {
        super.onDurationChanged(duration)
        Log.d(this::class.java.simpleName, "Medx-LOG %%% - on duration changed $duration")
        _duration = duration
    }
}