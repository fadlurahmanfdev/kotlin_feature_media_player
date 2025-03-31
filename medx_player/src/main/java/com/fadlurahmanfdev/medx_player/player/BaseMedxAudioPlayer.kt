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
import com.fadlurahmanfdev.medx_player.MedxAudioResourceManager
import com.fadlurahmanfdev.medx_player.base.IMedxAudioPlayer
import com.fadlurahmanfdev.medx_player.base.IMedxAudioPlayerListener
import com.fadlurahmanfdev.medx_player.base.IMedxAudioResourceManager
import com.fadlurahmanfdev.medx_player.data.enums.MedxAudioPlayerState

/**
 * Base class for playing an audio from remote, resources, etc.
 * */
abstract class BaseMedxAudioPlayer(private val context: Context) : IMedxAudioPlayer,
    Player.Listener {
    override val medxAudioResourceManager: IMedxAudioResourceManager = MedxAudioResourceManager()
    override lateinit var exoPlayer: ExoPlayer
    override lateinit var mediaSession: MediaSession

    override var _duration: Long = 0L

    /**
     * Duration of audio.
     *
     * The duration of audio will be available when [MedxAudioPlayerState] is [MedxAudioPlayerState.READY]
     * otherwise it will always return 0
     * */
    override val duration: Long
        get() = _duration
    override var _position: Long = 0L

    /**
     * Position of currently audio playing.
     *
     * The position of audio fetched from observe audio position by calling [listenAudioPosition].
     * By default, it will listen an audio position after playing an audio.
     * */
    override val position: Long
        get() = _position


    override var _medxAudioPlayerState: MedxAudioPlayerState = MedxAudioPlayerState.IDLE

    /**
     * The player state of audio player (e.g., [MedxAudioPlayerState.IDLE], [MedxAudioPlayerState.PLAYING], [MedxAudioPlayerState.BUFFERING], etc).
     *
     * @see MedxAudioPlayerState
     * */
    override val medxAudioPlayerState: MedxAudioPlayerState
        get() = _medxAudioPlayerState

    override var listener: IMedxAudioPlayerListener? = null

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

    override fun addListener(listener: IMedxAudioPlayerListener) {
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
                        intent: Intent
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
                                        onAudioPlayerStateChanged(MedxAudioPlayerState.PAUSED)
                                    }

                                    KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                        onAudioPlayerStateChanged(MedxAudioPlayerState.PLAYING)
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
    open fun listenAudioPosition() {
        handler.postDelayed(audioPositionRunnable, 250)
    }

    /**
     * Remove listener audio position changed every n seconds.
     * */
    fun removeListenerAudioPosition() {
        handler.removeCallbacks(audioPositionRunnable)
    }

    /**
     * Play audio that have resource from remote or internet.
     *
     * @param mediaItems list of media item that want to be played.
     *
     * @see [MedxAudioResourceManager.httpDatasourceFactory]
     * */
    @OptIn(UnstableApi::class)
    override fun playAudio(mediaItems: List<MediaItem>) {
        val mediaSources = arrayListOf<MediaSource>()
        repeat(mediaItems.size) { index ->
            val mediaItem = mediaItems[index]
            val dataSourceFactory: DataSource.Factory
            val mediaSource: MediaSource
            when (mediaItem.localConfiguration?.uri?.scheme) {
                "http", "https" -> {
                    dataSourceFactory =
                        medxAudioResourceManager.httpDatasourceFactory()
                    val cacheDataSourceFactory =
                        medxAudioResourceManager.cacheDatasourceFactory(context, dataSourceFactory)
                    mediaSource =
                        ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                            .createMediaSource(mediaItem)
                }

                "android.resource" -> {
                    dataSourceFactory =
                        medxAudioResourceManager.rawDatasourceFactory(context)
                    mediaSource =
                        ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)
                }

                "file" -> {
                    dataSourceFactory =
                        medxAudioResourceManager.fileDatasourceFactory()
                    mediaSource =
                        ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)
                }

                else -> {
                    dataSourceFactory =
                        medxAudioResourceManager.defaultDatasourceFactory(context)
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
        listenAudioPosition()
    }

    /**
     * pause an audio that currently playing.
     *
     * */
    override fun pause() {
        if (medxAudioPlayerState == MedxAudioPlayerState.PLAYING) {
            removeListenerAudioPosition()
            exoPlayer.pause()
            onAudioPlayerStateChanged(MedxAudioPlayerState.PAUSED)
        }
    }

    /**
     * resume an audio that currently playing.
     *
     * */
    override fun resume() {
        if (medxAudioPlayerState == MedxAudioPlayerState.PAUSED) {
            exoPlayer.play()
            onAudioPlayerStateChanged(MedxAudioPlayerState.PLAYING)
            listenAudioPosition()
        }
    }

    /**
     * stop an audio that currently playing.
     *
     * */
    override fun stop() {
        if (medxAudioPlayerState == MedxAudioPlayerState.PLAYING || medxAudioPlayerState == MedxAudioPlayerState.STOPPED) {
            removeListenerAudioPosition()
            exoPlayer.stop()
            onAudioPlayerStateChanged(MedxAudioPlayerState.STOPPED)
        }
    }

    /**
     * skip to previous media item if there is any previous media item.
     *
     * to enable skip to previous media item, add multiple audio when play an audio
     *
     * @see playAudio
     * @author @fadlurahmanfdev - Taufik Fadlurahman Fajari
     * */
    override fun skipToPreviousMediaItem() {
        if (exoPlayer.hasPreviousMediaItem()) {
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
     * skip to next media item if there is any next media item
     *
     * to enable skip to next media item, add multiple audio when play an audio
     *
     * @see playAudio
     * @author @fadlurahmanfdev - Taufik Fadlurahman Fajari
     * */
    override fun skipToNextMediaItem() {
        if (exoPlayer.hasNextMediaItem()) {
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
        if (medxAudioPlayerState != MedxAudioPlayerState.STOPPED) {
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
    override fun onAudioPlayerStateChanged(state: MedxAudioPlayerState) {
        if (medxAudioPlayerState != state) {
            Log.d(
                this::class.java.simpleName,
                "Medx-LOG %%% - audio state changed from $medxAudioPlayerState into $state"
            )
            _medxAudioPlayerState = state
            listener?.onPlayerStateChanged(state)
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        when (playbackState) {
            Player.STATE_IDLE -> {
                onAudioPlayerStateChanged(MedxAudioPlayerState.READY)
            }

            Player.STATE_BUFFERING -> {
                onAudioPlayerStateChanged(MedxAudioPlayerState.BUFFERING)
            }

            Player.STATE_READY -> {
                _duration = exoPlayer.duration
                listener?.onDurationChanged(duration)
                onAudioPlayerStateChanged(MedxAudioPlayerState.READY)
            }

            Player.STATE_ENDED -> {
                onAudioPlayerStateChanged(MedxAudioPlayerState.ENDED)
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (isPlaying) {
            onAudioPlayerStateChanged(MedxAudioPlayerState.PLAYING)
        }
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)
        listener?.onMediaMetadataChanged(mediaMetadata)
    }
}