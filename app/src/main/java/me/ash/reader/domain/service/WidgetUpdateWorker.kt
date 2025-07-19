package me.ash.reader.domain.service

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.glance.appwidget.compose
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
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
import me.ash.reader.ui.widget.ArticleCardWidgetReceiver
import me.ash.reader.ui.widget.ArticleListWidget
import me.ash.reader.ui.widget.ArticleListWidgetReceiver
import me.ash.reader.ui.widget.WidgetRepository

@HiltWorker
class WidgetUpdateWorker
@AssistedInject
constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val repository: WidgetRepository,
) : CoroutineWorker(context, workerParams) {

    private val appWidgetManager = AppWidgetManager.getInstance(context)

    override suspend fun doWork(): Result {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            generatePreviews()
        }

        ArticleListWidget().updateAll(applicationContext)
        ArticleCardWidget().updateAll(applicationContext)
        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private suspend fun generatePreviews() {
        if (
            appWidgetManager
                .getAppWidgetIds(ComponentName(context, ArticleListWidgetReceiver::class.java))
                .isEmpty()
        ) {
            appWidgetManager.setWidgetPreview(
                ComponentName(context, ArticleListWidgetReceiver::class.java),
                AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN or
                    AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD or
                    AppWidgetProviderInfo.WIDGET_CATEGORY_SEARCHBOX,
                ArticleListWidget().compose(context),
            )
        }
        if (
            appWidgetManager
                .getAppWidgetIds(ComponentName(context, ArticleCardWidgetReceiver::class.java))
                .isEmpty()
        ) {
            appWidgetManager.setWidgetPreview(
                ComponentName(context, ArticleCardWidgetReceiver::class.java),
                AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN or
                    AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD or
                    AppWidgetProviderInfo.WIDGET_CATEGORY_SEARCHBOX,
                ArticleCardWidget().compose(context),
            )
        }
    }

    companion object {
        private const val WORK_NAME_PERIODIC = "WidgetUpdateWorker"

        fun enqueueOneTimeWork(workManager: WorkManager) =
            workManager.enqueue(
                OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                    .setExpedited(policy = OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build()
            )

        fun enqueuePeriodicWork(
            workManager: WorkManager,
            syncInterval: SyncIntervalPreference,
            syncOnlyWhenCharging: SyncOnlyWhenChargingPreference,
            syncOnlyOnWiFi: SyncOnlyOnWiFiPreference,
        ) {
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<WidgetUpdateWorker>(syncInterval.value, TimeUnit.MINUTES)
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
