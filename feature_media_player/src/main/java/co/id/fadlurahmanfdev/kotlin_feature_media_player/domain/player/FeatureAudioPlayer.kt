package co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.common.BaseAudioPlayer

@UnstableApi
class FeatureAudioPlayer(override val context: Context) : BaseAudioPlayer(context) {}