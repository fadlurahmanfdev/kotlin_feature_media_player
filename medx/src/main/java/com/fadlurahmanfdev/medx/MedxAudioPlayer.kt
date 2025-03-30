package com.fadlurahmanfdev.medx

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.fadlurahmanfdev.medx.base.IMedxAudioResourceManager
import com.fadlurahmanfdev.medx.data.enums.MedxAudioPlayerState
import com.fadlurahmanfdev.medx.player.BaseMedxAudioPlayer

/**
 * Default class extended from [BaseMedxAudioPlayer]
 *
 * Simple media player to play audio from remote, file, or resource.
 * */
class MedxAudioPlayer(val context: Context) : BaseMedxAudioPlayer(context) {
    override val medxAudioResourceManager: IMedxAudioResourceManager = MedxAudioResourceManager()

    override lateinit var exoPlayer: ExoPlayer
    override lateinit var mediaSession: MediaSession

    /**
     * Method for handling initialize exo player & media session.
     * */
    override fun initialize() {
        initializeExoPlayer()
        initializeMediaSession()
    }

    private fun initializeExoPlayer() {
        exoPlayer = ExoPlayer.Builder(context).build()
        exoPlayer.addListener(this)
    }

    private fun initializeMediaSession() {
        mediaSession =
            MediaSession.Builder(context, exoPlayer).setCallback(
                object : MediaSession.Callback {
                    @UnstableApi
                    override fun onMediaButtonEvent(
                        session: MediaSession,
                        controllerInfo: MediaSession.ControllerInfo,
                        intent: Intent
                    ): Boolean {
                        Log.d(
                            this::class.java.simpleName,
                            "Medx-LOG %%% media session on media button event, intent:$intent"
                        )
                        if (intent.action == Intent.ACTION_MEDIA_BUTTON) {
                            val keyEvent: KeyEvent?
                            when {
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                                    keyEvent = intent.getParcelableExtra(
                                        Intent.EXTRA_KEY_EVENT,
                                        KeyEvent::class.java
                                    )
                                }

                                else -> {
                                    keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                                }
                            }

                            Log.d(
                                this::class.java.simpleName,
                                "Medx-LOG %%% media session, received key event: $keyEvent"
                            )

                            if (keyEvent != null) {
                                when (keyEvent.keyCode) {
                                    KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                                        onAudioPlayerStateChanged(MedxAudioPlayerState.PAUSED)
                                    }

                                    KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                        onAudioPlayerStateChanged(MedxAudioPlayerState.PLAYING)
                                    }
                                }
                            }
                        }
                        return super.onMediaButtonEvent(session, controllerInfo, intent)
                    }
                },
            ).build()
    }
}