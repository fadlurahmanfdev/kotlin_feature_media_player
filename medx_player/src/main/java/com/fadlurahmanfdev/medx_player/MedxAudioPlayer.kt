package com.fadlurahmanfdev.medx_player

import android.content.Context
import com.fadlurahmanfdev.medx_player.player.BaseMedxAudioPlayer

/**
 * Default class extended from [BaseMedxAudioPlayer]
 *
 * Simple media player to play audio from remote, file, or resource.
 * */
class MedxAudioPlayer(val context: Context) : BaseMedxAudioPlayer(context) {}