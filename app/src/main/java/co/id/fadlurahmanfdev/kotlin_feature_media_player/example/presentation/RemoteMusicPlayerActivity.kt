package co.id.fadlurahmanfdev.kotlin_feature_media_player.example.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.state.MusicPlayerState
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.manager.FeatureMusicPlayerManager
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.manager.FeatureMusicPlayerReceiverManager
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.R
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.domain.receiver.ExampleMusicPlayerReceiver
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.domain.service.ExampleMusicPlayerService

class RemoteMusicPlayerActivity : AppCompatActivity(), FeatureMusicPlayerReceiverManager.CallBack {
    lateinit var seekBar: SeekBar

    private lateinit var featurePlayerReceiverManager: FeatureMusicPlayerReceiverManager

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_remote_music_player)
        seekBar = findViewById(R.id.seekbar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        featurePlayerReceiverManager = FeatureMusicPlayerReceiverManager(this)
        featurePlayerReceiverManager.setCallBack(this)
        featurePlayerReceiverManager.registerReceiver(this)

        FeatureMusicPlayerManager.playRemoteAudio(
            this,
            notificationId = 1,
            urls = listOf(
                "https://equran.nos.wjv-1.neo.id/audio-full/Abdullah-Al-Juhany/110.mp3",
                "https://equran.nos.wjv-1.neo.id/audio-full/Abdullah-Al-Juhany/111.mp3",
            ),
            title = "Title",
            artist = "Artist",
            ExampleMusicPlayerService::class.java
        )
    }

    @UnstableApi
    override fun onDestroy() {
        featurePlayerReceiverManager.unregisterReceiver(this)
        super.onDestroy()
    }

    override fun onSendInfo(position: Long, duration: Long, state: MusicPlayerState) {
        seekBar.max = duration.toInt()
        seekBar.progress = position.toInt()
    }
}