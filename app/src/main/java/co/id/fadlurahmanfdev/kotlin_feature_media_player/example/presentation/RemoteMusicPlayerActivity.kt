package co.id.fadlurahmanfdev.kotlin_feature_media_player.example.presentation

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.enums.AudioPlayerEvent
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.enums.AudioPlayerState
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.manager.FeatureAudioPlayerManager
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.manager.FeatureAudioPlayerReceiverManager
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.R
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.domain.service.ExampleAudioPlayerService

class RemoteMusicPlayerActivity : AppCompatActivity(), FeatureAudioPlayerReceiverManager.CallBack {
    lateinit var seekBar: SeekBar

    lateinit var tvTitle: TextView
    lateinit var tvArtist: TextView

    lateinit var ivPrevious: ImageView
    lateinit var ivPlay: ImageView
    lateinit var ivNext: ImageView

    private lateinit var audios:List<MediaItem>

    private lateinit var featurePlayerReceiverManager: FeatureAudioPlayerReceiverManager

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_remote_music_player)
        seekBar = findViewById(R.id.seekbar)
        tvTitle = findViewById(R.id.tv_title)
        tvArtist = findViewById(R.id.tv_artist)
        ivPrevious = findViewById(R.id.iv_previous)
        ivPlay = findViewById(R.id.iv_play)
        ivNext = findViewById(R.id.iv_next)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        audios = listOf(
            MediaItem.Builder()
                .setUri(Uri.parse("https://equran.nos.wjv-1.neo.id/audio-full/Abdullah-Al-Juhany/110.mp3"))
                .setMediaMetadata(MediaMetadata.Builder().setTitle("SURAH 110").setArtist("AL QURAN").build())
                .build(),
            MediaItem.Builder()
                .setUri(Uri.parse("https://equran.nos.wjv-1.neo.id/audio-full/Abdullah-Al-Juhany/111.mp3"))
                .setMediaMetadata(MediaMetadata.Builder().setTitle("SURAH 111").setArtist("AL QURAN V2").build())
                .build(),
        )

        tvTitle.text = audios.first().mediaMetadata.title
        tvArtist.text = audios.first().mediaMetadata.artist

        featurePlayerReceiverManager = FeatureAudioPlayerReceiverManager(this)
        featurePlayerReceiverManager.setCallBack(this)
        featurePlayerReceiverManager.registerReceiver(this)

        FeatureAudioPlayerManager.playRemoteAudio(
            this,
            notificationId = 1,
            mediaItems = audios,
            ExampleAudioPlayerService::class.java
        )
    }

    @UnstableApi
    override fun onDestroy() {
        featurePlayerReceiverManager.unregisterReceiver(this)
        super.onDestroy()
    }

    override fun onReceiveInfo(position: Long, duration: Long, state: AudioPlayerState) {
        seekBar.max = duration.toInt()
        seekBar.progress = position.toInt()

        if(state == AudioPlayerState.PAUSED){
            ivPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.baseline_play_arrow_24))
        }

        if (state == AudioPlayerState.PLAYING){
            ivPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.baseline_pause_24))
        }
    }

    @UnstableApi
    override fun onReceiveEvent(event: AudioPlayerEvent) {
        Log.d(this::class.java.simpleName, "success get event: ${event}")
    }

    override fun onReceiveInfoMetaData(title: String, artist: String) {
        tvTitle.text = title
        tvArtist.text = artist
    }
}