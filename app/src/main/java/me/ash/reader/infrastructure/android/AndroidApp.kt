package me.ash.reader.infrastructure.android

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import coil.ImageLoader
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ash.reader.domain.service.*
import me.ash.reader.infrastructure.db.AndroidDatabase
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.net.NetworkDataSource
import me.ash.reader.infrastructure.rss.OPMLDataSource
import me.ash.reader.infrastructure.rss.RssHelper
import me.ash.reader.infrastructure.storage.AndroidImageDownloader
import me.ash.reader.ui.ext.del
import me.ash.reader.ui.ext.getLatestApk
import me.ash.reader.ui.ext.isGitHub
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * The Application class, where the Dagger components is generated.
 */
@HiltAndroidApp
class AndroidApp : Application(), Configuration.Provider {

    /**
     * From: [Feeder](https://gitlab.com/spacecowboy/Feeder).
     *
     * Install Conscrypt to handle TLSv1.3 pre Android10.
     */
    init {
        // Cancel TLSv1.3 support pre Android10
        // Security.insertProviderAt(Conscrypt.newProvider(), 1)
    }

    @Inject
    lateinit var androidDatabase: AndroidDatabase

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var networkDataSource: NetworkDataSource

    @Inject
    lateinit var OPMLDataSource: OPMLDataSource

    @Inject
    lateinit var rssHelper: RssHelper

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var appService: AppService

    @Inject
    lateinit var androidStringsHelper: AndroidStringsHelper

    @Inject
    lateinit var accountService: AccountService

    @Inject
    lateinit var localRssService: LocalRssService

    @Inject
    lateinit var opmlService: OpmlService

    @Inject
    lateinit var rssService: RssService

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @Inject
    @IODispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var imageDownloader: AndroidImageDownloader

    /**
     * When the application startup.
     *
     * 1. Set the uncaught exception handler
     * 2. Initialize the default account if there is none
     * 3. Synchronize once
     * 4. Check for new version
     */
    override fun onCreate() {
        super.onCreate()
        CrashHandler(this)
        applicationScope.launch {
            accountInit()
            workerInit()
            checkUpdate()
        }
    }

    /**
     * Override the [Configuration.Builder] to provide the [HiltWorkerFactory].
     */
    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    private suspend fun accountInit() {
        withContext(ioDispatcher) {
            if (accountService.isNoAccount()) {
                accountService.addDefaultAccount()
            }
        }
    }

    private suspend fun workerInit() {
        rssService.get().doSync(isOnStart = true)
    }

    private suspend fun checkUpdate() {
        if (!isGitHub) return
        withContext(ioDispatcher) {
            applicationContext.getLatestApk().let {
                if (it.exists()) it.del()
            }
        }
        appService.checkUpdate(showToast = false)
    }
}
