package me.ash.reader.data.repository

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.ash.reader.data.model.preference.SyncIntervalPreference
import me.ash.reader.data.model.preference.SyncOnlyOnWiFiPreference
import me.ash.reader.data.model.preference.SyncOnlyWhenChargingPreference
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val accountRepository: AccountRepository,
    private val rssRepository: RssRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result =
        withContext(Dispatchers.Default) {
            Log.i("RLog", "doWork: ")
            rssRepository.get().sync(this@SyncWorker).also {
                rssRepository.get().clearKeepArchivedArticles()
            }
        }

    companion object {

        private const val IS_SYNCING = "isSyncing"
        const val WORK_NAME = "ReadYou"
        lateinit var uuid: UUID

        fun enqueueOneTimeWork(
            workManager: WorkManager,
        ) {
            workManager.enqueue(OneTimeWorkRequestBuilder<SyncWorker>()
                .addTag(WORK_NAME)
                .build()
            )
        }

        fun enqueuePeriodicWork(
            workManager: WorkManager,
            syncInterval: SyncIntervalPreference,
            syncOnlyWhenCharging: SyncOnlyWhenChargingPreference,
            syncOnlyOnWiFi: SyncOnlyOnWiFiPreference,
        ) {
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<SyncWorker>(syncInterval.value, TimeUnit.MINUTES)
                    .setConstraints(Constraints.Builder()
                        .setRequiresCharging(syncOnlyWhenCharging.value)
                        .setRequiredNetworkType(if (syncOnlyOnWiFi.value) NetworkType.UNMETERED else NetworkType.CONNECTED)
                        .build()
                    )
                    .addTag(WORK_NAME)
                    .setInitialDelay(syncInterval.value, TimeUnit.MINUTES)
                    .build()
            )
        }

        fun setIsSyncing(boolean: Boolean) = workDataOf(IS_SYNCING to boolean)
        fun Data.getIsSyncing(): Boolean = getBoolean(IS_SYNCING, false)
    }
}
