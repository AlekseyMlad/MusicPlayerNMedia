package ru.netology.nmedia

import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.adapter.TrackAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.viewmodel.TrackViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TrackViewModel by viewModels()
    private var isSeeking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = TrackAdapter({ viewModel.play(it) })
        binding.trackList.adapter = adapter

        binding.progress.max = 1000

        viewModel.apply {
            album.observe(this@MainActivity) { albumData ->
                binding.albumTitle.text = albumData.title
                binding.artist.text = albumData.artist
                binding.published.text = albumData.published
                binding.genre.text = albumData.genre
                adapter.submitList(albumData.tracks)
            }

            playerState.observe(this@MainActivity) { state ->
                binding.play.setImageResource(
                    if (state.isPlaying) R.drawable.ic_pause_circle_48 else R.drawable.ic_play_circle_48
                )
                if (!isSeeking) {
                    binding.progress.progress = state.progress * 10
                }
                adapter.updatePlayerState(state)
            }

            errorEvent.observe(this@MainActivity) { message ->
                message?.let {
                    android.widget.Toast.makeText(this@MainActivity, it, android.widget.Toast.LENGTH_LONG).show()
                    onErrorShown()
                }
            }
        }

        binding.apply {
            progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    isSeeking = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    viewModel.seekToProgress(seekBar.progress)
                    isSeeking = false
                }
            })
            play.setOnClickListener { viewModel.playPause() }
            next.setOnClickListener { viewModel.playNext() }
            prev.setOnClickListener { viewModel.playPrevious() }
        }

        viewModel.getAlbum()
    }
}