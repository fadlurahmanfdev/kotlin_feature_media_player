package co.id.fadlurahmanfdev.kotlin_feature_media_player.example.domain.usecase

import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.data.repository.ExampleMediaNotificationRepository

class ExampleMediaPlayerUseCaseImpl(
    private val exampleMediaNotificationRepository: ExampleMediaNotificationRepository
) : ExampleMediaPlayerUseCase {
    override fun createChannel(){
        return exampleMediaNotificationRepository.createMediaNotificationChannel()
    }
}