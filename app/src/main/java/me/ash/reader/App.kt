package me.ash.reader

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ash.reader.data.repository.*
import me.ash.reader.data.source.OpmlLocalDataSource
import me.ash.reader.data.source.ReaderDatabase
import me.ash.reader.data.source.RssNetworkDataSource
import javax.inject.Inject

@DelicateCoroutinesApi
@HiltAndroidApp
class App : Application(), Configuration.Provider {
    @Inject
    lateinit var readerDatabase: ReaderDatabase

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var opmlLocalDataSource: OpmlLocalDataSource

    @Inject
    lateinit var rssNetworkDataSource: RssNetworkDataSource

    @Inject
    lateinit var rssHelper: RssHelper

    @Inject
    lateinit var stringsRepository: StringsRepository

    @Inject
    lateinit var accountRepository: AccountRepository

    @Inject
    lateinit var localRssRepository: LocalRssRepository

    @Inject
    lateinit var feverRssRepository: FeverRssRepository

    @Inject
    lateinit var opmlRepository: OpmlRepository

    @Inject
    lateinit var rssRepository: RssRepository

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch(Dispatchers.IO) {
            accountInit()
            workerInit()
        }
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

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}