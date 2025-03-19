package com.fadlurahmanfdev.medx.constant

import com.fadlurahmanfdev.medx.data.exception.FeatureMediaPlayerException

object MedxErrorConstant {
    val MEDIA_ITEM_MISSING = FeatureMediaPlayerException(code = "MEDIA_ITEM_MISSING", message = "Cannot play audio, probably caused by media item is missing or empty")
    val NOTIFICATION_ID_MISSING = FeatureMediaPlayerException(code = "NOTIFICATION_ID_MISSING", message = "The notification id is missing, notification ID is needed when start foreground service")
}