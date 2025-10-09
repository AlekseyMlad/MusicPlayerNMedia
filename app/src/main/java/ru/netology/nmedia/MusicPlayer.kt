package ru.netology.nmedia

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.PlayerState
import ru.netology.nmedia.dto.Track
import java.io.IOException

class MusicPlayer {

    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var currentTrack: Track? = null
    private var currentTrackIndex: Int = -1
    private var tracks: List<Track> = emptyList()

    private val _playerState = MutableLiveData(PlayerState())
    val playerState: LiveData<PlayerState>
        get() = _playerState

    private val handler = Handler(Looper.getMainLooper())
    private val progressUpdateRunnable = object : Runnable {
        override fun run() {
            if (mediaPlayer.isPlaying) {
                updatePlayerState()
                handler.postDelayed(this, 1000)
            }
        }
    }

    init {
        mediaPlayer.setOnCompletionListener {
            playNext()
        }
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
            val duration = mediaPlayer.duration
            currentTrack = currentTrack?.copy(duration = duration)
            updatePlayerState()
            handler.post(progressUpdateRunnable)
        }
        mediaPlayer.setOnErrorListener { _, _, _ ->
            updatePlayerState()
            true
        }
        mediaPlayer.setOnSeekCompleteListener {
            updatePlayerState()
        }
    }

    fun setTracks(newTracks: List<Track>) {
        tracks = newTracks
    }

    fun play(track: Track) {
        val index = tracks.indexOf(track)
        if (index != -1) {
            currentTrackIndex = index
            currentTrack = track
            mediaPlayer.reset()
            try {
                mediaPlayer.setDataSource(track.file)
                mediaPlayer.prepareAsync()
                updatePlayerState()
            } catch (e: IOException) {
                // Handle error
            }
        }
    }

    fun playPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            handler.removeCallbacks(progressUpdateRunnable)
        } else {
            if (currentTrack == null && tracks.isNotEmpty()) {
                play(tracks.first())
            } else {
                mediaPlayer.start()
                handler.post(progressUpdateRunnable)
            }
        }
        updatePlayerState()
    }

    fun playNext() {
        if (tracks.isNotEmpty()) {
            currentTrackIndex = (currentTrackIndex + 1) % tracks.size
            play(tracks[currentTrackIndex])
        }
    }

    fun playPrevious() {
        if (tracks.isNotEmpty()) {
            currentTrackIndex = (currentTrackIndex - 1 + tracks.size) % tracks.size
            play(tracks[currentTrackIndex])
        }
    }

    fun seekToProgress(progress: Int) {
        val duration = mediaPlayer.duration
        if (duration > 0) {
            val seekPosition = (duration * progress / 100)
            mediaPlayer.seekTo(seekPosition)
        }
    }

    fun release() {
        handler.removeCallbacks(progressUpdateRunnable)
        mediaPlayer.release()
    }

    private fun updatePlayerState() {
        _playerState.postValue(
            PlayerState(
                isPlaying = mediaPlayer.isPlaying,
                currentTrack = currentTrack,
                progress = if (mediaPlayer.duration > 0) (mediaPlayer.currentPosition * 100 / mediaPlayer.duration) else 0
            )
        )
    }
}