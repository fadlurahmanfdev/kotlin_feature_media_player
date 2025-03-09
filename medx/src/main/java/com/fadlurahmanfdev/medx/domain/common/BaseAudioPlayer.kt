package com.fadlurahmanfdev.medx.domain.common

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.MediaSession
import com.fadlurahmanfdev.medx.data.enums.AudioPlayerEvent
import com.fadlurahmanfdev.medx.data.enums.AudioPlayerState
import com.fadlurahmanfdev.medx.utilities.CacheUtilities

@UnstableApi
abstract class BaseAudioPlayer(open val context: Context) : Player.Listener, AnalyticsListener {
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSession: MediaSession
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

    open fun initialize() {
        exoPlayer = ExoPlayer.Builder(context).build()
//        mediaSession = androidx.media3.session.MediaSession.Builder(context, exoPlayer).setCallback().build()

        exoPlayer.addListener(this)
        exoPlayer.addAnalyticsListener(this)
    }

    private fun createHttpDatasourceFactory(): DefaultHttpDataSource.Factory {
        return DefaultHttpDataSource.Factory()
    }

    private fun createCacheDataSinkFactory(): CacheDataSink.Factory {
        return CacheDataSink.Factory()
            .setCache(CacheUtilities.getSimpleCache(context))
    }

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
        listener?.onAudioStateChanged(AudioPlayerState.IDLE)
        exoPlayer.prepare()
    }

    fun pause() {
        if (state == AudioPlayerState.PLAYING) {
            handler.removeCallbacks(audioPositionRunnable)
            exoPlayer.pause()
            onAudioStateChanged(AudioPlayerState.PAUSED)
        }
    }

    fun stop() {
        if (state == AudioPlayerState.PLAYING || state == AudioPlayerState.PAUSED) {
            handler.removeCallbacks(audioPositionRunnable)
            exoPlayer.stop()
            onAudioStateChanged(AudioPlayerState.STOPPED)
        }
    }

    fun resume() {
        if (state == AudioPlayerState.PAUSED) {
            exoPlayer.play()
            onEventChanged(AudioPlayerEvent.RESUME)
            onAudioStateChanged(AudioPlayerState.PLAYING)
            handler.postDelayed(audioPositionRunnable, 500)
        }
    }

    fun seekToPrevious() {
        if (exoPlayer.currentPosition > 3000) {
            Log.d(this::class.java.simpleName, "seek to 0 cause current position > 3000")
            exoPlayer.seekTo(0)
            _position = 0L
            onEventChanged(AudioPlayerEvent.SEEK_TO_ZERO)
        } else if (exoPlayer.hasPreviousMediaItem()) {
            Log.d(this::class.java.simpleName, "seek to previous media item")
            exoPlayer.seekToPreviousMediaItem()
            _position = 0L
            _duration = exoPlayer.duration
            onEventChanged(AudioPlayerEvent.SKIP_TO_PREVIOUS)
        } else {
            Log.d(
                this::class.java.simpleName,
                "unable to seek to previous media item cause player didnt have previous media item"
            )
        }
    }

    fun seekToNext() {
        if (exoPlayer.hasNextMediaItem()) {
            Log.d(this::class.java.simpleName, "seek to next media item")
            exoPlayer.seekToNextMediaItem()
            _position = 0L
            _duration = exoPlayer.duration
            onEventChanged(AudioPlayerEvent.SKIP_TO_NEXT)
        } else {
            Log.d(
                this::class.java.simpleName,
                "unable to seek to next media item cause player didnt have next media item"
            )
        }
    }

    /**
     * position: in milliSecond
     */
    fun seekToPosition(position: Long) {
        exoPlayer.seekTo(position)
        _position = position
        onEventChanged(AudioPlayerEvent.SEEK_TO_SPECIFIC_POSITION)
        onEventChanged(AudioPlayerEvent.RESUME)
        onAudioStateChanged(AudioPlayerState.PLAYING)
    }

    private val handler = Handler(Looper.getMainLooper())
    private val audioPositionRunnable = object : Runnable {
        override fun run() {
            if (exoPlayer.currentPosition > 0 && position != exoPlayer.currentPosition) {
                _position = exoPlayer.currentPosition
                listener?.onPositionChanged(position)
            }

            handler.postDelayed(this, 250)
        }
    }

//    override fun onIsLoadingChanged(isLoading: Boolean) {
//        super<Player.Listener>.onIsLoadingChanged(isLoading)
//        Log.d(BaseMusicPlayer::class.java.simpleName, "onIsLoadingChanged -> $isLoading")
//    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super<Player.Listener>.onIsPlayingChanged(isPlaying)
        Log.d(this::class.java.simpleName, "onIsPlayingChanged -> $isPlaying")
        if (isPlaying && state != AudioPlayerState.PLAYING) {
            onAudioStateChanged(AudioPlayerState.PLAYING)
        }
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super<Player.Listener>.onMediaMetadataChanged(mediaMetadata)
        listener?.onAudioMetadataChanged(mediaMetadata)
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        super<AnalyticsListener>.onPlaybackStateChanged(eventTime, state)
        Log.d(this::class.java.simpleName, "onPlaybackStateChanged -> $state")
        if (state == Player.STATE_IDLE && this.state != AudioPlayerState.IDLE) {
            onAudioStateChanged(AudioPlayerState.IDLE)
        } else if (state == Player.STATE_BUFFERING && this.state != AudioPlayerState.BUFFERING) {
            onAudioStateChanged(AudioPlayerState.BUFFERING)
        } else if (state == Player.STATE_READY && this.state != AudioPlayerState.READY) {
            _duration = exoPlayer.duration
            listener?.onDurationFetched(duration)
            handler.post(audioPositionRunnable)
            onAudioStateChanged(AudioPlayerState.READY)
        } else if (state == Player.STATE_ENDED && this.state != AudioPlayerState.ENDED) {
            handler.removeCallbacks(audioPositionRunnable)
            onAudioStateChanged(AudioPlayerState.ENDED)
        }
    }

    private fun onAudioStateChanged(state: AudioPlayerState) {
        Log.d(this::class.java.simpleName, "change audio state from $_audioPlayerState into $state")
        _audioPlayerState = state
        listener?.onAudioStateChanged(state)
    }

    private fun onEventChanged(event: AudioPlayerEvent) {
        listener?.onAudioEventChanged(event)
    }

    fun destroy() {
        handler.removeCallbacks(audioPositionRunnable)
        exoPlayer.pause()
        exoPlayer.stop()
        exoPlayer.release()
    }

    interface Listener {
        fun onAudioStateChanged(state: AudioPlayerState) {}
        fun onAudioMetadataChanged(metadata: MediaMetadata) {}
        fun onAudioEventChanged(event: AudioPlayerEvent) {}
        fun onDurationFetched(duration: Long) {}
        fun onPositionChanged(position: Long) {}
    }
}