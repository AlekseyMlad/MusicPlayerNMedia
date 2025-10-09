package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.TrackItemBinding
import ru.netology.nmedia.dto.PlayerState
import ru.netology.nmedia.dto.Track

interface OnInteractionListener {
    fun onPlay(track: Track)
}

class TrackAdapter(private val onInteractionListener: OnInteractionListener) :
    ListAdapter<Track, TrackViewHolder>(TrackDiffCallback()) {

    private var playerState: PlayerState? = null

    fun updatePlayerState(state: PlayerState) {
        val previousId = playerState?.currentTrack?.id
        val currentId = state.currentTrack?.id
        playerState = state

        if (previousId != currentId) {
            val previousIndex = currentList.indexOfFirst { it.id == previousId }
            if (previousIndex != -1) {
                notifyItemChanged(previousIndex)
            }
            val currentIndex = currentList.indexOfFirst { it.id == currentId }
            if (currentIndex != -1) {
                notifyItemChanged(currentIndex)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = TrackItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrackViewHolder(binding, onInteractionListener, this)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = getItem(position)
        holder.bind(track)
    }

    fun isPlaying(track: Track): Boolean {
        return playerState?.isPlaying == true && playerState?.currentTrack?.id == track.id
    }
}

class TrackViewHolder(
    private val binding: TrackItemBinding,
    private val onInteractionListener: OnInteractionListener,
    private val adapter: TrackAdapter
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(track: Track) {
        binding.apply {
            trackName.text = track.file.substringAfterLast("/")
            albumName.text = track.albumTitle

            val duration = track.duration
            trackDuration.text = if (duration > 0) {
                val durationInSeconds = duration / 1000
                val minutes = durationInSeconds / 60
                val seconds = durationInSeconds % 60
                String.format("%d:%02d", minutes, seconds)
            } else {
                "--:--"
            }

            playIndicator.visibility = if (adapter.isPlaying(track)) View.VISIBLE else View.INVISIBLE

            root.setOnClickListener {
                onInteractionListener.onPlay(track)
            }
        }
    }
}

class TrackDiffCallback : DiffUtil.ItemCallback<Track>() {
    override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem == newItem
    }
}
