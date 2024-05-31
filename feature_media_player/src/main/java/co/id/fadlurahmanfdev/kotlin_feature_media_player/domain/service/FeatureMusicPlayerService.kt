package co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.repository.MediaNotificationRepository
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.repository.MediaNotificationRepositoryImpl
import co.id.fadlurahmanfdev.kotlin_feature_media_player.data.state.MusicPlayerState
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.common.BaseMusicPlayer
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.manager.FeatureMusicPlayerManager
import java.util.Calendar

abstract class FeatureMusicPlayerService : Service(), BaseMusicPlayer.Listener {
    private lateinit var musicPlayer: FeatureMusicPlayerManager
    lateinit var mediaNotificationRepository: MediaNotificationRepository
    private var currentNotificationId: Int = -1
    private lateinit var audioUrls: List<String>
    private lateinit var currentAudioUrlPlaying: String
    private var currentTitlePlaying: String? = null
    private var currentArtistPlaying: String? = null
    var mediaSession: MediaSessionCompat? = null

    companion object {
        const val ACTION_PLAY_REMOTE_AUDIO =
            "co.id.fadlurahmanfdev.kotlin_feature_media_player.ACTION_PLAY_REMOTE_AUDIO"
        const val ACTION_PAUSE_AUDIO =
            "co.id.fadlurahmanfdev.kotlin_feature_media_player.ACTION_PAUSE_AUDIO"
        const val ACTION_RESUME_AUDIO =
            "co.id.fadlurahmanfdev.kotlin_feature_media_player.ACTION_RESUME_AUDIO"
        const val ACTION_PREVIOUS_AUDIO =
            "co.id.fadlurahmanfdev.kotlin_feature_media_player.ACTION_PREVIOUS_AUDIO"
        const val ACTION_NEXT_AUDIO =
            "co.id.fadlurahmanfdev.kotlin_feature_media_player.ACTION_NEXT_AUDIO"
        const val ACTION_SEEK_TO_POSITION =
            "co.id.fadlurahmanfdev.feature_media_player.ACTION_SEEK_TO_POSITION"

        // not used yet
        const val ACTION_REWIND_AUDIO =
            "co.id.fadlurahmanfdev.feature_media_player.ACTION_REWIND_AUDIO"
        const val ACTION_FORWARD_AUDIO =
            "co.id.fadlurahmanfdev.feature_media_player.ACTION_FORWARD_AUDIO"

        const val SEND_INFO =
            "co.id.fadlurahmanfdev.kotlin_feature_media_player.SEND_INFO"

        // param related
        const val PARAM_NOTIFICATION_ID = "PARAM_NOTIFICATION_ID"
        const val PARAM_AUDIO_URL = "PARAM_AUDIO_URL"
        const val PARAM_SEEK_TO_POSITION = "PARAM_SEEK_TO_POSITION"
        const val PARAM_POSITION = "PARAM_POSITION"
        const val PARAM_DURATION = "PARAM_DURATION"
        const val PARAM_STATE = "PARAM_STATE"
        const val PARAM_TITLE = "PARAM_TITLE"
        const val PARAM_ARTIST = "PARAM_ARTIST"


        private fun sendBroadcastSendInfo(
            context: Context,
            position: Long,
            duration: Long,
            state: MusicPlayerState
        ) {
            val intent = Intent().apply {
                action = SEND_INFO
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
        musicPlayer.addListener(this)

        mediaSession = MediaSessionCompat(this, "FeatureMusicPlayerService")
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
                audioUrls = intent.getStringArrayListExtra(PARAM_AUDIO_URL) ?: listOf()
                currentArtistPlaying = intent.getStringExtra(PARAM_TITLE)
                currentTitlePlaying = intent.getStringExtra(PARAM_ARTIST)
                Log.d(
                    FeatureMusicPlayerService::class.java.simpleName,
                    "prepare for play remote audio & start foreground, title: $currentTitlePlaying & artist: $currentArtistPlaying"
                )
                if (currentNotificationId != -1 && audioUrls.isNotEmpty()) {
                    currentAudioUrlPlaying = audioUrls.first()
                    onPlayRemoteAudioAndStartForeground(
                        notificationId = currentNotificationId,
                        urls = audioUrls,
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

            ACTION_PREVIOUS_AUDIO -> {
                if (currentNotificationId != -1) {
                    onPreviousAudio(currentNotificationId)
                } else {
                    Log.e(
                        FeatureMusicPlayerService::class.java.simpleName,
                        "$action -> notificationId missing"
                    )
                }
            }

            ACTION_NEXT_AUDIO -> {
                if (currentNotificationId != -1) {
                    onNextAudio(currentNotificationId)
                } else {
                    Log.e(
                        FeatureMusicPlayerService::class.java.simpleName,
                        "$action -> notificationId missing"
                    )
                }
            }

            ACTION_SEEK_TO_POSITION -> {
                val position = intent.getLongExtra(PARAM_SEEK_TO_POSITION, -1L)
                if (position != -1L) {
                    onSeekToPosition(position)
                } else {
                    Log.e(
                        FeatureMusicPlayerService::class.java.simpleName,
                        "$action -> notificationId missing"
                    )
                }
            }
        }
        return START_STICKY
    }

    @UnstableApi
    open fun onSeekToPosition(position: Long) {
        musicPlayer.seekToPosition(position = position)
    }

    @OptIn(UnstableApi::class)
    override fun onPositionChanged(position: Long) {
        super.onPositionChanged(position)
        sendBroadcastSendInfo(
            applicationContext,
            position = position,
            duration = musicPlayer.duration,
            state = musicPlayer.musicPlayerState,
        )
    }

    @UnstableApi
    override fun onStateChanged(state: MusicPlayerState) {
        super.onStateChanged(state)
        Log.d(
            FeatureMusicPlayerService::class.java.simpleName,
            "${FeatureMusicPlayerService::class.java.simpleName} onStateChanged: $state"
        )
        when (state) {
            MusicPlayerState.IDLE -> {

            }

            MusicPlayerState.READY -> {

            }

            MusicPlayerState.PLAYING -> {
                onUpdateAudioStateNotification(
                    notificationId = currentNotificationId,
                    title = currentTitlePlaying ?: "-",
                    artist = currentArtistPlaying ?: "-",
                    position = musicPlayer.position,
                    duration = musicPlayer.duration,
                    musicPlayerState = MusicPlayerState.PLAYING
                )
            }

            MusicPlayerState.RESUME -> {
                onUpdateAudioStateNotification(
                    notificationId = currentNotificationId,
                    title = currentTitlePlaying ?: "-",
                    artist = currentArtistPlaying ?: "-",
                    position = musicPlayer.position,
                    duration = musicPlayer.duration,
                    musicPlayerState = MusicPlayerState.PLAYING
                )
            }

            MusicPlayerState.SEEK_TO_ZERO -> {
                onUpdateAudioStateNotification(
                    notificationId = currentNotificationId,
                    title = currentTitlePlaying ?: "-",
                    artist = currentArtistPlaying ?: "-",
                    position = musicPlayer.position,
                    duration = musicPlayer.duration,
                    musicPlayerState = MusicPlayerState.PLAYING
                )
            }

            MusicPlayerState.SEEK_TO_PREVIOUS -> {
                onUpdateAudioStateNotification(
                    notificationId = currentNotificationId,
                    title = currentTitlePlaying ?: "-",
                    artist = currentArtistPlaying ?: "-",
                    position = musicPlayer.position,
                    duration = musicPlayer.duration,
                    musicPlayerState = MusicPlayerState.PLAYING
                )
            }

            MusicPlayerState.SEEK_TO_NEXT -> {
                onUpdateAudioStateNotification(
                    notificationId = currentNotificationId,
                    title = currentTitlePlaying ?: "-",
                    artist = currentArtistPlaying ?: "-",
                    position = musicPlayer.position,
                    duration = musicPlayer.duration,
                    musicPlayerState = MusicPlayerState.PLAYING,
                )
            }

            MusicPlayerState.PAUSED -> {
                onUpdateAudioStateNotification(
                    notificationId = currentNotificationId,
                    title = currentTitlePlaying ?: "-",
                    artist = currentArtistPlaying ?: "-",
                    position = musicPlayer.position,
                    duration = musicPlayer.duration,
                    musicPlayerState = MusicPlayerState.PAUSED,
                )
                sendBroadcastSendInfo(
                    applicationContext,
                    position = musicPlayer.position,
                    duration = musicPlayer.duration,
                    state = MusicPlayerState.PAUSED,
                )
            }

            MusicPlayerState.ENDED -> {
                onAudioEndedState(notificationId = currentNotificationId)
            }

            else -> {

            }
        }
    }

    @UnstableApi
    open fun onPlayRemoteAudioAndStartForeground(
        notificationId: Int,
        urls: List<String>,
        title: String,
        artist: String
    ) {
        musicPlayer.playRemoteAudio(urls)
        if (mediaSession == null) {
            mediaSession = MediaSessionCompat(applicationContext, "FeatureMusicPlayerService")
        }
        startForeground(
            notificationId,
            onIdleAudioNotification(
                notificationId = notificationId,
                title = title,
                artist = artist,
                mediaSession = mediaSession!!
            )
        )
    }

    @UnstableApi
    open fun onPauseAudio(notificationId: Int) {
        musicPlayer.pause()
    }

    @UnstableApi
    open fun onAudioEndedState(notificationId: Int) {
        onUpdateAudioStateNotification(
            notificationId = notificationId,
            title = currentTitlePlaying ?: "-",
            artist = currentArtistPlaying ?: "-",
            position = musicPlayer.position,
            duration = musicPlayer.duration,
            musicPlayerState = MusicPlayerState.ENDED
        )
    }

    @UnstableApi
    open fun onResumeAudio(notificationId: Int) {
        musicPlayer.resume()
    }

    @UnstableApi
    open fun onPreviousAudio(notificationId: Int) {
        musicPlayer.seekToPrevious()
    }

    @UnstableApi
    open fun onNextAudio(notificationId: Int) {
        musicPlayer.seekToNext()
    }

    abstract fun onIdleAudioNotification(
        notificationId: Int,
        title: String,
        artist: String,
        mediaSession: MediaSessionCompat
    ): Notification

    abstract fun onUpdateAudioStateNotification(
        notificationId: Int,
        title: String,
        artist: String,
        position: Long,
        duration: Long,
        musicPlayerState: MusicPlayerState
    )

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        musicPlayer.destroy()
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}