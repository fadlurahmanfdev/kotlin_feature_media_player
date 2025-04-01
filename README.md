# Medx Audio Player Library 📻

A lightweight yet powerful Android audio player library that handles:

- Audio playback operations
- Media session management
- Media notifications

# Installation 💽

Add the dependency to your build.gradle:

```kotlin
implementation("com.fadlurahmanfdev.medx_player:x.y.z")
```

# Quick Start 🚀

## Initiate Media Item

A MediaItem contains audio metadata including:

- Audio URI (HTTP, Raw Resource, or local file)
- Title, artist, artwork
- Media type

```kotlin
val mediaItems = arrayListOf<MediaItem>()

// HTTP Audio Example
mediaItems.add(
    MediaItem.Builder()
        .setUri(Uri.parse("https://example.com/audio.mp3"))
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle("Sample Song")
                .setArtist("Artist Name")
                .setArtworkUri(Uri.parse("https://example.com/artwork.jpg"))
                .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                .build()
        )
        .build()
)

// Raw Resource Example
mediaItems.add(
    MediaItem.Builder()
        .setUri(
            Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .path(R.raw.audio_file.toString())
                .build()
        )
        // ... metadata ...
        .build()
)
```

# Implementation Options

## Simple Medx Audio Player (Activity-bound)

Best for short audio playback that should stop when activity closes.

### Initialization

```kotlin
val medxAudioPlayer = MedxAudioPlayer(context)
medxAudioPlayer.initialize()
```

#### Playback Control

```kotlin
// Start playback
medxAudioPlayer.playAudio(mediaItems)

// Control playback
medxAudioPlayer.pause()
medxAudioPlayer.resume()
medxAudioPlayer.skipToNextMediaItem()
medxAudioPlayer.skipToPreviousMediaItem()
medxAudioPlayer.seekToPosition(5000L) // milliseconds
```

## Foreground Service Medx Audio Player

Best for long-running audio playback that should continue when app is backgrounded.

### Initialization

#### Create Service Class

```kotlin
class AppMedxAudioPlayerService : BaseMedxAudioPlayerService() {
    override fun onInitAndCreateMediaNotificationChannel() {
        // Initialize notification channel
        createNotificationChannel(
            channelId = "music_channel",
            channelName = "Music Playback",
            importance = NotificationManager.IMPORTANCE_LOW
        )
    }

    override fun idleAudioNotification(
        notificationId: Int,
        mediaItem: MediaItem,
        mediaSession: MediaSessionCompat,
        onReady: (Notification) -> Unit
    ) {
        // Build your custom notification
        val notification = buildNotification(mediaItem)
        onReady(notification)
    }
}
```

#### Create Broadcast Receiver

```kotlin
class AppAudioPlayerReceiver : BaseMedxAudioPlayerReceiver() {
    override fun onPauseAudio(context: Context) {
        // Handle pause action from notification
    }

    override fun onResumeAudio(context: Context, notificationId: Int) {
        // Handle play/resume action
    }

    // ... implement other callbacks ...
}
```

#### Register In Android Manifest

```xml

<manifest>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    ...

    <application>
        ...
        <receiver android:name=".domain.receiver.AppMedxAudioPlayerReceiver"
            android:exported="false">
            <intent-filter>
                <action android:name="com.fadlurahmanfdev.medx.ACTION_PAUSE_AUDIO" />
                <action android:name="com.fadlurahmanfdev.medx.ACTION_RESUME_AUDIO" />
                <action android:name="com.fadlurahmanfdev.medx.ACTION_SKIP_TO_PREVIOUS_AUDIO" />
                <action android:name="com.fadlurahmanfdev.medx.ACTION_SKIP_TO_NEXT_AUDIO" />
                <action android:name="com.fadlurahmanfdev.medx.ACTION_SEEK_TO_POSITION_AUDIO" />
            </intent-filter>
        </receiver>
        <service android:name=".domain.service.AppMedxAudioPlayerService" android:exported="false"
            android:foregroundServiceType="mediaPlayback">
            <intent-filter>
                <action android:name="com.fadlurahmanfdev.medx.ACTION_PLAY_AUDIO" />
                <action android:name="com.fadlurahmanfdev.medx.ACTION_PAUSE_AUDIO" />
                <action android:name="com.fadlurahmanfdev.medx.ACTION_RESUME_AUDIO" />
                <action android:name="com.fadlurahmanfdev.medx.ACTION_SKIP_TO_PREVIOUS_AUDIO" />
                <action android:name="com.fadlurahmanfdev.medx.ACTION_SKIP_TO_NEXT_AUDIO" />
                <action android:name="com.fadlurahmanfdev.medx.ACTION_SEEK_TO_POSITION_AUDIO" />
                <action android:name="com.fadlurahmanfdev.medx.ACTION_AUDIO_DURATION_INFO" />
                <action android:name="com.fadlurahmanfdev.medx.ACTION_AUDIO_POSITION_INFO" />
                <action android:name="com.fadlurahmanfdev.medx.ACTION_AUDIO_STATE_INFO" />
                <action android:name="com.fadlurahmanfdev.medx.ACTION_AUDIO_MEDIA_META_DATA_INFO" />
            </intent-filter>
        </service>
    </application>
</manifest>
```

### Playback Control

```kotlin
// Start playback
MedxAudioPlayerManager.play(
    context,
    mediaItems,
    AppMedxAudioPlayerService::class.java
)

// Control playback
MedxAudioPlayerManager.pause(context, AppMedxAudioPlayerService::class.java)
MedxAudioPlayerManager.resume(context, NOTIFICATION_ID, AppMedxAudioPlayerService::class.java)
MedxAudioPlayerManager.skipToNext(context, AppMedxAudioPlayerService::class.java)
MedxAudioPlayerManager.seekToPosition(context, positionMs, AppMedxAudioPlayerService::class.java)
```