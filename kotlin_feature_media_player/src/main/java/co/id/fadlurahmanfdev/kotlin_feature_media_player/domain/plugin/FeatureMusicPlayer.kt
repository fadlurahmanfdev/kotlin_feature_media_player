package co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.plugin

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import co.id.fadlurahmanfdev.kotlin_feature_media_player.domain.common.BaseMusicPlayer

@OptIn(UnstableApi::class)
class FeatureMusicPlayer(context: Context) : BaseMusicPlayer(context) {}