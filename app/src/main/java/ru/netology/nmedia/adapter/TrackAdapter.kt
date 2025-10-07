package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.TrackItemBinding
import ru.netology.nmedia.dto.Track

interface OnInteractionListener {
    fun onPlay(track: Track)
}

class TrackAdapter(private val onInteractionListener: OnInteractionListener) :
    ListAdapter<Track, TrackViewHolder>(TrackDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = TrackItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrackViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = getItem(position)
        holder.bind(track)
    }
}

class TrackViewHolder(private val binding: TrackItemBinding, private val onInteractionListener: OnInteractionListener) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(track: Track) {
        binding.apply {
            trackName.text = track.file
            albumName.text = track.albumTitle
            val durationInSeconds = track.duration / 1000
            val minutes = durationInSeconds / 60
            val seconds = durationInSeconds % 60
            trackDuration.text = String.format("%d:%02d", minutes, seconds)
            playIndicator.visibility = if (track.isPlaying) View.VISIBLE else View.INVISIBLE

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
