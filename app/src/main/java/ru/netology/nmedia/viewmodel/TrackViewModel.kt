package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import ru.netology.nmedia.MusicPlayer
import ru.netology.nmedia.dto.Album
import ru.netology.nmedia.dto.PlayerState
import ru.netology.nmedia.dto.Track
import java.io.IOException
import java.util.concurrent.TimeUnit

class TrackViewModel(application: Application) : AndroidViewModel(application) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val albumType = object : TypeToken<Album>() {}.type
    private var call: Call? = null

    companion object {
        private const val ALBUM_URL =
            "https://github.com/netology-code/andad-homeworks/raw/master/09_multimedia/data/album.json"
        private const val BASE_URL =
            "https://raw.githubusercontent.com/netology-code/andad-homeworks/master/09_multimedia/data/"
    }

    private val _album = MutableLiveData<Album>()
    val album: LiveData<Album> = _album

    private val _playerState = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> = _playerState

    private val _errorEvent = MutableLiveData<String?>()
    val errorEvent: LiveData<String?> = _errorEvent

    val musicPlayer = MusicPlayer()

    init {
        musicPlayer.playerState.observeForever { state ->
            _playerState.postValue(state)
        }
    }

    fun getAlbum() {
        val request: Request = Request.Builder()
            .url(ALBUM_URL)
            .build()

        call = client.newCall(request)
        call?.enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: throw RuntimeException("body is null")
                val album = gson.fromJson<Album>(body, albumType)
                val tracksWithData = album.tracks.map { track ->
                    track.copy(file = BASE_URL + track.file, albumTitle = album.title)
                }
                val albumWithTracks = album.copy(tracks = tracksWithData)
                _album.postValue(albumWithTracks)
                musicPlayer.setTracks(albumWithTracks.tracks)
            }

            override fun onFailure(call: Call, e: IOException) {
                _errorEvent.postValue("Не удалось загрузить альбом")
            }
        })
    }

    fun onErrorShown() {
        _errorEvent.value = null
    }

    fun play(track: Track) {
        musicPlayer.play(track)
    }

    fun playPause() {
        musicPlayer.playPause()
    }

    fun playNext() {
        musicPlayer.playNext()
    }

    fun playPrevious() {
        musicPlayer.playPrevious()
    }

    fun seekToProgress(progress: Int) {
        musicPlayer.seekToProgress(progress)
    }

    override fun onCleared() {
        super.onCleared()
        musicPlayer.release()
        call?.cancel()
    }
}