package ru.netology.nmedia

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.PlayerState
import ru.netology.nmedia.dto.Track

class MusicPlayer(private val onStateChanged: (PlayerState) -> Unit) {

    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var currentTrack: Track? = null
    private var currentTrackIndex: Int = -1
    private var tracks: List<Track> = emptyList()

    private val _playerState = MutableLiveData(PlayerState(false, null, 0))
    val playerState: LiveData<PlayerState>
        get() = _playerState

    init {
        mediaPlayer.setOnCompletionListener { 
            playNext()
        }
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
            updatePlayerState()
        }
        mediaPlayer.setOnErrorListener { _, _, _ ->
            updatePlayerState()
            true
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
            mediaPlayer.setDataSource(track.file)
            mediaPlayer.prepareAsync()
            updatePlayerState()
        }
    }

    fun playPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.start()
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
        val seekPosition = (duration * progress / 100)
        mediaPlayer.seekTo(seekPosition)
        updatePlayerState()
    }

    fun release() {
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
        onStateChanged(_playerState.value!!)
    }
}