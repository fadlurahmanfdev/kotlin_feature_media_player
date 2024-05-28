package co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.service.FeatureMusicPlayerService
import java.util.Calendar

abstract class FeatureMusicPlayerReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_PAUSE_AUDIO = FeatureMusicPlayerService.ACTION_PAUSE_AUDIO
        const val ACTION_RESUME_AUDIO = FeatureMusicPlayerService.ACTION_RESUME_AUDIO
        const val ACTION_PREVIOUS_AUDIO = FeatureMusicPlayerService.ACTION_PREVIOUS_AUDIO
        const val ACTION_NEXT_AUDIO = FeatureMusicPlayerService.ACTION_NEXT_AUDIO
        const val SEND_INFO = FeatureMusicPlayerService.SEND_INFO

        const val PARAM_NOTIFICATION_ID = FeatureMusicPlayerService.PARAM_NOTIFICATION_ID
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            Log.e(FeatureMusicPlayerReceiver::class.java.simpleName, "context is missing")
            return
        }

        val action = intent?.action
        Log.d(
            FeatureMusicPlayerReceiver::class.java.simpleName,
            "${FeatureMusicPlayerReceiver::class.java} action incoming: $action, ${Calendar.getInstance().time}"
        )
        when (intent?.action) {
            ACTION_PAUSE_AUDIO -> {
                val notificationId = intent.getIntExtra(PARAM_NOTIFICATION_ID, -1)
                if (notificationId != -1) {
                    onPauseAudio(context)
                } else {
                    Log.e(
                        FeatureMusicPlayerReceiver::class.java.simpleName,
                        "$action -> notificationId is missing"
                    )
                }
            }

            ACTION_RESUME_AUDIO -> {
                val notificationId = intent.getIntExtra(PARAM_NOTIFICATION_ID, -1)
                if (notificationId != -1) {
                    onResumeAudio(context)
                } else {
                    Log.e(
                        FeatureMusicPlayerReceiver::class.java.simpleName,
                        "$action -> notificationId is missing"
                    )
                }
            }

            ACTION_PREVIOUS_AUDIO -> {
                val notificationId = intent.getIntExtra(PARAM_NOTIFICATION_ID, -1)
                if (notificationId != -1) {
                    onPreviousAudio(context)
                } else {
                    Log.e(
                        FeatureMusicPlayerReceiver::class.java.simpleName,
                        "$action -> notificationId is missing"
                    )
                }
            }

            ACTION_NEXT_AUDIO -> {
                val notificationId = intent.getIntExtra(PARAM_NOTIFICATION_ID, -1)
                if (notificationId != -1) {
                    onNextAudio(context)
                } else {
                    Log.e(
                        FeatureMusicPlayerReceiver::class.java.simpleName,
                        "$action -> notificationId is missing"
                    )
                }
            }
        }
    }

    abstract fun onPauseAudio(context: Context)
    abstract fun onResumeAudio(context: Context)
    abstract fun onPreviousAudio(context: Context)
    abstract fun onNextAudio(context: Context)
}