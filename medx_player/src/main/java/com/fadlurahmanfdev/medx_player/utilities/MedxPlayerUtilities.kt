package com.fadlurahmanfdev.medx_player.utilities

import java.util.concurrent.TimeUnit

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

        fun formatDuration(durationMs: Long): String {
            val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60

            return if (hours > 0)
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else
                String.format("%02d:%02d", minutes, seconds)
        }
    }
}