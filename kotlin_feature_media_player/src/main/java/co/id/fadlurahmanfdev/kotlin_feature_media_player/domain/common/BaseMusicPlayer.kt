package co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.common

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.state.MusicPlayerState
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.utilities.CacheUtilities

@UnstableApi
abstract class BaseMusicPlayer(private val context: Context) {
    private var audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val handler = Handler(Looper.getMainLooper())
    lateinit var exoPlayer: ExoPlayer

    private fun getExoPlayerBuilder(): ExoPlayer.Builder {
        return ExoPlayer.Builder(context)
    }

    open fun initExoPlayer() {
        exoPlayer = getExoPlayerBuilder().build()
        exoPlayer.playWhenReady = true
    }


    private var currentDuration: Long? = null
    private var currentPosition: Long? = null
    open fun fetchAudioDurationAndPosition(callback: FeatureMusicPlayerCallback) {
        if (currentDuration == null || (currentDuration
                ?: 0L) <= 0L || exoPlayer.duration != currentDuration
        ) {
            currentDuration = exoPlayer.duration
            callback.onDurationChanged(currentDuration!!)
        }

        if (currentPosition == null || exoPlayer.currentPosition != currentPosition) {
            currentPosition = exoPlayer.currentPosition
            callback.onPositionChanged(currentPosition!!)
        }
    }

    open fun createCacheDataSinkFactory(): CacheDataSink.Factory {
        return CacheDataSink.Factory()
            .setCache(CacheUtilities.getSimpleCache(context))
    }

    open fun createHttpDataSource(): DefaultDataSource.Factory {
        val dataSource = DefaultHttpDataSource.Factory()
        return DefaultDataSource.Factory(context, dataSource)
    }

    open fun createProgressiveMediaSource(
        datasourceFactory: DataSource.Factory,
        mediaItem: MediaItem
    ): ProgressiveMediaSource {
        return ProgressiveMediaSource.Factory(datasourceFactory).createMediaSource(mediaItem)
    }

    open fun createCacheDataSource(): CacheDataSource.Factory {
        return CacheDataSource.Factory()
            .setCache(CacheUtilities.getSimpleCache(context))
            .setCacheWriteDataSinkFactory(createCacheDataSinkFactory())
            .setCacheReadDataSourceFactory(FileDataSource.Factory())
            .setUpstreamDataSourceFactory(createHttpDataSource())
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    private var currentAudioDeviceInfo: AudioDeviceInfo? = null

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkAudioOutputAboveM(callback: FeatureMusicPlayerCallback) {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)


        if (currentAudioDeviceInfo == null || (devices.isNotEmpty() && currentAudioDeviceInfo?.id != devices.last().id)) {
            currentAudioDeviceInfo = devices.last()
            callback.onAudioOutputChanged(currentAudioDeviceInfo!!)
        }
    }

    interface FeatureMusicPlayerCallback {
        fun onStateChanged(state: MusicPlayerState) {}
        //        fun onPlaybackStateChanged(playbackState: Int)
        fun onDurationChanged(duration: Long)
        fun onPositionChanged(position: Long)
        fun onAudioOutputChanged(audioDeviceInfo: AudioDeviceInfo)
//        fun onErrorHappened(exception: ExoPlaybackException)
    }
}