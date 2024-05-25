package co.id.fadlurahmanfdev.kotlin_feature_media_player.example.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.util.UnstableApi
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.plugin.FeatureMusicPlayer
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.R

class RemoteMusicPlayerActivity : AppCompatActivity() {
    lateinit var featureMusicPlayer: FeatureMusicPlayer

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

        featureMusicPlayer = FeatureMusicPlayer(this)
        featureMusicPlayer.initialize()
        featureMusicPlayer.registerListenerHeadsetPlug()
        featureMusicPlayer.playRemoteAudio("https://equran.nos.wjv-1.neo.id/audio-full/Abdullah-Al-Juhany/110.mp3")
    }

    @UnstableApi
    override fun onDestroy() {
        featureMusicPlayer.destroyListener()
        featureMusicPlayer.destroy()
        super.onDestroy()
    }
}