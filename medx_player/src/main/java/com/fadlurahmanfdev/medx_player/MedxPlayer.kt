package com.fadlurahmanfdev.medx_player

import android.content.Context
import com.fadlurahmanfdev.medx_player.player.BaseMedxPlayer

/**
 * Default class extended from [BaseMedxPlayer]
 *
 * Simple media player to play audio from remote, file, or resource.
 * */
class MedxPlayer(val context: Context) : BaseMedxPlayer(context) {}