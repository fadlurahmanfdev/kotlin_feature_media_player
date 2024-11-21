package co.id.fadlurahmanfdev.kotlin_feature_media_player.example.data.dto.model

import androidx.annotation.DrawableRes

data class FeatureModel(
    @DrawableRes val featureIcon: Int,
    val enum: String,
    val title: String,
    val desc: String? = null,
)
