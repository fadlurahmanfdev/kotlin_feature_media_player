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
    private var listener: Listener? = null
    private var _duration: Long = 0L
    val duration: Long
        get() = _duration
    private var _position: Long = 0L
    val position: Long
        get() = _position
    private var _musicPlayerState: MusicPlayerState = MusicPlayerState.IDLE
    val musicPlayerState: MusicPlayerState
        get() = _musicPlayerState

    fun addListener(listener: Listener) {
        this.listener = listener
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

    fun playRemoteAudio(listUriString: List<String>) {
        val dataSourceFactory: DataSource.Factory = createHttpDatasourceFactory()
        val mediaItems = arrayListOf<MediaItem>()
        repeat(listUriString.size) { index ->
            try {
                val mediaItem = MediaItem.fromUri(Uri.parse(listUriString[index]))
                mediaItems.add(mediaItem)
            } catch (t: Throwable) {
                Log.e(
                    BaseMusicPlayer::class.java.simpleName,
                    "error get media item from uri for ${listUriString[index]} caused by ${t.message}"
                )
            }
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
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
    }

    fun pause() {
        if (musicPlayerState == MusicPlayerState.PLAYING) {
            handler.removeCallbacks(audioPositionRunnable)
            exoPlayer.pause()
            updateOnStateChanged(MusicPlayerState.PAUSED)
        }
    }

    fun resume() {
        if (musicPlayerState == MusicPlayerState.PAUSED) {
            exoPlayer.play()
            updateOnStateChanged(MusicPlayerState.RESUME)
            updateOnStateChanged(MusicPlayerState.PLAYING)
            handler.postDelayed(audioPositionRunnable, 500)
        }
    }

    fun seekToPrevious() {
        if (exoPlayer.currentPosition > 3000) {
            Log.d(BaseMusicPlayer::class.java.simpleName, "seek to 0 cause current position > 3000")
            exoPlayer.seekTo(0)
            _position = 0L
            updateOnStateChanged(MusicPlayerState.SEEK_TO_ZERO)
        } else if (exoPlayer.hasPreviousMediaItem()) {
            Log.d(BaseMusicPlayer::class.java.simpleName, "seek to previous media item")
            exoPlayer.seekToPreviousMediaItem()
            _position = 0L
            _duration = exoPlayer.duration
            updateOnStateChanged(MusicPlayerState.SEEK_TO_PREVIOUS)
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
            _position = 0L
            _duration = exoPlayer.duration
            updateOnStateChanged(MusicPlayerState.SEEK_TO_NEXT)
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
        _position = position
        updateOnStateChanged(MusicPlayerState.SEEK_TO_SPECIFIC_POSITION)
        updateOnStateChanged(MusicPlayerState.RESUME)
        updateOnStateChanged(MusicPlayerState.PLAYING)
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
        Log.d(BaseMusicPlayer::class.java.simpleName, "onIsPlayingChanged -> $isPlaying")
        if (isPlaying && musicPlayerState != MusicPlayerState.PLAYING) {
            updateOnStateChanged(MusicPlayerState.PLAYING)
        }
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        super<AnalyticsListener>.onPlaybackStateChanged(eventTime, state)
        Log.d(BaseMusicPlayer::class.java.simpleName, "onPlaybackStateChanged -> $state")
        if (state == Player.STATE_IDLE && musicPlayerState != MusicPlayerState.IDLE) {
            updateOnStateChanged(MusicPlayerState.IDLE)
        } else if (state == Player.STATE_BUFFERING && musicPlayerState != MusicPlayerState.BUFFERING) {
            updateOnStateChanged(MusicPlayerState.BUFFERING)
        } else if (state == Player.STATE_READY && musicPlayerState != MusicPlayerState.READY) {
            _duration = exoPlayer.duration
            listener?.onDurationFetched(duration)
            handler.post(audioPositionRunnable)
            updateOnStateChanged(MusicPlayerState.READY)
        } else if (state == Player.STATE_ENDED && musicPlayerState != MusicPlayerState.ENDED) {
            handler.removeCallbacks(audioPositionRunnable)
            updateOnStateChanged(MusicPlayerState.ENDED)
        }
    }

    private fun updateOnStateChanged(state: MusicPlayerState) {
        Log.d(BaseMusicPlayer::class.java.simpleName, "current music state: $state")
        _musicPlayerState = state
        listener?.onStateChanged(state)
    }

    fun destroy() {
        handler.removeCallbacks(audioPositionRunnable)
        exoPlayer.pause()
        exoPlayer.stop()
        exoPlayer.release()
    }

    interface Listener {
        fun onStateChanged(state: MusicPlayerState) {}
        fun onDurationFetched(duration: Long) {}
        fun onPositionChanged(position: Long) {}
    }
}