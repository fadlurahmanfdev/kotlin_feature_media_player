package com.fadlurahmanfdev.medx_player

import android.content.Context
import com.fadlurahmanfdev.medx_player.player.BaseMedxVideoPlayer

/**
 * Default class extended from [BaseMedxVideoPlayer]
 *
 * Simple media player to play video from remote, file, or resource.
 * */
class MedxVideoPlayer(val context: Context) : BaseMedxVideoPlayer(context) {}