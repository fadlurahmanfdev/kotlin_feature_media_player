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
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.MediaSession
import com.fadlurahmanfdev.medx_player.MedxVideoResourceManager
import com.fadlurahmanfdev.medx_player.base.IMedxVideoPlayer
import com.fadlurahmanfdev.medx_player.base.IMedxVideoPlayerListener
import com.fadlurahmanfdev.medx_player.base.IMedxVideoResourceManager
import com.fadlurahmanfdev.medx_player.data.enums.MedxVideoPlayerState

/**
 * Base class for playing an audio from remote, resources, etc.
 * */
abstract class BaseMedxVideoPlayer(private val context: Context) : IMedxVideoPlayer,
    Player.Listener {
    override val medxVideoResourceManager: IMedxVideoResourceManager = MedxVideoResourceManager()
    override lateinit var exoPlayer: ExoPlayer
    override lateinit var mediaSession: MediaSession

    override var _duration: Long = 0L

    /**
     * Duration of audio.
     *
     * The duration of audio will be available when [MedxVideoPlayerState] is [MedxVideoPlayerState.READY]
     * otherwise it will always return 0
     * */
    override val duration: Long
        get() = _duration
    override var _position: Long = 0L

    /**
     * Position of currently audio playing.
     *
     * The position of audio fetched from observe audio position by calling [listenVideoPosition].
     * By default, it will listen an audio position after playing an audio.
     * */
    override val position: Long
        get() = _position


    override var _medxVideoPlayerState: MedxVideoPlayerState = MedxVideoPlayerState.IDLE

    /**
     * The player state of audio player (e.g., [MedxVideoPlayerState.IDLE], [MedxVideoPlayerState.PLAYING], [MedxVideoPlayerState.BUFFERING], etc).
     *
     * @see MedxVideoPlayerState
     * */
    override val medxVideoPlayerState: MedxVideoPlayerState
        get() = _medxVideoPlayerState

    override var listener: IMedxVideoPlayerListener? = null

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

    override fun addListener(listener: IMedxVideoPlayerListener) {
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
                                        onVideoPlayerStateChanged(MedxVideoPlayerState.PAUSED)
                                    }

                                    KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                        onVideoPlayerStateChanged(MedxVideoPlayerState.PLAYING)
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
    open fun listenVideoPosition() {
        handler.postDelayed(audioPositionRunnable, 250)
    }

    /**
     * Remove listener audio position changed every n seconds.
     * */
    fun removeListenerVideoPosition() {
        handler.removeCallbacks(audioPositionRunnable)
    }

    /**
     * Play audio that have resource from remote or internet.
     *
     * @param mediaItems list of media item that want to be played.
     *
     * @see [MedxVideoResourceManager.httpDatasourceFactory]
     * */
    @OptIn(UnstableApi::class)
    override fun playVideo(mediaItems: List<MediaItem>) {
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
                        medxVideoResourceManager.httpDatasourceFactory()
                    val cacheDataSourceFactory =
                        medxVideoResourceManager.cacheDatasourceFactory(context, dataSourceFactory)
                    mediaSource =
                        ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                            .createMediaSource(mediaItem)
                }

                "android.resource" -> {
                    dataSourceFactory =
                        medxVideoResourceManager.rawDatasourceFactory(context)
                    mediaSource =
                        ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)
                }

                "file" -> {
                    dataSourceFactory =
                        medxVideoResourceManager.fileDatasourceFactory()
                    mediaSource =
                        ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)
                }

                else -> {
                    dataSourceFactory =
                        medxVideoResourceManager.defaultDatasourceFactory(context)
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
        listenVideoPosition()
    }

    /**
     * pause an audio that currently playing.
     *
     * */
    override fun pause() {
        if (medxVideoPlayerState == MedxVideoPlayerState.PLAYING) {
            removeListenerVideoPosition()
            exoPlayer.pause()
            onVideoPlayerStateChanged(MedxVideoPlayerState.PAUSED)
        }
    }

    /**
     * resume an audio that currently playing.
     *
     * */
    override fun resume() {
        if (medxVideoPlayerState == MedxVideoPlayerState.PAUSED) {
            exoPlayer.play()
            onVideoPlayerStateChanged(MedxVideoPlayerState.PLAYING)
            listenVideoPosition()
        }
    }

    /**
     * stop an audio that currently playing.
     *
     * */
    override fun stop() {
        if (medxVideoPlayerState == MedxVideoPlayerState.PLAYING || medxVideoPlayerState == MedxVideoPlayerState.STOPPED) {
            removeListenerVideoPosition()
            exoPlayer.stop()
            onVideoPlayerStateChanged(MedxVideoPlayerState.STOPPED)
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
     * @see playVideo
     * @author @fadlurahmanfdev - Taufik Fadlurahman Fajari
     * */
    override fun skipToPreviousMediaItem() {
        if (hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
            _position = 0L
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
     * @see playVideo
     * @author @fadlurahmanfdev - Taufik Fadlurahman Fajari
     * */
    override fun skipToNextMediaItem() {
        if (hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
            _position = 0L
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
        _position = position
    }

    /**
     * release audio player instance.
     *
     * */
    override fun release() {
        if (medxVideoPlayerState != MedxVideoPlayerState.STOPPED) {
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
    override fun onVideoPlayerStateChanged(state: MedxVideoPlayerState) {
        if (medxVideoPlayerState != state) {
            Log.d(
                this::class.java.simpleName,
                "Medx-LOG %%% - audio state changed from $medxVideoPlayerState into $state"
            )
            _medxVideoPlayerState = state
            listener?.onPlayerStateChanged(state)
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        when (playbackState) {
            Player.STATE_IDLE -> {
                onVideoPlayerStateChanged(MedxVideoPlayerState.READY)
            }

            Player.STATE_BUFFERING -> {
                onVideoPlayerStateChanged(MedxVideoPlayerState.BUFFERING)
            }

            Player.STATE_READY -> {
                _duration = exoPlayer.duration
                listener?.onDurationChanged(duration)
                onVideoPlayerStateChanged(MedxVideoPlayerState.READY)
            }

            Player.STATE_ENDED -> {
                onVideoPlayerStateChanged(MedxVideoPlayerState.ENDED)
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (isPlaying) {
            onVideoPlayerStateChanged(MedxVideoPlayerState.PLAYING)
        }
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)
        listener?.onMediaMetadataChanged(mediaMetadata)
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Log.e(this::class.java.simpleName, "MedX-LOG %%% on player error, code:${error.errorCode}, codeName:${error.errorCodeName}, message:${error.message}")
        listener?.onPlayerError(error)
    }
}