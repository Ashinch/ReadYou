package me.ash.reader.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import me.ash.reader.R
import me.ash.reader.data.entity.toVersion
import me.ash.reader.data.module.ApplicationScope
import me.ash.reader.data.module.DispatcherIO
import me.ash.reader.data.source.AppNetworkDataSource
import me.ash.reader.ui.ext.*
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
    suspend fun checkUpdate(): Boolean = withContext(dispatcherIO) {
        try {
            val latest =
                appNetworkDataSource.getReleaseLatest(context.getString(R.string.update_link))
            val latestVersion = latest.tag_name.toVersion()
//            val latestVersion = "0.7.3".toVersion()
            val skipVersion = context.skipVersionNumber.toVersion()
            val currentVersion = context.getCurrentVersion()
            val latestLog = latest.body ?: ""
            val latestPublishDate = latest.published_at ?: latest.created_at ?: ""
            val latestSize = latest.assets
                ?.first()
                ?.size
                ?: 0
            val latestDownloadUrl = latest.assets
                ?.first()
                ?.browser_download_url
                ?: ""

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
            false
        }
    }
}