package me.ash.reader.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import me.ash.reader.data.module.ApplicationScope
import me.ash.reader.data.module.DispatcherIO
import me.ash.reader.data.source.AppNetworkDataSource
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put
import me.ash.reader.ui.ext.skipVersionNumber
import javax.inject.Inject

class AppRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val appNetworkDataSource: AppNetworkDataSource,
    @ApplicationScope
    private val applicationScope: CoroutineScope,
    @DispatcherIO
    private val dispatcherIO: CoroutineDispatcher,
) {
    suspend fun checkUpdate() {
        withContext(dispatcherIO) {
            try {
                val latest = appNetworkDataSource.getReleaseLatest()
                val latestVersion = latest.tag_name?.formatVersion() ?: listOf()
                val latestLog = latest.body ?: ""
                val latestPublishDate = latest.published_at ?: ""
                val latestSize = latest.assets
                    ?.first()
                    ?.size
                    ?: 0
                val latestDownloadUrl = latest.assets
                    ?.first()
                    ?.browser_download_url
                    ?: ""
                val currentVersion = context
                    .packageManager
                    .getPackageInfo(context.packageName, 0)
                    .versionName
                    .formatVersion()

                Log.i("RLog", "current version ${currentVersion.joinToString(".")}")
                if (latestVersion > context.skipVersionNumber.formatVersion() && latestVersion > currentVersion) {
                    Log.i("RLog", "new version ${latestVersion.joinToString(".")}")
                    context.dataStore.put(
                        DataStoreKeys.NewVersionNumber,
                        latestVersion.joinToString(".")
                    )
                    context.dataStore.put(DataStoreKeys.NewVersionLog, latestLog)
                    context.dataStore.put(DataStoreKeys.NewVersionPublishDate, latestPublishDate)
                    context.dataStore.put(DataStoreKeys.NewVersionSize, latestSize)
                    context.dataStore.put(DataStoreKeys.NewVersionDownloadUrl, latestDownloadUrl)
                }
                this
            } catch (e: Exception) {
                Log.e("RLog", "checkUpdate: ${e.message}")
            }
        }
    }
}

fun String.formatVersion(): List<String> = this.split(".")

operator fun List<String>.compareTo(target: List<String>): Int {
    for (i in 0 until minOf(size, target.size)) {
        val a = this[i].toIntOrNull() ?: 0
        val b = target[i].toIntOrNull() ?: 0
        if (a < b) return -1
        if (a > b) return 1
    }
    return if (size == target.size) 0 else if (size > target.size) 1 else -1
}