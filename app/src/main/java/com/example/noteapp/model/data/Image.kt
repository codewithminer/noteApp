package com.example.noteapp.model.data

import android.graphics.Bitmap
import android.net.Uri

data class Image(
    val id: String,
    val contentUri: String,
    val originalBitmap: Bitmap,
    val thumbnail: Bitmap,
    val name: String
)
