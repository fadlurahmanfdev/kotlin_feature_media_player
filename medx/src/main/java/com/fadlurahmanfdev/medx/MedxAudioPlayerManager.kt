package com.fadlurahmanfdev.medx

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.fadlurahmanfdev.medx.constant.MedxConstant
import com.fadlurahmanfdev.medx.data.enums.AudioPlayerState

class MedxAudioPlayerManager(val context: Context) {
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
                    val state = AudioPlayerState.valueOf(stateString)
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
        fun onReceiveInfoState(state: AudioPlayerState)
        fun onReceiveInfoMediaMetaData(mediaMetadata: MediaMetadata)
    }
}