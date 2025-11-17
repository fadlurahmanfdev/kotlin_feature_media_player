package com.fadlurahmanfdev.medx_player.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.fadlurahmanfdev.medx_player.MedxPlayer
import com.fadlurahmanfdev.medx_player.base.IMedxPlayerListener
import com.fadlurahmanfdev.medx_player.constant.MedxConstant
import com.fadlurahmanfdev.medx_player.constant.MedxErrorConstant
import com.fadlurahmanfdev.medx_player.data.enums.MedxPlayerState
import com.google.common.util.concurrent.ListenableFuture
import java.util.UUID

@UnstableApi
abstract class BaseMedxAudioPlayerService : Service(), IMedxPlayerListener {
    private lateinit var medxPlayer: MedxPlayer

    var notificationId: Int = -1
    private lateinit var mediaItems: List<MediaItem>
    lateinit var mediaMetadata: MediaMetadata

    private var _medxPlayerState: MedxPlayerState = MedxPlayerState.IDLE
    val medxPlayerState: MedxPlayerState
        get() = _medxPlayerState
    private var _duration: Long = 0L
    val duration: Long
        get() = _duration
    private var _position: Long = 0L
    val position: Long
        get() = _position

    var mediaSession: MediaSession? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @UnstableApi
    override fun onCreate() {
        super.onCreate()
        medxPlayer = MedxPlayer(applicationContext)
        medxPlayer.addListener(this)
        medxPlayer.initialize()

        mediaSession = onCreateMediaSession()

        onCreateMedxAudioPlayerService()
    }

    /**
     * function triggered after initialize [MedxPlayer] & [MediaSession]
     *
     * place for creating a notification channel for media
     * */
    abstract fun onCreateMedxAudioPlayerService()

    open fun onCreateMediaSession(): MediaSession =
        MediaSession.Builder(applicationContext, medxPlayer.exoPlayer)
            .setId(UUID.randomUUID().toString())
            .build()

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
            MedxConstant.ACTION_STOP_AUDIO -> onStartCommandStopAudio(intent)
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

        if (mediaSession == null) {
            Log.d(
                this::class.java.simpleName,
                "Medx-LOG %%% - MediaSession is missing or already been releases, creating new MediaSession"
            )
            mediaSession = onCreateMediaSession()
        }

        idleAudioNotification(
            notificationId = notificationId,
            mediaItem = mediaItems.first(),
            mediaSession = mediaSession!!,
            onReady = { notification ->
                startForeground(notificationId, notification)
                medxPlayer.playMedia(mediaItems)
            }
        )
    }

    /**
     * Handle service when command pause remote audio.
     * */
    open fun onStartCommandPauseAudio(intent: Intent) {
        medxPlayer.pause()
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
        if (medxPlayerState == MedxPlayerState.PAUSED || medxPlayerState == MedxPlayerState.READY) {
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
                    medxPlayer.resume()
                }
            )
        } else {
            Log.i(
                this::class.java.simpleName,
                "Medx-LOG %%% - unable to resume audio, current audio player state is $medxPlayerState"
            )
        }
    }

    /**
     * Handle service when command stop remote audio.
     * */
    open fun onStartCommandStopAudio(intent: Intent) {
        if (medxPlayerState != MedxPlayerState.STOPPED && medxPlayerState != MedxPlayerState.ENDED) {
            medxPlayer.stop()
            mediaSession?.release()
            mediaSession = null
            stopForeground()
        }
    }

    open fun onStartCommandSkipToPreviousAudio(intent: Intent) {
        if (medxPlayerState == MedxPlayerState.PAUSED) {
            stopForeground()
        }

        medxPlayer.skipToPreviousMediaItem()
    }

    open fun onStartCommandSkipToNextAudio(intent: Intent) {
        if (medxPlayerState == MedxPlayerState.PAUSED) {
            stopForeground()
        }

        medxPlayer.skipToNextMediaItem()
    }

    open fun onStartCommandSeekToPosition(intent: Intent) {
        if (medxPlayerState == MedxPlayerState.PAUSED) {
            stopForeground()
        }

        val position = intent.getLongExtra(MedxConstant.PARAM_POSITION, -1L)
        if (position == -1L) {
            Log.wtf(
                this::class.java.simpleName,
                "Medx-LOG %%% - unable to seek audio position, unable to get position: $position"
            )
            return
        }

        medxPlayer.seekToPosition(position)
    }

    private fun stopForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            stopForeground(false)
        }
    }


    abstract fun idleAudioNotification(
        notificationId: Int,
        mediaItem: MediaItem,
        mediaSession: MediaSession,
        onReady: (notification: Notification) -> Unit
    )

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        medxPlayer.release()
        super.onDestroy()
    }

    override fun onPositionChanged(position: Long) {
        super.onPositionChanged(position)
        _position = position
        sendBroadcastSendPositionInfo()
    }

    override fun onPlayerStateChanged(state: MedxPlayerState) {
        super.onPlayerStateChanged(state)
        Log.d(
            this::class.java.simpleName,
            "Medx-LOG %%% - on audio player state changed into $state"
        )
        _medxPlayerState = state
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
            putExtra(MedxConstant.PARAM_STATE, medxPlayerState.name)
        }
        applicationContext.sendBroadcast(intent)
        Log.d(
            this::class.java.simpleName,
            "MedX-LOG %%% - successfully send broadcast audio state: ${medxPlayerState.name}"
        )
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