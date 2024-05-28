package co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.common

import android.content.Context
import android.net.Uri
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
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.state.MusicPlayerState
import co.id.fadlurahmanfdev.kotlin_feature_media_player.others.utilities.CacheUtilities

@UnstableApi
abstract class BaseMusicPlayer(open val context: Context) : Player.Listener, AnalyticsListener {
    private lateinit var exoPlayer: ExoPlayer
    private var callback: Callback? = null
    private var _duration: Long = 0L
    val duration: Long
        get() = _duration
    private var _position: Long = 0L
    val position: Long
        get() = _position
    private var _musicPlayerState: MusicPlayerState = MusicPlayerState.IDLE
    val musicPlayerState: MusicPlayerState
        get() = _musicPlayerState

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    open fun initialize() {
        exoPlayer = ExoPlayer.Builder(context).build()
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

    fun playRemoteAudio(uriString: String) {
        val dataSourceFactory: DataSource.Factory = createHttpDatasourceFactory()
        val mediaItem = MediaItem.fromUri(Uri.parse(uriString))
        val cacheDataSourceFactory = createCacheDatasourceFactory(dataSourceFactory)
        val mediaSource: MediaSource =
            ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(mediaItem)
        exoPlayer.setMediaSource(mediaSource)
        updateOnStateChanged(MusicPlayerState.IDLE)
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
    }

    fun playRemoteAudio(listUriString: List<String>) {
        val dataSourceFactory: DataSource.Factory = createHttpDatasourceFactory()
        val mediaItems = arrayListOf<MediaItem>()
        repeat(listUriString.size) { index ->
            val mediaItem = MediaItem.fromUri(Uri.parse(listUriString[index]))
            mediaItems.add(mediaItem)
        }
        val cacheDataSourceFactory = createCacheDatasourceFactory(dataSourceFactory)
        val mediaSources = arrayListOf<MediaSource>()
        repeat(mediaItems.size) { index ->
            val mediaSource: MediaSource =
                ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                    .createMediaSource(mediaItems[index])
            mediaSources.add(mediaSource)
        }
        exoPlayer.setMediaSources(mediaSources)
        updateOnStateChanged(MusicPlayerState.IDLE)
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
    }

    fun pause() {
        if (musicPlayerState == MusicPlayerState.PLAYING) {
            handler.removeCallbacks(fetchDurationAndPositionRunnable)
            exoPlayer.pause()
            updateOnStateChanged(MusicPlayerState.PAUSED)
        }
    }

    fun resume() {
        if (musicPlayerState == MusicPlayerState.PAUSED) {
            handler.post(fetchDurationAndPositionRunnable)
            exoPlayer.play()
            updateOnStateChanged(MusicPlayerState.RESUME)
            handler.postDelayed({ updateOnStateChanged(MusicPlayerState.PLAYING) }, 500)
        }
    }

    fun seekToPrevious() {
        if (exoPlayer.currentPosition > 3000) {
            Log.d(BaseMusicPlayer::class.java.simpleName, "seek to 0 cause current position > 3000")
            exoPlayer.seekTo(0)
        } else if (exoPlayer.hasPreviousMediaItem()) {
            Log.d(BaseMusicPlayer::class.java.simpleName, "seek to previous media item")
        } else {
            Log.d(
                BaseMusicPlayer::class.java.simpleName,
                "unable to seek to previous media item cause player didnt have previous media item"
            )
        }
    }

    fun seekToNext() {
        if (exoPlayer.hasNextMediaItem()) {
            Log.d(BaseMusicPlayer::class.java.simpleName, "seek to next media item")
            exoPlayer.seekToNextMediaItem()
        } else {
            Log.d(
                BaseMusicPlayer::class.java.simpleName,
                "unable to seek to next media item cause player didnt have next media item"
            )
        }
    }

    /**
     * position: in milliSecond
     */
    fun seekToPosition(position: Long) {
        exoPlayer.seekTo(position)
    }

    private val handler = Handler(Looper.getMainLooper())
    private val fetchDurationAndPositionRunnable = object : Runnable {
        override fun run() {
            if (exoPlayer.currentPosition > 0 && position != exoPlayer.currentPosition) {
                _position = exoPlayer.currentPosition
                callback?.onPositionChanged(position)
            }

            handler.postDelayed(this, 250)
        }
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {
        super<Player.Listener>.onIsLoadingChanged(isLoading)
        Log.d(BaseMusicPlayer::class.java.simpleName, "IS LOADING -> $isLoading")
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super<Player.Listener>.onIsPlayingChanged(isPlaying)
        Log.d(BaseMusicPlayer::class.java.simpleName, "IS PLAYING -> $isPlaying")
        if (isPlaying && musicPlayerState != MusicPlayerState.PLAYING) {
            updateOnStateChanged(MusicPlayerState.PLAYING)
        }
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        super<AnalyticsListener>.onPlaybackStateChanged(eventTime, state)
        Log.d(BaseMusicPlayer::class.java.simpleName, "PLAYBACK STATE -> $state")
        if (state == Player.STATE_IDLE && musicPlayerState != MusicPlayerState.IDLE) {
            updateOnStateChanged(MusicPlayerState.IDLE)
        } else if (state == Player.STATE_BUFFERING && musicPlayerState != MusicPlayerState.LOADING) {
            updateOnStateChanged(MusicPlayerState.LOADING)
        } else if (state == Player.STATE_READY && musicPlayerState != MusicPlayerState.READY) {
            _duration = exoPlayer.duration
            callback?.onDurationFetched(duration)
            handler.post(fetchDurationAndPositionRunnable)
            updateOnStateChanged(MusicPlayerState.READY)
        } else if (state == Player.STATE_ENDED && musicPlayerState != MusicPlayerState.ENDED) {
            updateOnStateChanged(MusicPlayerState.ENDED)
        }
    }

    private fun updateOnStateChanged(state: MusicPlayerState) {
        Log.d(BaseMusicPlayer::class.java.simpleName, "current music state: $state")
        _musicPlayerState = state
        callback?.onStateChanged(state)
    }

    fun destroy() {
        handler.removeCallbacks(fetchDurationAndPositionRunnable)
        exoPlayer.pause()
        exoPlayer.stop()
        exoPlayer.release()
    }

    interface Callback {
        fun onStateChanged(state: MusicPlayerState) {}
        fun onDurationFetched(duration: Long) {}
        fun onPositionChanged(position: Long) {}
    }
}