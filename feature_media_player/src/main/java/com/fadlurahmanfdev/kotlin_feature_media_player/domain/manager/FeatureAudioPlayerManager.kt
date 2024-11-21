package com.fadlurahmanfdev.kotlin_feature_media_player.domain.manager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.fadlurahmanfdev.kotlin_feature_media_player.data.constant.FeatureAudioPlayerConstant
import com.fadlurahmanfdev.kotlin_feature_media_player.domain.receiver.FeatureAudioPlayerReceiver
import com.fadlurahmanfdev.kotlin_feature_media_player.domain.service.FeatureAudioPlayerService

@OptIn(UnstableApi::class)
class FeatureAudioPlayerManager {
    companion object {
        fun <T : FeatureAudioPlayerService> playRemoteAudio(
            context: Context,
            notificationId: Int,
            mediaItems: List<MediaItem>,
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            val bundleMediaItems = ArrayList(mediaItems.map { mediaItem ->
                mediaItem.toBundleIncludeLocalConfiguration()
            }.toList())
            intent.apply {
                action = FeatureAudioPlayerConstant.ACTION_PLAY_REMOTE_AUDIO
                putExtra(FeatureAudioPlayerConstant.PARAM_NOTIFICATION_ID, notificationId)
                putExtra(FeatureAudioPlayerConstant.PARAM_MEDIA_ITEMS, bundleMediaItems)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun <T : FeatureAudioPlayerService> pause(
            context: Context,
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            intent.apply {
                action = FeatureAudioPlayerConstant.ACTION_PAUSE_AUDIO
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun <T : FeatureAudioPlayerService> resume(
            context: Context,
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            intent.apply {
                action = FeatureAudioPlayerConstant.ACTION_RESUME_AUDIO
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun <T : FeatureAudioPlayerService> seekToPrevious(
            context: Context,
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            intent.apply {
                action = FeatureAudioPlayerConstant.ACTION_SKIP_TO_PREVIOUS_AUDIO
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun <T : FeatureAudioPlayerService> seekToNext(
            context: Context,
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            intent.apply {
                action = FeatureAudioPlayerConstant.ACTION_SKIP_TO_NEXT_AUDIO
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun <T : FeatureAudioPlayerReceiver> getPausePendingIntent(
            context: Context,
            notificationId: Int,
            clazz: Class<T>
        ): PendingIntent {
            val intent = Intent(context, clazz)
            intent.apply {
                action = FeatureAudioPlayerConstant.ACTION_PAUSE_AUDIO
                putExtra(FeatureAudioPlayerConstant.PARAM_NOTIFICATION_ID, notificationId)
            }
            return PendingIntent.getBroadcast(
                context,
                1,
                intent,
                getFlagPendingIntent()
            )
        }

        fun <T : FeatureAudioPlayerReceiver> getResumePendingIntent(
            context: Context,
            notificationId: Int,
            clazz: Class<T>
        ): PendingIntent {
            val intent = Intent(context, clazz)
            intent.apply {
                action = FeatureAudioPlayerConstant.ACTION_RESUME_AUDIO

                putExtra(FeatureAudioPlayerConstant.PARAM_NOTIFICATION_ID, notificationId)
            }
            return PendingIntent.getBroadcast(
                context,
                2,
                intent,
                getFlagPendingIntent()
            )
        }

        fun <T : FeatureAudioPlayerReceiver> getPreviousPendingIntent(
            context: Context,
            notificationId: Int,
            clazz: Class<T>
        ): PendingIntent {
            val intent = Intent(context, clazz)
            intent.apply {
                action = FeatureAudioPlayerConstant.ACTION_SKIP_TO_PREVIOUS_AUDIO
                putExtra(FeatureAudioPlayerConstant.PARAM_NOTIFICATION_ID, notificationId)
            }
            return PendingIntent.getBroadcast(
                context,
                3,
                intent,
                getFlagPendingIntent()
            )
        }

        fun <T : FeatureAudioPlayerReceiver> getNextPendingIntent(
            context: Context,
            notificationId: Int,
            clazz: Class<T>
        ): PendingIntent {
            val intent = Intent(context, clazz)
            intent.apply {
                action = FeatureAudioPlayerConstant.ACTION_SKIP_TO_NEXT_AUDIO
                putExtra(FeatureAudioPlayerConstant.PARAM_NOTIFICATION_ID, notificationId)
            }
            return PendingIntent.getBroadcast(
                context,
                4,
                intent,
                getFlagPendingIntent()
            )
        }

        fun <T : FeatureAudioPlayerReceiver> getNonePendingIntent(
            context: Context,
            clazz: Class<T>
        ): PendingIntent {
            val intent = Intent(context, clazz)
            intent.apply {
                action = FeatureAudioPlayerConstant.ACTION_NONE
            }
            return PendingIntent.getBroadcast(
                context,
                -1,
                intent,
                getFlagPendingIntent()
            )
        }

        fun <T : FeatureAudioPlayerReceiver> sendBroadcastSeekToPosition(
            context: Context,
            notificationId: Int,
            position: Long,
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            intent.apply {
                action = FeatureAudioPlayerConstant.ACTION_SEEK_TO_POSITION
                putExtra(FeatureAudioPlayerConstant.PARAM_NOTIFICATION_ID, notificationId)
                putExtra(FeatureAudioPlayerConstant.PARAM_SEEK_TO_POSITION, position)
            }
            context.sendBroadcast(intent)
        }

        fun <T : FeatureAudioPlayerService> seekToPosition(
            context: Context,
            position: Long,
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            intent.apply {
                action = FeatureAudioPlayerConstant.ACTION_SEEK_TO_POSITION
                putExtra(FeatureAudioPlayerConstant.PARAM_SEEK_TO_POSITION, position)
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