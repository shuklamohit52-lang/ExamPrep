package com.examprep.data.download

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters

class OfflineDownloadManager(private val workManager: WorkManager) {
    fun enqueuePdfOrEbookDownload(fileId: String, url: String) {
        val request = OneTimeWorkRequestBuilder<DownloadWorker>().build()
        workManager.enqueue(request)
    }
}

class DownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return Result.success()
    }
}
