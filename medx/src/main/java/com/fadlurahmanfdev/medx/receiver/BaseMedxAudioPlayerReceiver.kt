package com.fadlurahmanfdev.medx.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fadlurahmanfdev.medx.constant.MedxConstant

abstract class BaseMedxAudioPlayerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(this::class.java.simpleName, "Medx-LOG %%% - receive action ${intent?.action}")

        if (context == null) {
            Log.w(
                this::class.java.simpleName,
                "Medx-LOG %%% - unable to continue, context is missing"
            )
            return
        }

        when (intent?.action) {
            MedxConstant.ACTION_PAUSE_AUDIO -> {
                onReceiveActionPauseAudio(context, intent)
            }

            MedxConstant.ACTION_RESUME_AUDIO -> {
                onReceiveActionResumeAudio(context, intent)
            }

            MedxConstant.ACTION_SKIP_TO_PREVIOUS_AUDIO -> {
                onReceiveActionSkipToPreviousAudio(context, intent)
            }

            MedxConstant.ACTION_SKIP_TO_NEXT_AUDIO -> {
                onReceiveActionSkipToNextAudio(context, intent)
            }

            MedxConstant.ACTION_SEEK_TO_POSITION_AUDIO -> {
                onReceiveActionSeekToPositionAudio(context, intent)
            }
        }
    }

    open fun onReceiveActionPauseAudio(context: Context, intent: Intent) {
        onPauseAudio(context)
    }

    open fun onReceiveActionResumeAudio(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(MedxConstant.PARAM_NOTIFICATION_ID, -1)
        Log.d(
            this::class.java.simpleName,
            "Medx-LOG %%% - receive action resume audio, get notification id: $notificationId"
        )
        if (notificationId != -1) {
            onResumeAudio(context, notificationId)
        }
    }

    open fun onReceiveActionSkipToPreviousAudio(context: Context, intent: Intent) {
        onSkipToPreviousAudio(context)
    }

    open fun onReceiveActionSkipToNextAudio(context: Context, intent: Intent) {
        onSkipToNextAudio(context)
    }

    open fun onReceiveActionSeekToPositionAudio(context: Context, intent: Intent) {
        val seekToPosition = intent.getLongExtra(MedxConstant.PARAM_SEEK_TO_POSITION, -1L)
        if (seekToPosition != -1L) {
            onSeekToPositionAudio(context, seekToPosition)
        }
    }

    abstract fun onPauseAudio(context: Context)
    abstract fun onResumeAudio(context: Context, notificationId: Int)
    abstract fun onSkipToPreviousAudio(context: Context)
    abstract fun onSkipToNextAudio(context: Context)
    abstract fun onSeekToPositionAudio(context: Context, position: Long)
}