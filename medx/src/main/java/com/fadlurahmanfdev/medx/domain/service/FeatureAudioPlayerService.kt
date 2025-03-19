package com.fadlurahmanfdev.medx.domain.service

import android.app.Notification
import android.app.Service
import android.content.Context
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
import com.fadlurahmanfdev.medx.AudioPlayer
import com.fadlurahmanfdev.medx.data.constant.FeatureAudioPlayerConstant
import com.fadlurahmanfdev.medx.data.constant.FeatureMediaPlayerErrorConstant
import com.fadlurahmanfdev.medx.data.dto.model.MediaStateModel
import com.fadlurahmanfdev.medx.data.enums.AudioPlayerEvent
import com.fadlurahmanfdev.medx.data.enums.AudioPlayerState
import com.fadlurahmanfdev.medx.data.exception.FeatureMediaPlayerException
import com.fadlurahmanfdev.medx.domain.common.BaseAudioPlayer
import java.util.Calendar

@UnstableApi
abstract class FeatureAudioPlayerService : Service(), BaseAudioPlayer.Listener {
    private lateinit var audioPlayer: AudioPlayer

    private var notificationId: Int = -1
    private lateinit var mediaItems: List<MediaItem>
    private var mediaItemCurrentlyPlaying: MediaItem? = null
    private var mediaStateCurrentlyPlaying: MediaStateModel? = null

    var mediaSession: MediaSessionCompat? = null

    companion object {
        private fun sendBroadcastSendInfo(
            context: Context,
            mediaStateModel: MediaStateModel
        ) {
            val intent = Intent().apply {
                action = FeatureAudioPlayerConstant.SEND_INFO
                putExtra(FeatureAudioPlayerConstant.PARAM_DURATION, mediaStateModel.duration)
                putExtra(FeatureAudioPlayerConstant.PARAM_POSITION, mediaStateModel.position)
                putExtra(FeatureAudioPlayerConstant.PARAM_STATE, mediaStateModel.state.name)
            }
            context.sendBroadcast(intent)
        }

        private fun sendBroadcastSendAudioMetaData(
            context: Context,
            mediaMetadata: MediaMetadata
        ) {
            val intent = Intent().apply {
                action = FeatureAudioPlayerConstant.SEND_INFO_META_DATA
                putExtra(FeatureAudioPlayerConstant.PARAM_TITLE, mediaMetadata.title)
                putExtra(FeatureAudioPlayerConstant.PARAM_ARTIST, mediaMetadata.artist)
            }
            context.sendBroadcast(intent)
        }

        private fun sendBroadcastSendEvent(
            context: Context,
            event: String,
        ) {
            val intent = Intent().apply {
                action = FeatureAudioPlayerConstant.SEND_EVENT
                putExtra(FeatureAudioPlayerConstant.PARAM_CHANNEL, this::class.java.simpleName)
                putExtra(FeatureAudioPlayerConstant.PARAM_EVENT, event)
            }
            context.sendBroadcast(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @UnstableApi
    override fun onCreate() {
        super.onCreate()
        Log.d(this::class.java.simpleName, "initialize audio player")
        audioPlayer = AudioPlayer(applicationContext)
        audioPlayer.initialize()
        audioPlayer.addListener(this)
        Log.d(this::class.java.simpleName, "successfully initialize audio player")

        mediaSession = MediaSessionCompat(this, "FeatureAudioPlayerService")

        onInitAndCreateMediaNotificationChannel()
    }

    abstract fun onInitAndCreateMediaNotificationChannel()

    private fun getMediaItems(intent: Intent): List<MediaItem> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                (intent.getParcelableArrayListExtra(
                    FeatureAudioPlayerConstant.PARAM_MEDIA_ITEMS,
                    Bundle::class.java
                ) ?: listOf<Bundle>()).map { bundle ->
                    MediaItem.fromBundle(bundle)
                }
            }

            else -> {
                (intent.getParcelableArrayListExtra<Bundle>(FeatureAudioPlayerConstant.PARAM_MEDIA_ITEMS)
                    ?: listOf<Bundle>())
                    .map { bundle ->
                        MediaItem.fromBundle(bundle)
                    }
            }
        }
    }

    private fun shouldStartOverAudio(mediaItems: List<MediaItem>): Boolean {
        return mediaItems.firstOrNull()?.mediaId != mediaItemCurrentlyPlaying?.mediaId
    }

    private fun isCurrentlyPlayingAudio(): Boolean {
        return mediaItemCurrentlyPlaying != null && mediaStateCurrentlyPlaying != null && mediaStateCurrentlyPlaying?.state == AudioPlayerState.PLAYING
    }

