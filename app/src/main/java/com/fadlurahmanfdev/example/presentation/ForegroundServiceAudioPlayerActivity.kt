package com.fadlurahmanfdev.example.presentation

import android.content.ContentResolver
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
import com.fadlurahmanfdev.example.domain.service.AppMedxAudioPlayerService
import com.fadlurahmanfdev.medx_player.MedxPlayerManager
import com.fadlurahmanfdev.medx_player.data.enums.MedxPlayerState
import com.fadlurahmanfdev.medx_player.data.enums.MedxPlayerState.BUFFERING
import com.fadlurahmanfdev.medx_player.data.enums.MedxPlayerState.PAUSED
import com.fadlurahmanfdev.medx_player.data.enums.MedxPlayerState.PLAYING
import com.fadlurahmanfdev.medx_player.utilities.MedxPlayerUtilities

class ForegroundServiceAudioPlayerActivity : AppCompatActivity(), MedxPlayerManager.Listener {
    lateinit var medxAudioPlayerManager: MedxPlayerManager

    lateinit var seekBar: SeekBar

    lateinit var tvTitle: TextView
    lateinit var tvArtist: TextView

    lateinit var ivPrevious: ImageView
    lateinit var ivPlay: ImageView
    lateinit var ivNext: ImageView
    lateinit var tvPosition: TextView
    lateinit var tvDuration: TextView

    private lateinit var mediaItems: List<MediaItem>

    private var audioState = MedxPlayerState.IDLE

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_audio_player)
        seekBar = findViewById(R.id.seekbar)
        tvTitle = findViewById(R.id.tv_title)
        tvArtist = findViewById(R.id.tv_artist)
        ivPrevious = findViewById(R.id.iv_previous)
        ivPlay = findViewById(R.id.iv_play)
        ivNext = findViewById(R.id.iv_next)
        tvPosition = findViewById(R.id.tv_progress)
        tvDuration = findViewById(R.id.tv_duration)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        mediaItems = listOf(
            MediaItem.Builder()
                .setUri(Uri.parse("https://www.bensound.com/bensound-music/bensound-acousticbreeze.mp3"))
                .setMediaMetadata(
                    MediaMetadata.Builder().setTitle("Acoustic Breeze").setArtist("Bensound")
                        .setArtworkUri(Uri.parse("https://www.bensound.com/bensound-img/acousticbreeze.jpg"))
                        .setMediaType(MediaMetadata.MEDIA_TYPE_AUDIO_BOOK_CHAPTER)
                        .build()
                )
                .build(),
            MediaItem.Builder()
                .setUri(Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).path(R.raw.bensound_creativeminds.toString()).build())
                .setMediaMetadata(
                    MediaMetadata.Builder().setTitle("Creative Minds").setArtist("Bensound")
                        .setArtworkUri(Uri.parse("https://www.bensound.com/bensound-img/creativeminds.jpg"))
                        .setMediaType(MediaMetadata.MEDIA_TYPE_AUDIO_BOOK_CHAPTER)
                        .build()
                )
                .build(),
        )

        tvTitle.text = "-"
        tvArtist.text = "-"

        medxAudioPlayerManager = MedxPlayerManager(this)
        medxAudioPlayerManager.addListener(this)

        ivPlay.setOnClickListener {
            when (audioState) {
                PLAYING -> {
                    MedxPlayerManager.pause(
                        this,
                        AppMedxAudioPlayerService::class.java
                    )
                }

                PAUSED -> {
                    MedxPlayerManager.resume(
                        this,
                        notificationId = 1,
                        AppMedxAudioPlayerService::class.java
                    )
                }

                else -> {

                }
            }
        }

        ivPrevious.setOnClickListener {
            MedxPlayerManager.skipToPrevious(
                context = this,
                AppMedxAudioPlayerService::class.java
            )
        }

        ivNext.setOnClickListener {
            MedxPlayerManager.skipToNext(context = this, AppMedxAudioPlayerService::class.java)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    Log.d(
                        this@ForegroundServiceAudioPlayerActivity::class.java.simpleName,
                        "on progress changed: $progress"
                    )
                    MedxPlayerManager.seekToPosition(
                        this@ForegroundServiceAudioPlayerActivity,
                        progress.toLong(),
                        AppMedxAudioPlayerService::class.java
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })


        MedxPlayerManager.playAudio(
            this,
            notificationId = 1,
            mediaItems = mediaItems,
            AppMedxAudioPlayerService::class.java
        )
    }

    override fun onPause() {
        medxAudioPlayerManager.unregisterReceiver(this)
        super.onPause()
    }

    override fun onResume() {
        medxAudioPlayerManager.registerReceiver(this)
        super.onResume()
    }

    @UnstableApi
    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onReceiveInfoDuration(duration: Long) {
        Log.d(this::class.java.simpleName, "Medx-LOG %%% - receive info duration: $duration")
        seekBar.max = duration.toInt()
        tvDuration.text = MedxPlayerUtilities.formatDuration(duration)
    }

    override fun onReceiveInfoPosition(position: Long) {
        seekBar.progress = position.toInt()
        tvPosition.text = MedxPlayerUtilities.formatDuration(position)
    }

    override fun onReceiveInfoState(state: MedxPlayerState) {
        audioState = state
        Log.d(this::class.java.simpleName, "App-MedX-LOG %%% on receive info state: $state")
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