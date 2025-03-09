package com.fadlurahmanfdev.medx.data.exception

data class FeatureMediaPlayerException(
    val code: String,
    override val message: String?
) : Throwable(message = message)
