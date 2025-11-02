package com.fadlurahmanfdev.example.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.ui.PlayerView
import com.fadlurahmanfdev.example.R
import com.fadlurahmanfdev.medx_player.MedxPlayer

class SimpleVideoPlayerActivity : AppCompatActivity() {
    lateinit var medxVideoPlayer: MedxPlayer
    lateinit var playerView: PlayerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_simple_video_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        playerView = findViewById<PlayerView>(R.id.playerView)

        medxVideoPlayer = MedxPlayer(this)
        medxVideoPlayer.initialize()
        playerView.player = medxVideoPlayer.exoPlayer

        medxVideoPlayer.playMedia(
            listOf(
                MediaItem.Builder()
                    .setUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4".toUri())
                    .setMediaMetadata(
                        MediaMetadata.Builder().setTitle("Big Buck Bunny")
                            .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
                            .build()
                    )
                    .build()
            )
        )
    }

    override fun onPause() {
        medxVideoPlayer.pause()
        super.onPause()
    }

    override fun onStop() {
        medxVideoPlayer.stop()
        super.onStop()
    }

    override fun onDestroy() {
        medxVideoPlayer.release()
        super.onDestroy()
    }
}