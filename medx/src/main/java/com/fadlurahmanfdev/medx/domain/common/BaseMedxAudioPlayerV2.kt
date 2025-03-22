package com.fadlurahmanfdev.medx.domain.common

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.MediaSession
import com.fadlurahmanfdev.medx.data.enums.AudioPlayerState
import com.fadlurahmanfdev.medx.utilities.CacheUtilities


open class BaseMedxAudioPlayerV2(private val context: Context) : Player.Listener {
    lateinit var exoPlayer: ExoPlayer
    lateinit var mediaSession: MediaSession
    private var listener: Listener? = null

    private var _duration: Long = 0L
    val duration: Long
        get() = _duration
    private var _position: Long = 0L
    val position: Long
        get() = _position

    private var _audioPlayerState: AudioPlayerState = AudioPlayerState.IDLE
    val state: AudioPlayerState
        get() = _audioPlayerState

    fun addListener(listener: Listener) {
        this.listener = listener
    }

    fun removeListener() {
        this.listener = null
    }

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

    open fun initialize() {
        exoPlayer = ExoPlayer.Builder(context).build()
        exoPlayer.addListener(this)

        mediaSession =
            MediaSession.Builder(context, exoPlayer).setCallback(object : MediaSession.Callback {

                @UnstableApi
                override fun onMediaButtonEvent(
                    session: MediaSession,
                    controllerInfo: MediaSession.ControllerInfo,
                    intent: Intent
                ): Boolean {
                    Log.d(
                        this@BaseMedxAudioPlayerV2::class.java.simpleName,
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
                            this@BaseMedxAudioPlayerV2::class.java.simpleName,
                            "Medx-LOG %%% media session, received key event: $keyEvent"
                        )

                        if (keyEvent != null) {
                            when (keyEvent.keyCode) {
                                KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                                    onAudioStateChanged(AudioPlayerState.PAUSED)
                                }

                                KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                    onAudioStateChanged(AudioPlayerState.PLAYING)
                                }
                            }
                        }
                    }
                    return super.onMediaButtonEvent(session, controllerInfo, intent)
                }
            }).build()
    }

    private fun createHttpDatasourceFactory(): DefaultHttpDataSource.Factory {
        return DefaultHttpDataSource.Factory()
    }

    @UnstableApi
    private fun createCacheDataSinkFactory(): CacheDataSink.Factory {
        return CacheDataSink.Factory()
            .setCache(CacheUtilities.getSimpleCache(context))
    }

    @UnstableApi
    private fun createCacheDatasourceFactory(
        dataSourceFactory: DataSource.Factory
    ): CacheDataSource.Factory {
        return CacheDataSource.Factory()
            .setCache(CacheUtilities.getSimpleCache(context))
            .setCacheWriteDataSinkFactory(createCacheDataSinkFactory())
            .setCacheReadDataSourceFactory(FileDataSource.Factory())
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    @UnstableApi
    fun playRemoteAudio(mediaItems: List<MediaItem>) {
        val dataSourceFactory: DataSource.Factory = createHttpDatasourceFactory()
        val cacheDataSourceFactory = createCacheDatasourceFactory(dataSourceFactory)
        val mediaSources = arrayListOf<MediaSource>()
        repeat(mediaItems.size) { index ->
            val mediaSource: MediaSource =
                ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                    .createMediaSource(mediaItems[index])
            mediaSources.add(mediaSource)
        }
        exoPlayer.setMediaSources(mediaSources)
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
        handler.postDelayed(audioPositionRunnable, 1000)
    }

    fun pause() {
        if (_audioPlayerState == AudioPlayerState.PLAYING) {
            handler.removeCallbacks(audioPositionRunnable)
            exoPlayer.pause()
            onAudioStateChanged(AudioPlayerState.PAUSED)
        }
    }

    fun resume() {
        if (_audioPlayerState == AudioPlayerState.PAUSED) {
            exoPlayer.play()
            onAudioStateChanged(AudioPlayerState.PLAYING)
            handler.postDelayed(audioPositionRunnable, 500)
        }
    }

    fun stop() {
        if (_audioPlayerState == AudioPlayerState.PLAYING || _audioPlayerState == AudioPlayerState.STOPPED) {
            handler.removeCallbacks(audioPositionRunnable)
            exoPlayer.stop()
            onAudioStateChanged(AudioPlayerState.STOPPED)
        }
    }

    fun skipToPreviousItem() {
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

    fun skipToNextItem() {
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

    fun seekToPosition(position: Long) {
        exoPlayer.seekTo(position)
        _position = position
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        when (playbackState) {
            Player.STATE_IDLE -> {
                onAudioStateChanged(AudioPlayerState.READY)
            }

            Player.STATE_BUFFERING -> {
                onAudioStateChanged(AudioPlayerState.BUFFERING)
            }

            Player.STATE_READY -> {
                _duration = exoPlayer.duration
                listener?.onDurationChanged(duration)
                onAudioStateChanged(AudioPlayerState.READY)
            }

            Player.STATE_ENDED -> {
                onAudioStateChanged(AudioPlayerState.ENDED)
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (isPlaying) {
            onAudioStateChanged(AudioPlayerState.PLAYING)
        }
    }

    private fun onAudioStateChanged(state: AudioPlayerState) {
        if (_audioPlayerState != state) {
            Log.d(
                this::class.java.simpleName,
                "Medx-LOG %%% - audio state changed from $_audioPlayerState into $state"
            )
            _audioPlayerState = state
            listener?.onPlayerStateChanged(state)
        }
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)
        listener?.onMediaMetadataChanged(mediaMetadata)
    }

    interface Listener {
        /**
         * this function triggered when state of audio player changed.
         *
         * @param state the state of the playback (e.g., [AudioPlayerState.IDLE], [AudioPlayerState.BUFFERING], etc)
         * */
        fun onPlayerStateChanged(state: AudioPlayerState) {}
        fun onDurationChanged(duration: Long) {}
        fun onPositionChanged(position: Long) {}
        fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {}
    }
}