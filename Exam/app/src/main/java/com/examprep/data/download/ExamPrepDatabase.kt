package com.examprep.data.download

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DownloadItemEntity::class], version = 1, exportSchema = false)
abstract class ExamPrepDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
}
