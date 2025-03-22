package com.fadlurahmanfdev.example.domain.receiver

import android.content.Context
import com.fadlurahmanfdev.example.domain.service.AppAudioPlayerServiceV2
import com.fadlurahmanfdev.medx.MedxAudioPlayerManager
import com.fadlurahmanfdev.medx.domain.receiver.BaseMedxAudioPlayerReceiver

class AppAudioPlayerReceiverV2 : BaseMedxAudioPlayerReceiver() {
    override fun onPauseAudio(context: Context) {
        MedxAudioPlayerManager.pause(context = context, clazz = AppAudioPlayerServiceV2::class.java)
    }

    override fun onResumeAudio(context: Context, notificationId: Int) {
        MedxAudioPlayerManager.resume(
            context = context,
            notificationId = notificationId,
            clazz = AppAudioPlayerServiceV2::class.java
        )
    }

    override fun onSkipToPreviousAudio(context: Context) {
        MedxAudioPlayerManager.skipToPrevious(
            context = context,
            clazz = AppAudioPlayerServiceV2::class.java
        )
    }

    override fun onSkipToNextAudio(context: Context) {
        MedxAudioPlayerManager.skipToNext(
            context = context,
            clazz = AppAudioPlayerServiceV2::class.java
        )
    }

    override fun onSeekToPositionAudio(context: Context, position:Long) {
        MedxAudioPlayerManager.seekToPosition(
            context = context,
            position = position,
            clazz = AppAudioPlayerServiceV2::class.java
        )
    }
}