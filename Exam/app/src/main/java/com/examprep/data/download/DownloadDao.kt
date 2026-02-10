package com.examprep.data.download

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: DownloadItemEntity)

    @Query("SELECT * FROM downloads ORDER BY title")
    fun observeAll(): Flow<List<DownloadItemEntity>>

    @Query("UPDATE downloads SET status=:status, localPath=:path WHERE id=:id")
    suspend fun updateStatus(id: String, status: String, path: String)
}
