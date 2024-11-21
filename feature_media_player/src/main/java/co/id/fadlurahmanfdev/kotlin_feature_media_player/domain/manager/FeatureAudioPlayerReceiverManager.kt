package co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.manager

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.constant.FeatureAudioPlayerConstant
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.enums.AudioPlayerEvent
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.enums.AudioPlayerState

class FeatureAudioPlayerReceiverManager(val context: Context) {
    var position: Long = -1L
    var duration: Long = -1L

    var title: String? = null
    var artist: String? = null


    var state: AudioPlayerState = AudioPlayerState.IDLE
    private var callback: CallBack? = null
    private val sendInfoReceiver = object : BroadcastReceiver() {
        @UnstableApi
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                duration =
                    intent?.getLongExtra(FeatureAudioPlayerConstant.PARAM_DURATION, -1L) ?: -1L
                position =
                    intent?.getLongExtra(FeatureAudioPlayerConstant.PARAM_POSITION, -1L) ?: -1L
                val stateString = intent?.getStringExtra(FeatureAudioPlayerConstant.PARAM_STATE)
                state = AudioPlayerState.valueOf(stateString ?: "")

                callback?.onReceiveInfo(
                    position = position,
                    duration = duration,
                    state = state,
                )
            } catch (e: Throwable) {
                Log.e(
                    this@FeatureAudioPlayerReceiverManager::class.java.simpleName,
                    "failed receive info: ${e.message}"
                )
            }
        }
    }

    private val sendInfoMetaDataReceiver = object : BroadcastReceiver() {
        @UnstableApi
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                title =
                    intent?.getStringExtra(FeatureAudioPlayerConstant.PARAM_TITLE)
                artist =
                    intent?.getStringExtra(FeatureAudioPlayerConstant.PARAM_ARTIST)

                callback?.onReceiveInfoMetaData(
                    title = title ?: "-",
                    artist = artist ?: "-"
                )
            } catch (e: Throwable) {
                Log.e(
                    this@FeatureAudioPlayerReceiverManager::class.java.simpleName,
                    "failed receive info: ${e.message}"
                )
            }
        }
    }

    private val sendEventReceiver = object : BroadcastReceiver() {
        @UnstableApi
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                val eventName = intent?.getStringExtra(FeatureAudioPlayerConstant.PARAM_EVENT)

                if (eventName == null) {
                    Log.e(
                        this@FeatureAudioPlayerReceiverManager::class.java.simpleName,
                        "event name missing"
                    )
                    return
                }

                val event = AudioPlayerEvent.valueOf(eventName)
                callback?.onReceiveEvent(event)
            } catch (e: Throwable) {
                Log.e(
                    this@FeatureAudioPlayerReceiverManager::class.java.simpleName,
                    "failed receive event: ${e.message}"
                )
            }
        }
    }

    fun setCallBack(callback: CallBack) {
        this.callback = callback
    }

    fun registerReceiver(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.registerReceiver(
                sendInfoReceiver,
                IntentFilter(FeatureAudioPlayerConstant.SEND_INFO),
                Context.RECEIVER_NOT_EXPORTED,
            )
            activity.registerReceiver(
                sendInfoMetaDataReceiver,
                IntentFilter(FeatureAudioPlayerConstant.SEND_INFO_META_DATA),
                Context.RECEIVER_NOT_EXPORTED,
            )
            activity.registerReceiver(
                sendEventReceiver,
                IntentFilter(FeatureAudioPlayerConstant.SEND_EVENT),
                Context.RECEIVER_NOT_EXPORTED,
            )

        } else {
            activity.registerReceiver(
                sendInfoReceiver,
                IntentFilter(FeatureAudioPlayerConstant.SEND_INFO),
            )
            activity.registerReceiver(
                sendInfoMetaDataReceiver,
                IntentFilter(FeatureAudioPlayerConstant.SEND_INFO_META_DATA),
            )
            activity.registerReceiver(
                sendEventReceiver,
                IntentFilter(FeatureAudioPlayerConstant.SEND_EVENT),
            )
        }
    }

    fun unregisterReceiver(activity: Activity) {
        activity.unregisterReceiver(sendInfoReceiver)
        activity.unregisterReceiver(sendInfoMetaDataReceiver)
        activity.unregisterReceiver(sendEventReceiver)
    }

    interface CallBack {
        fun onReceiveInfo(position: Long, duration: Long, state: AudioPlayerState)
        fun onReceiveInfoMetaData(title: String, artist: String)
        fun onReceiveEvent(event: AudioPlayerEvent)
    }
}