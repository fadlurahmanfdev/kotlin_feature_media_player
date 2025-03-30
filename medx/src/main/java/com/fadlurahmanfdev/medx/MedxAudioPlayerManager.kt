package com.fadlurahmanfdev.medx

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.fadlurahmanfdev.medx.constant.MedxConstant
import com.fadlurahmanfdev.medx.data.enums.MedxAudioPlayerState
import com.fadlurahmanfdev.medx.receiver.BaseMedxAudioPlayerReceiver
import com.fadlurahmanfdev.medx.service.BaseMedxAudioPlayerService

class MedxAudioPlayerManager(val context: Context) {
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
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            intent.apply {
                action = MedxConstant.ACTION_PAUSE_AUDIO
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

        fun <T : BaseMedxAudioPlayerReceiver> getPauseAudioPendingIntent(
            context: Context,
            requestCode: Int,
            clazz: Class<T>
        ): PendingIntent {
            val intent = Intent(context, clazz)
            intent.apply {
                action = MedxConstant.ACTION_PAUSE_AUDIO
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                getFlagPendingIntent()
            )
        }

        fun <T : BaseMedxAudioPlayerReceiver> getSkipToPreviousAudioPendingIntent(
            context: Context,
            requestCode: Int,
            clazz: Class<T>
        ): PendingIntent {
            val intent = Intent(context, clazz)
            intent.apply {
                action = MedxConstant.ACTION_SKIP_TO_PREVIOUS_AUDIO
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                getFlagPendingIntent()
            )
        }

        fun <T : BaseMedxAudioPlayerReceiver> getSkipToNextAudioPendingIntent(
            context: Context,
            requestCode: Int,
            clazz: Class<T>
        ): PendingIntent {
            val intent = Intent(context, clazz)
            intent.apply {
                action = MedxConstant.ACTION_SKIP_TO_NEXT_AUDIO
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                getFlagPendingIntent()
            )
        }

        fun <T : BaseMedxAudioPlayerReceiver> getResumePendingIntent(
            context: Context,
            requestCode: Int,
            notificationId: Int,
            clazz: Class<T>
        ): PendingIntent {
            val intent = Intent(context, clazz)
            intent.apply {
                action = MedxConstant.ACTION_RESUME_AUDIO
                putExtra(MedxConstant.PARAM_NOTIFICATION_ID, notificationId)
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                getFlagPendingIntent()
            )
        }

        fun <T : BaseMedxAudioPlayerReceiver> sendBroadcastSeekToPosition(
            context: Context,
            notificationId: Int,
            position: Long,
            clazz: Class<T>
        ) {
            val intent = Intent(context, clazz)
            intent.apply {
                action = MedxConstant.ACTION_SEEK_TO_POSITION_AUDIO
                putExtra(MedxConstant.PARAM_NOTIFICATION_ID, notificationId)
                putExtra(MedxConstant.PARAM_SEEK_TO_POSITION, position)
            }
            context.sendBroadcast(intent)
        }

        fun <T : BaseMedxAudioPlayerReceiver> getNonePendingIntent(
            context: Context,
            clazz: Class<T>
        ): PendingIntent {
            val intent = Intent(context, clazz)
            intent.apply {
                action = MedxConstant.ACTION_NONE
            }
            return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                getFlagPendingIntent()
            )
        }

