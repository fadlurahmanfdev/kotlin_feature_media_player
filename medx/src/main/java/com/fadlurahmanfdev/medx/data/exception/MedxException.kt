package com.fadlurahmanfdev.medx.data.exception

data class MedxException(
    val code: String,
    override val message: String?
) : Throwable(message = message)
