package me.ash.reader.domain.service

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import me.ash.reader.infrastructure.preference.SyncIntervalPreference
import me.ash.reader.infrastructure.preference.SyncOnlyOnWiFiPreference
import me.ash.reader.infrastructure.preference.SyncOnlyWhenChargingPreference
import me.ash.reader.ui.widget.ArticleCardWidget
import me.ash.reader.ui.widget.ArticleListWidget

@HiltWorker
class WidgetUpdateWorker
@AssistedInject
constructor(@Assisted context: Context, @Assisted workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        ArticleListWidget().updateAll(applicationContext)
        ArticleCardWidget().updateAll(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME_PERIODIC = "WidgetUpdateWorker"

        fun enqueueOneTimeWork(workManager: WorkManager) =
            workManager.enqueue(OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build())

        fun enqueuePeriodicWork(
            workManager: WorkManager,
            syncInterval: SyncIntervalPreference,
            syncOnlyWhenCharging: SyncOnlyWhenChargingPreference,
            syncOnlyOnWiFi: SyncOnlyOnWiFiPreference,
        ) {
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
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

        fun cancelPeriodicWork(workManager: WorkManager) =
            workManager.cancelUniqueWork(WORK_NAME_PERIODIC)
    }
}
