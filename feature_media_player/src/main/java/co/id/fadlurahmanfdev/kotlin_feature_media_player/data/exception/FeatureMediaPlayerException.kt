package co.id.fadlurahmanfdev.kotlin_feature_media_player.data.exception

data class FeatureMediaPlayerException(
    val code: String,
    override val message: String?
) : Throwable(message = message)
