package co.id.fadlurahmanfdev.kotlin_feature_media_player.data.state

enum class MusicPlayerState {
    IDLE, BUFFERING, READY, PLAYING, SEEK_TO_ZERO, SEEK_TO_SPECIFIC_POSITION, SEEK_TO_PREVIOUS, SEEK_TO_NEXT, PAUSED, RESUME, STOPPED, ENDED
}