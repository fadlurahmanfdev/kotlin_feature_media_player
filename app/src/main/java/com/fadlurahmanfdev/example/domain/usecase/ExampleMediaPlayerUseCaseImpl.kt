package com.fadlurahmanfdev.example.domain.usecase

import com.fadlurahmanfdev.example.data.repository.ExampleMediaNotificationRepository

class ExampleMediaPlayerUseCaseImpl(
    private val exampleMediaNotificationRepository: ExampleMediaNotificationRepository
) : ExampleMediaPlayerUseCase {
    override fun createChannel(){
        return exampleMediaNotificationRepository.createMediaNotificationChannel()
    }
}