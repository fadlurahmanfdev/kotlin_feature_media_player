package com.fadlurahmanfdev.medx

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.fadlurahmanfdev.medx.constant.MedxConstant
import com.fadlurahmanfdev.medx.domain.service.BaseMedxAudioPlayerService

class MedxAudioPlayerManager(val context: Context) {
    companion object{
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
    }
}