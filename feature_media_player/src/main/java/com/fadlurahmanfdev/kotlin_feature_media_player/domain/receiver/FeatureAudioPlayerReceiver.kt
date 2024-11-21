package com.fadlurahmanfdev.kotlin_feature_media_player.domain.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.media3.common.util.UnstableApi
import com.fadlurahmanfdev.kotlin_feature_media_player.data.constant.FeatureAudioPlayerConstant
import com.fadlurahmanfdev.kotlin_feature_media_player.data.enums.AudioPlayerEvent
import java.util.Calendar

@UnstableApi
abstract class FeatureAudioPlayerReceiver : BroadcastReceiver() {
    companion object {
        private fun sendBroadcastSendEvent(
            context: Context,
            event: String,
        ) {
            val intent = Intent().apply {
                action = FeatureAudioPlayerConstant.SEND_EVENT
                putExtra(FeatureAudioPlayerConstant.PARAM_CHANNEL, this::class.java.simpleName)
                putExtra(FeatureAudioPlayerConstant.PARAM_EVENT, event)
            }
            context.sendBroadcast(intent)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            Log.e(this::class.java.simpleName, "context is missing")
            return
        }

        val action = intent?.action
        Log.d(
            this::class.java.simpleName,
            "action incoming: $action at ${Calendar.getInstance().time}"
        )
        when (intent?.action) {
            FeatureAudioPlayerConstant.ACTION_PAUSE_AUDIO -> {
                val notificationId =
                    intent.getIntExtra(FeatureAudioPlayerConstant.PARAM_NOTIFICATION_ID, -1)
                if (notificationId != -1) {
                    sendBroadcastSendEvent(context, AudioPlayerEvent.PAUSE.name)
                    onPauseAudio(context)
                } else {
                    Log.e(this::class.java.simpleName, "$action -> notificationId is missing")
                }
            }

            FeatureAudioPlayerConstant.ACTION_RESUME_AUDIO -> {
                val notificationId =
                    intent.getIntExtra(FeatureAudioPlayerConstant.PARAM_NOTIFICATION_ID, -1)
                if (notificationId != -1) {
                    sendBroadcastSendEvent(context, AudioPlayerEvent.RESUME.name)
                    onResumeAudio(context)
                } else {
                    Log.e(this::class.java.simpleName, "$action -> notificationId is missing")
                }
            }

            FeatureAudioPlayerConstant.ACTION_SKIP_TO_PREVIOUS_AUDIO -> {
                val notificationId =
                    intent.getIntExtra(FeatureAudioPlayerConstant.PARAM_NOTIFICATION_ID, -1)
                if (notificationId != -1) {
                    sendBroadcastSendEvent(context, AudioPlayerEvent.SKIP_TO_PREVIOUS.name)
                    onPreviousAudio(context)
                } else {
                    Log.e(this::class.java.simpleName, "$action -> notificationId is missing")
                }
            }

            FeatureAudioPlayerConstant.ACTION_SKIP_TO_NEXT_AUDIO -> {
                val notificationId =
                    intent.getIntExtra(FeatureAudioPlayerConstant.PARAM_NOTIFICATION_ID, -1)
                if (notificationId != -1) {
                    sendBroadcastSendEvent(context, AudioPlayerEvent.SKIP_TO_NEXT.name)
                    onNextAudio(context)
                } else {
                    Log.e(this::class.java.simpleName, "$action -> notificationId is missing")
                }
            }

            FeatureAudioPlayerConstant.ACTION_SEEK_TO_POSITION -> {
                val notificationId =
                    intent.getIntExtra(FeatureAudioPlayerConstant.PARAM_NOTIFICATION_ID, -1)
                val position =
                    intent.getLongExtra(FeatureAudioPlayerConstant.PARAM_SEEK_TO_POSITION, -1L)
                if (notificationId != -1 && position != -1L) {
                    sendBroadcastSendEvent(context, AudioPlayerEvent.SEEK_TO_SPECIFIC_POSITION.name)
                    onSeekToPosition(context, position = position)
                } else {
                    Log.e(this::class.java.simpleName, "$action -> notificationId is missing")
                }
            }
        }
    }

    abstract fun onPauseAudio(context: Context)
    abstract fun onResumeAudio(context: Context)
    abstract fun onPreviousAudio(context: Context)
    abstract fun onNextAudio(context: Context)
    abstract fun onSeekToPosition(context: Context, position: Long)
}