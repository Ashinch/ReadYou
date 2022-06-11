package me.ash.reader.data.repository

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val rssRepository: RssRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result =
        withContext(Dispatchers.Default) {
            Log.i("RLog", "doWork: ")
            rssRepository.get().sync(this@SyncWorker)
        }

    companion object {

        const val WORK_NAME = "article.sync"

        val uuid: UUID

        val repeatingRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .build()
        ).addTag(WORK_NAME).build().also {
            uuid = it.id
        }

        fun setIsSyncing(boolean: Boolean) = workDataOf("isSyncing" to boolean)
        fun Data.getIsSyncing(): Boolean = getBoolean("isSyncing", false)
    }
}
