package com.fadlurahmanfdev.example.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.fadlurahmanfdev.example.R
import com.fadlurahmanfdev.example.data.dto.model.FeatureModel
import com.fadlurahmanfdev.example.data.repository.AppMedxNotification
import com.fadlurahmanfdev.example.domain.usecase.ExampleMediaPlayerUseCaseImpl

class MainActivity : AppCompatActivity(), ListExampleAdapter.Callback {
    lateinit var viewModel: MainViewModel

    private val features: List<FeatureModel> = listOf<FeatureModel>(
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Create Channel",
            desc = "Create media channel",
            enum = "CREATE_MEDIA_CHANNEL"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Simple Audio Player",
            desc = "Simple Audio Player",
            enum = "SIMPLE_AUDIO_PLAYER"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Simple Audio Player For Audio File",
            desc = "Simple Audio Player For Audio File",
            enum = "SIMPLE_AUDIO_PLAYER_FOR_AUDIO_FILE"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Foreground Service Audio Player",
            desc = "Foreground Service Audio Player",
            enum = "FOREGROUND_SERVICE_AUDIO_PLAYER"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Simple Video Player",
            desc = "Simple Video Player",
            enum = "SIMPLE_VIDEO_PLAYER"
        ),
    )

    private lateinit var rv: RecyclerView

    private lateinit var adapter: ListExampleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        rv = findViewById<RecyclerView>(R.id.rv)

        viewModel = MainViewModel(
            exampleMediaPlayerUseCase = ExampleMediaPlayerUseCaseImpl(
                appMedxNotificationRepository = AppMedxNotification(
                    this
                )
            )
        )

        rv.setItemViewCacheSize(features.size)
        rv.setHasFixedSize(true)

        adapter = ListExampleAdapter()
        adapter.setCallback(this)
        adapter.setList(features)
        adapter.setHasStableIds(true)
        rv.adapter = adapter
    }

    override fun onClicked(item: FeatureModel) {
        when (item.enum) {
            "CREATE_MEDIA_CHANNEL" -> {
                viewModel.createChannel()
            }

            "SIMPLE_AUDIO_PLAYER" -> {
                val intent = Intent(this, SimpleAudioPlayerActivity::class.java)
                startActivity(intent)
            }

            "SIMPLE_AUDIO_PLAYER_FOR_AUDIO_FILE" -> {
                val intent = Intent(this, SimpleAudioPlayerForAudioFileActivity::class.java)
                startActivity(intent)
            }

            "FOREGROUND_SERVICE_AUDIO_PLAYER" -> {
                val intent = Intent(this, ForegroundServiceAudioPlayerActivity::class.java)
                startActivity(intent)
            }

            "SIMPLE_VIDEO_PLAYER" -> {
                val intent = Intent(this, SimpleVideoPlayerActivity::class.java)
                startActivity(intent)
            }
        }
    }
}