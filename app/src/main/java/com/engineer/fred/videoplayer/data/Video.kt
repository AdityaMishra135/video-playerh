package com.engineer.fred.videoplayer.data

import android.net.Uri

data class Video(
    val id: String,
    var title: String,
    val displayName: String,
    val folderName: String,
    val duration: Long,
    val size: Long = 0,
    var path: String,
    var playUri: Uri
)