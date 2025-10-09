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
    private var isPrepared = false

    private val _playerState = MutableLiveData(PlayerState())
    val playerState: LiveData<PlayerState>
        get() = _playerState

    private val handler = Handler(Looper.getMainLooper())
    private val progressUpdateRunnable = object : Runnable {
        override fun run() {
            if (isPrepared && mediaPlayer.isPlaying) {
                updatePlayerState()
                handler.postDelayed(this, 1000)
            }
        }
    }

    init {
        mediaPlayer.setOnCompletionListener {
            isPrepared = false
            playNext()
        }
        mediaPlayer.setOnPreparedListener {
            isPrepared = true
            mediaPlayer.start()
            updatePlayerState()
            handler.post(progressUpdateRunnable)
        }
        mediaPlayer.setOnErrorListener { _, _, _ ->
            isPrepared = false
            updatePlayerState()
            true
        }
        mediaPlayer.setOnSeekCompleteListener {
            updatePlayerState()
            if (isPrepared && mediaPlayer.isPlaying) {
                handler.post(progressUpdateRunnable)
            }
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
            isPrepared = false
            mediaPlayer.reset()
            try {
                mediaPlayer.setDataSource(track.file)
                mediaPlayer.prepareAsync()
            } catch (e: IOException) {
                // Handle error
            }
            updatePlayerState()
        }
    }

    fun playPause() {
        if (isPrepared && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            handler.removeCallbacks(progressUpdateRunnable)
        } else {
            if (currentTrack == null && tracks.isNotEmpty()) {
                play(tracks.first())
            } else if (isPrepared) {
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
        if (isPrepared) {
            handler.removeCallbacks(progressUpdateRunnable)
            val duration = mediaPlayer.duration
            if (duration > 0) {
                var seekPosition = (duration * progress / 100)
                if (seekPosition >= duration) {
                    seekPosition = duration - 1
                }
                mediaPlayer.seekTo(seekPosition)
            }
        }
    }

    fun release() {
        handler.removeCallbacks(progressUpdateRunnable)
        mediaPlayer.release()
    }

    private fun updatePlayerState() {
        if (!isPrepared) {
            _playerState.postValue(
                PlayerState(
                    isPlaying = false,
                    currentTrack = currentTrack,
                    progress = 0
                )
            )
            return
        }
        _playerState.postValue(
            PlayerState(
                isPlaying = mediaPlayer.isPlaying,
                currentTrack = currentTrack,
                progress = if (mediaPlayer.duration > 0) (mediaPlayer.currentPosition * 100 / mediaPlayer.duration) else 0
            )
        )
    }
}