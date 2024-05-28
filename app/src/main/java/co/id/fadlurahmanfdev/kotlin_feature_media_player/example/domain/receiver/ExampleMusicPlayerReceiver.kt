package co.id.fadlurahmanfdev.kotlin_feature_media_player.example.domain.receiver

import android.content.Context
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.manager.FeatureMusicPlayerManager
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.receiver.FeatureMusicPlayerReceiver
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.service.FeatureMusicPlayerService
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.domain.service.ExampleMusicPlayerService

class ExampleMusicPlayerReceiver : FeatureMusicPlayerReceiver() {
    override fun onPauseAudio(context: Context) {
        FeatureMusicPlayerManager.pause(
            context,
            ExampleMusicPlayerService::class.java
        )
    }

    override fun onResumeAudio(context: Context) {
        FeatureMusicPlayerManager.resume(
            context,
            ExampleMusicPlayerService::class.java
        )
    }

    override fun onPreviousAudio(context: Context) {
        FeatureMusicPlayerManager.seekToPrevious(
            context,
            ExampleMusicPlayerService::class.java
        )
    }

    override fun onNextAudio(context: Context) {
        FeatureMusicPlayerManager.seekToNext(
            context,
            ExampleMusicPlayerService::class.java,
        )
    }
}