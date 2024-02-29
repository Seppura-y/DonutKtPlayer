package com.example.donutktplayer

import android.net.Uri

data class VideoData(val id: String, val title: String, val duration: Long = 0,
            val folderName: String, val size: String, val path: String, val artUri: Uri)

data class FolderData(val id: String, val folderName: String)