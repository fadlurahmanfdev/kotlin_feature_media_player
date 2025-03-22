package com.fadlurahmanfdev.example.presentation

import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.media3.common.util.UnstableApi
import com.fadlurahmanfdev.example.R
import com.fadlurahmanfdev.example.domain.service.AppAudioPlayerServiceV2
import com.fadlurahmanfdev.medx.MedxAudioPlayerManager
import com.fadlurahmanfdev.medx.data.enums.MedxAudioPlayerState
import com.fadlurahmanfdev.medx.data.enums.MedxAudioPlayerState.BUFFERING
import com.fadlurahmanfdev.medx.data.enums.MedxAudioPlayerState.PAUSED
import com.fadlurahmanfdev.medx.data.enums.MedxAudioPlayerState.PLAYING

class RemoteMusicPlayerActivityV2 : AppCompatActivity(), MedxAudioPlayerManager.Listener {
    lateinit var medxAudioPlayerManager: MedxAudioPlayerManager

    lateinit var seekBar: SeekBar

    lateinit var tvTitle: TextView
    lateinit var tvArtist: TextView

    lateinit var ivPrevious: ImageView
    lateinit var ivPlay: ImageView
    lateinit var ivNext: ImageView

    private lateinit var audios: List<MediaItem>

    private var audioState = MedxAudioPlayerState.IDLE

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
                .setMediaMetadata(
                    MediaMetadata.Builder().setTitle("SURAH 110").setArtist("AL QURAN").build()
                )
                .build(),
            MediaItem.Builder()
                .setUri(Uri.parse("https://equran.nos.wjv-1.neo.id/audio-full/Abdullah-Al-Juhany/111.mp3"))
                .setMediaMetadata(
                    MediaMetadata.Builder().setTitle("SURAH 111").setArtist("AL QURAN V2").build()
                )
                .build(),
        )

        tvTitle.text = "-"
        tvArtist.text = "-"

        ivPlay.setOnClickListener {
            when (audioState) {
                PLAYING -> {
                    MedxAudioPlayerManager.pause(
                        this,
                        AppAudioPlayerServiceV2::class.java
                    )
                }

                PAUSED -> {
                    MedxAudioPlayerManager.resume(
                        this,
                        notificationId = 1,
                        AppAudioPlayerServiceV2::class.java
                    )
                }

                else -> {

                }
            }
        }

        ivPrevious.setOnClickListener {
            MedxAudioPlayerManager.skipToPrevious(context = this, AppAudioPlayerServiceV2::class.java)
        }

        ivNext.setOnClickListener {
            MedxAudioPlayerManager.skipToNext(context = this, AppAudioPlayerServiceV2::class.java)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    Log.d(
                        this@RemoteMusicPlayerActivityV2::class.java.simpleName,
                        "on progress changed: $progress"
                    )
                    MedxAudioPlayerManager.seekToPosition(
                        this@RemoteMusicPlayerActivityV2,
                        progress.toLong(),
                        AppAudioPlayerServiceV2::class.java
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        medxAudioPlayerManager = MedxAudioPlayerManager(this)
        medxAudioPlayerManager.registerReceiver(this)
        medxAudioPlayerManager.addListener(this)


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

    override fun onReceiveInfoDuration(duration: Long) {
        Log.d(this::class.java.simpleName, "Medx-LOG %%% - receive info duration: $duration")
        seekBar.max = duration.toInt()
    }

    override fun onReceiveInfoPosition(position: Long) {
        seekBar.progress = position.toInt()
    }

    override fun onReceiveInfoState(state: MedxAudioPlayerState) {
        audioState = state
        when (state) {
            BUFFERING, PLAYING -> {
                ivPlay.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.baseline_pause_24
                    )
                )
            }

            else -> {
                ivPlay.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.baseline_play_arrow_24
                    )
                )
            }
        }

        Log.d(this::class.java.simpleName, "Medx-LOG %%% - receive info state: $state")
    }

    override fun onReceiveInfoMediaMetaData(mediaMetadata: MediaMetadata) {
        tvTitle.text = mediaMetadata.title
        tvArtist.text = mediaMetadata.artist

    }
}