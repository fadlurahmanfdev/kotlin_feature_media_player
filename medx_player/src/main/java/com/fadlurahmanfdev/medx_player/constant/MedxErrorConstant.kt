package com.fadlurahmanfdev.medx_player.constant

import com.fadlurahmanfdev.medx_player.data.exception.MedxException

object MedxErrorConstant {
    val MEDIA_ITEM_MISSING = MedxException(code = "MEDIA_ITEM_MISSING", message = "Cannot play audio, probably caused by media item is missing or empty")
    val NOTIFICATION_ID_MISSING = MedxException(code = "NOTIFICATION_ID_MISSING", message = "The notification id is missing, notification ID is needed when start foreground service")
}