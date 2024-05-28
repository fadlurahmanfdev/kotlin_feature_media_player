package co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.repository.MediaNotificationRepository
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.repository.MediaNotificationRepositoryImpl
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.state.MusicPlayerState
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.common.BaseMusicPlayer
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.manager.FeatureMusicPlayerManager
import java.util.Calendar

abstract class FeatureMusicPlayerService : Service(), BaseMusicPlayer.Callback {
    private lateinit var musicPlayer: FeatureMusicPlayerManager
    lateinit var mediaNotificationRepository: MediaNotificationRepository
    private var currentNotificationId: Int = -1
    private var currentTitlePlaying: String? = null
    private var currentArtistPlaying: String? = null

    companion object {
        const val ACTION_PLAY_REMOTE_AUDIO =
            "co.id.fadlurahmanfdev.kotlin_feature_media_player.ACTION_PLAY_REMOTE_AUDIO"
        const val ACTION_PAUSE_AUDIO =
            "co.id.fadlurahmanfdev.kotlin_feature_media_player.ACTION_PAUSE_AUDIO"
        const val ACTION_RESUME_AUDIO =
            "co.id.fadlurahmanfdev.kotlin_feature_media_player.ACTION_RESUME_AUDIO"
        const val ACTION_REWIND_AUDIO =
            "co.id.fadlurahmanfdev.feature_media_player.ACTION_REWIND_AUDIO"
        const val ACTION_FORWARD_AUDIO =
            "co.id.fadlurahmanfdev.feature_media_player.ACTION_FORWARD_AUDIO"
        const val ACTION_SEEK_TO_POSITION =
            "co.id.fadlurahmanfdev.feature_media_player.ACTION_SEEK_TO_POSITION"
        const val ACTION_SEND_INFO =
            "co.id.fadlurahmanfdev.feature_media_player.ACTION_SEND_INFO"

        // param related
        const val PARAM_NOTIFICATION_ID = "PARAM_NOTIFICATION_ID"
        const val PARAM_AUDIO_URL = "PARAM_AUDIO_URL"
        const val PARAM_SEEK_TO_POSITION = "PARAM_SEEK_TO_POSITION"
        const val PARAM_POSITION = "PARAM_POSITION"
        const val PARAM_DURATION = "PARAM_DURATION"
        const val PARAM_STATE = "PARAM_STATE"
        const val PARAM_TITLE = "PARAM_TITLE"
        const val PARAM_ARTIST = "PARAM_ARTIST"


        fun sendBroadcastSendInfo(
            context: Context,
            position: Long,
            duration: Long,
            state: MusicPlayerState
        ) {
            val intent = Intent().apply {
                action = ACTION_SEND_INFO
                putExtra(PARAM_DURATION, duration)
                putExtra(PARAM_POSITION, position)
                putExtra(PARAM_STATE, state.name)
            }
            context.sendBroadcast(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @UnstableApi
    override fun onCreate() {
        super.onCreate()
        Log.d(
            FeatureMusicPlayerService::class.java.simpleName,
            "init on create ${FeatureMusicPlayerService::class.java.simpleName}"
        )
        mediaNotificationRepository = MediaNotificationRepositoryImpl(applicationContext)

        musicPlayer = FeatureMusicPlayerManager(applicationContext)
        musicPlayer.initialize()
        musicPlayer.setCallback(this)
        Log.d(
            FeatureMusicPlayerService::class.java.simpleName,
            "successfully on create ${FeatureMusicPlayerService::class.java.simpleName}"
        )
    }

    @UnstableApi
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d(
            FeatureMusicPlayerService::class.java.simpleName,
            "${FeatureMusicPlayerService::class.java} action incoming: $action, ${Calendar.getInstance().time}"
        )
        when (action) {
            ACTION_PLAY_REMOTE_AUDIO -> {
                currentNotificationId = intent.getIntExtra(PARAM_NOTIFICATION_ID, -1)
                val audioUrl = intent.getStringExtra(PARAM_AUDIO_URL)
                currentArtistPlaying = intent.getStringExtra(PARAM_TITLE)
                currentTitlePlaying = intent.getStringExtra(PARAM_ARTIST)
                Log.d(
                    FeatureMusicPlayerService::class.java.simpleName,
                    "prepare for play remote audio & start foreground, title: $currentTitlePlaying & artist: $currentArtistPlaying"
                )
                if (currentNotificationId != -1 && audioUrl != null) {
                    onPlayRemoteAudioAndStartForeground(
                        notificationId = currentNotificationId,
                        url = audioUrl,
                        title = currentTitlePlaying ?: "-",
                        artist = currentArtistPlaying ?: "-"
                    )
                } else {
                    Log.e(
                        FeatureMusicPlayerService::class.java.simpleName,
                        "unable to play remote audio, cause either notificationId & url is missing"
                    )
                }
            }

            ACTION_PAUSE_AUDIO -> {
                if (currentNotificationId != -1) {
                    onPauseAudio(currentNotificationId)
                } else {
                    Log.e(
                        FeatureMusicPlayerService::class.java.simpleName,
                        "$action -> notificationId missing"
                    )
                }
            }

            ACTION_RESUME_AUDIO -> {
                if (currentNotificationId != -1) {
                    onResumeAudio(currentNotificationId)
                } else {
                    Log.e(
                        FeatureMusicPlayerService::class.java.simpleName,
                        "$action -> notificationId missing"
                    )
                }
            }


            ACTION_SEEK_TO_POSITION -> {
                val seekToPosition = intent.getLongExtra(PARAM_SEEK_TO_POSITION, -1L)
                if (seekToPosition != -1L) {
                    musicPlayer.seekToPosition(seekToPosition)
                }
            }
        }
        return START_STICKY
    }

    @OptIn(UnstableApi::class)
    override fun onPositionChanged(position: Long) {
        super.onPositionChanged(position)
        onUpdatePositionNotification(
            notificationId = currentNotificationId,
            title = currentTitlePlaying ?: "-",
            artist = currentArtistPlaying ?: "-",
            position = position,
            duration = musicPlayer.duration
        )
//        sendBroadcastSendInfo(
//            applicationContext,
//            musicPlayer.position,
//            musicPlayer.duration,
//            musicPlayer.musicPlayerState
//        )
    }

    @UnstableApi
    override fun onStateChanged(state: MusicPlayerState) {
        super.onStateChanged(state)
        Log.d(
            FeatureMusicPlayerService::class.java.simpleName,
            "${FeatureMusicPlayerService::class.java.simpleName} onStateChanged: $state"
        )
//        sendBroadcastSendInfo(
//            applicationContext,
//            musicPlayer.position,
//            musicPlayer.duration,
//            musicPlayer.musicPlayerState
//        )
        when (state) {
            MusicPlayerState.ENDED -> {
                onAudioEndedState(notificationId = currentNotificationId)
            }

            MusicPlayerState.RESUME -> {}
            MusicPlayerState.READY -> {}

            else -> {

            }
        }
    }

    @UnstableApi
    open fun onPlayRemoteAudioAndStartForeground(
        notificationId: Int,
        url: String,
        title: String,
        artist: String
    ) {
        musicPlayer.playRemoteAudio(url)
        startForeground(
            notificationId,
            onGetNotification(
                notificationId = notificationId,
                title = title,
                artist = artist
            )
        )
    }

    @UnstableApi
    open fun onPauseAudio(notificationId: Int) {
        musicPlayer.pause()
        onUpdatePauseNotification(
            notificationId = notificationId,
            title = currentTitlePlaying ?: "-",
            artist = currentArtistPlaying ?: "-",
            position = musicPlayer.position,
            duration = musicPlayer.duration
        )
    }

    @UnstableApi
    open fun onAudioEndedState(notificationId: Int) {

    }

    @UnstableApi
    open fun onResumeAudio(notificationId: Int) {
        musicPlayer.resume()
        onUpdatePauseNotification(
            notificationId = notificationId,
            title = currentTitlePlaying ?: "-",
            artist = currentArtistPlaying ?: "-",
            position = musicPlayer.position,
            duration = musicPlayer.duration
        )
    }

    abstract fun onGetNotification(
        notificationId: Int,
        title: String,
        artist: String
    ): Notification

    abstract fun onUpdatePositionNotification(
        notificationId: Int,
        title: String,
        artist: String,
        position: Long,
        duration: Long
    )

    abstract fun onUpdatePauseNotification(
        notificationId: Int,
        title: String,
        artist: String,
        position: Long,
        duration: Long
    )

    abstract fun onEndedAudioNotification(
        notificationId: Int,
        title: String,
        artist: String,
        position: Long,
        duration: Long
    )

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        musicPlayer.destroy()
        super.onDestroy()
    }
}