package co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.manager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.common.BaseMusicPlayer
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.receiver.FeatureMusicPlayerReceiver
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.service.FeatureMusicPlayerService

@OptIn(UnstableApi::class)
class FeatureMusicPlayerManager(override val context: Context) : BaseMusicPlayer(context) {
    companion object {
        fun <T : FeatureMusicPlayerService> playRemoteAudio(
            context: Context,
            notificationId: Int,
            url: String,
            title: String,
            artist: String,
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            intent.apply {
                action = FeatureMusicPlayerService.ACTION_PLAY_REMOTE_AUDIO
                putExtra(FeatureMusicPlayerService.PARAM_NOTIFICATION_ID, notificationId)
                putExtra(FeatureMusicPlayerService.PARAM_AUDIO_URL, url)
                putExtra(FeatureMusicPlayerService.PARAM_TITLE, title)
                putExtra(FeatureMusicPlayerService.PARAM_ARTIST, artist)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun <T : FeatureMusicPlayerService> pause(
            context: Context,
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            intent.apply {
                action = FeatureMusicPlayerService.ACTION_PAUSE_AUDIO
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun <T : FeatureMusicPlayerService> resume(
            context: Context,
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            intent.apply {
                action = FeatureMusicPlayerService.ACTION_RESUME_AUDIO
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun <T : FeatureMusicPlayerReceiver> getPausePendingIntent(
            context: Context,
            notificationId: Int,
            clazz: Class<T>
        ): PendingIntent {
            val intent = Intent(context, clazz)
            intent.apply {
                action = FeatureMusicPlayerReceiver.ACTION_PAUSE_AUDIO
                putExtra(FeatureMusicPlayerReceiver.PARAM_NOTIFICATION_ID, notificationId)
            }
            return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                getFlagPendingIntent()
            )
        }

        fun <T : FeatureMusicPlayerReceiver> getResumePendingIntent(
            context: Context,
            notificationId: Int,
            clazz: Class<T>
        ): PendingIntent {
            val intent = Intent(context, clazz)
            intent.apply {
                action = FeatureMusicPlayerReceiver.ACTION_RESUME_AUDIO

                putExtra(FeatureMusicPlayerReceiver.PARAM_NOTIFICATION_ID, notificationId)
            }
            return PendingIntent.getBroadcast(
                context,
                1,
                intent,
                getFlagPendingIntent()
            )
        }

        fun <T : FeatureMusicPlayerService> seekToPosition(
            context: Context,
            position: Long,
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            intent.apply {
                action = FeatureMusicPlayerService.ACTION_SEEK_TO_POSITION
                putExtra(FeatureMusicPlayerService.PARAM_SEEK_TO_POSITION, position)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        private fun getFlagPendingIntent(): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        }
    }
}