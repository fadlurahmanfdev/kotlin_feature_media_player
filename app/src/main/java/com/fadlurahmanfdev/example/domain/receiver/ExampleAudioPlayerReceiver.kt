package com.fadlurahmanfdev.example.domain.receiver

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.fadlurahmanfdev.kotlin_feature_media_player.domain.manager.FeatureAudioPlayerManager
import com.fadlurahmanfdev.kotlin_feature_media_player.domain.receiver.FeatureAudioPlayerReceiver
import com.fadlurahmanfdev.example.domain.service.ExampleAudioPlayerService

@OptIn(UnstableApi::class)
class ExampleAudioPlayerReceiver : FeatureAudioPlayerReceiver() {
    override fun onPauseAudio(context: Context) {
        FeatureAudioPlayerManager.pause(
            context,
            ExampleAudioPlayerService::class.java
        )
    }

    override fun onResumeAudio(context: Context) {
        FeatureAudioPlayerManager.resume(
            context,
            ExampleAudioPlayerService::class.java
        )
    }

    override fun onPreviousAudio(context: Context) {
        FeatureAudioPlayerManager.seekToPrevious(
            context,
            ExampleAudioPlayerService::class.java
        )
    }

    override fun onNextAudio(context: Context) {
        FeatureAudioPlayerManager.seekToNext(
            context,
            ExampleAudioPlayerService::class.java,
        )
    }

    override fun onSeekToPosition(context: Context, position: Long) {
        FeatureAudioPlayerManager.seekToPosition(
            context,
            position = position,
            ExampleAudioPlayerService::class.java,
        )
    }
}