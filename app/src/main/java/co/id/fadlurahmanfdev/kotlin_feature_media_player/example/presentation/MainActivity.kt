package co.id.fadlurahmanfdev.kotlin_feature_media_player.example.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.R
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.data.dto.model.FeatureModel
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.data.repository.ExampleMediaNotificationRepositoryImpl
import co.id.fadlurahmanfdev.kotlin_feature_media_player.example.domain.usecase.ExampleMediaPlayerUseCaseImpl

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
            title = "Play Music",
            desc = "Play remote music",
            enum = "PLAY_REMOTE_MUSIC"
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
                exampleMediaNotificationRepository = ExampleMediaNotificationRepositoryImpl(
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

            "PLAY_REMOTE_MUSIC" -> {
                val intent = Intent(this, RemoteMusicPlayerActivity::class.java)
                startActivity(intent)
            }
        }
    }
}