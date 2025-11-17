package com.fadlurahmanfdev.medx_player.player

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.MediaSession
import com.fadlurahmanfdev.medx_player.MedxResourceManager
import com.fadlurahmanfdev.medx_player.base.IMedxPlayer
import com.fadlurahmanfdev.medx_player.base.IMedxPlayerListener
import com.fadlurahmanfdev.medx_player.base.IMedxResourceManager
import com.fadlurahmanfdev.medx_player.data.enums.MedxPlayerState

/**
 * Base class for playing an audio from remote, resources, etc.
 * */
abstract class BaseMedxPlayer(private val context: Context) : IMedxPlayer,
    Player.Listener {
    override val medxResourceManager: IMedxResourceManager = MedxResourceManager()
    override lateinit var exoPlayer: ExoPlayer
    override lateinit var mediaSession: MediaSession

    override var _duration: Long = 0L

    /**
     * Duration of audio.
     *
     * The duration of audio will be available when [MedxPlayerState] is [MedxPlayerState.READY]
     * otherwise it will always return 0
     * */
    override val duration: Long
        get() = _duration
    override var _position: Long = 0L

    /**
     * Position of currently audio playing.
     *
     * The position of audio fetched from observe audio position by calling [listenPosition].
     * By default, it will listen an audio position after playing an audio.
     * */
    override val position: Long
        get() = _position


    override var _medxPlayerState: MedxPlayerState = MedxPlayerState.IDLE

    /**
     * The player state of audio player (e.g., [MedxPlayerState.IDLE], [MedxPlayerState.PLAYING], [MedxPlayerState.BUFFERING], etc).
     *
     * @see MedxPlayerState
     * */
    override val medxPlayerState: MedxPlayerState
        get() = _medxPlayerState

    override var listener: IMedxPlayerListener? = null

    private val handler = Handler(Looper.getMainLooper())
    private val audioPositionRunnable = object : Runnable {
        override fun run() {
            if (exoPlayer.currentPosition > 0 && position != exoPlayer.currentPosition) {
                _position = exoPlayer.currentPosition
                listener?.onPositionChanged(position)
            }
            handler.postDelayed(this, 500)
        }
    }

    override fun addListener(listener: IMedxPlayerListener) {
        this.listener = listener
    }

    override fun removeListener() {
        this.listener = null
    }

    /**
     * Initialize the media player class, usually initialize exo player & media session.
     * */
    override fun initialize() {
        exoPlayer = ExoPlayer.Builder(context).build()
        exoPlayer.addListener(this)
        mediaSession =
            MediaSession.Builder(context, exoPlayer).setCallback(
                object : MediaSession.Callback {
                    @UnstableApi
                    override fun onMediaButtonEvent(
                        session: MediaSession,
                        controllerInfo: MediaSession.ControllerInfo,
                        intent: Intent,
                    ): Boolean {
                        Log.d(
                            this::class.java.simpleName,
                            "Medx-LOG %%% media session on media button event, intent:$intent"
                        )
                        if (intent.action == Intent.ACTION_MEDIA_BUTTON) {
                            val keyEvent: KeyEvent?
                            when {
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                                    keyEvent = intent.getParcelableExtra(
                                        Intent.EXTRA_KEY_EVENT,
                                        KeyEvent::class.java
                                    )
                                }

                                else -> {
                                    keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                                }
                            }

                            Log.d(
                                this::class.java.simpleName,
                                "Medx-LOG %%% media session, received key event: $keyEvent"
                            )

                            if (keyEvent != null) {
                                when (keyEvent.keyCode) {
                                    KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                                        onPlayerStateChanged(MedxPlayerState.PAUSED)
                                    }

                                    KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                        onPlayerStateChanged(MedxPlayerState.PLAYING)
                                    }
                                }
                            }
                        }
                        return super.onMediaButtonEvent(session, controllerInfo, intent)
                    }
                },
            ).build()
    }

    /**
     * Listening audio position changed every n seconds.
     * */
    open fun listenPosition() {
        handler.postDelayed(audioPositionRunnable, 250)
    }

    /**
     * Remove listener audio position changed every n seconds.
     * */
    private fun removeListenerPosition() {
        handler.removeCallbacks(audioPositionRunnable)
    }

    private fun updatePosition(position: Long) {
        _position = position
        listener?.onPositionChanged(position)
    }

    /**
     * Play audio that have resource from remote or internet.
     *
     * @param mediaItems list of media item that want to be played.
     *
     * @see [MedxResourceManager.httpDatasourceFactory]
     * */
    @OptIn(UnstableApi::class)
    override fun playMedia(mediaItems: List<MediaItem>) {
        val mediaSources = arrayListOf<MediaSource>()
        Log.d(
            this::class.java.simpleName,
            "Medx-LOG %%% received total ${mediaItems.size} media items to play"
        )
        repeat(mediaItems.size) { index ->
            val mediaItem = mediaItems[index]
            val dataSourceFactory: DataSource.Factory
            val mediaSource: MediaSource
            when (mediaItem.localConfiguration?.uri?.scheme) {
                "http", "https" -> {
                    dataSourceFactory =
                        medxResourceManager.httpDatasourceFactory()
                    val cacheDataSourceFactory =
                        medxResourceManager.cacheDatasourceFactory(context, dataSourceFactory)
                    mediaSource =
                        ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                            .createMediaSource(mediaItem)
                }

                "android.resource" -> {
                    dataSourceFactory =
                        medxResourceManager.rawDatasourceFactory(context)
                    mediaSource =
                        ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)
                }

                "file" -> {
                    dataSourceFactory =
                        medxResourceManager.fileDatasourceFactory()
                    mediaSource =
                        ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)
                }

                else -> {
                    dataSourceFactory =
                        medxResourceManager.defaultDatasourceFactory(context)
                    mediaSource =
                        ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)
                }
            }
            mediaSources.add(mediaSource)
        }
        exoPlayer.setMediaSources(mediaSources)
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
        listenPosition()
    }

    /**
     * pause an audio that currently playing.
     *
     * */
    override fun pause() {
        if (medxPlayerState == MedxPlayerState.PLAYING) {
            removeListenerPosition()
            exoPlayer.pause()
            onPlayerStateChanged(MedxPlayerState.PAUSED)
        }
    }

    /**
     * resume an audio that currently playing.
     *
     * */
    override fun resume() {
        if (medxPlayerState == MedxPlayerState.PAUSED || medxPlayerState == MedxPlayerState.READY) {
            exoPlayer.play()
            onPlayerStateChanged(MedxPlayerState.PLAYING)
            listenPosition()
        }
    }

    /**
     * stop an audio that currently playing.
     *
     * */
    override fun stop() {
        if (medxPlayerState == MedxPlayerState.READY || medxPlayerState == MedxPlayerState.PLAYING
            || medxPlayerState == MedxPlayerState.PAUSED || medxPlayerState == MedxPlayerState.BUFFERING
        ) {
            removeListenerPosition()
            exoPlayer.stop()
            updatePosition(0L)
            onPlayerStateChanged(MedxPlayerState.STOPPED)
        }
    }

    /**
     * check if the player has previous media item
     * */
    override fun hasPreviousMediaItem(): Boolean = exoPlayer.hasPreviousMediaItem()

    /**
     * skip to previous media item if there is any previous media item.
     *
     * to enable skip to previous media item, add multiple audio when play an audio
     *
     * @see playMedia
     * @author @fadlurahmanfdev - Taufik Fadlurahman Fajari
     * */
    override fun skipToPreviousMediaItem() {
        if (hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
            updatePosition(0L)
        } else {
            Log.i(
                this::class.java.simpleName,
                "Medx-LOG %%% - unable to skip to previous item because the player didnt have previous media item"
            )
        }
    }

    /**
     * check if the player has next media item
     * */
    override fun hasNextMediaItem(): Boolean = exoPlayer.hasNextMediaItem()

    /**
     * skip to next media item if there is any next media item
     *
     * to enable skip to next media item, add multiple audio when play an audio
     *
     * @see playMedia
     * @author @fadlurahmanfdev - Taufik Fadlurahman Fajari
     * */
    override fun skipToNextMediaItem() {
        if (hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
            updatePosition(0L)
        } else {
            Log.i(
                this::class.java.simpleName,
                "Medx-LOG %%% - unable to skip to next item because the player didnt have next media item"
            )
        }
    }

    /**
     * seek to specific position.
     *
     * @param position the position want to be jumped in.
     *
     * */
    override fun seekToPosition(position: Long) {
        exoPlayer.seekTo(position)
        updatePosition(position)
    }

    /**
     * release audio player instance.
     *
     * */
    override fun release() {
        if (medxPlayerState != MedxPlayerState.STOPPED) {
            stop()
        }
        mediaSession.release()
        exoPlayer.release()
    }

    /**
     * Triggered when audio player state changed.
     *
     * @param state the state of audio player
     * */
    override fun onPlayerStateChanged(state: MedxPlayerState) {
        if (medxPlayerState != state) {
            Log.d(
                this::class.java.simpleName,
                "Medx-LOG %%% - audio state changed from $medxPlayerState into $state"
            )
            _medxPlayerState = state
            listener?.onPlayerStateChanged(state)
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        when (playbackState) {
            Player.STATE_IDLE -> {
                onPlayerStateChanged(MedxPlayerState.READY)
            }

            Player.STATE_BUFFERING -> {
                onPlayerStateChanged(MedxPlayerState.BUFFERING)
            }

            Player.STATE_READY -> {
                _duration = exoPlayer.duration
                listener?.onDurationChanged(duration)
                onPlayerStateChanged(MedxPlayerState.READY)
            }

            Player.STATE_ENDED -> {
                onPlayerStateChanged(MedxPlayerState.ENDED)
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (isPlaying && exoPlayer.playbackState == Player.STATE_READY) {
            onPlayerStateChanged(MedxPlayerState.PLAYING)
        } else if (!isPlaying && exoPlayer.playbackState == Player.STATE_READY) {
            onPlayerStateChanged(MedxPlayerState.PAUSED)
        }

        listener?.onIsPlayingChanged(isPlaying)
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)
        listener?.onMediaMetadataChanged(mediaMetadata)
    }
}