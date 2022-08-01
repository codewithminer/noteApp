package com.example.noteapp.model.data

import java.io.Serializable

data class Recording(
    var uri: String,
    var fileName: String,
    var isPlaying: Boolean
): Serializable