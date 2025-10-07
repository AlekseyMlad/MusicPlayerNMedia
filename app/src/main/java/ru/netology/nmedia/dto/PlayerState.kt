package ru.netology.nmedia.dto

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentTrack: Track? = null,
    val progress: Int = 0
)