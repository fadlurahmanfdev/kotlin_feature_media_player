package co.id.fadlurahmanfdev.kotlin_feature_media_player.example.presentation

import androidx.lifecycle.ViewModel
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.domain.usecase.ExampleMediaPlayerUseCase

class MainViewModel(
    private val exampleMediaPlayerUseCase: ExampleMediaPlayerUseCase
) : ViewModel() {

    fun createChannel(){
        exampleMediaPlayerUseCase.createChannel()
    }

}