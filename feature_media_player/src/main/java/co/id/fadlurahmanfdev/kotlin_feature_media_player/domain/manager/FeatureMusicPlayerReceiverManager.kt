package co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.manager

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.state.MusicPlayerState
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.receiver.FeatureMusicPlayerReceiver
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.service.FeatureMusicPlayerService

class FeatureMusicPlayerReceiverManager(val context: Context) {
    var position: Long = -1L
    var duration: Long = -1L
    var state: MusicPlayerState = MusicPlayerState.IDLE
    private var callback: CallBack? = null
    private val receiver = object : BroadcastReceiver() {
        @UnstableApi
        override fun onReceive(context: Context?, intent: Intent?) {
            duration =
                intent?.getLongExtra(FeatureMusicPlayerService.PARAM_DURATION, -1L) ?: -1L
            position =
                intent?.getLongExtra(FeatureMusicPlayerService.PARAM_POSITION, -1L) ?: -1L
            val stateString = intent?.getStringExtra(FeatureMusicPlayerService.PARAM_STATE)
            state = MusicPlayerState.valueOf(stateString ?: "")

            callback?.onSendInfo(
                position = position,
                duration = duration,
                state = state,
            )
        }
    }

    fun setCallBack(callback: CallBack) {
        this.callback = callback
    }

    fun registerReceiver(activity: Activity) {
        activity.registerReceiver(
            receiver,
            IntentFilter(FeatureMusicPlayerReceiver.SEND_INFO)
        )
    }

    fun unregisterReceiver(activity: Activity) {
        activity.unregisterReceiver(receiver)
    }

    interface CallBack {
        fun onSendInfo(position: Long, duration: Long, state: MusicPlayerState)
    }
}