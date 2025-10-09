package ru.netology.nmedia

import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.TrackAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Track
import ru.netology.nmedia.viewmodel.TrackViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TrackViewModel by viewModels()
    private var isSeeking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = TrackAdapter(object : OnInteractionListener {
            override fun onPlay(track: Track) {
                viewModel.play(track)
            }
        })
        binding.trackList.adapter = adapter

        viewModel.album.observe(this) { albumData ->
            binding.albumTitle.text = albumData.title
            binding.artist.text = albumData.artist
            binding.published.text = albumData.published
            binding.genre.text = albumData.genre
            adapter.submitList(albumData.tracks)
        }

        viewModel.playerState.observe(this) { state ->
            binding.play.setImageResource(
                if (state.isPlaying) R.drawable.ic_pause_circle_48 else R.drawable.ic_play_circle_48
            )
            if (!isSeeking) {
                binding.progress.progress = state.progress
            }
            adapter.updatePlayerState(state)
        }

        viewModel.errorEvent.observe(this) { message ->
            message?.let {
                android.widget.Toast.makeText(this, it, android.widget.Toast.LENGTH_LONG).show()
                viewModel.onErrorShown()
            }
        }

        binding.progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Nothing to do here
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                viewModel.seekToProgress(seekBar.progress)
                isSeeking = false
            }
        })

        binding.play.setOnClickListener {
            viewModel.playPause()
        }

        binding.next.setOnClickListener {
            viewModel.playNext()
        }

        binding.prev.setOnClickListener {
            viewModel.playPrevious()
        }

        viewModel.getAlbum()
    }
}