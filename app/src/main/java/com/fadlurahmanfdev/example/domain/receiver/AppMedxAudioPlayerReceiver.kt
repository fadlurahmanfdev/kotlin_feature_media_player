package com.fadlurahmanfdev.example.domain.receiver

import android.content.Context
import com.fadlurahmanfdev.example.domain.service.AppMedxAudioPlayerService
import com.fadlurahmanfdev.medx_player.MedxAudioPlayerManager
import com.fadlurahmanfdev.medx_player.receiver.BaseMedxAudioPlayerReceiver

class AppMedxAudioPlayerReceiver : BaseMedxAudioPlayerReceiver() {
    override fun onPauseAudio(context: Context) {
        MedxAudioPlayerManager.pause(context = context, clazz = AppMedxAudioPlayerService::class.java)
    }

    override fun onResumeAudio(context: Context, notificationId: Int) {
        MedxAudioPlayerManager.resume(
            context = context,
            notificationId = notificationId,
            clazz = AppMedxAudioPlayerService::class.java
        )
    }

    override fun onSkipToPreviousAudio(context: Context) {
        MedxAudioPlayerManager.skipToPrevious(
            context = context,
            clazz = AppMedxAudioPlayerService::class.java
        )
    }

    override fun onSkipToNextAudio(context: Context) {
        MedxAudioPlayerManager.skipToNext(
            context = context,
            clazz = AppMedxAudioPlayerService::class.java
        )
    }

    override fun onSeekToPositionAudio(context: Context, position:Long) {
        MedxAudioPlayerManager.seekToPosition(
            context = context,
            position = position,
            clazz = AppMedxAudioPlayerService::class.java
        )
    }
}