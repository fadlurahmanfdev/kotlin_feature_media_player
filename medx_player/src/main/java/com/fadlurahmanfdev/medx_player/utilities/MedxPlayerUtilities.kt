package com.fadlurahmanfdev.medx_player.utilities

class MedxPlayerUtilities {
    companion object {
        fun formatToReadableTime(millis: Long): String {
            val seconds = (millis / 1000).toInt()
            val rawSeconds = seconds % 60
            var formattedSeconds = rawSeconds.toString()
            if (rawSeconds < 10) {
                formattedSeconds = "0$rawSeconds"
            }
            val rawMinutes = (seconds / 60)
            var formattedMinutes = rawMinutes.toString()
            if (rawMinutes < 10) {
                formattedMinutes = "0$rawMinutes"
            }

            val rawHours = (seconds / 3600)
            var formattedHours = rawHours.toString()
            if (rawHours < 10) {
                formattedHours = "0$rawHours"
            }

            return if (rawHours > 0) {
                "$formattedHours:$formattedMinutes:$formattedSeconds"
            } else {
                "$formattedMinutes:$formattedSeconds"
            }

        }
    }
}