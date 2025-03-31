package com.fadlurahmanfdev.example.presentation

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.fadlurahmanfdev.example.R
import com.fadlurahmanfdev.medx_player.MedxAudioPlayer
import com.fadlurahmanfdev.medx_player.base.IMedxAudioPlayerListener
import com.fadlurahmanfdev.medx_player.data.enums.MedxAudioPlayerState

class SimpleAudioPlayerForAudioFileActivity : AppCompatActivity(), IMedxAudioPlayerListener {
    lateinit var medxAudioPlayer: MedxAudioPlayer

    lateinit var seekBar: SeekBar

    lateinit var tvTitle: TextView
    lateinit var tvArtist: TextView

    lateinit var ivPrevious: ImageView
    lateinit var ivPlay: ImageView
    lateinit var ivNext: ImageView

    private lateinit var mediaItems: ArrayList<MediaItem>

    private var audioState = MedxAudioPlayerState.IDLE

    val audioFilePickerLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriContents ->
        mediaItems = arrayListOf()
        uriContents.forEach { uriContent ->
            val mediaItem = MediaItem.Builder()
                .setUri(uriContent)
                .setMediaMetadata(
                    MediaMetadata.Builder().setTitle("Uri Content").setArtist("Uri Content Artist")
                        .setMediaType(MediaMetadata.MEDIA_TYPE_AUDIO_BOOK_CHAPTER)
                        .build()
                )
                .build()
            mediaItems.add(mediaItem)
        }

        medxAudioPlayer.playAudio(mediaItems)
    }

    val filePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                audioFilePickerLauncher.launch("audio/mpeg")
            }
        }

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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        medxAudioPlayer = MedxAudioPlayer(this)
        medxAudioPlayer.initialize()


        filePermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)

        tvTitle.text = "-"
        tvArtist.text = "-"

        ivPlay.setOnClickListener {
            when (audioState) {
                MedxAudioPlayerState.PLAYING -> {
                    medxAudioPlayer.pause()
                }

                MedxAudioPlayerState.PAUSED -> {
                    medxAudioPlayer.resume()
                }

                else -> {

                }
            }
        }

        ivPrevious.setOnClickListener {
            medxAudioPlayer.skipToPreviousMediaItem()
        }

        ivNext.setOnClickListener {
            medxAudioPlayer.skipToNextMediaItem()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    Log.d(
                        this@SimpleAudioPlayerForAudioFileActivity::class.java.simpleName,
                        "on progress changed: $progress"
                    )
                    medxAudioPlayer.seekToPosition(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        medxAudioPlayer.addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        medxAudioPlayer.stop()
        medxAudioPlayer.release()
    }

    override fun onPlayerStateChanged(state: MedxAudioPlayerState) {
        super.onPlayerStateChanged(state)
        audioState = state
        when (state) {
            MedxAudioPlayerState.BUFFERING, MedxAudioPlayerState.PLAYING -> {
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

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)
        tvTitle.text = mediaMetadata.title
        tvArtist.text = mediaMetadata.artist
    }

    override fun onDurationChanged(duration: Long) {
        super.onDurationChanged(duration)
        seekBar.max = duration.toInt()
    }

    override fun onPositionChanged(position: Long) {
        super.onPositionChanged(position)
        seekBar.progress = position.toInt()
    }

    override fun onPause() {
        super.onPause()
        medxAudioPlayer.pause()
    }
}