    @UnstableApi
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d(this::class.java.simpleName, "action: $action at ${Calendar.getInstance().time}")
        when (action) {
            FeatureAudioPlayerConstant.ACTION_GET_INFO_CURRENTLY_AUDIO_PLAYING -> onStartCommandGetInfoCurrentlyAudioPlaying(
                intent
            )

            FeatureAudioPlayerConstant.ACTION_PLAY_REMOTE_AUDIO -> onStartCommandPlayRemoteAudio(
                intent
            )

            FeatureAudioPlayerConstant.ACTION_PAUSE_AUDIO -> onStartCommandPauseAudio(intent)

            FeatureAudioPlayerConstant.ACTION_RESUME_AUDIO -> onStartCommandResumeAudio(intent)

            FeatureAudioPlayerConstant.ACTION_SKIP_TO_PREVIOUS_AUDIO -> onStartCommandPreviousAudio(
                intent
            )

            FeatureAudioPlayerConstant.ACTION_SKIP_TO_NEXT_AUDIO -> onStartCommandNextAudio(intent)

            FeatureAudioPlayerConstant.ACTION_SEEK_TO_POSITION -> onStartCommandSeekToPosition(
                intent
            )
        }
        return START_STICKY
    }

    private fun onStartCommandGetInfoCurrentlyAudioPlaying(intent: Intent) {
        if (isCurrentlyPlayingAudio()) {
            if (mediaStateCurrentlyPlaying != null) {
                sendBroadcastSendInfo(
                    applicationContext,
                    mediaStateModel = mediaStateCurrentlyPlaying!!,
                )
            }
        }
    }

    private fun onStartCommandPlayRemoteAudio(intent: Intent) {
        val mediaItems: List<MediaItem> = getMediaItems(intent)

        // throw exception if the mediaItems empty
        if (mediaItems.isEmpty()) {
            throw FeatureMediaPlayerException(
                code = FeatureMediaPlayerErrorConstant.MEDIA_ITEM_MISSING,
                message = "media item missing, it can't be empty"
            )
        }

        // Check if the audio in the parameter same with the one that currently play
        if (!shouldStartOverAudio(mediaItems)) {
            Log.i(
                this::class.java.simpleName,
                "media item id in the parameter identical with the one that currently play," +
                        "no need to start over"
            )
            return
        }

        notificationId =
            intent.getIntExtra(FeatureAudioPlayerConstant.PARAM_NOTIFICATION_ID, -1)

        // Check Notification ID, notification id should not be null or less than zero
        if (notificationId < 0) {
            throw FeatureMediaPlayerException(
                code = FeatureMediaPlayerErrorConstant.NOTIFICATION_ID_MISSING,
                message = "Invalid/missing notification id"
            )
        }

        this.mediaItems = mediaItems

        // Set media item want to be played
        mediaItemCurrentlyPlaying = mediaItems.first()

        audioPlayer.playRemoteAudio(mediaItems)

        if (mediaSession == null) {
            mediaSession = MediaSessionCompat(this, "FeatureAudioPlayerService")
        }

        startForeground(
            notificationId,
            onIdleAudioNotification(
                notificationId = notificationId,
                mediaItemCurrentlyPlaying = mediaItemCurrentlyPlaying!!,
                mediaSession = mediaSession!!
            )
        )
        onPlayRemoteAudio(intent)
    }

    private fun onStartCommandPauseAudio(intent: Intent) {
        audioPlayer.pause()
        onPauseAudio(intent)
    }

    private fun onStartCommandResumeAudio(intent: Intent) {
        audioPlayer.resume()
        onResumeAudio(intent)
    }

    private fun onStartCommandPreviousAudio(intent: Intent) {
        audioPlayer.seekToPrevious()
        onSkipToPreviousAudio(intent)
    }

    private fun onStartCommandNextAudio(intent: Intent) {
        audioPlayer.seekToNext()
        onSkipToNextAudio(intent)
    }

    private fun onStartCommandSeekToPosition(intent: Intent) {
        val position = intent.getLongExtra(FeatureAudioPlayerConstant.PARAM_SEEK_TO_POSITION, -1L)
        if (position != -1L) {
            audioPlayer.seekToPosition(position = position)
            onSeekToPosition(position)
        } else {
            Log.e(
                this::class.java.simpleName,
                "action:${intent.action} -> position missing"
            )
        }
    }

    @OptIn(UnstableApi::class)
    override fun onPositionChanged(position: Long) {
        super.onPositionChanged(position)
        mediaStateCurrentlyPlaying = mediaStateCurrentlyPlaying?.copy(
            position = position
        )
        sendBroadcastSendInfo(
            applicationContext,
            mediaStateModel = mediaStateCurrentlyPlaying!!
        )
    }

    @UnstableApi
    override fun onAudioStateChanged(state: AudioPlayerState) {
        super.onAudioStateChanged(state)
        Log.d(this::class.java.simpleName, "onAudioStateChanged: $state")
        when (state) {
            AudioPlayerState.IDLE -> {
                mediaStateCurrentlyPlaying = MediaStateModel(
                    position = 0,
                    duration = 1,
                    state = state
                )
            }

            AudioPlayerState.READY -> {
                mediaStateCurrentlyPlaying = MediaStateModel(
                    position = audioPlayer.position,
                    duration = audioPlayer.duration,
                    state = state
                )
            }

//            AudioPlayerState.PLAYING, AudioPlayerState. -> {
//
//            }
//
//            AudioPlayerState.PAUSED -> {
//                onUpdateAudioStateNotification(
//                    notificationId = notificationId,
//                    title = mediaItemCurrentlyPlaying?.mediaMetadata?.title?.toString() ?: "-",
//                    artist = mediaItemCurrentlyPlaying?.mediaMetadata?.artist?.toString() ?: "-",
//                    position = audioPlayer.position,
//                    duration = audioPlayer.duration,
//                    audioPlayerState = AudioPlayerState.PAUSED,
//                )
////                sendBroadcastSendInfo(
////                    applicationContext,
////                    position = audioPlayer.position,
////                    duration = audioPlayer.duration,
////                    state = AudioPlayerState.PAUSED,
////                )
//            }
//
//            AudioPlayerState.ENDED -> {
//                onAudioEndedState(notificationId = notificationId)
//            }

            else -> {
                mediaStateCurrentlyPlaying = mediaStateCurrentlyPlaying?.copy(
                    state = state
                )
            }
        }
        Log.d(
            this::class.java.simpleName,
            "current media state model playing: $mediaStateCurrentlyPlaying"
        )

        if (mediaStateCurrentlyPlaying != null && mediaItemCurrentlyPlaying != null) {
            updateAudioNotification(
                mediaMetadata = mediaItemCurrentlyPlaying!!.mediaMetadata,
                mediaState = mediaStateCurrentlyPlaying!!
            )
        }

        if (mediaStateCurrentlyPlaying != null) {
            sendBroadcastSendInfo(
                applicationContext,
                mediaStateModel = this.mediaStateCurrentlyPlaying!!
            )
        }
    }

    private fun updateAudioNotification(mediaMetadata: MediaMetadata, mediaState: MediaStateModel) {
        onUpdateAudioNotification(
            notificationId = notificationId,
            metadata = mediaMetadata,
            mediaState = mediaState
        )
    }

    override fun onAudioMetadataChanged(metadata: MediaMetadata) {
        super.onAudioMetadataChanged(metadata)
        mediaItemCurrentlyPlaying =
            mediaItemCurrentlyPlaying?.buildUpon()?.setMediaMetadata(metadata)?.build()
        if (mediaStateCurrentlyPlaying != null) {
            updateAudioNotification(
                mediaMetadata = mediaItemCurrentlyPlaying!!.mediaMetadata,
                mediaState = mediaStateCurrentlyPlaying!!
            )
        }
        sendBroadcastSendAudioMetaData(
            applicationContext,
            mediaMetadata = mediaItemCurrentlyPlaying!!.mediaMetadata,
        )
    }

    override fun onAudioEventChanged(event: AudioPlayerEvent) {
        super.onAudioEventChanged(event)
        sendBroadcastSendEvent(
            applicationContext,
            event = event.name
        )
    }

    open fun onPlayRemoteAudio(intent: Intent) {}

    open fun onPauseAudio(intent: Intent) {}

    open fun onResumeAudio(intent: Intent) {}

    open fun onSkipToPreviousAudio(notificationId: Intent) {}

    open fun onSkipToNextAudio(notificationId: Intent) {}

    open fun onSeekToPosition(position: Long) {}

    abstract fun onIdleAudioNotification(
        notificationId: Int,
        mediaItemCurrentlyPlaying: MediaItem,
        mediaSession: MediaSessionCompat
    ): Notification

    abstract fun onUpdateAudioNotification(
        notificationId: Int,
        metadata: MediaMetadata,
        mediaState: MediaStateModel,
    )

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        audioPlayer.destroy()
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}