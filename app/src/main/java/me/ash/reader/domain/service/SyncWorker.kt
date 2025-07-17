package me.ash.reader.domain.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import me.ash.reader.infrastructure.preference.SyncIntervalPreference
import me.ash.reader.infrastructure.preference.SyncOnlyOnWiFiPreference
import me.ash.reader.infrastructure.preference.SyncOnlyWhenChargingPreference
import me.ash.reader.infrastructure.rss.ReaderCacheHelper

@HiltWorker
class SyncWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val rssService: RssService,
    private val readerCacheHelper: ReaderCacheHelper,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val data = inputData
        val feedId = data.getString("feedId")
        val groupId = data.getString("groupId")

        return rssService.get().sync(feedId = feedId, groupId = groupId).also {
            rssService.get().clearKeepArchivedArticles().forEach {
                readerCacheHelper.deleteCacheFor(articleId = it.id)
            }
        }
    }

    companion object {
        private const val SYNC_WORK_NAME_PERIODIC = "ReadYou"
        private const val READER_WORK_NAME_PERIODIC = "FETCH_FULL_CONTENT_PERIODIC"

        private const val WORK_NAME_ONETIME = "SYNC_ONETIME"
        const val WORK_TAG = "SYNC_TAG"

        fun cancelOneTimeWork(workManager: WorkManager) {
            workManager.cancelUniqueWork(WORK_NAME_ONETIME)
        }

        fun cancelPeriodicWork(workManager: WorkManager) {
            workManager.cancelUniqueWork(SYNC_WORK_NAME_PERIODIC)
            workManager.cancelUniqueWork(READER_WORK_NAME_PERIODIC)
        }

        fun enqueueOneTimeWork(workManager: WorkManager, inputData: Data = workDataOf()) {
            workManager
                .beginUniqueWork(
                    WORK_NAME_ONETIME,
                    ExistingWorkPolicy.KEEP,
                    OneTimeWorkRequestBuilder<SyncWorker>()
                        .addTag(WORK_TAG)
                        .setInputData(inputData)
                        .build(),
                )
                .then(OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build())
                .then(OneTimeWorkRequestBuilder<ReaderWorker>().build())
                .enqueue()
        }

        fun enqueuePeriodicWork(
            workManager: WorkManager,
            syncInterval: SyncIntervalPreference,
            syncOnlyWhenCharging: SyncOnlyWhenChargingPreference,
            syncOnlyOnWiFi: SyncOnlyOnWiFiPreference,
        ) {
            workManager.enqueueUniquePeriodicWork(
                SYNC_WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<SyncWorker>(syncInterval.value, TimeUnit.MINUTES)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresCharging(syncOnlyWhenCharging.value)
                            .setRequiredNetworkType(
                                if (syncOnlyOnWiFi.value) NetworkType.UNMETERED
                                else NetworkType.CONNECTED
                            )
                            .build()
                    )
                    .addTag(WORK_TAG)
                    .setInitialDelay(syncInterval.value, TimeUnit.MINUTES)
                    .build(),
            )

            workManager.enqueueUniquePeriodicWork(
                READER_WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<ReaderWorker>(syncInterval.value, TimeUnit.MINUTES)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresCharging(syncOnlyWhenCharging.value)
                            .setRequiredNetworkType(
                                if (syncOnlyOnWiFi.value) NetworkType.UNMETERED
                                else NetworkType.CONNECTED
                            )
                            .build()
                    )
                    .setInitialDelay(syncInterval.value, TimeUnit.MINUTES)
                    .build(),
            )
        }
    }
}
