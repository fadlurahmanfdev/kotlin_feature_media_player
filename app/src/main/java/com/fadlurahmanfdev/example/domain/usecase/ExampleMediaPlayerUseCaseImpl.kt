package com.fadlurahmanfdev.example.domain.usecase

import com.fadlurahmanfdev.example.data.repository.AppMedxNotificationRepository

class ExampleMediaPlayerUseCaseImpl(
    private val appMedxNotificationRepository: AppMedxNotificationRepository
) : ExampleMediaPlayerUseCase {
    override fun createChannel(){
        return appMedxNotificationRepository.createMediaNotificationChannel()
    }
}