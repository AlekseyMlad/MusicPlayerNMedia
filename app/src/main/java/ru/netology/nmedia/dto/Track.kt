package ru.netology.nmedia.dto

data class Track(
    val id: Long,
    val file: String,
    val albumTitle: String,
    val duration: Int = 0,
    val isPlaying: Boolean = false
)
