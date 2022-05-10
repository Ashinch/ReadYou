package me.ash.reader

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.data.module.ApplicationScope
import me.ash.reader.data.module.DispatcherDefault
import me.ash.reader.data.repository.*
import me.ash.reader.data.source.AppNetworkDataSource
import me.ash.reader.data.source.OpmlLocalDataSource
import me.ash.reader.data.source.ReaderDatabase
import me.ash.reader.ui.ext.*
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {
    @Inject
    lateinit var readerDatabase: ReaderDatabase

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var appNetworkDataSource: AppNetworkDataSource

    @Inject
    lateinit var opmlLocalDataSource: OpmlLocalDataSource

    @Inject
    lateinit var rssHelper: RssHelper

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var stringsRepository: StringsRepository

    @Inject
    lateinit var accountRepository: AccountRepository

    @Inject
    lateinit var localRssRepository: LocalRssRepository

//    @Inject
//    lateinit var feverRssRepository: FeverRssRepository

    @Inject
    lateinit var opmlRepository: OpmlRepository

    @Inject
    lateinit var rssRepository: RssRepository

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @Inject
    @DispatcherDefault
    lateinit var dispatcherDefault: CoroutineDispatcher

    override fun onCreate() {
        super.onCreate()
        CrashHandler(this)
        dataStoreInit()
        applicationScope.launch(dispatcherDefault) {
            accountInit()
            workerInit()
            if (BuildConfig.FLAVOR != "fdroid") {
                checkUpdate()
            }
        }
    }

    private fun dataStoreInit() {
    }

    private suspend fun accountInit() {
        if (accountRepository.isNoAccount()) {
            val account = accountRepository.addDefaultAccount()
            applicationContext.dataStore.put(DataStoreKeys.CurrentAccountId, account.id!!)
            applicationContext.dataStore.put(DataStoreKeys.CurrentAccountType, account.type)
        }
    }

    private fun workerInit() {
        rssRepository.get().doSync()
    }

    private suspend fun checkUpdate() {
        applicationContext.getLatestApk().let {
            if (it.exists()) {
                it.del()
            }
        }
        appRepository.checkUpdate(showToast = false)
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}