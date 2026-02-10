package com.examprep.data.download

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val url: String,
    val localPath: String,
    val status: String
)
