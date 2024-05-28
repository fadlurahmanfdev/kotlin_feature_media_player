package co.id.fadlurahmanfdev.kotlin_feature_media_player.example.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.util.UnstableApi
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.manager.FeatureMusicPlayerManager
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.R
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.domain.service.ExampleMusicPlayerService

class RemoteMusicPlayerActivity : AppCompatActivity() {

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_remote_music_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        FeatureMusicPlayerManager.playRemoteAudio(
            this,
            notificationId = 1,
            url = "https://equran.nos.wjv-1.neo.id/audio-full/Abdullah-Al-Juhany/110.mp3",
            title = "Title",
            artist = "Artist",
            ExampleMusicPlayerService::class.java
        )
    }

    @UnstableApi
    override fun onDestroy() {
        super.onDestroy()
    }
}