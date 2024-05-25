package co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.plugin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.common.BaseMusicPlayer

@OptIn(UnstableApi::class)
class FeatureMusicPlayer(override val context: Context) : BaseMusicPlayer(context) {
    var headsetPluginReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val state = intent?.getIntExtra("state", -1)
            val name = intent?.getStringExtra("name")
            val microphone = intent?.getIntExtra("microphone", -1)
            println("MASUK SINI ${state} & $name & $microphone")
        }

    }

    fun registerListenerHeadsetPlug() {
        context.registerReceiver(
            headsetPluginReceiver,
            IntentFilter(AudioManager.ACTION_HEADSET_PLUG)
        )
    }

    fun destroyListener() {
        context.unregisterReceiver(headsetPluginReceiver)
    }
}