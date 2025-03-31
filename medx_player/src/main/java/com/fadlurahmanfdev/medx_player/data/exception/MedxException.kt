package com.fadlurahmanfdev.medx_player.data.exception

data class MedxException(
    val code: String,
    override val message: String?
) : Throwable(message = message)
