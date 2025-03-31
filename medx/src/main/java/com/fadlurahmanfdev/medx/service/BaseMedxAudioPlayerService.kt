package com.fadlurahmanfdev.medx.service

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
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.fadlurahmanfdev.medx.MedxAudioPlayer
import com.fadlurahmanfdev.medx.base.IMedxAudioPlayerListener
import com.fadlurahmanfdev.medx.constant.MedxConstant
import com.fadlurahmanfdev.medx.constant.MedxErrorConstant
import com.fadlurahmanfdev.medx.data.enums.MedxAudioPlayerState

@UnstableApi
abstract class BaseMedxAudioPlayerService : Service(), IMedxAudioPlayerListener {
    private lateinit var audioPlayer: MedxAudioPlayer

    var notificationId: Int = -1
    private lateinit var mediaItems: List<MediaItem>
    lateinit var mediaMetadata: MediaMetadata

    private var _medxAudioPlayerState: MedxAudioPlayerState = MedxAudioPlayerState.IDLE
    val medxAudioPlayerState: MedxAudioPlayerState
        get() = _medxAudioPlayerState
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
            MedxConstant.ACTION_PLAY_AUDIO -> onStartCommandPlayAudio(intent)
            MedxConstant.ACTION_PAUSE_AUDIO -> onStartCommandPauseAudio(intent)
            MedxConstant.ACTION_RESUME_AUDIO -> onStartCommandResumeAudio(intent)
            MedxConstant.ACTION_SKIP_TO_PREVIOUS_AUDIO -> onStartCommandSkipToPreviousAudio(intent)
            MedxConstant.ACTION_SKIP_TO_NEXT_AUDIO -> onStartCommandSkipToNextAudio(intent)
            MedxConstant.ACTION_SEEK_TO_POSITION_AUDIO -> onStartCommandSeekToPosition(intent)
        }
        return START_STICKY
    }

    /**
     * Handle service when command play remote audio.
     * */
    open fun onStartCommandPlayAudio(intent: Intent) {
        val mediaItems: List<MediaItem> = getMediaItems(intent)

        // throw if the mediaItems empty
        if (mediaItems.isEmpty()) {
            Log.e(
                this::class.java.simpleName,
                "Medx-LOG %%% - cannot play audio, the list of  media item is missing"
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

        idleAudioNotification(
            notificationId = notificationId,
            mediaItem = mediaItems.first(),
            mediaSession = mediaSession!!,
            onReady = { notification ->
                startForeground(notificationId, notification)
                audioPlayer.playAudio(mediaItems)
            }
        )
    }

    /**
     * Handle service when command pause remote audio.
     * */
    open fun onStartCommandPauseAudio(intent: Intent) {
        audioPlayer.pause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            stopForeground(false)
        }
    }

    /**
     * Handle service when command resume remote audio.
     * */
    open fun onStartCommandResumeAudio(intent: Intent) {
        if (medxAudioPlayerState == MedxAudioPlayerState.PAUSED) {
            notificationId = intent.getIntExtra(MedxConstant.PARAM_NOTIFICATION_ID, -1)

            if (notificationId == -1) {
                Log.wtf(
                    this::class.java.simpleName,
                    "Medx-LOG %%% - unable to resume audio caused by unable to get notification id: $notificationId"
                )
                return
            }

            idleAudioNotification(
                notificationId = notificationId,
                mediaItem = mediaItems.first(),
                mediaSession = mediaSession!!,
                onReady = { notification ->
                    startForeground(notificationId, notification)
                    audioPlayer.resume()
                }
            )
        } else {
            Log.i(
                this::class.java.simpleName,
                "Medx-LOG %%% - unable to resume audio, current audio player state is $medxAudioPlayerState"
            )
        }
    }

    open fun onStartCommandSkipToPreviousAudio(intent: Intent) {
        audioPlayer.skipToPreviousMediaItem()
    }

    open fun onStartCommandSkipToNextAudio(intent: Intent) {
        audioPlayer.skipToNextMediaItem()
    }

    open fun onStartCommandSeekToPosition(intent: Intent) {
        val position = intent.getLongExtra(MedxConstant.PARAM_POSITION, -1L)
        if (position == -1L) {
            Log.wtf(
                this::class.java.simpleName,
                "Medx-LOG %%% - unable to seek audio position, unable to get position: $position"
            )
            return
        }

        audioPlayer.seekToPosition(position)
    }


    abstract fun idleAudioNotification(
        notificationId: Int,
        mediaItem: MediaItem,
        mediaSession: MediaSessionCompat,
        onReady: (notification: Notification) -> Unit
    )

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        audioPlayer.release()
        super.onDestroy()
    }

    override fun onPositionChanged(position: Long) {
        super.onPositionChanged(position)
//        Log.d(this::class.java.simpleName, "Medx-LOG %%% - on position changed $position")
        _position = position
        sendBroadcastSendPositionInfo()
    }

    override fun onPlayerStateChanged(state: MedxAudioPlayerState) {
        super.onPlayerStateChanged(state)
        Log.d(
            this::class.java.simpleName,
            "Medx-LOG %%% - on audio player state changed into $state"
        )
        _medxAudioPlayerState = state
        sendBroadcastSendAudioStateInfo()
    }

    override fun onDurationChanged(duration: Long) {
        super.onDurationChanged(duration)
        Log.d(this::class.java.simpleName, "Medx-LOG %%% - on duration changed $duration")
        _duration = duration
        sendBroadcastSendDurationInfo()
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)
        this.mediaMetadata = mediaMetadata
        sendBroadcastSendAudioMediaMetaDataInfo()
    }

    private fun sendBroadcastSendDurationInfo() {
        val intent = Intent().apply {
            action = MedxConstant.ACTION_AUDIO_DURATION_INFO
            putExtra(MedxConstant.PARAM_DURATION, duration)
        }
        applicationContext.sendBroadcast(intent)
    }

    private fun sendBroadcastSendPositionInfo() {
        val intent = Intent().apply {
            action = MedxConstant.ACTION_AUDIO_POSITION_INFO
            putExtra(MedxConstant.PARAM_POSITION, position)
        }
        applicationContext.sendBroadcast(intent)
    }

    private fun sendBroadcastSendAudioStateInfo() {
        val intent = Intent().apply {
            action = MedxConstant.ACTION_AUDIO_STATE_INFO
            putExtra(MedxConstant.PARAM_STATE, medxAudioPlayerState.name)
        }
        applicationContext.sendBroadcast(intent)
    }

    private fun sendBroadcastSendAudioMediaMetaDataInfo() {
        val intent = Intent().apply {
            action = MedxConstant.ACTION_AUDIO_MEDIA_META_DATA_INFO
            putExtra(MedxConstant.PARAM_TITLE, mediaMetadata.title)
            putExtra(MedxConstant.PARAM_ARTIST, mediaMetadata.artist)
        }
        applicationContext.sendBroadcast(intent)
    }
}