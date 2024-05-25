package co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.plugin

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.state.MusicPlayerState
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.common.BaseMusicPlayer
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.utilities.CacheUtilities

class FeatureMusicPlayer(private val context: Context) : BaseMusicPlayer(context), Player.Listener {
    private var _duration: Long = 0L
    val duration: Long
        get() = _duration
    private var _position: Long = 0L
    val position: Long
        get() = _position
    private var _musicPlayerState: MusicPlayerState = MusicPlayerState.IDLE
    val musicPlayerState: MusicPlayerState
        get() = _musicPlayerState
    private var callback: FeatureMusicPlayerCallback? = null

    fun playRemoteAudio(url: String) {
        val datasourceFactory = DefaultHttpDataSource.Factory()
        val mediaItem = MediaItem.fromUri(Uri.parse(url))
        val cache = CacheUtilities.getSimpleCache(context)
        val cacheDataSourceFactory =
            CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(datasourceFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        val mediaSource = createProgressiveMediaSource(
            cacheDataSourceFactory, mediaItem
        )
        exoPlayer.addListener(this)
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
    }

    private fun updateOnStateChanged(state: MusicPlayerState) {
        _musicPlayerState = state
        callback?.onStateChanged(state)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (isPlaying && musicPlayerState != MusicPlayerState.PLAYING) {
            updateOnStateChanged(MusicPlayerState.PLAYING)
        }
    }
}