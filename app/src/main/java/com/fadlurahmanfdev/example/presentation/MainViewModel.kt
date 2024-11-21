package com.fadlurahmanfdev.example.presentation

import androidx.lifecycle.ViewModel
import com.fadlurahmanfdev.example.domain.usecase.ExampleMediaPlayerUseCase

class MainViewModel(
    private val exampleMediaPlayerUseCase: ExampleMediaPlayerUseCase
) : ViewModel() {

    fun createChannel(){
        exampleMediaPlayerUseCase.createChannel()
    }

}