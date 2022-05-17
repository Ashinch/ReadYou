package me.ash.reader.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import me.ash.reader.R
import me.ash.reader.data.model.toVersion
import me.ash.reader.data.module.DispatcherIO
import me.ash.reader.data.module.DispatcherMain
import me.ash.reader.data.source.AppNetworkDataSource
import me.ash.reader.data.source.Download
import me.ash.reader.data.source.downloadToFileWithProgress
import me.ash.reader.ui.ext.*
import javax.inject.Inject

class AppRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val appNetworkDataSource: AppNetworkDataSource,
    @DispatcherIO
    private val dispatcherIO: CoroutineDispatcher,
    @DispatcherMain
    private val dispatcherMain: CoroutineDispatcher,
) {
    suspend fun checkUpdate(showToast: Boolean = true): Boolean? = withContext(dispatcherIO) {
        try {
            val response =
                appNetworkDataSource.getReleaseLatest(context.getString(R.string.update_link))
            when {
                response.code() == 403 -> {
                    withContext(dispatcherMain) {
                        if (showToast) context.showToast(context.getString(R.string.rate_limit))
                    }
                    return@withContext null
                }
                response.body() == null -> {
                    withContext(dispatcherMain) {
                        if (showToast) context.showToast(context.getString(R.string.check_failure))
                    }
                    return@withContext null
                }
            }
            val latest = response.body()!!
            val latestVersion = latest.tag_name.toVersion()
//            val latestVersion = "0.7.3".toVersion()
            val skipVersion = context.skipVersionNumber.toVersion()
            val currentVersion = context.getCurrentVersion()
            val latestLog = latest.body ?: ""
            val latestPublishDate = latest.published_at ?: latest.created_at ?: ""
            val latestSize = latest.assets?.first()?.size ?: 0
            val latestDownloadUrl = latest.assets?.first()?.browser_download_url ?: ""

            Log.i("RLog", "current version $currentVersion")
            if (latestVersion.whetherNeedUpdate(currentVersion, skipVersion)) {
                Log.i("RLog", "new version $latestVersion")
                context.dataStore.put(
                    DataStoreKeys.NewVersionNumber,
                    latestVersion.toString()
                )
                context.dataStore.put(DataStoreKeys.NewVersionLog, latestLog)
                context.dataStore.put(DataStoreKeys.NewVersionPublishDate, latestPublishDate)
                context.dataStore.put(DataStoreKeys.NewVersionSize, latestSize)
                context.dataStore.put(DataStoreKeys.NewVersionDownloadUrl, latestDownloadUrl)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RLog", "checkUpdate: ${e.message}")
            withContext(dispatcherMain) {
                if (showToast) context.showToast(context.getString(R.string.check_failure))
            }
            null
        }
    }

    suspend fun downloadFile(url: String): Flow<Download> =
        withContext(dispatcherIO) {
            Log.i("RLog", "downloadFile start: $url")
            try {
                return@withContext appNetworkDataSource.downloadFile(url)
                    .downloadToFileWithProgress(context.getLatestApk())
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("RLog", "downloadFile: ${e.message}")
                withContext(dispatcherMain) {
                    context.showToast(context.getString(R.string.download_failure))
                }
            }
            emptyFlow()
        }
}