# Medx Audio Player Library 📻

A lightweight yet powerful Android audio player library that handles:
- Audio playback operations
- Media session management
- Media notifications

## Installation 💽

Add the dependency to your build.gradle:

```kotlin
implementation("com.fadlurahmanfdev.medx_player:x.y.z")
```

## Quick Start 🚀

### Simple Audio Player

Medx Audio Player have a simple implementation of media player, it operates without foreground service,
and will have session along with the activity.

#### Initialization

Initialize the player in your Activity/Fragment

```kotlin
val medxAudioPlayer = MedxAudioPlayer(context)
medxAudioPlayer.initialize()
```

### Core Features

#### Playing Audio

Play single or multiple audio tracks

```kotlin
// initialize media item
val mediaItems = listOf(
    // HTTP Audio
    MediaItem.Builder()
        .setUri(Uri.parse("https://www.bensound.com/bensound-music/bensound-acousticbreeze.mp3"))
        .setMediaMetadata(
            MediaMetadata.Builder().setTitle("Acoustic Breeze").setArtist("Bensound")
                .setArtworkUri(Uri.parse("https://www.bensound.com/bensound-img/acousticbreeze.jpg"))
                .setMediaType(MediaMetadata.MEDIA_TYPE_AUDIO_BOOK_CHAPTER)
                .build()
        )
        .build(),
    // Raw Resource Audio
    MediaItem.Builder()
        .setUri(Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).path(R.raw.bensound_creativeminds.toString()).build())
        .setMediaMetadata(
            MediaMetadata.Builder().setTitle("Creative Minds").setArtist("Bensound")
                .setArtworkUri(Uri.parse("https://www.bensound.com/bensound-img/creativeminds.jpg"))
                .setMediaType(MediaMetadata.MEDIA_TYPE_AUDIO_BOOK_CHAPTER)
                .build()
        )
        .build(),
)
medxAudioPlayer.playAudio(mediaItems)
```

#### Playback Control

```kotlin
// Pause
medxAudioPlayer.pause()

// Resume
medxAudioPlayer.resume()

// Skip tracks
medxAudioPlayer.skipToNextMediaItem()
medxAudioPlayer.skipToPreviousMediaItem()

// Seek to position (in milliseconds)
medxAudioPlayer.seekToPosition(5000L)
```

### Foreground Service Audio Player

Medx Audio Player have a implementation of media player, it operates with foreground service.

If the audio played with the foreground service, if the activity is destroy or the apps is in minimized mode, the audio still playing.

The audio player will stop, if the app destroy or the user dismiss the notification.

#### Initialization

Create the service class, extend from `BaseMedxAudioPlayerService`

```kotlin
class AppMedxAudioPlayerService : BaseMedxAudioPlayerService() {
    ...
}
```

Foreground service need notification to make the session active. Initialize notification channel when create notification

See [example]()

```kotlin
class AppMedxAudioPlayerService : BaseMedxAudioPlayerService() {
    override fun onInitAndCreateMediaNotificationChannel() {
        // create notification channel
    }
}
```

Audio Player will play after notification ready asynchronously, make sure prepare `idleAudioNotification`.

`idleAudioNotification` is a notification that will shown when audio player state being idle.

```kotlin
class AppMedxAudioPlayerService : BaseMedxAudioPlayerService() {
    override fun idleAudioNotification(
        notificationId: Int,
        mediaItem: MediaItem,
        mediaSession: MediaSessionCompat,
        onReady: (notification: Notification) -> Unit
    ){
        
    }
}
```