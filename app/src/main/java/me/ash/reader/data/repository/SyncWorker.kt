package me.ash.reader.data.repository

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val rssRepository: RssRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.i("RLog", "doWork: ")
        return rssRepository.get().sync(this)
    }

    companion object {
        const val WORK_NAME = "article.sync"

        val UUID: UUID

        val repeatingRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .build()
        ).addTag(WORK_NAME).build().also {
            UUID = it.id
        }

        fun setIsSyncing(boolean: Boolean) = workDataOf("isSyncing" to boolean)
        fun Data.getIsSyncing(): Boolean = getBoolean("isSyncing", false)
    }
}