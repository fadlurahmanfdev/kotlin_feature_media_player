package com.fadlurahmanfdev.medx.domain.common

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
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
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    Log.d(
                        this@BaseMedxAudioPlayerV2::class.java.simpleName,
                        "Medx-LOG %%% media session - on connect"
                    )
                    return super.onConnect(session, controller)
                }

                @UnstableApi
                override fun onMediaButtonEvent(
                    session: MediaSession,
                    controllerInfo: MediaSession.ControllerInfo,
                    intent: Intent
                ): Boolean {
                    Log.d(
                        this@BaseMedxAudioPlayerV2::class.java.simpleName,
                        "Medx-LOG %%% media session - on media button event"
                    )
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
        listener?.onPlayerStateChanged(AudioPlayerState.IDLE)
        handler.postDelayed(audioPositionRunnable, 1000)
    }

    fun pause() {
        handler.removeCallbacks(audioPositionRunnable)
        exoPlayer.pause()
    }

    fun stop() {
        handler.removeCallbacks(audioPositionRunnable)
        exoPlayer.stop()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == Player.STATE_READY) {
            _duration = exoPlayer.duration
            listener?.onDurationChanged(duration)
        }
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
    }
}