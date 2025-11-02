package com.fadlurahmanfdev.example.domain.receiver

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.fadlurahmanfdev.example.domain.service.AppMedxAudioPlayerService
import com.fadlurahmanfdev.medx_player.MedxPlayerManager
import com.fadlurahmanfdev.medx_player.receiver.BaseMedxAudioPlayerReceiver

@OptIn(UnstableApi::class)
class AppMedxAudioPlayerReceiver : BaseMedxAudioPlayerReceiver() {
    override fun onPauseAudio(context: Context) {
        MedxPlayerManager.pause(context = context, clazz = AppMedxAudioPlayerService::class.java)
    }

    override fun onResumeAudio(context: Context, notificationId: Int) {
        MedxPlayerManager.resume(
            context = context,
            notificationId = notificationId,
            clazz = AppMedxAudioPlayerService::class.java
        )
    }

    override fun onSkipToPreviousAudio(context: Context) {
        MedxPlayerManager.skipToPrevious(
            context = context,
            clazz = AppMedxAudioPlayerService::class.java
        )
    }

    override fun onSkipToNextAudio(context: Context) {
        MedxPlayerManager.skipToNext(
            context = context,
            clazz = AppMedxAudioPlayerService::class.java
        )
    }

    override fun onSeekToPositionAudio(context: Context, position:Long) {
        MedxPlayerManager.seekToPosition(
            context = context,
            position = position,
            clazz = AppMedxAudioPlayerService::class.java
        )
    }
}