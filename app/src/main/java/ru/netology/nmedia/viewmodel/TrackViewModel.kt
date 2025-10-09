package ru.netology.nmedia.viewmodel

import android.app.Application
import android.media.MediaMetadataRetriever
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

class TrackViewModel(application: Application) : AndroidViewModel(application) {

    private val client = OkHttpClient()
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
                val tracksWithUrls = album.tracks.map { track ->
                    track.copy(file = BASE_URL + track.file, albumTitle = album.title)
                }
                val albumWithUrls = album.copy(tracks = tracksWithUrls)
                _album.postValue(albumWithUrls)
                musicPlayer.setTracks(albumWithUrls.tracks)
                fetchDurations(albumWithUrls)
            }

            override fun onFailure(call: Call, e: IOException) {
                _errorEvent.postValue("Не удалось загрузить альбом")
            }
        })
    }

    private fun fetchDurations(album: Album) {
        viewModelScope.launch {
            val tracksWithDurations = withContext(Dispatchers.IO) {
                album.tracks.map {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(it.file, HashMap<String, String>())
                        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0
                        it.copy(duration = duration)
                    } catch (_: Exception) {
                        it
                    } finally {
                        retriever.release()
                    }
                }
            }
            val albumWithDurations = album.copy(tracks = tracksWithDurations)
            _album.postValue(albumWithDurations)
            musicPlayer.setTracks(albumWithDurations.tracks)
        }
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