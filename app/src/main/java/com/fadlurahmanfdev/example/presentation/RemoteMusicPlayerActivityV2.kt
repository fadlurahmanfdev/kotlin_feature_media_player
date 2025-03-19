package com.fadlurahmanfdev.example.presentation

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.fadlurahmanfdev.example.R
import com.fadlurahmanfdev.example.domain.service.AppAudioPlayerServiceV2
import com.fadlurahmanfdev.medx.MedxAudioPlayerManager

class RemoteMusicPlayerActivityV2 : AppCompatActivity() {
    lateinit var seekBar: SeekBar

    lateinit var tvTitle: TextView
    lateinit var tvArtist: TextView

    lateinit var ivPrevious: ImageView
    lateinit var ivPlay: ImageView
    lateinit var ivNext: ImageView

    private lateinit var audios:List<MediaItem>

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


        MedxAudioPlayerManager.playRemoteAudio(
            this,
            notificationId = 1,
            mediaItems = audios,
            AppAudioPlayerServiceV2::class.java
        )
    }

    @UnstableApi
    override fun onDestroy() {
        super.onDestroy()
    }
}