        private fun getFlagPendingIntent(): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        }
    }

    private var listener: Listener? = null
    private val audioDurationInfoReceiver = object : BroadcastReceiver() {
        @UnstableApi
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                val duration = intent?.getLongExtra(MedxConstant.PARAM_DURATION, 0L)
                if (duration != null) {
                    listener?.onReceiveInfoDuration(duration)
                }
            } catch (e: Throwable) {
                Log.e(
                    this::class.java.simpleName,
                    "Medx-LOG %%% - failed to receive audio duration info",
                    e
                )
            }
        }
    }

    private val audioPositionInfoReceiver = object : BroadcastReceiver() {
        @UnstableApi
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                val position = intent?.getLongExtra(MedxConstant.PARAM_POSITION, 0L)
                if (position != null) {
                    listener?.onReceiveInfoPosition(position)
                }
            } catch (e: Throwable) {
                Log.e(
                    this::class.java.simpleName,
                    "Medx-LOG %%% - failed to receive audio position info",
                    e
                )
            }
        }
    }

    private val audioStateInfoReceiver = object : BroadcastReceiver() {
        @UnstableApi
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                val stateString = intent?.getStringExtra(MedxConstant.PARAM_STATE)
                if (stateString != null) {
                    val state = MedxAudioPlayerState.valueOf(stateString)
                    listener?.onReceiveInfoState(state)
                }
            } catch (e: Throwable) {
                Log.e(this::class.java.simpleName, "Medx-LOG %%% - failed to receive audio info", e)
            }
        }
    }

    private val audioMetaDataInfoReceiver = object : BroadcastReceiver() {
        @UnstableApi
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                val builder = MediaMetadata.Builder()
                val title = intent?.getStringExtra(MedxConstant.PARAM_TITLE)
                if (title != null) {
                    builder.setTitle(title)
                }

                val artist = intent?.getStringExtra(MedxConstant.PARAM_ARTIST)
                if (artist != null) {
                    builder.setArtist(artist)
                }

                val mediaMetadata = builder.build()
                Log.d(
                    this::class.java.simpleName,
                    "Medx-LOG %%% - media meta data changed ${mediaMetadata.title}, ${mediaMetadata.artist}"
                )
                listener?.onReceiveInfoMediaMetaData(mediaMetadata)
            } catch (e: Throwable) {
                Log.e(
                    this::class.java.simpleName,
                    "Medx-LOG %%% - failed to receive audio meta data",
                    e
                )
            }
        }
    }

    fun registerReceiver(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.registerReceiver(
                audioDurationInfoReceiver,
                IntentFilter(MedxConstant.ACTION_AUDIO_DURATION_INFO),
                Context.RECEIVER_NOT_EXPORTED,
            )
            activity.registerReceiver(
                audioPositionInfoReceiver,
                IntentFilter(MedxConstant.ACTION_AUDIO_POSITION_INFO),
                Context.RECEIVER_NOT_EXPORTED,
            )
            activity.registerReceiver(
                audioStateInfoReceiver,
                IntentFilter(MedxConstant.ACTION_AUDIO_STATE_INFO),
                Context.RECEIVER_NOT_EXPORTED,
            )
            activity.registerReceiver(
                audioMetaDataInfoReceiver,
                IntentFilter(MedxConstant.ACTION_AUDIO_MEDIA_META_DATA_INFO),
                Context.RECEIVER_NOT_EXPORTED,
            )

        } else {
            activity.registerReceiver(
                audioDurationInfoReceiver,
                IntentFilter(MedxConstant.ACTION_AUDIO_DURATION_INFO),
            )
            activity.registerReceiver(
                audioPositionInfoReceiver,
                IntentFilter(MedxConstant.ACTION_AUDIO_POSITION_INFO),
            )
            activity.registerReceiver(
                audioStateInfoReceiver,
                IntentFilter(MedxConstant.ACTION_AUDIO_STATE_INFO),
            )
            activity.registerReceiver(
                audioMetaDataInfoReceiver,
                IntentFilter(MedxConstant.ACTION_AUDIO_MEDIA_META_DATA_INFO),
            )
        }
    }

    fun addListener(listener: Listener) {
        this.listener = listener
    }

    fun removeListener() {
        this.listener = null
    }

    interface Listener {
        fun onReceiveInfoDuration(duration: Long)
        fun onReceiveInfoPosition(position: Long)
        fun onReceiveInfoState(state: MedxAudioPlayerState)
        fun onReceiveInfoMediaMetaData(mediaMetadata: MediaMetadata)
    }
}