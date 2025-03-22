package com.fadlurahmanfdev.medx

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.fadlurahmanfdev.medx.constant.MedxConstant
import com.fadlurahmanfdev.medx.domain.common.BaseMedxAudioPlayerV2
import com.fadlurahmanfdev.medx.domain.service.BaseMedxAudioPlayerService

class MedxAudioPlayer(val context: Context) : BaseMedxAudioPlayerV2(context) {
    companion object {
        @UnstableApi
        fun <T : BaseMedxAudioPlayerService> playRemoteAudio(
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
                action = MedxConstant.ACTION_PLAY_REMOTE_AUDIO
                putExtra(MedxConstant.PARAM_NOTIFICATION_ID, notificationId)
                putExtra(MedxConstant.PARAM_MEDIA_ITEMS, bundleMediaItems)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        @UnstableApi
        fun <T : BaseMedxAudioPlayerService> pause(
            context: Context,
            notificationId: Int,
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            intent.apply {
                action = MedxConstant.ACTION_PAUSE_AUDIO
                putExtra(MedxConstant.PARAM_NOTIFICATION_ID, notificationId)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        @UnstableApi
        fun <T : BaseMedxAudioPlayerService> resume(
            context: Context,
            notificationId: Int,
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            intent.apply {
                action = MedxConstant.ACTION_RESUME_AUDIO
                putExtra(MedxConstant.PARAM_NOTIFICATION_ID, notificationId)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun <T : BaseMedxAudioPlayerService> skipToPrevious(
            context: Context,
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            intent.apply {
                action = MedxConstant.ACTION_SKIP_TO_PREVIOUS_AUDIO
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun <T : BaseMedxAudioPlayerService> skipToNext(
            context: Context,
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            intent.apply {
                action = MedxConstant.ACTION_SKIP_TO_NEXT_AUDIO
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun <T : BaseMedxAudioPlayerService> seekToPosition(
            context: Context,
            position: Long,
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            intent.apply {
                action = MedxConstant.ACTION_SEEK_TO_POSITION_AUDIO
                putExtra(MedxConstant.PARAM_POSITION, position)
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }
}