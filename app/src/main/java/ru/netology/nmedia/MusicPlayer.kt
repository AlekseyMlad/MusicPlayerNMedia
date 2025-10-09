package ru.netology.nmedia

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
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
        mediaPlayer.apply {
            setOnCompletionListener {
                isPrepared = false
                playNext()
            }
            setOnPreparedListener {
                isPrepared = true
                start()
                updatePlayerState()
                handler.post(progressUpdateRunnable)
            }
            setOnErrorListener { _, _, _ ->
                isPrepared = false
                updatePlayerState()
                true
            }
            setOnSeekCompleteListener {
                updatePlayerState()
                if (isPrepared && isPlaying) {
                    handler.post(progressUpdateRunnable)
                }
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
                Log.e("MusicPlayer", "Error setting data source", e)
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
        navigateTo(1)
    }

    fun playPrevious() {
        navigateTo(-1)
    }

    private fun navigateTo(offset: Int) {
        if (tracks.isEmpty()) return
        currentTrackIndex = (currentTrackIndex + offset + tracks.size) % tracks.size
        play(tracks[currentTrackIndex])
    }

    fun seekToProgress(progress: Int) {
        if (isPrepared) {
            handler.removeCallbacks(progressUpdateRunnable)
            val duration = mediaPlayer.duration
            if (duration > 0) {
                val newPosition = (duration.toLong() * progress) / 1000L
                val safePosition = newPosition.coerceIn(0, (duration - 200).toLong().coerceAtLeast(0))
                mediaPlayer.seekTo(safePosition.toInt())
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